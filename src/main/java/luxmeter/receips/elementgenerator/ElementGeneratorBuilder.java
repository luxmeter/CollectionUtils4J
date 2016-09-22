package luxmeter.receips.elementgenerator;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static luxmeter.collectionutils.CollectionUtils.product;

/**
 * Convenient builder to create a configured instance of {@link ElementGenerator} via a fluent API.
 * @param <T> type of the concrete elements
 */
public final class ElementGeneratorBuilder<T> {
    private KeyPropertyMetadataSet<T> keyPropertyMetadataSet = new KeyPropertyMetadataSet<T>();

    private Collection<T> existingElements;
    private Collection<ElementAbstraction> intermediateEndResult; // goal
    private ElementFactory<T> elementConstructor;

    private Function<T, Collection<ElementAbstraction>> intermediateResultsMapper;
    private List<Reducer<T>> reducers = new ArrayList<>();

    /**
     * Using this constructor forces the user to declare a variable with the typed generics first.
     * {@link #create(Class)} in contrast helps Java to determine the type on the fly.
     * Alternatively, the user could declare the generic type before the invocation, i.e.:<br/>
     * {@code ElementGeneratorBuilder.<SomeType>.create()... }
     *
     * @param <T> concrete type of the elements
     * @see #create(Class)
     * @return type builder
     */
    public static <T> ElementGeneratorBuilder<T> create() {
        return new ElementGeneratorBuilder<>();
    }

    /**
     * Constructor method for this builder.
     * @param clazz Class of the concrete elements helping Java to determine the generic types of this builder on the fly.
     * @param <T> type of the concrete elements
     * @see #create()
     * @return type builder
     */
    public static <T> ElementGeneratorBuilder<T> create(Class<T> clazz) {
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
        return intermediateResultsMapper != null;
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

    /**
     *
     * @param existingElements collection for which missing elements shall be generated
     * @return this
     */
    public ElementGeneratorBuilder<T> withExistingElements(Collection<? extends T> existingElements) {
        this.existingElements = (Collection<T>) existingElements;
        return this;
    }

    /**
     * Defines mandatory factory method to create concrete elements for the collection for which the missing elements have been identified.<br/>
     * Example:
     * <pre><code>
     *   // SimplifiedRate(String chargeCode, Product product, Zone zone)
     *   .withElementFactory(abstractElement -> new SimplifiedRate(
     *          abstractElement.get("chargeCode"),
     *          abstractElement.get("product"),
     *          abstractElement.get("zone"))
     *    )
     * </code>
     * </pre>
     * Notice that ElementAbstraction only returns singular attributes. Thus, similar looking elements could be generated.
     * Define a reducer to merge these together.
     * @param elementConstructor constructor to create a concrete element from its internal used abstraction
     * @see ElementAbstraction
     * @see #withElementFactory(ElementFactory)
     * @see #withReducer(Function, BinaryOperator)
     * @return this
     */
    public ElementGeneratorBuilder<T> withElementFactory(Function<ElementAbstraction, T> elementConstructor) {
        withElementFactory((allGeneratedElements, toGenerateFrom) -> elementConstructor.apply(toGenerateFrom));
        return this;
    }

    /**
     * In contrast to {@link #withElementFactory(Function)} the user has also the information
     * about the other missing elements when defining the constructor method.
     * @param elementConstructor  constructor to create a concrete element from its internal used abstraction
     * @see #withElementFactory(Function)
     * @see ElementFactory
     * @return this
     */
    public ElementGeneratorBuilder<T> withElementFactory(ElementFactory<T> elementConstructor) {
        this.elementConstructor = elementConstructor;
        return this;
    }

    /**
     * It may happen that similar looking elements could be generated by the element factory
     * since the internal used abstraction of the concrete type consists only of singular attributes.
     * In this case, a reducer can be defined to merge these together, for example:
     * <pre><code>
     // Rate(String chargeCode, Set<Product></Product> product)
     .withReducer(Rate::getChargeCode,
             (rate1, rate2) -> new Rate(
                    rate1.getChargeCode(),
                    chain(rate1.getProducts(), rate2.getProducts()))
     )
     * </code></pre>
     * @param groupingKey key to identify similar looking elements
     * @param reducingFunction binary function returning the merged element
     * @return this
     */
    public ElementGeneratorBuilder<T> withReducer(Function<T, ?> groupingKey, BinaryOperator<T> reducingFunction) {
        reducers.clear();
        reducers.add(Reducer.create(groupingKey, reducingFunction));
        return this;
    }

    /**
     * In contrast to {@link #withReducer(Function, BinaryOperator)}, this method allows to specify multiple reducers
     * in case the merging logic is more complex and consists of multiple steps.<br/>
     * Example:
     * <pre><code>
     .withReducers(
            Reducer.create(this::groupByChargeCodeAndProduct, this::mergeByZones),
            Reducer.create(this::groupByChargeCodeAndZone, this::mergeByProducts))
     * </code></pre>
     * @param reducers reducing functions
     * @return this
     */
    @SafeVarargs
    public final ElementGeneratorBuilder<T> withReducers(Reducer<T>... reducers) {
        Objects.nonNull(reducers);
        this.reducers.clear();
        for (Reducer<T> reducer : reducers) {
            this.reducers.add(reducer);
        }
        return this;
    }

    /**
     * Part of the unique key for an instance of a concrete element.
     * All parts together form the abstraction of a concrete element which will be help to identify the missing elements.<br/>
     * For example:<pre>
     * {@code .withSingleValueProperty("product", applicableProducts, SimplifiedRate::getProduct)}</pre>
     * @param propertyName Name of the property. Used also to access the value from the element abstraction
     * @param valuesRange all possible values for the property
     * @param propertyExtractor getter for the property
     * @param <R> type of the property
     * @see #withSingleValueProperty(String, Collection, SingleValueExtractor, Function)
     * @return this
     */
    public <R> ElementGeneratorBuilder<T> withSingleValueProperty(String propertyName, Collection<R> valuesRange, SingleValueExtractor<T, R> propertyExtractor) {
        keyPropertyMetadataSet.add(new KeyPropertyMetadata<>(propertyName, propertyExtractor, valuesRange, null));
        return this;
    }

    /**
     * In addition to {@link #withSingleValueProperty(String, Collection, SingleValueExtractor)} it is also possible to specify how to retrieve the unique string representation.
     * @param propertyName Name of the property. Used also to access the value from the element abstraction
     * @param valuesRange all possible values for the property
     * @param propertyExtractor getter for the property
     * @param toStringMapper unique string representation
     * @param <R> type of the property
     * @see #withSingleValueProperty(String, Collection, SingleValueExtractor)
     * @return this
     */
    public <R> ElementGeneratorBuilder<T> withSingleValueProperty(String propertyName, Collection<R> valuesRange, SingleValueExtractor<T, R> propertyExtractor, Function<R, String> toStringMapper) {
        keyPropertyMetadataSet.add(new KeyPropertyMetadata<>(propertyName, propertyExtractor, valuesRange, toStringMapper));
        return this;
    }

    /**
     * Part of the unique key for an instance of a concrete element.
     * All parts together form the abstraction of a concrete element which will be help to identify the missing elements.<br/>
     * For example:<pre>
     * {@code .withCollectionProperty("zone", applicableZones, SimplifiedRate::getZones)}</pre>
     *
     * @param propertyName      Name of the property. Used also to access the value from the element abstraction
     * @param valuesRange       all possible values for the property
     * @param propertyExtractor getter for the property
     * @param <R>               type of the property
     * @return this
     * @see #withSingleValueProperty(String, Collection, SingleValueExtractor, Function)
     */
    public <R, C extends Collection<R>> ElementGeneratorBuilder<T> withCollectionProperty(String propertyName, Collection<R> valuesRange, ValuesExtractor<T, R, C> propertyExtractor) {
        keyPropertyMetadataSet.add(new KeyPropertyMetadata<>(propertyName, propertyExtractor, valuesRange, true, null));
        return this;
    }

    /**
     * In addition to {@link #withCollectionProperty(String, Collection, ValuesExtractor)} it is also possible to specify how to retrieve the unique string representation.
     *
     * @param propertyName      Name of the property. Used also to access the value from the element abstraction
     * @param valuesRange       all possible values for the property
     * @param propertyExtractor getter for the property
     * @param toStringMapper    unique string representation
     * @param <R>               type of the property
     * @return this
     * @see #withCollectionProperty(String, Collection, ValuesExtractor)
     */
    public <R, C extends Collection<R>> ElementGeneratorBuilder<T> withCollectionProperty(String propertyName, Collection<R> valuesRange, ValuesExtractor<T, R, C> propertyExtractor, Function<R, String> toStringMapper) {
        keyPropertyMetadataSet.add(new KeyPropertyMetadata<>(propertyName, propertyExtractor, valuesRange, true, toStringMapper));
        return this;
    }

    /**
     * Overrides default behaviour.
     * @param overriddenDefaults custom behaviour
     * @return this
     */
    public ElementGeneratorBuilder<T> withOverriddenDefaults(OverriddenDefaults<T> overriddenDefaults) {
        if (overriddenDefaults.getIntermediateResultMapper() != null) {
             Function<T, ElementAbstraction> intermediateResultMapper = concreteElement -> {
                Function<T, Map<String, Object>> mapper = overriddenDefaults.getIntermediateResultMapper();
                return new ElementAbstraction(
                        concreteElement, mapper.apply(concreteElement), keyPropertyMetadataSet.getToStringMapper());
            };
            this.intermediateResultsMapper = wrapIntoResultsMapper(intermediateResultMapper);
        }
        else {
            this.intermediateResultsMapper = concreteElement -> {
                Function<T, Collection<Map<String, Object>>> mappers = overriddenDefaults.getIntermediateResultsMapper();
                Collection<Map<String, Object>> maps = mappers.apply(concreteElement);
                return maps.stream()
                        .map(map -> new ElementAbstraction(
                                concreteElement, map, keyPropertyMetadataSet.getToStringMapper()))
                        .collect(Collectors.toList());
            };
        }
        return this;
    }

    private Function<T, Collection<ElementAbstraction>> wrapIntoResultsMapper(Function<T, ElementAbstraction> intermediateResultMapper) {
        return e -> Collections.singletonList(intermediateResultMapper.apply(e));
    }

    /**
     * Builds the ElementGenerator.
     * @return configured ElementGenerator
     */
    @SuppressWarnings("unchecked")
    public ElementGenerator<T> build() {
        intermediateEndResult = createIntermediateEndResult();

        if (!intermediateResultMappersExist()) {
            if (keyPropertyMetadataSet.atLeastOnePropertyIsACollection()) {
                intermediateResultsMapper = createIntermediateResultsMapper();
            } else {
                intermediateResultsMapper = wrapIntoResultsMapper(createIntermediateResultMapper());
            }
        }

        return new ElementGenerator<>(this);
    }

    /**
     * @return unique key identifier
     */
    KeyPropertyMetadataSet<T> getKeyPropertyMetadataSet() {
        return keyPropertyMetadataSet;
    }

    /**
     * @return existing elements
     */
    Collection<T> getExistingElements() {
        return existingElements;
    }

    /**
     * @return intermediate end result
     */
    Collection<ElementAbstraction> getIntermediateEndResult() {
        return intermediateEndResult;
    }

    /**
     * @return element constructor
     */
    ElementFactory<T> getElementConstructor() {
        return elementConstructor;
    }

    /**
     * @return intermediate results mapper
     */
    Function<T, Collection<ElementAbstraction>> getIntermediateResultsMapper() {
        return intermediateResultsMapper;
    }

    /**
     * @return reducers
     */
    List<Reducer<T>> getReducers() {
        return reducers;
    }
}
