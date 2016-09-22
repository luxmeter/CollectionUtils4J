package luxmeter.receips.elementgenerator;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 * Builder to override default behaviour of the {@link ElementGeneratorBuilder}.
 * Used by {@link ElementGeneratorBuilder#withOverriddenDefaults(OverriddenDefaults)}
 * @see ElementGeneratorBuilder
 */
public final class OverriddenDefaults<T> {
    private Function<T, Map<String, Object>> intermediateResultMapper;
    private Function<T, Collection<Map<String, Object>>> intermediateResultsMapper;

    private void checkValidity() {
        if (intermediateResultMapper != null
                && intermediateResultsMapper != null) {
            throw new IllegalArgumentException(
                    "Either an intermediateResultMapper or intermediateResult[s]Mapper must be passed in.");
        }
    }

    private OverriddenDefaults() {
    }

    /**
     * Constructor
     * @param <T> concrete element type
     * @return this
     */
    public static <T> OverriddenDefaults<T> create() {
        return new OverriddenDefaults<>();
    }

    /**
     * @return the IntermediateResultMapper
     */
    public Function<T, Map<String, Object>> getIntermediateResultMapper() {
        return intermediateResultMapper;
    }

    public OverriddenDefaults<T> setIntermediateResultMapper(Function<T, Map<String, Object>> intermediateResultMapper) {
        this.intermediateResultMapper = intermediateResultMapper;
        checkValidity();
        return this;
    }

    /**
     * @return the IntermediateResultsMapper
     */
    public Function<T, Collection<Map<String, Object>>> getIntermediateResultsMapper() {
        return intermediateResultsMapper;
    }

    /**
     * @param intermediateResultsMapper creates an abstraction of an concrete element in form of a Map
     * @return the intermediateResultsMapper
     */
    public OverriddenDefaults<T> setIntermediateResultsMapper(Function<T, Collection<Map<String, Object>>> intermediateResultsMapper) {
        this.intermediateResultsMapper = intermediateResultsMapper;
        checkValidity();
        return this;
    }
}
