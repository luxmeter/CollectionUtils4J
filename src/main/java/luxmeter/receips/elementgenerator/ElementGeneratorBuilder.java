package luxmeter.receips.elementgenerator;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static luxmeter.collectionutils.CollectionUtils.*;

public final class ElementGeneratorBuilder<T> {
    private KeyPropertyMetadataSet<T> keyPropertyMetadataSet = new KeyPropertyMetadataSet<T>();

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
            Map<String, Object> keyValues = keyPropertyMetadataSet.getPropertyValues(concreteElement);
            return new ElementAbstraction(concreteElement, keyValues, keyPropertyMetadataSet.getToStringMapper());
        };
    }

    private Function<T, Collection<ElementAbstraction>> createIntermediateResultsMapper() {
        return concreteElement -> {
            Collection<Object> collect = keyPropertyMetadataSet.getPropertyValues(
                    concreteElement, KeyPropertyMetadata::isCollection).values();
            List<List<Object>> product = product(collect.toArray(new Collection[collect.size()]));

            // product[0] = (PX,A)
            List<ElementAbstraction> result = product.stream()
                    .map(singleCombination -> keyPropertyMetadataSet.getPropertyValues(
                            singleCombination, KeyPropertyMetadata::isCollection))
                    .map(map -> {
                        map.putAll(keyPropertyMetadataSet.getPropertyValues(concreteElement, p -> !p.isCollection()));
                        ElementAbstraction elementAbstraction = new ElementAbstraction(
                                concreteElement, map, keyPropertyMetadataSet.getToStringMapper());
                        return elementAbstraction;
                    }).collect(Collectors.toList());
            return result;
        };
    }

    private boolean intermediateResultMappersExist() {
        return intermediateResultMapper != null || intermediateResultsMapper != null;
    }

    @SuppressWarnings("unchecked")
    private Set<ElementAbstraction> createIntermediateEndResult() {
        List<List<?>> valueRanges = keyPropertyMetadataSet.getValueRanges();
        List<List<Object>> productOfAllValues =
                product((Collection[]) valueRanges.toArray(new List[valueRanges.size()]));
        List<Map<String, ?>> namedProductOfAllValues = attachNames(productOfAllValues);
        return namedProductOfAllValues.stream().map(map -> new ElementAbstraction(
                null, map, keyPropertyMetadataSet.getToStringMapper())).collect(Collectors.toSet());
    }

    private List<Map<String, ?>> attachNames(List<List<Object>> productOfAllValues) {
        return productOfAllValues.stream()
                .map(singleCombination -> keyPropertyMetadataSet.getPropertyValues(singleCombination))
                .collect(Collectors.toList());
    }

    private void injectDependenciesIntoIntermediateResultMappers() {
        if (intermediateResultMapper != null) {
            final Function<T, ElementAbstraction> oldIntermediateResultMapper = intermediateResultMapper;
            intermediateResultMapper = concreteElement -> {
                ElementAbstraction e = oldIntermediateResultMapper.apply(concreteElement);
                e.setToStringMapper(keyPropertyMetadataSet.getToStringMapper());
                return e;
            };
        }
        else if (intermediateResultsMapper != null) {
            final Function<T, Collection<ElementAbstraction>> oldIntermediateResultsMapper = intermediateResultsMapper;
            intermediateResultsMapper = concreteElement -> {
                Collection<ElementAbstraction> result = oldIntermediateResultsMapper.apply(concreteElement);
                result.forEach(e -> e.setToStringMapper(keyPropertyMetadataSet.getToStringMapper()));
                return result;
            };
        }
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
        keyPropertyMetadataSet.add(new KeyPropertyMetadata<>(propertyName, propertyExtractor, valuesRange, null));
        return this;
    }

    public <R> ElementGeneratorBuilder<T> withKeyProperty(String propertyName, Collection<R> valuesRange, SingleValueExtractor<T, R> propertyExtractor, Function<R, String> toStringMapper) {
        keyPropertyMetadataSet.add(new KeyPropertyMetadata<>(propertyName, propertyExtractor, valuesRange, toStringMapper));
        return this;
    }

    public <R, C extends Collection<R>> ElementGeneratorBuilder<T> withKeyProperty(String propertyName, Collection<R> valuesRange, ValuesExtractor<T, R, C> propertyExtractor) {
        keyPropertyMetadataSet.add(new KeyPropertyMetadata<>(propertyName, propertyExtractor, valuesRange, true, null));
        return this;
    }

    public <R, C extends Collection<R>> ElementGeneratorBuilder<T> withKeyProperty(String propertyName, Collection<R> valuesRange, ValuesExtractor<T, R, C> propertyExtractor, Function<R, String> toStringMapper) {
        keyPropertyMetadataSet.add(new KeyPropertyMetadata<>(propertyName, propertyExtractor, valuesRange, true, toStringMapper));
        return this;
    }

    public ElementGeneratorBuilder<T> withOverridenDefaults(OverridenDefaults<T> overridenDefaults) {
        this.intermediateResultMapper = overridenDefaults.getIntermediateResultMapper();
        this.intermediateResultsMapper = overridenDefaults.getIntermediateResultsMapper();
        return this;
    }

    @SuppressWarnings("unchecked")
    public ElementGenerator<T> build() {
        intermediateEndResult = createIntermediateEndResult();

        if (!intermediateResultMappersExist()) {
            if (keyPropertyMetadataSet.atLeastOnePropertyIsACollection()) {
                intermediateResultsMapper = createIntermediateResultsMapper();
            } else {
                intermediateResultMapper = createIntermediateResultMapper();
            }
        }
        // inject into custom user provided mappers
        else {
            injectDependenciesIntoIntermediateResultMappers();
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

        private ElementGenerator(ElementGeneratorBuilder<T> builder) {
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

            this.existingConcreteElements = new HashSet<>(builder.existingElements);
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
