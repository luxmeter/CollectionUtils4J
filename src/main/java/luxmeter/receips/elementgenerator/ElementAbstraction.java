package luxmeter.receips.elementgenerator;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Abstraction of a concrete element. Used internally to identify the missing and existing elements.
 * The user can access the properties via the key property names.
 *
 * @see ElementGeneratorBuilder#withSingleValueProperty(String, Collection, SingleValueExtractor)
 * @see ElementGeneratorBuilder#withCollectionProperty(String, Collection, ValuesExtractor)
 * @see ElementGeneratorBuilder#withElementFactory(Function)
 */
public final class ElementAbstraction {
    private final Object sourceElement;
    private final Map<String, ?> keyPropertyValues;
    private List<String> keyPropertyValuesAsString;

    ElementAbstraction(Object sourceElement, Map<String, Object> keyPropertyValues,
                       Map<String, Function<Object, String>> toStringMapper) {
        this.sourceElement = sourceElement;
        this.keyPropertyValues = keyPropertyValues;
        List<Map.Entry<String, Object>> nullOrEmptyValues = keyPropertyValues.entrySet().stream().filter(e -> (e.getValue() instanceof Collection && ((Collection) e.getValue()).isEmpty()) || e.getValue() == null).collect(Collectors.toList());
        if (!nullOrEmptyValues.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("It is forbidden for key properties to return null or an empty collections (or collection with null elements): %s",
                            nullOrEmptyValues));
        }
        keyPropertyValuesAsString = keyPropertyValues.entrySet().stream()
                .map(entry -> toStringMapper.getOrDefault(entry.getKey(), Objects::toString).apply(entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElementAbstraction elementAbstraction = (ElementAbstraction) o;
        return keyPropertyValuesAsString.size() == elementAbstraction.keyPropertyValuesAsString.size()
                && keyPropertyValuesAsString.containsAll(elementAbstraction.keyPropertyValuesAsString);
    }

    /**
     * @return true if this abstraction is based on an existing element
     */
    public boolean isGenerated() {
        return sourceElement == null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyPropertyValuesAsString);
    }

    /**
     * @param propertyName name of the key property
     * @param <R> return type (for convenience)
     * @return non-null value
     */
    public <R> R get(String propertyName) {
        return (R) keyPropertyValues.get(propertyName);
    }

    /**
     * @return map of the properties this abstraction consists of
     */
    public Map<String, Object> getProperties() {
        return new HashMap<>(keyPropertyValues);
    }

    /**
     * @return string representation of this abstraction
     */
    @Override
    public String toString() {
        List<String> mapAsString = keyPropertyValues.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.toList());
        String repr = mapAsString.stream().collect(Collectors.joining(",", "<ElementAbstraction{", "}>"));
        return repr;
    }
}
