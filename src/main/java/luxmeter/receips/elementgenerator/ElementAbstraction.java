package luxmeter.receips.elementgenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ElementAbstraction {
    private final Object sourceElement;
    private final Map<String, ?> keyPropertyValues;
    private List<String> keyPropertyValuesAsString;
    private Map<String, Function<Object, String>> toStringMapper = new HashMap<>();

    public ElementAbstraction(Object sourceElement, Map<String, ?> keyPropertyValues) {
        this.sourceElement = sourceElement;
        this.keyPropertyValues = keyPropertyValues;
    }

    ElementAbstraction(Object sourceElement, Map<String, ?> keyPropertyValues, Map<String, Function<Object, String>> toStringMapper) {
        this(sourceElement, keyPropertyValues);
        setToStringMapper(toStringMapper);
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

    /**
     * Custom toString mappers for each property injected by the ElementGeneratorBuilder.
     * @param toStringMapper Custom toString mappers for each property
     */
    void setToStringMapper(Map<String, Function<Object, String>> toStringMapper) {
        this.toStringMapper = toStringMapper;
        keyPropertyValuesAsString = keyPropertyValues.entrySet().stream()
                .map(entry -> toStringMapper.getOrDefault(entry.getKey(), Objects::toString).apply(entry.getValue()))
                .collect(Collectors.toList());
    }
}
