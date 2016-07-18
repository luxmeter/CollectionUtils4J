package luxmeter.receips.elementgenerator;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static luxmeter.collectionutils.CollectionUtils.removeAll;

/**
 *
 */
public final class ElementGenerator<T> {
    private final Set<T> existingConcreteElements;
    private final Set<ElementAbstraction> intermediateEndResult; // goal
    private final Function<ElementAbstraction, T> elementConstructor;

    private final Function<T, Collection<ElementAbstraction>> intermediateResultsMapper;
    private final Function<T, ElementAbstraction> intermediateResultMapper;
    private final Function<T, ?> groupingKey;
    private final BinaryOperator<T> reducer;

    ElementGenerator(ElementGeneratorBuilder<T> builder) {
        Objects.requireNonNull(builder.getExistingElements());
        Objects.requireNonNull(builder.getIntermediateEndResult());
        Objects.requireNonNull(builder.getElementConstructor());

        if ((builder.getIntermediateResultMapper() != null) == (builder.getIntermediateResultsMapper() != null)) {
            throw new IllegalArgumentException(
                    "Either an intermediateResultMapper or intermediateResult[s]Mapper must be passed in.");
        }

        if (builder.getGroupingKey() != null || builder.getReducer() != null) {
            Objects.requireNonNull(builder.getGroupingKey());
            Objects.requireNonNull(builder.getReducer());
        }

        this.intermediateResultMapper = builder.getIntermediateResultMapper();
        if (intermediateResultMapper != null) {
            this.intermediateResultsMapper = e -> Collections.singletonList(intermediateResultMapper.apply(e));
        } else {
            this.intermediateResultsMapper = builder.getIntermediateResultsMapper();
        }

        this.existingConcreteElements = new HashSet<>(builder.getExistingElements());
        this.intermediateEndResult = new HashSet<>(builder.getIntermediateEndResult());
        this.elementConstructor = builder.getElementConstructor();
        this.groupingKey = builder.getGroupingKey();
        this.reducer = builder.getReducer();
    }

    public Set<T> generateMissingElements() {
        return generateMissingElements(MergeType.NOT_MERGED);
    }

    public Set<T> generateMissingElements(MergeType merged) {
        Set<ElementAbstraction> existingAbstractElements = existingConcreteElements.stream()
                .flatMap(e -> intermediateResultsMapper.apply(e).stream())
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
