package luxmeter.receips.elementgenerator;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ElementAbstraction {
    private final Object sourceElement;
    private final Map<String, ?> keyPropertyValues;
    private final List<String> keyPropertyValuesAsString;

    public ElementAbstraction(Object sourceElement, Map<String, ?> keyPropertyValues) {
        this.sourceElement = sourceElement;
        this.keyPropertyValues = keyPropertyValues;
        keyPropertyValuesAsString = keyPropertyValues.values().stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    public ElementAbstraction(Map<String, ?> keyPropertyValues) {
        this(null, keyPropertyValues);
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
}
