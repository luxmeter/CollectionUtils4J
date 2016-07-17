package luxmeter.receips.elementgenerator;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static luxmeter.collectionutils.CollectionUtils.zip;

class KeyPropertyMetadataSet<T> {
    public static final String BLANK_STRING = "";
    private List<KeyPropertyMetadata<T, ?>> keyPropertiesMetadata = new ArrayList<>();

    private void validateKeyProperty(KeyPropertyMetadata<T, ?> keyPropertyMetadata, boolean propertyNameIsEmpty) {
        if (propertyNameIsEmpty) {
            throw new IllegalArgumentException("Property name is not allowed to be null or blank");
        }
        boolean propertyExistsAlready = keyPropertiesMetadata.stream()
                .anyMatch(k -> k.getPropertyName().equals(keyPropertyMetadata.getPropertyName()));
        if (propertyExistsAlready) {
            throw new IllegalArgumentException(
                    String.format("Key property '%s' already defined", keyPropertyMetadata.getPropertyName()));
        }
    }

    public void add(KeyPropertyMetadata<T, ?> keyPropertyMetadata) {
        boolean propertyNameIsEmpty = keyPropertyMetadata.getPropertyName() == null
                || keyPropertyMetadata.getPropertyName().trim().equals(BLANK_STRING);
        validateKeyProperty(keyPropertyMetadata, propertyNameIsEmpty);
        keyPropertiesMetadata.add(keyPropertyMetadata);
    }

    public boolean atLeastOnePropertyIsACollection() {
        return keyPropertiesMetadata.stream().anyMatch(KeyPropertyMetadata::isCollection);
    }

    public Map<String, Object> getPropertyValues(T concreteElement) {
        return  keyPropertiesMetadata.stream()
                .collect(Collectors.toMap(KeyPropertyMetadata::getPropertyName,
                        keyProperty -> keyProperty.getValueExtractor().apply(concreteElement)));
    }

    public Map<String, Object> getPropertyValues(T concreteElement, Predicate<KeyPropertyMetadata<T, ?>> predicate) {
        return  keyPropertiesMetadata.stream()
                .filter(predicate::test)
                .collect(Collectors.toMap(KeyPropertyMetadata::getPropertyName,
                        keyProperty -> keyProperty.getValueExtractor().apply(concreteElement)));
    }

    public Map<String, Object> getPropertyValues(List<Object> singleCombination, Predicate<KeyPropertyMetadata<T, ?>> predicate) {
        Stream<String> propertyNames = keyPropertiesMetadata.stream()
                .filter(predicate::test)
                .map(KeyPropertyMetadata::getPropertyName);
        List<Pair<String, Object>> zipped = zip(propertyNames, singleCombination.stream()).collect(Collectors.toList());
        Map<String, Object> map = zipped.stream().collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        return map;
    }

    public Map<String, Object> getPropertyValues(List<Object> singleCombination) {
        Stream<String> propertyNames = keyPropertiesMetadata.stream()
                .map(KeyPropertyMetadata::getPropertyName);
        List<Pair<String, Object>> zipped = zip(propertyNames, singleCombination.stream()).collect(Collectors.toList());
        Map<String, Object> map = zipped.stream().collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        return map;
    }

    public List<List<?>> getValueRanges() {
        return keyPropertiesMetadata.stream()
                .map(keyProperty -> keyProperty.getValuesRange().stream()
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    public Map<String, Function<Object, String>> getToStringMapper() {
        return keyPropertiesMetadata.stream().collect(Collectors.toMap(KeyPropertyMetadata::getPropertyName, KeyPropertyMetadata::getToStringMapper));
    }
}
