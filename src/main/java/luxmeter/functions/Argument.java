package luxmeter.functions;

import java.util.function.Supplier;

public final class Argument<T> {
    private final T value;

    private Argument(T value) {
        this.value = value;
    }

    public static <T> Argument<T> bind(T value) {
        return new Argument<>(value);
    }

    public static <T> Argument<T> free() {
        return new Argument<>(null);
    }

    boolean isApplicable() {
        return value != null;
    }

    T orElse(Supplier<T> alternative) {
        return isApplicable() ? value : alternative.get();
    }
}
