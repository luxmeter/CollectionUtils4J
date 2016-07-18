package luxmeter.receips.elementgenerator;

import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 *
 */
public final class Reducer<T> {
    private final Function<T, ?> groupingKey;
    private final BinaryOperator<T> reducer;

    private Reducer(Function<T, ?> groupingKey, BinaryOperator<T> reducer) {
        Objects.requireNonNull(groupingKey, "If you don't need to group," +
                " you can just simply add the identity function" +
                " (java.util.function.Function.identity)");
        Objects.requireNonNull(reducer, "Reducing without a reducing function is somehow meaningless, don't you think?");
        this.groupingKey = groupingKey;
        this.reducer = reducer;
    }

    public static <T> Reducer<T> create(Function<T, ?> groupingKey, BinaryOperator<T> reducer) {
        return new Reducer<T>(groupingKey, reducer);
    }

    public Function<T, ?> getGroupingKey() {
        return groupingKey;
    }

    public BinaryOperator<T> getReducingFunction() {
        return reducer;
    }
}
