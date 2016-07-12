package luxmeter.receips;

import luxmeter.collectionutils.CollectionUtils;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ManufactureBuilder<T, A> {
    private Collection<T> existingElements;
    private Collection<A> intermediateEndResult; // goal
    private Function<A, T> elementConstructor;

    private Function<T, Collection<A>> intermediateResultsMapper;
    private Function<T, ?> groupingKey;
    private BinaryOperator<T> reducer;
    private Function<T, A> intermediateResultMapper;

    private ManufactureBuilder() {
    }

    public static <T,A > ManufactureBuilder<T, A> create() {
        return new ManufactureBuilder<>();
    }

    public ManufactureBuilder<T, A> withExistingElements(Collection<T> existingElements) {
        this.existingElements = existingElements;
        return this;
    }

    public ManufactureBuilder<T, A> withIntermediateResultsMapper(Function<T, Collection<A>>  intermediateResultsMapper) {
        this.intermediateResultsMapper = intermediateResultsMapper;
        return this;
    }

    public ManufactureBuilder<T, A> withIntermediateResultMapper(Function<T, A>  intermediateResultMapper) {
        this.intermediateResultMapper = intermediateResultMapper;
        return this;
    }

    public ManufactureBuilder<T, A> withIntermediateEndResult(Collection<A> intermediateEndResult) {
        this.intermediateEndResult = intermediateEndResult;
        return this;
    }

    public ManufactureBuilder<T, A> withElementConstructor(Function<A, T> elementConstructor) {
        this.elementConstructor = elementConstructor;
        return this;
    }

    public ManufactureBuilder<T, A> withReducer(Function<T, ?> groupingKey, BinaryOperator<T> reducer) {
        this.groupingKey = groupingKey;
        this.reducer = reducer;
        return this;
    }


    public Manufacture<T, A> build() {
        return new Manufacture<>(existingElements, intermediateResultMapper,
                intermediateResultsMapper,
                intermediateEndResult, elementConstructor,
                groupingKey, reducer);
    }

    public enum MergeType {
        MERGED,
        NOT_MERGED
    }

    public static final class Manufacture<T, A> {
        private final Set<T> existingConcreteElements;
        private final Set<A> intermediateEndResult; // goal
        private final Function<A, T> elementConstructor;

        private final Function<T, Collection<A>> intermediateResultsMapper;
        private final Function<T, A> intermediateResultMapper;
        private final Function<T, ?> groupingKey;
        private final BinaryOperator<T> reducer;

        public Manufacture(Collection<T> existingConcreteElements, Function<T, A> intermediateResultMapper,
                           Function<T, Collection<A>> intermediateResultsMapper,
                           Collection<A> intermediateEndResult, Function<A, T> elementConstructor,
                           Function<T, ?> groupingKey, BinaryOperator<T> reducer) {
            Objects.requireNonNull(existingConcreteElements);
            Objects.requireNonNull(intermediateEndResult);
            Objects.requireNonNull(elementConstructor);

            if (!(intermediateResultMapper != null ^ intermediateResultsMapper != null)) {
                throw new IllegalArgumentException(
                        "Either an intermediateResultMapper or intermediateResult[s]Mapper must be passed in.");
            }
            if (groupingKey != null || reducer != null) {
                Objects.requireNonNull(groupingKey);
                Objects.requireNonNull(reducer);
            }

            this.intermediateResultMapper = intermediateResultMapper;
            if (intermediateResultMapper != null) {
                this.intermediateResultsMapper = e -> Collections.singletonList(intermediateResultMapper.apply(e));
            }
            else {
                this.intermediateResultsMapper = intermediateResultsMapper;
            }

            this.existingConcreteElements = new HashSet<T>(existingConcreteElements);
            this.intermediateEndResult = new HashSet<A>(intermediateEndResult);
            this.elementConstructor = elementConstructor;
            this.groupingKey = groupingKey;
            this.reducer = reducer;
        }

        public Set<T> generateMissingElements() {
            return generateMissingElements(MergeType.NOT_MERGED);
        }

        public Set<T> generateMissingElements(MergeType merged) {
            Set<A> existingAbstractElements = existingConcreteElements.stream()
                    .flatMap(e->intermediateResultsMapper.apply(e).stream())
                    .collect(Collectors.toSet());

            Set<A> missingAbstractElements = CollectionUtils.removeAll(intermediateEndResult, existingAbstractElements);

            Set<T> generatedMissingConcreteElements = missingAbstractElements.stream()
                    .map(elementConstructor)
                    .collect(Collectors.toSet());

            if (merged == MergeType.MERGED && groupingKey != null) {
                Map<?, List<T>> groupedGeneratedMissingConcreteElements =
                        generatedMissingConcreteElements.stream().collect(Collectors.groupingBy(groupingKey));
                groupedGeneratedMissingConcreteElements.entrySet().forEach(this::reduce);

                // finish: flatten the result
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
