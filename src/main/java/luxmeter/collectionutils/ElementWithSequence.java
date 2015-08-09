package luxmeter.collectionutils;

import java.util.Collection;

/**
 * Used by {@link CollectionUtils#enumerate(Collection, int)}
 * to sequence the elements of a collection.
 */
public final class ElementWithSequence<T> {
	private final int sequence;
	private final T element;

	public ElementWithSequence(int sequence, T element) {
		this.sequence = sequence;
		this.element = element;
	}

    /**
     * @return sequence of the element within the collection
     */
	public int getSequence() {
		return sequence;
	}

    /**
     * @return the element this sequence was applied to
     */
	public T getElement() {
		return element;
	}
}
