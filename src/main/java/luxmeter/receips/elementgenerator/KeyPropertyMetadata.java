package luxmeter.receips.elementgenerator;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

final class KeyPropertyMetadata<T, R> {
    private final String propertyName;
    private final Function<T, ?> valueExtractor;
    private final Function<?, String> toStringMapper;
    private final Collection<R> valuesRange;
    private final boolean isCollection;


    private Function<T, Object> nonNullValueExtractor(String propertyName, Function<T, ?> valueExtractor) {
        return concreteElement -> {
            Object val = valueExtractor.apply(concreteElement);
            if (isNullOrEmpty(val)) {
                throw new IllegalArgumentException(
                        String.format("Error while extracting values from %s: " +
                                        "It is forbidden for a key property like '%s' " +
                                        "to return null or an empty collection (or collection with null elements).",
                                concreteElement.toString(), propertyName));
            }
            return val;
        };
    }

    @SuppressWarnings("unchecked")
    private boolean isNullOrEmpty(Object val) {
        if (val instanceof Collection) {
            return ((Collection) val).isEmpty() || ((Collection) val).stream().anyMatch(Objects::isNull);
        }
        return val == null;
    }


    public KeyPropertyMetadata(
            String propertyName, SingleValueExtractor<T, R> valueExtractor, Collection<R> valuesRange) {
        this.propertyName = propertyName;
        this.valueExtractor = nonNullValueExtractor(propertyName, valueExtractor);
        this.valuesRange = valuesRange;
        this.toStringMapper = Object::toString;
        this.isCollection = false;
    }

    public <C extends Collection<R>> KeyPropertyMetadata(
            String propertyName, ValuesExtractor<T, R, C> valueExtractor,
            Collection<R> valuesRange, boolean isCollection) {
        this.propertyName = propertyName;
        this.valueExtractor = nonNullValueExtractor(propertyName, valueExtractor);
        this.valuesRange = valuesRange;
        this.toStringMapper = Object::toString;
        this.isCollection = isCollection;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Function<T, ?> getValueExtractor() {
        return valueExtractor;
    }

    public Collection<R> getValuesRange() {
        return valuesRange;
    }

    public Function<?, String> getToStringMapper() {
        return toStringMapper;
    }

    public boolean isCollection() {
        return isCollection;
    }
}
