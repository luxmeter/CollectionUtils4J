package luxmeter.collectionutils;

import java.util.*;
import java.util.function.Function;

import static luxmeter.collectionutils.ComparableWithSortOrder.differently;

/**
 * Util class defining convenient sort methods applicable for collections.
 */
public class CollectionSortUtils {

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
     * @see #sortedByKey(java.util.Collection, luxmeter.collectionutils.CollectionSortUtils.SingleKeyProvider, SortOrder)
     * @see #sortedByKey(java.util.Collection, luxmeter.collectionutils.CollectionSortUtils.SingleKeyProvider, SortOrder, NullOrder)
     * @return new sorted collection
     */
    public static <T> List<T> sortedByKey(Collection<T> collection, SingleKeyProvider<T> keyProvider) {
        ComposedKeyProvider<T> wrapInComposedKeyProvider = t -> new Comparable[]{keyProvider.apply(t)};
        return sortedByKeys(collection, wrapInComposedKeyProvider, SortOrder.ASC, NullOrder.NULL_LAST);
    }

    /**
     * In addition to {@link #sortedByKey(java.util.Collection, luxmeter.collectionutils.CollectionSortUtils.SingleKeyProvider)}
     * you can specify here the default sorting order for non-null values.
     * @param collection collection to sort
     * @param keyProvider mapping function to generate the key for an element of the collection
     * @param <T> tpye of the elements within the collection
     * @param sortingOrder the sorting order for non-null values (ascending or descending)
     * @see #sortedByKey(java.util.Collection, luxmeter.collectionutils.CollectionSortUtils.SingleKeyProvider)
     * @see #sortedByKey(java.util.Collection, luxmeter.collectionutils.CollectionSortUtils.SingleKeyProvider, SortOrder, NullOrder)
     * @return new sorted collection
     */
    public static <T> List<T> sortedByKey(Collection<T> collection,
                                          SingleKeyProvider<T> keyProvider, SortOrder sortingOrder) {
        ComposedKeyProvider<T> wrapInComposedKeyProvider = t -> new Comparable[]{keyProvider.apply(t)};
        return sortedByKeys(collection, wrapInComposedKeyProvider, sortingOrder, NullOrder.NULL_LAST);
    }

    /**
     * In addition to {@link #sortedByKeys(java.util.Collection, luxmeter.collectionutils.CollectionSortUtils.ComposedKeyProvider)}
     * you can specify here the default sorting order for non-null values as well as null values.
     *
     * @param collection collection to sort
     * @param keyProvider mapping function to generate the key for an element of the collection
     * @param <T> tpye of the elements within the collection
     * @param sortingOrder the sorting order for non-null values (ascending or descending)
     * @param nullOrder the sorting order for null values(first or last)
     * @see #sortedByKeys(java.util.Collection, luxmeter.collectionutils.CollectionSortUtils.ComposedKeyProvider)
     * @see #sortedByKeys(java.util.Collection, luxmeter.collectionutils.CollectionSortUtils.ComposedKeyProvider, SortOrder)
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
     * @see #sortedByKeys(java.util.Collection, luxmeter.collectionutils.CollectionSortUtils.ComposedKeyProvider, SortOrder)
     * @see #sortedByKeys(java.util.Collection, luxmeter.collectionutils.CollectionSortUtils.ComposedKeyProvider, SortOrder, NullOrder)
     * @return new sorted collection
     */
    public static <T> List<T> sortedByKeys(Collection<T> collection, ComposedKeyProvider<T> keyProvider) {
        return sortedByKeys(collection, keyProvider, SortOrder.ASC, NullOrder.NULL_LAST);
    }

    /**
     * In addition to {@link #sortedByKeys(java.util.Collection, luxmeter.collectionutils.CollectionSortUtils.ComposedKeyProvider)}
     * you can specify here the default sorting order for non-null values.
     * @param collection collection to sort
     * @param keyProvider mapping function to generate the key for an element of the collection
     * @param <T> tpye of the elements within the collection
     * @param sortingOrder the sorting order for non-null values (ascending or descending)
     * @see #sortedByKeys(java.util.Collection, luxmeter.collectionutils.CollectionSortUtils.ComposedKeyProvider)
     * @see #sortedByKeys(java.util.Collection, luxmeter.collectionutils.CollectionSortUtils.ComposedKeyProvider, SortOrder, NullOrder)
     * @return new sorted collection
     */
    public static <T> List<T> sortedByKeys(Collection<T> collection,
                                           ComposedKeyProvider<T> keyProvider, SortOrder sortingOrder) {
        return sortedByKeys(collection, keyProvider, sortingOrder, NullOrder.NULL_LAST);
    }

    /**
     * In addition to {@link #sortedByKeys(java.util.Collection, luxmeter.collectionutils.CollectionSortUtils.ComposedKeyProvider)}
     * you can specify here the default sorting order for non-null values as well as null values.
     *
     * @param collection collection to sort
     * @param keyProvider mapping function to generate the key for an element of the collection
     * @param <T> tpye of the elements within the collection
     * @param sortingOrder the sorting order for non-null values (ascending or descending)
     * @param nullOrder the sorting order for null values(first or last)
     * @see #sortedByKeys(java.util.Collection, luxmeter.collectionutils.CollectionSortUtils.ComposedKeyProvider)
     * @see #sortedByKeys(java.util.Collection, luxmeter.collectionutils.CollectionSortUtils.ComposedKeyProvider, SortOrder)
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
     * {@link #sortedByKeys(java.util.Collection, luxmeter.collectionutils.CollectionSortUtils.ComposedKeyProvider)} more readable.
     * @param elements keys the collection in sorted is sorted by
     * @return array of Comparables
     */
    public static Comparable[] tuple(Comparable...elements) {
        return elements;
    }
}
