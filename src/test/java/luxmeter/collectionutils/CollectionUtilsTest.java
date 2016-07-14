package luxmeter.collectionutils;


import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static luxmeter.collectionutils.CollectionUtils.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class CollectionUtilsTest {
    @Test
    public void shouldZipIterablesWithSameSize() {
        Iterable<Pair<String, Integer>> zipped = zip(Arrays.asList("a", "b", "c"), Arrays.asList(1, 2, 3));
        assertThat(zipped, contains(Pair.of("a", 1), Pair.of("b", 2), Pair.of("c", 3)));
    }

    @Test
    public void shouldZipIterablesWithDifferentSize() {
        Iterable<Pair<String, Integer>> zipped = zip(Arrays.asList("a", "b"), Arrays.asList(1, 2, 3, 4));
        assertThat(zipped, contains(Pair.of("a", 1), Pair.of("b", 2), Pair.of(null, 3), Pair.of(null, 4)));
    }

    @Test
    public void shouldZipIterablesWithDifferentSizeAndDefaultValues() {
        String defaultValue = "_";
        Iterable<Pair<String, Integer>> zipped = zip( Arrays.asList("a", "b"), Arrays.asList(1, 2, 3, 4),
                defaultValue, null);
        assertThat(zipped, contains(Pair.of("a", 1), Pair.of("b", 2), Pair.of(defaultValue, 3), Pair.of(defaultValue, 4)));
    }

    @Test
    public void shouldAppendIterables() {
        Iterable<String> append = chain(Arrays.asList("a", "b", "c"),  Arrays.asList("d", "e"));
        assertThat(append, contains("a", "b", "c", "d", "e"));
    }

    @Test
    public void shouldRepeat3Times() {
        assertThat(repeat("a", 3), contains("a", "a", "a"));
    }

    @Test
    public void shouldCycle3Times() {
        assertThat(cycle(Arrays.asList("a", "b"), 3), contains("a", "b", "a", "b", "a", "b"));
    }

    @Test
    public void shouldBuildProduct() {
        List<Pair<String, Integer>> combi = product(Arrays.asList("a", "b", "c"), Arrays.asList(1, 2, 3));
        assertThat(combi, hasSize(9));
        assertThat(combi, containsInAnyOrder(
                Pair.of("a", 1),
                Pair.of("b", 1),
                Pair.of("c", 1),
                Pair.of("a", 2),
                Pair.of("b", 2),
                Pair.of("c", 2),
                Pair.of("a", 3),
                Pair.of("b", 3),
                Pair.of("c", 3)));
    }

    @Test
    public void shouldBuildProduct2() {
        List<Pair<String, Integer>> combi = product(Arrays.asList("a", "b", "c"), Arrays.asList(1, 2));
        assertThat(combi, hasSize(6));
        assertThat(combi, containsInAnyOrder(
                Pair.of("a", 1),
                Pair.of("b", 1),
                Pair.of("c", 1),
                Pair.of("a", 2),
                Pair.of("b", 2),
                Pair.of("c", 2)));
    }

    @Test
    public void shouldBuildProduct3() {
        List<String> firstCollection = Arrays.asList("5510", "5520");
        List<String> secondCollection = Arrays.asList("PX", "TX", "XX");
        List<String> thirdCollection = Arrays.asList("A", "B");

        List<List<Object>> product = product(firstCollection, secondCollection, thirdCollection);
        assertThat(product, hasSize(12));
        assertThat(product.stream().map(List::toString).collect(Collectors.toList()), contains(
            "[5510, PX, A]",
            "[5510, PX, B]",
            "[5510, TX, A]",
            "[5510, TX, B]",
            "[5510, XX, A]",
            "[5510, XX, B]",
            "[5520, PX, A]",
            "[5520, PX, B]",
            "[5520, TX, A]",
            "[5520, TX, B]",
            "[5520, XX, A]",
            "[5520, XX, B]"
        ));
    }
}
