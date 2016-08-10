package luxmeter.receips.elementgenerator;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

final class KeyPropertyMetadata<T, R> {
    private final String propertyName;
    private final Function<T, ?> valueExtractor;
    private final Function<Object, String> toStringMapper;
    private final Collection<R> valuesRange;
    private final boolean isCollection;

    @SuppressWarnings("unchecked")
    private boolean isNullOrEmpty(Object val) {
        if (val instanceof Collection) {
            return ((Collection) val).isEmpty() || ((Collection) val).stream().anyMatch(Objects::isNull);
        }
        return val == null;
    }

    public KeyPropertyMetadata(
            String propertyName, SingleValueExtractor<T, R> valueExtractor,
            Collection<R> valuesRange, Function<R, String> toStringMapper) {
        this.propertyName = propertyName;
        this.valueExtractor = valueExtractor;
        this.valuesRange = checkedValuesRange(valuesRange);
        this.toStringMapper = toStringMapper == null ? Object::toString : (Function<Object, String>) toStringMapper;
        this.isCollection = false;
    }

    private Collection<R> checkedValuesRange(Collection<R> valuesRange) {
        if (isNullOrEmpty(valuesRange)) {
            throw new IllegalArgumentException(String.format("%s: Range cannot be null or empty", this.propertyName));
        }
        return valuesRange;
    }

    public <C extends Collection<R>> KeyPropertyMetadata(
            String propertyName, ValuesExtractor<T, R, C> valueExtractor,
            Collection<R> valuesRange, boolean isCollection, Function<R, String> toStringMapper) {
        this.propertyName = propertyName;
        this.valueExtractor = valueExtractor;
        this.valuesRange = checkedValuesRange(valuesRange);
        this.toStringMapper = toStringMapper == null ? Object::toString : (Function<Object, String>) toStringMapper;
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

    public Function<Object, String> getToStringMapper() {
        return toStringMapper;
    }

    public boolean isCollection() {
        return isCollection;
    }
}
