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
    private final List<Reducer<T>> reducers;

    ElementGenerator(ElementGeneratorBuilder<T> builder) {
        Objects.requireNonNull(builder.getExistingElements());
        Objects.requireNonNull(builder.getIntermediateEndResult());
        Objects.requireNonNull(builder.getElementConstructor());

        if ((builder.getIntermediateResultMapper() != null) == (builder.getIntermediateResultsMapper() != null)) {
            throw new IllegalArgumentException(
                    "Either an intermediateResultMapper or intermediateResult[s]Mapper must be passed in.");
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
        this.reducers = new ArrayList<>(builder.getReducers());
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

        if (merged == MergeType.MERGED && !reducers.isEmpty()) {
            generatedMissingConcreteElements = reduce(generatedMissingConcreteElements, reducers);
        }

        return generatedMissingConcreteElements;
    }

    @SuppressWarnings("unchecked")
    public Set<T> reduce(Set<T> generatedMissingConcreteElements, List<Reducer<T>> reducers) {
        if (reducers.isEmpty()) {
            return generatedMissingConcreteElements;
        }
        Reducer reducer = reducers.remove(0);
        Map<Object, List<T>> groupedGeneratedMissingConcreteElements =
                (Map<Object, List<T>>) generatedMissingConcreteElements.stream()
                        .collect(Collectors.groupingBy(reducer.getGroupingKey()));
        Map<Object, List<T>> reducedMap = new HashMap<>(groupedGeneratedMissingConcreteElements);
        reducedMap.entrySet().forEach(entry ->
                entry.setValue(reduced(entry.getValue(), reducer.getReducingFunction())));
        // finish: flatten the resultl
        generatedMissingConcreteElements = reducedMap.values().stream()
                .map(r -> r.get(0))
                .collect(Collectors.toSet());
        return reduce(generatedMissingConcreteElements, reducers);
    }

    private List<T> reduced(List<T> list, BinaryOperator<T> reducer) {
        List<T> reducedList = Collections.singletonList(list.stream()
                .collect(Collectors.reducing(list.get(0), reducer)));
        return reducedList;
    }
}
