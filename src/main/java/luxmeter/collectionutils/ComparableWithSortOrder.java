package luxmeter.collectionutils;

/**
 * Class used by {@link CollectionUtils#sortedByKeys(java.util.Collection, CollectionUtils.ComposedKeyProvider)}
 * to define individual sorting orders for the key attributes.
 *
 * @see CollectionUtils#sortedByKeys(java.util.Collection, CollectionUtils.ComposedKeyProvider)
 */
public final class ComparableWithSortOrder implements Comparable<Comparable> {
    private final Comparable comparable;
    private final SortOrder sortingOrder;
    private final NullOrder nullOrder;

    private ComparableWithSortOrder(Comparable comparable,
                                    SortOrder order, NullOrder nullOrder) {
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
    public static ComparableWithSortOrder differently(Comparable comparable, SortOrder order, NullOrder nullOrder) {
        return new ComparableWithSortOrder(comparable, order, nullOrder);
    }

    /**
     * Constructor with  name that fits in the API grammar of {@link CollectionUtils#sortedByKeys}.
     * Default order for null values is {@link NullOrder#NULL_LAST}.
     * @param comparable a comparable, e.g. int, str, tuple...
     * @param order sorting order for non-null values
     * @return instance of this class
     */
    public static ComparableWithSortOrder differently(Comparable comparable, SortOrder order) {
        return new ComparableWithSortOrder(comparable, order, NullOrder.NULL_LAST);
    }

    SortOrder getSortOrder() {
        return sortingOrder;
    }

    NullOrder getNullOrder() {
        return nullOrder;
    }

    Comparable getComparable() {
        return comparable;
    }

    @Override
    public int compareTo(Comparable o) {
        // never used!
        return comparable.compareTo(o);
    }
}
