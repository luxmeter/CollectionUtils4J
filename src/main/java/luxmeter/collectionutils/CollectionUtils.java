package luxmeter.collectionutils;

import org.apache.commons.lang3.tuple.Pair;

import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Util class defining convenient methods to operate on collections.
 */
public final class CollectionUtils {
    private static final Iterable NULL_REPEATABLE = () -> new Iterator() {
        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public Object next() {
            return null;
        }
    };

    /**
     * Convenient method to sequence the elements of a collection in encountered order.
     * Handy in use with the for each loop or the Stream API, e.g.:
     * <pre>{@code
     * for(ElementWithSequence es: enumerate(persons)) {
     *     int sequence = es.getSequence();
     *     Person p = es.getEelement();
     *     ...
     * }
     * }
     * </pre>
     *
     * @param collection collection to sequence
     * @param offset starting number
     * @param <T> type of the elements within the collection
     * @return Iterable of {@link ElementWithSequence}
     */
	public static <T> Iterable<ElementWithSequence<T>> enumerate(Collection<T> collection, int offset) {
		return () -> {
            Iterator<T> iterator = collection.iterator();
            return new Iterator<ElementWithSequence<T>>() {
                int sequence = offset;

                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public ElementWithSequence<T> next() {
                        return new ElementWithSequence<>(sequence++, iterator.next());
                    }
            };
        };
    }

    /**
     * As {@link CollectionUtils#enumerate(Collection, int)} but with a default offset of 0.
     * @see CollectionUtils#enumerate(Collection, int)
     * @param collection collection to sequence
     * @param <T> type of the elements within the collection
     * @return Iterable of {@link ElementWithSequence}
     */
	public static <T> Iterable<ElementWithSequence<T>> enumerate(Collection<T> collection) {
        return enumerate(collection, 0);
    }

    /**
     * Converts an iterable into a stream.
     * By default the stream cannot be parallelized.
     * @param iterable iterable to convert into a stream
     * @return stream
     */
    public static <T> Stream<T> toStream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    /**
     * Zipps passed in iterables into a new iterable of pairs.
     * A pair at the n'th position consists of the elements at the same position in the corresponding iterables.
     * In case that the iterables don't have the same length, {@code null} is used as fallback value for the shorter one.
     * <br/>
     * <p>
     * Example: <pre>{@code
    List<String> values = Arrays.asList("a", "b", "c");
    List<Integer> sequence = IntStream.range(0, values.size()) .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    Iterable<Pair<Integer, String>> sequencedValues = zip(sequence, values); // [(0, "a"), (1, "b"), (2, "c")]
    }
     * </pre>
     * </p>
     * @param firstIterable first zip source
     * @param secondIterable second zip source
     * @return zipped iterable
     */
    @SuppressWarnings("unchecked")
    public static <K,V> Iterable<Pair<K, V>> zip(Iterable<K> firstIterable, Iterable<V> secondIterable) {
        return zip(firstIterable, secondIterable, null, null);
    }

    /**
     * Zipps passed in iterables into a new iterable of pairs.
     * A pair at the n'th position consists of the elements at the same position in the corresponding iterables.
     * In case that the iterables don't have the same length, a fallback value can be specified for the shorter one.
     * However, this methods accepts two fallback values since the user might not know (or doesn't care) which iterable is shorter.
     * <br/>
     * <p>
     * Example: <pre>{@code
    List<String> values = Arrays.asList("a", "b", "c");
    List<Integer> sequence = IntStream.range(0, values.size()) .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    Iterable<Pair<Integer, String>> sequencedValues = zip(sequence, values); // [(0, "a"), (1, "b"), (2, "c")]
     }
     * </pre>
     * </p>
     * @param firstIterable first zip source
     * @param secondIterable second zip source
     * @param firstDefaultValue fallback value in case the first iterable is shorter than the other
     * @param secondDefaultValue fallback value in case the second iterable is shorter than the other
     * @return zipped iterable
     */
    @SuppressWarnings("unchecked")
    public static <K,V> Iterable<Pair<K, V>> zip(Iterable<K> firstIterable, Iterable<V> secondIterable, K firstDefaultValue, V secondDefaultValue) {
        Iterable firstRepeatable = repeat(firstDefaultValue);
        Iterable secondRepeatable = repeat(secondDefaultValue);
        List<Iterator<?>> iterators = Arrays.asList(firstIterable.iterator(), secondIterable.iterator());
        Iterator<K> endlessFirstIterator = chain(iterators.get(0), firstRepeatable.iterator());
        Iterator<V> endlessSecondIterator = chain(iterators.get(1), secondRepeatable.iterator());

        return () -> new Iterator<Pair<K, V>>() {
            @Override
            public boolean hasNext() {
                return iterators.stream().anyMatch(Iterator::hasNext);
            }

            @Override
            public Pair<K, V> next() {
                return Pair.of(endlessFirstIterator.next(), endlessSecondIterator.next());
            }
        };
    }

    /**
     * Convenient method to use {@link CollectionUtils#zip(Iterable, Iterable)} on streams.
     * Otherwise the user had to convert the streams into iterable intermediate results.
     *
     * @see CollectionUtils#zip(Iterable, Iterable)
     * @param firstIterable first zip source
     * @param secondIterable second zip source
     * @return stream
     */
    public static <K,V> Stream<Pair<K, V>> zip(Stream<K> firstIterable, Stream<V> secondIterable) {
        return toStream(zip(firstIterable::iterator, secondIterable::iterator));
    }

    /**
     * Convenient method to use {@link CollectionUtils#zip(Iterable, Iterable, Object, Object)} on streams.
     * Otherwise the user had to convert the streams into iterable intermediate results.
     *
     * @see CollectionUtils#zip(Iterable, Iterable, Object, Object)
     * @param firstIterable first zip source
     * @param secondIterable second zip source
     * @param firstDefaultValue fallback value in case the first iterable is shorter than the other
     * @param secondDefaultValue fallback value in case the second iterable is shorter than the other
     * @return stream
     */
    public static <K,V> Stream<Pair<K, V>> zip(Stream<K> firstIterable, Stream<V> secondIterable, K firstDefaultValue, V secondDefaultValue) {
        return toStream(zip(firstIterable::iterator, secondIterable::iterator, firstDefaultValue, secondDefaultValue));
    }

    /**
     * @param value value to return on each iteration
     * @return iterable returning forever the passed in value
     */
    public static <T> Iterable<T> repeat(T value) {
        return repeat(value, -1);
    }

    /**
     * @param value value to return on each iteration
     * @param n specifies how often the value is repeated
     * @return iterable returning n times the passed in value
     */
    @SuppressWarnings("unchecked")
    public static <T> Iterable<T> repeat(T value, int n) {
        if (value == null) {
            return NULL_REPEATABLE;
        }

        return  () -> new Iterator() {
            private int count = 0;
            @Override
            public boolean hasNext() {
                return n < 0 || count < n;
            }

            @Override
            public Object next() {
                count ++;
                return value;
            }
        };
    }


    /**
     * Convenient method to chain two or more lists together.
     * @param first first list
     * @param others other lists
     * @return chained list
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <T> List<T> chain(List<T> first, List<T>... others) {
        int size = first.size();
        for (List<T> other : others) {
            size += other.size();
        }
        ArrayList<T> result = new ArrayList<>(size);
        result.addAll(first);
        for (List<T> other : others) {
            result.addAll(other);
        }
        return result;
    }

    /**
     * Convenient method to chain two sets together.
     * @param first first set
     * @param others other sets
     * @return chained set
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <T> Set<T> chain(Set<T> first, Set<T>... others) {
        HashSet<T> result = new HashSet<>(first);
        for (Set<T> other : others) {
            result.addAll(other);
        }
        return result;
    }

    /**
     * Convenient method to chain iterables together.
     * @param iterables to chain
     * @return chained iterator
     */
    public static <T> Iterator chain(Iterator<T>... iterables) {
        List<Iterator<T>>  iterableList = Arrays.asList(iterables);
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return iterableList.stream().anyMatch(Iterator::hasNext);
            }

            @Override
            public T next() {
                return iterableList.stream()
                        .filter(Iterator::hasNext)
                        .findFirst()
                        .map(Iterator::next)
                        .orElse(null);
            }
        };
    }

    /**
     * Repeats the same iterable over and over.
     * @param iterable to repeat
     * @param n specifies how often the iterable is repeated
     * @return n times repeated iterbale
     */
    public static <T> Iterable<T> cycle(Iterable<T> iterable, int n) {
        return () -> new Iterator<T>() {
            private Iterator<T> iterator = iterable.iterator();
            private int count = 0;

            @Override
            public boolean hasNext() {
                if (!iterator.hasNext()) {
                    iterator = iterable.iterator();
                    count++;
                }
                return n < 0 || count < n;
            }

            @Override
            public T next() {
                return iterator.next();
            }
        };
    }

    public static <T> List<T> toList(Iterable<T> iterable) {
        ArrayList<T> result = new ArrayList<>();
        for (T t : iterable) {
            result.add(t);
        }
        return result;
    }

    /**
     * Constructs a kind of cartesian product but without removing duplicates if any exist.
     * This can happen when the passed in collections have redundant values.
     * @param firstCollection left side of the product
     * @param secondCollection right side of the product
     * @return `cartesian` product over the two collections
     */
    public static <K,V> List<Pair<K, V>> product(Collection<K> firstCollection, Collection<V> secondCollection) {
        Iterable<K> repeatedFirstCollection = cycle(firstCollection, secondCollection.size());
        List<V> repeatedSecondCollection = secondCollection.stream()
                .flatMap(e -> toList(repeat(e, firstCollection.size())).stream())
                .collect(Collectors.toList());
        return toList(zip(repeatedFirstCollection, repeatedSecondCollection));
    }

    /**
     * Constructs a kind of cartesian product but without removing duplicates if any exist.
     * This can happen when the passed in collections have redundant values.
     * @param lists collections over which the product should be built
     * @return `cartesian` product over the two collections
     */
    public static List<List<Object>> product(List... lists) {
        return toList(new CartesianProduct(lists));
    }

    public static List<List<Object>> productWithLists(List<? extends Collection> lists) {
        return toList(new CartesianProduct(lists));
    }

    /**
     * Convenient method to remove elements from a collection.
     * @param toRemoveFrom collection to remove elements from
     * @param elementsToRemove elements to remove
     * @return new collection without the removed elements
     */
    public static <T> List<T> removeAll(List<T> toRemoveFrom, List<T> elementsToRemove) {
        ArrayList<T> subtract = new ArrayList<>(toRemoveFrom);
        subtract.removeAll(elementsToRemove);
        return subtract;
    }

    /**
     * Convenient method to remove elements from a collection.
     * @param toRemoveFrom collection to remove elements from
     * @param elementsToRemove elements to remove
     * @return new collection without the removed elements
     */
    public static <T> Set<T> removeAll(Set<T> toRemoveFrom, Set<T> elementsToRemove) {
        Set<T> subtract = new HashSet<T>(toRemoveFrom);
        subtract.removeAll(elementsToRemove);
        return subtract;
    }

    /**
     * Convenient method to print an iterable.
     * @param iterable iterable to print
     * @param out print stream
     */
    public static void println(Iterable iterable, PrintStream out) {
        for (Object o : iterable) {
            if (o == null) {
                continue;
            }
            out.println(o.toString());
        }
    }

    /**
     * Convenient method to print an iterable into System.out.
     * * @param iterable iterable to print
     * @see CollectionUtils#println(Iterable, PrintStream)
     */
    public static void println(Iterable iterable) {
        println(iterable, System.out);
    }
}
