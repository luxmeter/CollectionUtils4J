package luxmeter.collectionutils;

import org.apache.commons.lang3.tuple.Pair;

import java.io.PrintStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static luxmeter.collectionutils.ComparableWithSortOrder.differently;

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

    @FunctionalInterface
    public interface ComposedKeyProvider<T> extends Function<T, Comparable[]> {
    }
    @FunctionalInterface
    public interface SingleKeyProvider<T> extends Function<T, Comparable> {
    }

    /**
     * Returns a sorted collection by a user provided key generator.
     * The key generator is just a function that maps an element of the collection to a comparable.
     * The sorting order is by default ascending. Null values come at last. The sorting algorithm is guaranteed to be stable.<br/>
     *
     * <ul>
     * <li>Simple example:
     * <pre>{@code sortedByName = sortedByKey(students, student -> student.getLastName()); }</pre>
     * </li>
     * <li>
     * Simple example with user defined sorting order:
     * <pre>{@code sortedByName = sortedByKey(students, student -> student.getLastName(), DESC, NULL_FIRST); }</pre>
     * </li>
     * </ul>
     *
     * @param collection collection to sort
     * @param keyProvider mapping function to generate the key for an element of the collection
     * @param <T> tpye of the elements within the collection
     * @see #sortedByKey(java.util.Collection, luxmeter.collectionutils.CollectionUtils.SingleKeyProvider, SortOrder)
     * @see #sortedByKey(java.util.Collection, luxmeter.collectionutils.CollectionUtils.SingleKeyProvider, SortOrder, NullOrder)
     * @return new sorted collection
     */
    public static <T> List<T> sortedByKey(Collection<T> collection, SingleKeyProvider<T> keyProvider) {
        ComposedKeyProvider<T> wrapInComposedKeyProvider = t -> new Comparable[]{keyProvider.apply(t)};
        return sortedByKeys(collection, wrapInComposedKeyProvider, SortOrder.ASC, NullOrder.NULL_LAST);
    }

    /**
     * In addition to {@link #sortedByKey(java.util.Collection, luxmeter.collectionutils.CollectionUtils.SingleKeyProvider)}
     * you can specify here the default sorting order for non-null values.
     * @param collection collection to sort
     * @param keyProvider mapping function to generate the key for an element of the collection
     * @param <T> tpye of the elements within the collection
     * @param sortingOrder the sorting order for non-null values (ascending or descending)
     * @see #sortedByKey(java.util.Collection, luxmeter.collectionutils.CollectionUtils.SingleKeyProvider)
     * @see #sortedByKey(java.util.Collection, luxmeter.collectionutils.CollectionUtils.SingleKeyProvider, SortOrder, NullOrder)
     * @return new sorted collection
     */
    public static <T> List<T> sortedByKey(Collection<T> collection,
            SingleKeyProvider<T> keyProvider, SortOrder sortingOrder) {
        ComposedKeyProvider<T> wrapInComposedKeyProvider = t -> new Comparable[]{keyProvider.apply(t)};
        return sortedByKeys(collection, wrapInComposedKeyProvider, sortingOrder, NullOrder.NULL_LAST);
    }

    /**
     * In addition to {@link #sortedByKeys(java.util.Collection, luxmeter.collectionutils.CollectionUtils.ComposedKeyProvider)}
     * you can specify here the default sorting order for non-null values as well as null values.
     *
     * @param collection collection to sort
     * @param keyProvider mapping function to generate the key for an element of the collection
     * @param <T> tpye of the elements within the collection
     * @param sortingOrder the sorting order for non-null values (ascending or descending)
     * @param nullOrder the sorting order for null values(first or last)
     * @see #sortedByKeys(java.util.Collection, luxmeter.collectionutils.CollectionUtils.ComposedKeyProvider)
     * @see #sortedByKeys(java.util.Collection, luxmeter.collectionutils.CollectionUtils.ComposedKeyProvider, SortOrder)
     * @return new sorted collection
     */
    public static <T> List<T> sortedByKey(Collection<T> collection,
            SingleKeyProvider<T> keyProvider, SortOrder sortingOrder, NullOrder nullOrder) {
        ComposedKeyProvider<T> wrapInComposedKeyProvider = t -> new Comparable[]{keyProvider.apply(t)};
        return sortedByKeys(collection, wrapInComposedKeyProvider, sortingOrder, nullOrder);
    }

    /**
     * Returns a sorted collection by a user provided key generator.
     * The key generator is just a function that maps an element of the collection to an array of Comparables.
     * For the sake of readability you can use the {@link #tuple(Comparable[])}  method to let you generate the array (see example).
     * The sorting order is by default ascending. Null values come at last. The sorting algorithm is guaranteed to be stable.<br/>
     *
     * If you want to specify individual sorting orders for the key attributes, use the {@link ComparableWithSortOrder}.
     * Notice that you don't need to use ComparableWithSortOrder for  all attributes if you just want to sort an attribute individually (see example).
     * If no ComparableWithSortOrder is given, the default sorting order for non-null and null values is used.<br/><br/>
     * <ul>
     * <li>Simple example:
     * <pre>{@code sortedByName = sortedByKeys(students, student -> tuple(student.getLastName(), student.getFirstName()); }</pre>
     * </li>
     * <li>
     * Complex example with individual sorting order:
     * <pre>{@code sortedByName = sortedByKeys(students, student -> tuple(student.getLastName(), differently(student.getFirstName(), DESC)); }</pre>
     * </li>
     * </ul>
     *
     * @param collection collection to sort
     * @param keyProvider mapping function to generate the key for an element of the collection
     * @param <T> tpye of the elements within the collection
     * @see #sortedByKeys(java.util.Collection, luxmeter.collectionutils.CollectionUtils.ComposedKeyProvider, SortOrder)
     * @see #sortedByKeys(java.util.Collection, luxmeter.collectionutils.CollectionUtils.ComposedKeyProvider, SortOrder, NullOrder)
     * @return new sorted collection
     */
    public static <T> List<T> sortedByKeys(Collection<T> collection, ComposedKeyProvider<T> keyProvider) {
        return sortedByKeys(collection, keyProvider, SortOrder.ASC, NullOrder.NULL_LAST);
    }

    /**
     * In addition to {@link #sortedByKeys(java.util.Collection, luxmeter.collectionutils.CollectionUtils.ComposedKeyProvider)}
     * you can specify here the default sorting order for non-null values.
     * @param collection collection to sort
     * @param keyProvider mapping function to generate the key for an element of the collection
     * @param <T> tpye of the elements within the collection
     * @param sortingOrder the sorting order for non-null values (ascending or descending)
     * @see #sortedByKeys(java.util.Collection, luxmeter.collectionutils.CollectionUtils.ComposedKeyProvider)
     * @see #sortedByKeys(java.util.Collection, luxmeter.collectionutils.CollectionUtils.ComposedKeyProvider, SortOrder, NullOrder)
     * @return new sorted collection
     */
    public static <T> List<T> sortedByKeys(Collection<T> collection,
            ComposedKeyProvider<T> keyProvider, SortOrder sortingOrder) {
        return sortedByKeys(collection, keyProvider, sortingOrder, NullOrder.NULL_LAST);
    }

    /**
     * In addition to {@link #sortedByKeys(java.util.Collection, luxmeter.collectionutils.CollectionUtils.ComposedKeyProvider)}
     * you can specify here the default sorting order for non-null values as well as null values.
     *
     * @param collection collection to sort
     * @param keyProvider mapping function to generate the key for an element of the collection
     * @param <T> tpye of the elements within the collection
     * @param sortingOrder the sorting order for non-null values (ascending or descending)
     * @param nullOrder the sorting order for null values(first or last)
     * @see #sortedByKeys(java.util.Collection, luxmeter.collectionutils.CollectionUtils.ComposedKeyProvider)
     * @see #sortedByKeys(java.util.Collection, luxmeter.collectionutils.CollectionUtils.ComposedKeyProvider, SortOrder)
     * @return new sorted collection
     */
    public static <T> List<T> sortedByKeys(Collection<T> collection,
            ComposedKeyProvider<T> keyProvider, SortOrder sortingOrder, NullOrder nullOrder) {
        if (collection == null) {
            return new LinkedList<>();
        }

        // defensive copy
        List<T> copy = new ArrayList<>(collection);

        // is guaranteed to be stable
        Collections.sort(copy, byKey(keyProvider, sortingOrder, nullOrder));

        return copy;
    }

    @SuppressWarnings("unchecked")
    public static <T> Comparator<T> byKey(ComposedKeyProvider<T> keyProvider, SortOrder sortingOrder, NullOrder nullOrder) {
        return (e1, e2) -> {
            Comparable[] keyA = keyProvider.apply(e1);
            Comparable[] keyB = keyProvider.apply(e2);
            for (int i = 0; i < keyA.length; i++) {
                ComparableWithSortOrder a = differently(keyA[i], sortingOrder, nullOrder);
                ComparableWithSortOrder b = differently(keyB[i], sortingOrder, nullOrder);
                int res = a.compareTo(b);
                if (res != 0) {
                    return res;
                }
            }
            return 0;
        };
    }

    /**
     * Convenient method to make the use of
     * {@link #sortedByKeys(java.util.Collection, luxmeter.collectionutils.CollectionUtils.ComposedKeyProvider)} more readable.
     * @param elements keys the collection in sorted is sorted by
     * @return array of Comparables
     */
    public static Comparable[] tuple(Comparable...elements) {
        return elements;
    }

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
    public static <T> Iterable<T> append(Iterable<T>... iterables) {
        return () -> {
            Iterator[] iterators = Arrays.asList(iterables).stream().map(Iterable::iterator).toArray(Iterator[]::new);
            return append(iterators);
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
