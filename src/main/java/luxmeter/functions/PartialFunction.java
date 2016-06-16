package luxmeter.functions;

public interface PartialFunction<R> {
    R apply(Object... arguments);
}
