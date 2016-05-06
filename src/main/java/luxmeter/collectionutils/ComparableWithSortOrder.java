package luxmeter.collectionutils;

import java.util.Objects;

/**
 * Class used by {@link CollectionUtils#sortedByKeys(java.util.Collection, CollectionUtils.ComposedKeyProvider)}
 * to define individual sorting orders for the key attributes.
 *
 * @see CollectionUtils#sortedByKeys(java.util.Collection, CollectionUtils.ComposedKeyProvider)
 */
public final class ComparableWithSortOrder<T> implements Comparable<ComparableWithSortOrder<T>> {
    private final Comparable<T> comparable;
    private final SortOrder sortingOrder;
    private final NullOrder nullOrder;

    private ComparableWithSortOrder(Comparable<T>comparable, SortOrder order, NullOrder nullOrder) {
        this.comparable = comparable;
        this.sortingOrder = order;
        this.nullOrder = nullOrder;
    }

    /**
     * Constructor with  name that fits in the API grammar of {@link CollectionUtils#sortedByKeys}.
     * @param comparable a comparable, e.g. int, str, tuple...
     * @param order sorting order for non-null values
     * @param nullOrder sorting order for null values
     * @return instance of this class
     */
    public static <T> ComparableWithSortOrder<T> differently(Comparable<T> comparable, SortOrder order, NullOrder nullOrder) {
        if (comparable instanceof ComparableWithSortOrder) {
            ComparableWithSortOrder<T> comparableWithSortOrder = (ComparableWithSortOrder<T>) comparable;
            SortOrder overridenSortOrder = comparableWithSortOrder.getSortOrder();
            NullOrder overridenNullOrder = comparableWithSortOrder.getNullOrder();
            if (isDefaultSortOrder(overridenSortOrder)) {
                overridenSortOrder = order;
            }
            if (isDefaultNullOrder(overridenNullOrder)) {
                overridenNullOrder = nullOrder;
            }
            return new ComparableWithSortOrder<>(comparableWithSortOrder.getComparable(), overridenSortOrder, overridenNullOrder);
        }
        return new ComparableWithSortOrder<>(comparable, order, nullOrder);
    }

    private static boolean isDefaultNullOrder(NullOrder overridenNullOrder) {
        return NullOrder.NULL_LAST == overridenNullOrder;
    }

    private static boolean isDefaultSortOrder(SortOrder overridenSortOrder) {
        return SortOrder.ASC  == overridenSortOrder;
    }

    /**
     * Constructor with  name that fits in the API grammar of {@link CollectionUtils#sortedByKeys}.
     * Default order for null values is {@link NullOrder#NULL_LAST}.
     * @param comparable a comparable, e.g. int, str, tuple...
     * @param order sorting order for non-null values
     * @return instance of this class
     */
    public static <T> ComparableWithSortOrder<T> differently(Comparable<T> comparable, SortOrder order) {
        return new ComparableWithSortOrder<>(comparable, order, NullOrder.NULL_LAST);
    }

    SortOrder getSortOrder() {
        return sortingOrder;
    }

    NullOrder getNullOrder() {
        return nullOrder;
    }

    Comparable<T> getComparable() {
        return comparable;
    }

    @Override
    public int compareTo(ComparableWithSortOrder<T> b) {
        int res = (comparable != null && b.getComparable() != null)
                ? compareNonNullables(comparable, b.getComparable(), sortingOrder)
                : compareNullables(comparable, b.getComparable(), nullOrder);
        return res;
    }

    private static <T> int compareNonNullables(Comparable<T> a, Comparable<T> b,
            SortOrder overriddenSortOrder) {
        int res = a.compareTo((T) b);
        return (overriddenSortOrder == SortOrder.ASC) ? res : (-1 * res);
    }

    private static int compareNullables(Comparable a, Comparable b, NullOrder overriddenNullOrder) {
        // 10
        if (a != null && b == null) {
            // null last by default
            return (overriddenNullOrder == NullOrder.NULL_LAST) ? -1 : 1;
        }
        // 01
        else if (a == null && b != null) {
            return (overriddenNullOrder == NullOrder.NULL_LAST) ? 1 : -1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ComparableWithSortOrder<?> that = (ComparableWithSortOrder<?>) o;
        return Objects.equals(comparable, that.comparable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(comparable);
    }
}
