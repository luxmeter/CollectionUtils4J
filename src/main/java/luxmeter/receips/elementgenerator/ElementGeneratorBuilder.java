package luxmeter.receips.elementgenerator;

import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private static final class KeyPropertyMetadata<T, R> {
        private final String propertyName;
        private final Function<T, ?> valueExtractor;
        private final Function<?, String> toStringMapper;
        private final Collection<R> valuesRange;
        private final boolean isCollection;


        private Function<T, Object> nonNullValueExtractor(String propertyName, Function<T, ?> valueExtractor) {
            return concreteElement -> {
                Object val = valueExtractor.apply(concreteElement);
                if (isNullOrEmpty(val)) {
                    throw new IllegalArgumentException(
                            String.format("Error while extracting values from %s: " +
                                    "It is forbidden for a key property like '%s' " +
                                    "to return null or an empty collection.",
                                    concreteElement.toString(), propertyName));
                }
                return val;
            };
        }

        private boolean isNullOrEmpty(Object val) {
            if (val instanceof Collection) {
                return ((Collection) val).isEmpty();
            }
            return val == null;
        }


        public KeyPropertyMetadata(
                String propertyName, SingleValueExtractor<T, R> valueExtractor, Collection<R> valuesRange) {
            this.propertyName = propertyName;
            this.valueExtractor = nonNullValueExtractor(propertyName, valueExtractor);
            this.valuesRange = valuesRange;
            this.toStringMapper = Object::toString;
            this.isCollection = false;
        }

        public <C extends Collection<R>> KeyPropertyMetadata(
                String propertyName, ValuesExtractor<T, R, C> valueExtractor,
                Collection<R> valuesRange, boolean isCollection) {
            this.propertyName = propertyName;
            this.valueExtractor = nonNullValueExtractor(propertyName, valueExtractor);
            this.valuesRange = valuesRange;
            this.toStringMapper = Object::toString;
            this.isCollection = isCollection;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public Function<T, ?> getValueExtractor() {
            return valueExtractor;
        }

        public Collection<R> getValuesRange() {
            return valuesRange;
        }

        public Function<?, String> getToStringMapper() {
            return toStringMapper;
        }

        public boolean isCollection() {
            return isCollection;
        }
    }


    public static <T,A > ElementGeneratorBuilder<T> create() {
        return new ElementGeneratorBuilder<>();
    }

    private ElementGeneratorBuilder() {
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
        // lists[0] -> chargecodes
        // lists[1] -> products
        // lists[2] -> zones
        List<List<?>> valueRanges = keyPropertiesMetadata.stream()
                .map(keyProperty -> keyProperty.getValuesRange().stream()
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        List<?>[] lists = (List<?>[]) valueRanges.toArray(new List[0]);
        List<List<?>> product = (List<List<?>>) (List<?>) product(lists);
        List<Map<String, ?>> namedProduct = product.stream().map(singleCombination ->
                toList(zip(getKeyPropertyNames(), singleCombination)).stream()
                        .collect(Collectors.toMap(Pair::getLeft, Pair::getRight)))
                .collect(Collectors.toList());
        intermediateEndResult = namedProduct.stream().map(ElementAbstraction::new).collect(Collectors.toSet());

        List<String> propertyNamesForGeneration = keyPropertiesMetadata.stream().filter(KeyPropertyMetadata::isCollection).map(KeyPropertyMetadata::getPropertyName).collect(Collectors.toList());

        if (!propertyNamesForGeneration.isEmpty() && intermediateResultMapper == null && intermediateResultsMapper == null) {
            intermediateResultsMapper = concreteElement -> {
                List<Collection> collect = propertyNamesForGeneration.stream().map(propertyName -> {
                    KeyPropertyMetadata<T, ?> propertyMetadata = getKeyPropertyMetadataBy(propertyName);
                    Collection value = (Collection) propertyMetadata.getValueExtractor().apply(concreteElement);
                    return value;
                }).collect(Collectors.toList());
                List<List<Object>> productForGeneration = productWithLists(collect);

                // productForGeneration[0] = (PX,A)
                List<ElementAbstraction> result = productForGeneration.stream()
                        .map(this::namedValues)
                        .map(map -> {
                            List<Object> values = keyPropertiesMetadata.stream().filter(prop -> !prop.isCollection)
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
        } else if (intermediateResultsMapper == null && intermediateResultMapper == null) {
            intermediateResultMapper = concreteElement -> {
                Map<String, Object> keyValues = keyPropertiesMetadata.stream()
                        .collect(Collectors.toMap(KeyPropertyMetadata::getPropertyName,
                                keyProperty -> keyProperty.getValueExtractor().apply(concreteElement)));
                return new ElementAbstraction(concreteElement, keyValues);
            };
        }
        return new ElementGenerator<>(this);
    }

    private KeyPropertyMetadata<T, ?> getKeyPropertyMetadataBy(String propertyName) {
        return keyPropertiesMetadata.stream().filter(prop -> prop.getPropertyName().equals(propertyName)).findFirst().orElse(null);
    }

    private Map<String, Object> namedValues(List<Object> singleCombination) {
        List<Pair<String, Object>> zipped = toList(zip(keyPropertiesMetadata.stream()
                .filter(KeyPropertyMetadata::isCollection)
                .map(KeyPropertyMetadata::getPropertyName)
                .collect(Collectors.toList()), singleCombination));
        Map<String, Object> map = zipped.stream().collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        return map;
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
