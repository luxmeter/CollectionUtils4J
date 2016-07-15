package luxmeter.receips;

import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static luxmeter.collectionutils.CollectionUtils.*;

public final class ManufactureBuilder<T> {
    private List<KeyPropertyMetadata<T, ?>> keyProperties = new ArrayList<>();

    private Collection<T> existingElements;
    private Collection<ElementAbstraction> intermediateEndResult; // goal
    private Function<ElementAbstraction, T> elementConstructor;

    private Function<T, Collection<ElementAbstraction>> intermediateResultsMapper;
    private Function<T, ?> groupingKey;
    private BinaryOperator<T> reducer;
    private Function<T, ElementAbstraction> intermediateResultMapper;

    private ManufactureBuilder() {
    }

    public static <T,A > ManufactureBuilder<T> create() {
        return new ManufactureBuilder<>();
    }

    public ManufactureBuilder<T> withExistingElements(Collection<T> existingElements) {
        this.existingElements = existingElements;
        return this;
    }

    public ManufactureBuilder<T> withIntermediateResultsMapper(Function<T, Collection<ElementAbstraction>>  intermediateResultsMapper) {
        this.intermediateResultsMapper = intermediateResultsMapper;
        return this;
    }

    public ManufactureBuilder<T> withElementConstructor(Function<ElementAbstraction, T> elementConstructor) {
        this.elementConstructor = elementConstructor;
        return this;
    }

    public ManufactureBuilder<T> withReducer(Function<T, ?> groupingKey, BinaryOperator<T> reducer) {
        this.groupingKey = groupingKey;
        this.reducer = reducer;
        return this;
    }

    public <R> ManufactureBuilder<T> withKeyProperty(String propertyName, Collection<R> valuesRange, SingleValueExtractor<T, R> propertyExtractor) {
        keyProperties.add(new KeyPropertyMetadata<>(propertyName, propertyExtractor, valuesRange));
        return this;
    }

    public <R, C extends Collection<R>> ManufactureBuilder<T> withKeyProperty(String propertyName, Collection<R> valuesRange, ValuesExtractor<T, R, C> propertyExtractor) {
        keyProperties.add(new KeyPropertyMetadata<>(propertyName, propertyExtractor, valuesRange));
        return this;
    }

    public interface SingleValueExtractor<T, R> extends Function<T, R> {

    }

    public interface ValuesExtractor<T, R, C extends Collection<R>> extends Function<T, C> {

    }


    @SuppressWarnings("unchecked")
    public Manufacture<T> build() {
        // lists[0] -> chargecodes
        // lists[1] -> products
        // lists[2] -> zones
        List<List<?>> valueRanges = keyProperties.stream()
                .map(keyProperty -> keyProperty.getValuesRange().stream()
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
        
        List<?>[] lists = (List<?>[]) valueRanges.toArray(new List[0]);
        List<List<?>> product = (List<List<?>>)(List<?>) product(lists);
        List<Map<String, ?>> namedProduct = product.stream().map(singleCombination ->
                toList(zip(getKeyPropertyNames(), singleCombination)).stream()
                        .collect(Collectors.toMap(Pair::getLeft, Pair::getRight)))
                .collect(Collectors.toList());
        intermediateEndResult = namedProduct.stream().map(ElementAbstraction::new).collect(Collectors.toSet());
        if (this.intermediateResultsMapper == null) {
            intermediateResultMapper = concreteElement -> {
                Map<String, Object> keyValues = keyProperties.stream()
                        .collect(Collectors.toMap(KeyPropertyMetadata::getPropertyName,
                                keyProperty -> keyProperty.getValueExtractor().apply(concreteElement)));
                return new ElementAbstraction(concreteElement, keyValues);
            };
        }
        return new Manufacture<>(this);
    }

    private List<String> getKeyPropertyNames() {
        return keyProperties.stream().map(KeyPropertyMetadata::getPropertyName).collect(Collectors.toList());
    }

    private static final class KeyPropertyMetadata<T, R> {
        private final String propertyName;
        private final Function<T, ?> valueExtractor;
        private final Function<?, String> toStringMapper;
        private final Collection<R> valuesRange;

        public KeyPropertyMetadata(String propertyName, SingleValueExtractor<T, R> valueExtractor, Collection<R> valuesRange) {
            this.propertyName = propertyName;
            this.valueExtractor = valueExtractor;
            this.valuesRange = valuesRange;
            this.toStringMapper = Object::toString;
        }

        public <C extends Collection<R>> KeyPropertyMetadata(String propertyName, ValuesExtractor<T, R, C> valueExtractor, Collection<R> valuesRange) {
            this.propertyName = propertyName;
            this.valueExtractor = valueExtractor;
            this.valuesRange = valuesRange;
            this.toStringMapper = Object::toString;
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
    }

    public static final class ElementAbstraction {
        private final Object sourceElement;
        private final Map<String, ?> keyPropertyValues;
        private final List<String> keyPropertyValuesAsString;

        public ElementAbstraction(Object sourceElement, Map<String, ?> keyPropertyValues) {
            this.sourceElement = sourceElement;
            this.keyPropertyValues = keyPropertyValues;
            keyPropertyValuesAsString = keyPropertyValues.values().stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }

        public ElementAbstraction(Map<String, ?> keyPropertyValues) {
            this(null, keyPropertyValues);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ElementAbstraction elementAbstraction = (ElementAbstraction) o;
            return keyPropertyValuesAsString.size() == elementAbstraction.keyPropertyValuesAsString.size()
                    && keyPropertyValuesAsString.containsAll(elementAbstraction.keyPropertyValuesAsString);
        }

        @Override
        public int hashCode() {
            return Objects.hash(keyPropertyValuesAsString);
        }

        public <R> R get(String propertyName) {
            return (R) keyPropertyValues.get(propertyName);
        }
    }

    public enum MergeType {
        MERGED,
        NOT_MERGED
    }

    public static final class Manufacture<T> {
        private final Set<T> existingConcreteElements;
        private final Set<ElementAbstraction> intermediateEndResult; // goal
        private final Function<ElementAbstraction, T> elementConstructor;

        private final Function<T, Collection<ElementAbstraction>> intermediateResultsMapper;
        private final Function<T, ElementAbstraction> intermediateResultMapper;
        private final Function<T, ?> groupingKey;
        private final BinaryOperator<T> reducer;

        public Manufacture(ManufactureBuilder<T> builder) {
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
                groupedGeneratedMissingConcreteElements.entrySet().forEach(this::reduce);

                // finish: flatten the resultl
                generatedMissingConcreteElements = groupedGeneratedMissingConcreteElements.values().stream()
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
