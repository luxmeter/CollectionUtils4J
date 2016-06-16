package luxmeter.functions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public abstract class Functions {
    private Functions() {

    }

    public static Supplier<Integer> createCounter() {
        return new Supplier<Integer>() {
            private int count;
            @Override
            public Integer get() {
                return count++;
            }
        };
    }

    public static <A, B, C, R> PartialFunction<R> partial(Function3<A, B, C, R> function, Argument<A> a, Argument<B> b, Argument<C> c) {
        return createPartialFunction(function, a, b, c);
    }

    public static <A, B, R> PartialFunction<R> partial(BiFunction<A, B, R> function, Argument<A> a, Argument<B> b) {
        return createPartialFunction(function, a, b);
    }

    private static void checkArgumentSize(int maxSize, int rawArgumentsSize, int applicableArgumentsSize) {
        if (rawArgumentsSize + applicableArgumentsSize != maxSize) {
            throw new IllegalArgumentException(
                    String.format("Cannot invoke partial function. Expected %s argument(s) but were %s", maxSize - applicableArgumentsSize, maxSize));
        }
    }

    private static Method getMethod(Class<?> clazz) {
        return Stream.of(clazz.getMethods()).filter(m -> m.getName().equals("apply")).findFirst().get();
    }

    @SuppressWarnings("unchecked")
    private static <R> PartialFunction<R> createPartialFunction(Object function, Argument... args) {
        Collection<Argument<?>> applicableArgs = getApplicableArguments(args);
        return rawArguments -> {
            checkArgumentSize(args.length, rawArguments.length, applicableArgs.size());
            Supplier<Integer> counter = createCounter();
            Method method = getMethod(function.getClass());
            try {
                return (R) method.invoke(function,
                        Stream.of(args).map(arg -> arg.orElse(() -> rawArguments[counter.get()])).toArray());
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return null;
        };
    }

    private static List<Argument<?>> getApplicableArguments(Argument<?>... args) {
        return Stream.of(args).filter(Argument::isApplicable).collect(toList());
    }
}
