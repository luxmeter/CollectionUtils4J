package luxmeter.receips;

import luxmeter.collectionutils.CollectionUtils;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ManufactureBuilder<T> {
    private List<KeyProperty<T, ?>> keyProperties = new ArrayList<>();

    private Collection<T> existingElements;
    private Collection<Key> intermediateEndResult; // goal
    private Function<Key, T> elementConstructor;

    private Function<T, Collection<Key>> intermediateResultsMapper;
    private Function<T, ?> groupingKey;
    private BinaryOperator<T> reducer;
    private Function<T, Key> intermediateResultMapper;

    private ManufactureBuilder() {
    }

    public static <T,A > ManufactureBuilder<T> create() {
        return new ManufactureBuilder<>();
    }

    public ManufactureBuilder<T> withExistingElements(Collection<T> existingElements) {
        this.existingElements = existingElements;
        return this;
    }

    public ManufactureBuilder<T> withIntermediateResultsMapper(Function<T, Collection<Key>>  intermediateResultsMapper) {
        this.intermediateResultsMapper = intermediateResultsMapper;
        return this;
    }

    public ManufactureBuilder<T> withElementConstructor(Function<Key, T> elementConstructor) {
        this.elementConstructor = elementConstructor;
        return this;
    }

    public ManufactureBuilder<T> withReducer(Function<T, ?> groupingKey, BinaryOperator<T> reducer) {
        this.groupingKey = groupingKey;
        this.reducer = reducer;
        return this;
    }

    public <R> ManufactureBuilder<T> withKeyProperty(String propertyName, Collection<R> valuesRange, SingleValueExtractor<T, R> propertyExtractor) {
        keyProperties.add(new KeyProperty<>(propertyName, propertyExtractor, valuesRange));
        return this;
    }

    public <R, C extends Collection<R>> ManufactureBuilder<T> withKeyProperty(String propertyName, Collection<R> valuesRange, ValuesExtractor<T, R, C> propertyExtractor) {
        keyProperties.add(new KeyProperty<>(propertyName, propertyExtractor, valuesRange));
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
        List<List<String>> valueRangesAsString = keyProperties.stream()
                .map(keyProperty -> keyProperty.getValuesRange().stream()
                        .map(Object::toString)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
        
        List<String>[] lists = (List<String>[]) valueRangesAsString.toArray(new List[0]);
        List<List<String>> product = (List<List<String>>)(List<?>) CollectionUtils.product(lists);
        Set<Key> collect = product.stream().map(Key::new).collect(Collectors.toSet());
        intermediateEndResult = collect;
        if (this.intermediateResultsMapper == null) {
            intermediateResultMapper = concreteElement -> {
                List<String> combination = keyProperties.stream()
                        .map(KeyProperty::getValueExtractor)
                        .map(extractor -> extractor.apply(concreteElement).toString())
                        .collect(Collectors.toList());
                return new Key(combination);
            };
        }
        return new Manufacture<>(this);
    }

    private static final class KeyProperty<T, R> {
        private final String propertyName;
        private final Function<T, ?> valueExtractor;
        private final Collection<R> valuesRange;

        public KeyProperty(String propertyName, SingleValueExtractor<T, R> valueExtractor, Collection<R> valuesRange) {
            this.propertyName = propertyName;
            this.valueExtractor = valueExtractor;
            this.valuesRange = valuesRange;
        }

        public <C extends Collection<R>> KeyProperty(String propertyName, ValuesExtractor<T, R, C> valueExtractor, Collection<R> valuesRange) {
            this.propertyName = propertyName;
            this.valueExtractor = valueExtractor;
            this.valuesRange = valuesRange;
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
    }

    public static final class Key {
        private final List<String> keyProperties;

        public Key(Collection<String> keyProperties) {
            this.keyProperties = new ArrayList<>(keyProperties);
        }

        public List<String> getKeyProperties() {
            return keyProperties;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return keyProperties.size() == key.keyProperties.size() && keyProperties.containsAll(key.keyProperties);
        }

        @Override
        public int hashCode() {
            return Objects.hash(keyProperties);
        }
    }

    public enum MergeType {
        MERGED,
        NOT_MERGED
    }

    public static final class Manufacture<T> {
        private final Set<T> existingConcreteElements;
        private final Set<Key> intermediateEndResult; // goal
        private final Function<Key, T> elementConstructor;

        private final Function<T, Collection<Key>> intermediateResultsMapper;
        private final Function<T, Key> intermediateResultMapper;
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
            this.intermediateEndResult = new HashSet<Key>(builder.intermediateEndResult);
            this.elementConstructor = builder.elementConstructor;
            this.groupingKey = builder.groupingKey;
            this.reducer = builder.reducer;
        }

        public Set<T> generateMissingElements() {
            return generateMissingElements(MergeType.NOT_MERGED);
        }

        public Set<T> generateMissingElements(MergeType merged) {
            Set<Key> existingAbstractElements = existingConcreteElements.stream()
                    .flatMap(e->intermediateResultsMapper.apply(e).stream())
                    .collect(Collectors.toSet());

            Set<Key> missingAbstractElements = CollectionUtils.removeAll(intermediateEndResult, existingAbstractElements);

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
