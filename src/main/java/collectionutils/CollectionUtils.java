package collectionutils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Util class defining convenient methods to operate on collections.
 */
public final class CollectionUtils {
    @FunctionalInterface
    public interface ComposedKeyProvider<T> extends Function<T, Comparable[]> {
    }
    @FunctionalInterface
    public interface SingleKeyProvider<T> extends Function<T, Comparable> {
    }
    
    public enum SortOrder {
        ASC,
        DESC
    }
    
    /**
     * Enum specifying the sorting order of null values used by
     * {@link #sortedByKeys(java.util.Collection, collectionutils.CollectionUtils.ComposedKeyProvider,
     collectionutils.CollectionUtils.SortOrder,
     collectionutils.CollectionUtils.NullOrder)}
     */
    public enum NullOrder {
        NULL_FIRST,
        NULL_LAST
    }

    /**
     * Class used by {@link #sortedByKeys(java.util.Collection, collectionutils.CollectionUtils.ComposedKeyProvider)}
     * to define individual sorting orders for the key attributes.
     *
     * @see #sortedByKeys(java.util.Collection, collectionutils.CollectionUtils.ComposedKeyProvider)
     */
    public static class ComparableWithSortOrder implements Comparable<Comparable> {
        private final Comparable comparable;
        private final SortOrder sortingOrder;
        private final NullOrder nullOrder;

        private ComparableWithSortOrder(Comparable comparable,
                SortOrder order, NullOrder nullOrder) {
            this.comparable = comparable;
            this.sortingOrder = order;
            this.nullOrder = nullOrder;
        }

        public static ComparableWithSortOrder of(Comparable comparable, SortOrder order, NullOrder nullOrder) {
            return new ComparableWithSortOrder(comparable, order, nullOrder);
        }

        public static ComparableWithSortOrder of(Comparable comparable, SortOrder order) {
            return new ComparableWithSortOrder(comparable, order, NullOrder.NULL_LAST);
        }

        public static ComparableWithSortOrder of(Comparable comparable) {
            return new ComparableWithSortOrder(comparable, SortOrder.ASC, NullOrder.NULL_LAST);
        }

        public SortOrder getSortOrder() {
            return sortingOrder;
        }

        public NullOrder getNullOrder() {
            return nullOrder;
        }

        public Comparable getComparable() {
            return comparable;
        }

        @Override
        public int compareTo(Comparable o) {
            // never used!
            return comparable.compareTo(o);
        }
    }

    /**
     * Returns a sorted collection by a user provided key generator.
     * The key generator is just a function that maps an element of the collection to a comparable.
     * The sorting order is by default ascending. Null values come at last. The sorting algorithm is guaranteed to be stable.<br/>
     *
     * <ul>
     * <li>Simple example:<br/>
     * <code>sortedByName = sortedByKey(students, student -> student.getLastName()); </code><br/><br/>
     * </li>
     * <li>
     * Simple example with user defined sorting order:<br/>
     * <code>sortedByName = sortedByKey(students, student -> student.getLastName(), DESC, NULL_FIRST); </code>
     * </li>
     * </ul>
     *
     * @param collection collection to sort
     * @param keyProvider mapping function to generate the key for an element of the collection
     * @param <T> tpye of the elements within the collection
     * @see #sortedByKey(java.util.Collection, collectionutils.CollectionUtils.SingleKeyProvider, SortOrder)
     * @see #sortedByKey(java.util.Collection, collectionutils.CollectionUtils.SingleKeyProvider, SortOrder, collectionutils.CollectionUtils.NullOrder)
     * @return new sorted collection
     */
    public static <T> List<T> sortedByKey(Collection<T> collection, SingleKeyProvider<T> keyProvider) {
        ComposedKeyProvider<T> wrapper = wrapInComposedKeyProvider(keyProvider);
        return sortedByKeys(collection, wrapper, SortOrder.ASC, NullOrder.NULL_LAST);
    }

    /**
     * In addition to {@link #sortedByKey(java.util.Collection, collectionutils.CollectionUtils.SingleKeyProvider)}
     * you can specify here the default sorting order for non-null values.
     * @param collection collection to sort
     * @param keyProvider mapping function to generate the key for an element of the collection
     * @param <T> tpye of the elements within the collection
     * @param sortingOrder the sorting order for non-null values (ascending or descending)
     * @see #sortedByKey(java.util.Collection, collectionutils.CollectionUtils.SingleKeyProvider)
     * @see #sortedByKey(java.util.Collection, collectionutils.CollectionUtils.SingleKeyProvider, SortOrder, collectionutils.CollectionUtils.NullOrder)
     * @return new sorted collection
     */
    public static <T> List<T> sortedByKey(Collection<T> collection,
            SingleKeyProvider<T> keyProvider, SortOrder sortingOrder) {
        ComposedKeyProvider<T> wrapper = wrapInComposedKeyProvider(keyProvider);
        return sortedByKeys(collection, wrapper, sortingOrder, NullOrder.NULL_LAST);
    }

    /**
     * In addition to {@link #sortedByKeys(java.util.Collection, collectionutils.CollectionUtils.ComposedKeyProvider)}
     * you can specify here the default sorting order for non-null values as well as null values.
     *
     * @param collection collection to sort
     * @param keyProvider mapping function to generate the key for an element of the collection
     * @param <T> tpye of the elements within the collection
     * @param sortingOrder the sorting order for non-null values (ascending or descending)
     * @param nullOrder the sorting order for null values(first or last)
     * @see #sortedByKeys(java.util.Collection, collectionutils.CollectionUtils.ComposedKeyProvider)
     * @see #sortedByKeys(java.util.Collection, collectionutils.CollectionUtils.ComposedKeyProvider, SortOrder)
     * @return new sorted collection
     */
    public static <T> List<T> sortedByKey(Collection<T> collection,
            SingleKeyProvider<T> keyProvider, SortOrder sortingOrder, NullOrder nullOrder) {
        ComposedKeyProvider<T> wrapper = wrapInComposedKeyProvider(keyProvider);
        return sortedByKeys(collection, wrapper, sortingOrder, nullOrder);
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
     * <li>Simple example:<br/>
     * <code>sortedByName = sortedByKeys(students, student -> tuple(student.getLastName(), student.getFirstName()); </code><br/><br/>
     * </li>
     * <li>
     * Complex example with individual sorting order:<br/>
     * <code>sortedByName = sortedByKeys(students, student -> tuple(student.getLastName(), ComparableWithSortOrder.of(student.getFirstName(), DESC)); </code>
     * </li>
     * </ul>
     *
     * @param collection collection to sort
     * @param keyProvider mapping function to generate the key for an element of the collection
     * @param <T> tpye of the elements within the collection
     * @see #sortedByKeys(java.util.Collection, collectionutils.CollectionUtils.ComposedKeyProvider, SortOrder)
     * @see #sortedByKeys(java.util.Collection, collectionutils.CollectionUtils.ComposedKeyProvider, SortOrder, collectionutils.CollectionUtils.NullOrder)
     * @return new sorted collection
     */
    public static <T> List<T> sortedByKeys(Collection<T> collection, ComposedKeyProvider<T> keyProvider) {
        return sortedByKeys(collection, keyProvider, SortOrder.ASC, NullOrder.NULL_LAST);
    }

    /**
     * In addition to {@link #sortedByKeys(java.util.Collection, collectionutils.CollectionUtils.ComposedKeyProvider)}
     * you can specify here the default sorting order for non-null values.
     * @param collection collection to sort
     * @param keyProvider mapping function to generate the key for an element of the collection
     * @param <T> tpye of the elements within the collection
     * @param sortingOrder the sorting order for non-null values (ascending or descending)
     * @see #sortedByKeys(java.util.Collection, collectionutils.CollectionUtils.ComposedKeyProvider)
     * @see #sortedByKeys(java.util.Collection, collectionutils.CollectionUtils.ComposedKeyProvider, SortOrder, collectionutils.CollectionUtils.NullOrder)
     * @return new sorted collection
     */
    public static <T> List<T> sortedByKeys(Collection<T> collection,
            ComposedKeyProvider<T> keyProvider, SortOrder sortingOrder) {
        return sortedByKeys(collection, keyProvider, sortingOrder, NullOrder.NULL_LAST);
    }

    /**
     * In addition to {@link #sortedByKeys(java.util.Collection, collectionutils.CollectionUtils.ComposedKeyProvider)}
     * you can specify here the default sorting order for non-null values as well as null values.
     *
     * @param collection collection to sort
     * @param keyProvider mapping function to generate the key for an element of the collection
     * @param <T> tpye of the elements within the collection
     * @param sortingOrder the sorting order for non-null values (ascending or descending)
     * @param nullOrder the sorting order for null values(first or last)
     * @see #sortedByKeys(java.util.Collection, collectionutils.CollectionUtils.ComposedKeyProvider)
     * @see #sortedByKeys(java.util.Collection, collectionutils.CollectionUtils.ComposedKeyProvider, SortOrder)
     * @return new sorted collection
     */
    public static <T> List<T> sortedByKeys(Collection<T> collection,
            ComposedKeyProvider<T> keyProvider, SortOrder sortingOrder, NullOrder nullOrder) {
        if (collection == null) {
            return new ArrayList<>();
        }

        // defensive copy
        List<T> copy = new ArrayList<>(collection);

        // is guaranteed to be stable
        Collections.sort(copy, (e1, e2) -> {
            Comparable[] keyA = keyProvider.apply(e1);
            Comparable[] keyB = keyProvider.apply(e2);
            for (int i = 0; i < keyA.length; i++) {
                Comparable a = keyA[i];
                Comparable b = keyB[i];

                SortOrder overriddenSortOrder = sortingOrder;
                NullOrder overriddenNullOrder = nullOrder;

                if(a instanceof ComparableWithSortOrder) {
                    overriddenSortOrder = ((ComparableWithSortOrder) a).getSortOrder();
                    overriddenNullOrder = ((ComparableWithSortOrder) a).getNullOrder();

                    a = ((ComparableWithSortOrder) a).getComparable();
                    b = ((ComparableWithSortOrder) b).getComparable();
                }

                // 11
                if (a != null && b != null) {
                    int res = a.compareTo(b);
                    if (res != 0) {
                        return (overriddenSortOrder == SortOrder.ASC) ? res : (-1 * res);
                    }
                }
                // 10
                else if (a != null && b == null) {
                    // null last by default
                    return (overriddenNullOrder == NullOrder.NULL_LAST) ? -1 : 1;
                }
                // 01
                else if (a == null && b != null) {
                    return (overriddenNullOrder == NullOrder.NULL_LAST) ? 1 : -1;
                }
            }
            return 0;
        });

        return copy;
    }

    /**
     * Convenient method to make the use of
     * {@link #sortedByKeys(java.util.Collection, collectionutils.CollectionUtils.ComposedKeyProvider)} more readable.
     * @param elements keys the collection in sorted is sorted by
     * @return array of Comparables
     */
    public static Comparable[] tuple(Comparable...elements) {
        return elements;
    }

    private static <T> ComposedKeyProvider<T> wrapInComposedKeyProvider(
            final SingleKeyProvider<T> keyProvider) {
        return new ComposedKeyProvider<T>() {
            @Override
            public Comparable[] apply(T t) {
                return new Comparable[]{keyProvider.apply(t)};
            }
        };
    }
}
