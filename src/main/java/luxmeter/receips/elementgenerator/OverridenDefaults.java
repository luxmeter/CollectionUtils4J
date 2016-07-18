package luxmeter.receips.elementgenerator;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 *
 */
public final class OverridenDefaults<T> {
    private Function<T, Map<String, Object>> intermediateResultMapper;
    private Function<T, Collection<Map<String, Object>>> intermediateResultsMapper;

    private void checkValidity() {
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

    public Function<T, Map<String, Object>> getIntermediateResultMapper() {
        return intermediateResultMapper;
    }

    public OverridenDefaults<T> setIntermediateResultMapper(Function<T, Map<String, Object>> intermediateResultMapper) {
        this.intermediateResultMapper = intermediateResultMapper;
        checkValidity();
        return this;
    }

    public Function<T, Collection<Map<String, Object>>> getIntermediateResultsMapper() {
        return intermediateResultsMapper;
    }

    public OverridenDefaults<T> setIntermediateResultsMapper(Function<T, Collection<Map<String, Object>>> intermediateResultsMapper) {
        this.intermediateResultsMapper = intermediateResultsMapper;
        checkValidity();
        return this;
    }
}
