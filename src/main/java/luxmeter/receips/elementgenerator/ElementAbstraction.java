package luxmeter.receips.elementgenerator;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public boolean isGenerated() {
        return sourceElement == null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyPropertyValuesAsString);
    }

    public <R> R get(String propertyName) {
        return (R) keyPropertyValues.get(propertyName);
    }

    public Map<String, Object> getProperties() {
        return new HashMap<>(keyPropertyValues);
    }

    @Override
    public String toString() {
        List<String> mapAsString = keyPropertyValues.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.toList());
        String repr = mapAsString.stream().collect(Collectors.joining(",", "<ElementAbstraction{", "}>"));
        return repr;
    }
}
