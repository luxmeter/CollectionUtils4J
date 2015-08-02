package luxmeter.collectionutils;

/**
 * Enum specifying the sorting order of null values used by
 * {@link CollectionUtils#sortedByKeys(java.util.Collection,
        CollectionUtils.ComposedKeyProvider, SortOrder, NullOrder)}
 */
public enum NullOrder {
    NULL_FIRST,
    NULL_LAST
}
