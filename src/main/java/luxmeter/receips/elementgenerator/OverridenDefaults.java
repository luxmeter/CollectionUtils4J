package luxmeter.receips.elementgenerator;

import java.util.Collection;
import java.util.function.Function;

/**
 *
 */
public final class OverridenDefaults<T> {
    private Function<T, ElementAbstraction> intermediateResultMapper;
    private Function<T, Collection<ElementAbstraction>> intermediateResultsMapper;

    private void checkValidity(Function<T, ElementAbstraction> intermediateResultMapper) {
        if (intermediateResultMapper != null
                && intermediateResultsMapper != null) {
            throw new IllegalArgumentException(
                    "Either an intermediateResultMapper or intermediateResult[s]Mapper must be passed in.");
        }
    }

    private OverridenDefaults() {
    }

    public static <T> OverridenDefaults<T> create() {
        return new OverridenDefaults<>();
    }

    public Function<T, ElementAbstraction> getIntermediateResultMapper() {
        return intermediateResultMapper;
    }

    public OverridenDefaults<T> setIntermediateResultMapper(Function<T, ElementAbstraction> intermediateResultMapper) {
        this.intermediateResultMapper = intermediateResultMapper;
        checkValidity(intermediateResultMapper);
        return this;
    }

    public Function<T, Collection<ElementAbstraction>> getIntermediateResultsMapper() {
        return intermediateResultsMapper;
    }

    public OverridenDefaults<T> setIntermediateResultsMapper(Function<T, Collection<ElementAbstraction>> intermediateResultsMapper) {
        this.intermediateResultsMapper = intermediateResultsMapper;
        checkValidity(intermediateResultMapper);
        return this;
    }
}
