package luxmeter.receips.elementgenerator;

import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static luxmeter.collectionutils.CollectionUtils.*;

public final class ElementGeneratorBuilder<T> {
    private List<KeyPropertyMetadata<T, ?>> keyPropertiesMetadata = new ArrayList<>();

    private Collection<T> existingElements;
    private Collection<ElementAbstraction> intermediateEndResult; // goal
    private Function<ElementAbstraction, T> elementConstructor;

    private Function<T, ElementAbstraction> intermediateResultMapper;
    private Function<T, Collection<ElementAbstraction>> intermediateResultsMapper;
    private Function<T, ?> groupingKey;
    private BinaryOperator<T> reducer;


    public static <T,A > ElementGeneratorBuilder<T> create() {
        return new ElementGeneratorBuilder<>();
    }

    private ElementGeneratorBuilder() {
    }

    private Function<T, ElementAbstraction> createIntermediateResultMapper() {
        return concreteElement -> {
            Map<String, Object> keyValues = getPropertyValues(concreteElement);
            return new ElementAbstraction(concreteElement, keyValues);
        };
    }

    private Map<String, Object> getPropertyValues(T concreteElement) {
        return keyPropertiesMetadata.stream()
                .collect(Collectors.toMap(KeyPropertyMetadata::getPropertyName,
                        keyProperty -> keyProperty.getValueExtractor().apply(concreteElement)));
    }

    private Function<T, Collection<ElementAbstraction>> createIntermediateResultsMapper(List<String> propertyNamesForGeneration) {
        return concreteElement -> {
            List<Collection> collect = propertyNamesForGeneration.stream().map(propertyName -> {
                KeyPropertyMetadata<T, ?> propertyMetadata = getKeyPropertyMetadataBy(propertyName);
                Collection value = (Collection) propertyMetadata.getValueExtractor().apply(concreteElement);
                return value;
            }).collect(Collectors.toList());
            List<List<Object>> productForGeneration = product(collect.toArray(new Collection[collect.size()]));

            // productForGeneration[0] = (PX,A)
            List<ElementAbstraction> result = productForGeneration.stream()
                    .map(this::namedValues)
                    .map(map -> {
                        // TODO(refactor)
                        List<Object> values = keyPropertiesMetadata.stream().filter(prop -> !prop.isCollection())
                                .map(KeyPropertyMetadata::getValueExtractor)
                                .map(extractor -> extractor.apply(concreteElement))
                                .collect(Collectors.toList());
                        List<String> names = keyPropertiesMetadata.stream()
                                .filter(p -> !p.isCollection())
                                .map(KeyPropertyMetadata::getPropertyName)
                                .collect(Collectors.toList());
                        zip(names, values).forEach(pair -> map.put(pair.getLeft(), pair.getRight()));
                        return new ElementAbstraction(map);
                    }).collect(Collectors.toList());
            return result;
        };
    }

    private boolean intermediateResultMappersExist() {
        return intermediateResultMapper != null || intermediateResultsMapper != null;
    }

    @SuppressWarnings("unchecked")
    private Set<ElementAbstraction> createIntermediateEndResult() {
        List<List<?>> valueRanges = getValueRanges();
        List<List<Object>> productOfAllValues =
                product((Collection[]) valueRanges.toArray(new List[valueRanges.size()]));
        List<Map<String, ?>> namedProductOfAllValues = attachNames(productOfAllValues);
        return namedProductOfAllValues.stream().map(ElementAbstraction::new).collect(Collectors.toSet());
    }

    private List<Map<String, ?>> attachNames(List<List<Object>> productOfAllValues) {
        return productOfAllValues.stream()
                .map(singleCombination -> toList(zip(getKeyPropertyNames(), singleCombination))
                        .stream()
                        .collect(Collectors.toMap(Pair::getLeft, Pair::getRight))
                ).collect(Collectors.toList());
    }

    private List<List<?>> getValueRanges() {
        return keyPropertiesMetadata.stream()
                .map(keyProperty -> keyProperty.getValuesRange().stream()
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    private KeyPropertyMetadata<T, ?> getKeyPropertyMetadataBy(String propertyName) {
        return keyPropertiesMetadata.stream()
                .filter(prop -> prop.getPropertyName().equals(propertyName))
                .findFirst()
                .orElse(null);
    }

    private Map<String, Object> namedValues(List<Object> singleCombination) {
        Stream<String> propertyNames = keyPropertiesMetadata.stream()
                .filter(KeyPropertyMetadata::isCollection)
                .map(KeyPropertyMetadata::getPropertyName);
        List<Pair<String, Object>> zipped = zip(propertyNames, singleCombination.stream()).collect(Collectors.toList());
        Map<String, Object> map = zipped.stream().collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        return map;
    }

    private List<String> getKeyPropertyNames() {
        return keyPropertiesMetadata.stream().map(KeyPropertyMetadata::getPropertyName).collect(Collectors.toList());
    }

    public ElementGeneratorBuilder<T> withExistingElements(Collection<T> existingElements) {
        this.existingElements = existingElements;
        return this;
    }

    public ElementGeneratorBuilder<T> withElementConstructor(Function<ElementAbstraction, T> elementConstructor) {
        this.elementConstructor = elementConstructor;
        return this;
    }

    public ElementGeneratorBuilder<T> withReducer(Function<T, ?> groupingKey, BinaryOperator<T> reducer) {
        this.groupingKey = groupingKey;
        this.reducer = reducer;
        return this;
    }

    public <R> ElementGeneratorBuilder<T> withKeyProperty(String propertyName, Collection<R> valuesRange, SingleValueExtractor<T, R> propertyExtractor) {
        keyPropertiesMetadata.add(new KeyPropertyMetadata<>(propertyName, propertyExtractor, valuesRange));
        return this;
    }

    public <R, C extends Collection<R>> ElementGeneratorBuilder<T> withKeyProperty(String propertyName, Collection<R> valuesRange, ValuesExtractor<T, R, C> propertyExtractor) {
        keyPropertiesMetadata.add(new KeyPropertyMetadata<>(propertyName, propertyExtractor, valuesRange, true));
        return this;
    }

    @SuppressWarnings("unchecked")
    public ElementGenerator<T> build() {
        intermediateEndResult = createIntermediateEndResult();

        if (!intermediateResultMappersExist()) {
            List<String> propertyNamesForGeneration = keyPropertiesMetadata.stream()
                    .filter(KeyPropertyMetadata::isCollection)
                    .map(KeyPropertyMetadata::getPropertyName)
                    .collect(Collectors.toList());

            if (!propertyNamesForGeneration.isEmpty()) {
                intermediateResultsMapper = createIntermediateResultsMapper(propertyNamesForGeneration);
            } else {
                intermediateResultMapper = createIntermediateResultMapper();
            }
        }
        return new ElementGenerator<>(this);
    }

    public static final class ElementGenerator<T> {
        private final Set<T> existingConcreteElements;
        private final Set<ElementAbstraction> intermediateEndResult; // goal
        private final Function<ElementAbstraction, T> elementConstructor;

        private final Function<T, Collection<ElementAbstraction>> intermediateResultsMapper;
        private final Function<T, ElementAbstraction> intermediateResultMapper;
        private final Function<T, ?> groupingKey;
        private final BinaryOperator<T> reducer;

        public ElementGenerator(ElementGeneratorBuilder<T> builder) {
            Objects.requireNonNull(builder.existingElements);
            Objects.requireNonNull(builder.intermediateEndResult);
            Objects.requireNonNull(builder.elementConstructor);

            if ((builder.intermediateResultMapper != null) == (builder.intermediateResultsMapper != null)) {
                throw new IllegalArgumentException(
                        "Either an intermediateResultMapper or intermediateResult[s]Mapper must be passed in.");
            }

            if (builder.groupingKey != null || builder.reducer != null) {
                Objects.requireNonNull(builder.groupingKey);
                Objects.requireNonNull(builder.reducer);
            }

            this.intermediateResultMapper = builder.intermediateResultMapper;
            if (intermediateResultMapper != null) {
                this.intermediateResultsMapper = e -> Collections.singletonList(intermediateResultMapper.apply(e));
            }
            else {
                this.intermediateResultsMapper = builder.intermediateResultsMapper;
            }

            this.existingConcreteElements = new HashSet<T>(builder.existingElements);
            this.intermediateEndResult = new HashSet<>(builder.intermediateEndResult);
            this.elementConstructor = builder.elementConstructor;
            this.groupingKey = builder.groupingKey;
            this.reducer = builder.reducer;
        }

        public Set<T> generateMissingElements() {
            return generateMissingElements(MergeType.NOT_MERGED);
        }

        public Set<T> generateMissingElements(MergeType merged) {
            Set<ElementAbstraction> existingAbstractElements = existingConcreteElements.stream()
                    .flatMap(e->intermediateResultsMapper.apply(e).stream())
                    .collect(Collectors.toSet());

            Set<ElementAbstraction> missingAbstractElements = removeAll(intermediateEndResult, existingAbstractElements);

            Set<T> generatedMissingConcreteElements = missingAbstractElements.stream()
                    .map(elementConstructor)
                    .collect(Collectors.toSet());

            if (merged == MergeType.MERGED && groupingKey != null) {
                Map<?, List<T>> groupedGeneratedMissingConcreteElements =
                        generatedMissingConcreteElements.stream().collect(Collectors.groupingBy(groupingKey));
                HashMap<?, List<T>> reducedMap = new HashMap<>(groupedGeneratedMissingConcreteElements);
                reducedMap.entrySet().forEach(this::reduce);

                // finish: flatten the resultl
                generatedMissingConcreteElements = reducedMap.values().stream()
                        .map(r -> r.get(0))
                        .collect(Collectors.toSet());
            }

            return generatedMissingConcreteElements;
        }

        private void reduce(Map.Entry<?, List<T>> entry) {
            List<T> reducedList = Collections.singletonList(entry.getValue().stream()
                    .collect(Collectors.reducing(entry.getValue().get(0), reducer)));
            entry.setValue(reducedList);
        }
    }
}
