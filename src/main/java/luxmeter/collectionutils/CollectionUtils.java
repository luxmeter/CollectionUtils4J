package luxmeter.collectionutils;

import org.apache.commons.lang3.tuple.Pair;

import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

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

    @SuppressWarnings("unchecked")
    public static <K,V> Iterable<Pair<K, V>> zip(Iterable<K> firstIterable, Iterable<V> secondIterable) {
        return zip(firstIterable, secondIterable, null, null);
    }

    @SuppressWarnings("unchecked")
    public static <K,V> Iterable<Pair<K, V>> zip(Iterable<K> firstIterable, Iterable<V> secondIterable, K firstDefaultValue, V secondDefaultValue) {
        Iterable firstRepeatable = repeat(firstDefaultValue);
        Iterable secondRepeatable = repeat(secondDefaultValue);
        List<Iterator<?>> iterators = Arrays.asList(firstIterable.iterator(), secondIterable.iterator());
        Iterator<K> endlessFirstIterator = append(iterators.get(0), firstRepeatable.iterator());
        Iterator<V> endlessSecondIterator = append(iterators.get(1), secondRepeatable.iterator());

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

    public static <T> Iterable<T> repeat(T value) {
        return repeat(value, -1);
    }

    @SuppressWarnings("unchecked")
    public static <T> Iterable<T> repeat(T value, int times) {
        if (value == null) {
            return NULL_REPEATABLE;
        }

        return  () -> new Iterator() {
            private int count = 0;
            @Override
            public boolean hasNext() {
                return times < 0 || count < times;
            }

            @Override
            public Object next() {
                count ++;
                return value;
            }
        };
    }

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

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <T> Set<T> chain(Set<T> first, Set<T>... others) {
        HashSet<T> result = new HashSet<>(first);
        for (Set<T> other : others) {
            result.addAll(other);
        }
        return result;
    }

    public static <T> Iterator append(Iterator<T>... iterables) {
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

    public static <T> Iterable<T> cycle(Iterable<T> iterable, int times) {
        return () -> new Iterator<T>() {
            private Iterator<T> iterator = iterable.iterator();
            private int count = 0;

            @Override
            public boolean hasNext() {
                if (!iterator.hasNext()) {
                    iterator = iterable.iterator();
                    count++;
                }
                return times < 0 || count < times;
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

    public static <K,V> List<Pair<K, V>> product(Collection<K> firstCollection, Collection<V> secondCollection) {
        Iterable<K> repeatedFirstCollection = cycle(firstCollection, secondCollection.size());
        List<V> repeatedSecondCollection = secondCollection.stream()
                .flatMap(e -> toList(repeat(e, firstCollection.size())).stream())
                .collect(Collectors.toList());
        return toList(zip(repeatedFirstCollection, repeatedSecondCollection));
    }

    public static List<List<Object>> product(List... lists) {
        return toList(new CartesianProduct(lists));
    }

    public static List<List<Object>> productWithLists(List<? extends Collection> lists) {
        return toList(new CartesianProduct(lists));
    }

    public static <T> List<T> removeAll(List<T> toRemoveFrom, List<T> elementsToRemove) {
        ArrayList<T> subtract = new ArrayList<>(toRemoveFrom);
        subtract.removeAll(elementsToRemove);
        return subtract;
    }

    public static <T> Set<T> removeAll(Set<T> toRemoveFrom, Set<T> elementsToRemove) {
        Set<T> subtract = new HashSet<T>(toRemoveFrom);
        subtract.removeAll(elementsToRemove);
        return subtract;
    }

    public static void println(Iterable iterable, PrintStream out) {
        for (Object o : iterable) {
            if (o == null) {
                continue;
            }
            out.println(o.toString());
        }
    }

    public static void println(Iterable iterable) {
        println(iterable, System.out);
    }
}
