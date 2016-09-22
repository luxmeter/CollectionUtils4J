package luxmeter.receips.elementgenerator;

import java.util.function.Function;

/**
 * Functional interface
 * @param <T> input
 * @param <R> output
 */
public interface SingleValueExtractor<T, R> extends Function<T, R> {

}
