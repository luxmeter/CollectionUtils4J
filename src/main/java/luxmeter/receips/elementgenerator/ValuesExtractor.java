package luxmeter.receips.elementgenerator;

import java.util.Collection;
import java.util.function.Function;

/**
 * Functional interface
 * @param <T> input
 * @param <R> type of the elements within the collection
 * @param <C> collection type
 */
public interface ValuesExtractor<T, R, C extends Collection<R>> extends Function<T, C> {

}
