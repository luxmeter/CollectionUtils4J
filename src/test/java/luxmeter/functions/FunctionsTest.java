package luxmeter.functions;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static luxmeter.functions.Argument.bind;
import static luxmeter.functions.Argument.free;
import static luxmeter.functions.Functions.partial;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FunctionsTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testPartialApplication2() {
        PartialFunction<Integer> partial = Functions.<Integer, Integer, Integer>partial(
                this::add, free(), bind(2));
        Integer result = partial.apply(5);
        assertThat(result, is(add(5, 2)));
    }

    @Test
    public void testPartialApplication3() {
        PartialFunction<Integer> partial = Functions.<Integer, Integer, Integer, Integer>partial(
                this::add, bind(2), free(), bind(3));
        Integer result = partial.apply(5);
        assertThat(result, is(add(2, 5, 3)));
    }

    @Test
    public void testPartialApplicationFail() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Cannot invoke partial function. Expected 1 argument(s) but were 2");
        PartialFunction<Integer> partial = partial(this::sub, bind(10), free());
        partial.apply(5, 2); // should fail since there is no third parameter
    }

    private int add(int a, int b, int c) {
        return a + b + c;
    }

    private int add(int a, int b) {
        return a + b;
    }

    private int sub(int a, int b) {
        return a - b;
    }
}
