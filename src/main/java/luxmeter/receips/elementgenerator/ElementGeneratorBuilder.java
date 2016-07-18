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
    private List<Reducer<T>> reducers = new ArrayList<>();


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
        List<Map<String, Object>> namedProductOfAllValues = attachNames(productOfAllValues);
        return namedProductOfAllValues.stream().map(map -> new ElementAbstraction(
                null, map, keyPropertyMetadataSet.getToStringMapper())).collect(Collectors.toSet());
    }

    private List<Map<String, Object>> attachNames(List<List<Object>> productOfAllValues) {
        return productOfAllValues.stream()
                .map(singleCombination -> keyPropertyMetadataSet.getPropertyValues(singleCombination))
                .collect(Collectors.toList());
    }

    public ElementGeneratorBuilder<T> withExistingElements(Collection<T> existingElements) {
        this.existingElements = existingElements;
        return this;
    }

    public ElementGeneratorBuilder<T> withElementFactory(Function<ElementAbstraction, T> elementConstructor) {
        this.elementConstructor = elementConstructor;
        return this;
    }

    public ElementGeneratorBuilder<T> withReducer(Function<T, ?> groupingKey, BinaryOperator<T> reducingFunction) {
        reducers.clear();
        reducers.add(Reducer.create(groupingKey, reducingFunction));
        return this;
    }

    @SafeVarargs
    public final ElementGeneratorBuilder<T> withReducers(Reducer<T>... reducers) {
        Objects.nonNull(reducers);
        this.reducers.clear();
        for (Reducer<T> reducer : reducers) {
            this.reducers.add(reducer);
        }
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
        if (overridenDefaults.getIntermediateResultMapper() != null) {
            this.intermediateResultMapper = concreteElement -> {
                Function<T, Map<String, Object>> mapper = overridenDefaults.getIntermediateResultMapper();
                return new ElementAbstraction(
                        concreteElement, mapper.apply(concreteElement), keyPropertyMetadataSet.getToStringMapper());
            };
        }
        else {
            this.intermediateResultsMapper = concreteElement -> {
                Function<T, Collection<Map<String, Object>>> mappers = overridenDefaults.getIntermediateResultsMapper();
                Collection<Map<String, Object>> maps = mappers.apply(concreteElement);
                return maps.stream()
                        .map(map -> new ElementAbstraction(
                                concreteElement, map, keyPropertyMetadataSet.getToStringMapper()))
                        .collect(Collectors.toList());
            };
        }
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

        return new ElementGenerator<>(this);
    }

    KeyPropertyMetadataSet<T> getKeyPropertyMetadataSet() {
        return keyPropertyMetadataSet;
    }

    Collection<T> getExistingElements() {
        return existingElements;
    }

    Collection<ElementAbstraction> getIntermediateEndResult() {
        return intermediateEndResult;
    }

    Function<ElementAbstraction, T> getElementConstructor() {
        return elementConstructor;
    }

    Function<T, ElementAbstraction> getIntermediateResultMapper() {
        return intermediateResultMapper;
    }

    Function<T, Collection<ElementAbstraction>> getIntermediateResultsMapper() {
        return intermediateResultsMapper;
    }

    List<Reducer<T>> getReducers() {
        return reducers;
    }
}
