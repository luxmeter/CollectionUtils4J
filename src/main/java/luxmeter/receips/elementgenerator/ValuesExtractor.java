package luxmeter.receips.elementgenerator;

import java.util.Collection;
import java.util.function.Function;

public interface ValuesExtractor<T, R, C extends Collection<R>> extends Function<T, C> {

}
