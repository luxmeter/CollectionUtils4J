package luxmeter.receips.elementgenerator;

import luxmeter.receips.elementgenerator.model.ChargeCode;
import luxmeter.receips.elementgenerator.model.Product;
import luxmeter.receips.elementgenerator.model.Rate;
import luxmeter.receips.elementgenerator.model.Zone;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static luxmeter.collectionutils.CollectionUtils.chain;
import static luxmeter.receips.elementgenerator.MergeType.MERGED;
import static luxmeter.receips.elementgenerator.model.Product.*;
import static luxmeter.receips.elementgenerator.model.Zone.A;
import static luxmeter.receips.elementgenerator.model.Zone.B;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

public class CommonUsecasesWithComplexObjectsTest {
    private Set<Zone> applicableZones;
    private Set<Product> applicableProducts;

    @Before
    public void setup() {
        applicableProducts = EnumSet.allOf(Product.class);
        applicableZones = EnumSet.allOf(Zone.class);
    }

    private Rate createRate(String chargeCode, String chargeName, Product... product) {
        return new Rate(
                new ChargeCode(chargeCode, chargeName),
                asList(product),
                Collections.emptySet());
    }

    private Rate createRate(String chargeCode, String chargeName, Collection<Product> products, Collection<Zone> zones) {
        return new Rate(
                new ChargeCode(chargeCode, chargeName),
                products,
                zones);
    }

    private Rate createRate(String chargeCode, String chargeName, Product product, Zone zone) {
        return new Rate(
                new ChargeCode(chargeCode, chargeName),
                singleton(product),
                singleton(zone));
    }

    private List<Rate> createRatesWithoutZones() {
        return asList(
                createRate("5500", "someName", PX, TX),
                createRate("5510", "someName", PX));
    }

    private List<Rate> createRatesWithZones() {
        return asList(
                new Rate(new ChargeCode("5500", "someName"), EnumSet.of(PX, TX), EnumSet.of(A, B)),
                new Rate(new ChargeCode("5510", "someName"), EnumSet.of(PX), EnumSet.of(A)));
    }

    private Rate mergeByZones(Rate a, Rate b) {
        return new Rate(a.getChargeCode(), a.getProducts(), chain(a.getZones(), b.getZones()));
    }

    private Rate mergeByProducts(Rate a, Rate b) {
        return new Rate(a.getChargeCode(), chain(a.getProducts(), b.getProducts()), a.getZones());
    }

    private String groupByChargeCodeAndProduct(Rate rate) {
        List<Product> products = rate.getProducts().stream().sorted(
                Comparator.comparing(Product::name)).collect(Collectors.toList());
        return rate.getChargeCode().getCode() + products.toString();
    }

    private String groupByChargeCodeAndZone(Rate rate) {
        List<Zone> zones = rate.getZones().stream().sorted(
                Comparator.comparing(Zone::name)).collect(Collectors.toList());
        return rate.getChargeCode().getCode() + zones.toString();
    }

    private List<Map<String, Object>> createIntermediateResultsMapper(Rate concreteElement) {
        Set<Product> products = (concreteElement.getProducts().isEmpty())
                ? applicableProducts
                : concreteElement.getProducts();
        return products.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("chargeCode", concreteElement.getChargeCode());
            map.put("product", p);
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * Tests if complex objects with key properties being collections can be generated.
     */
    @Test
    public void shouldGenerateMissingElements() {
        // we don't care for zones in this test case - they also don't belong to the key properties
        List<Rate> existingRates = createRatesWithoutZones();

        List<ChargeCode> allChargeCodes = existingRates.stream()
                .map(Rate::getChargeCode)
                .collect(Collectors.toList());

        ElementGeneratorBuilder<Rate> elementGeneratorBuilder = ElementGeneratorBuilder.create();
        ValuesExtractor<Rate, Product, Set<Product>> productExtractor = Rate::getProducts;
        elementGeneratorBuilder
                .withExistingElements(existingRates)
                // would fail without specifying how to obtain the unique string representation
                .withSingleValueProperty("chargeCode", allChargeCodes, Rate::getChargeCode, ChargeCode::getCode)
                .withCollectionProperty("product", applicableProducts, productExtractor)
                .withElementFactory(abstractElement -> new Rate(
                        abstractElement.get("chargeCode"),
                        singleton(abstractElement.get("product")),
                        Collections.emptySet()));

        ElementGenerator<Rate> elementGenerator = elementGeneratorBuilder.build();
        Set<Rate> generatedMissingSimpleRates = elementGenerator.generateMissingElements();

        assertThat(generatedMissingSimpleRates, hasSize(3));
        assertThat(generatedMissingSimpleRates,
                containsInAnyOrder(
                        createRate("5500", "someName", XX),
                        createRate("5510", "someName", TX),
                        createRate("5510", "someName", XX)));
    }

    /**
     * Tests if complex objects with multiple key properties being collections can be generated.
     */
    @Test
    public void shouldGenerateMissingElements2() {
        // we don't care for zones in this test case - they also don't belong to the key properties
        List<Rate> existingRates = createRatesWithZones();

        List<ChargeCode> allChargeCodes = existingRates.stream()
                .map(Rate::getChargeCode)
                .collect(Collectors.toList());

        ElementGeneratorBuilder<Rate> elementGeneratorBuilder = ElementGeneratorBuilder.create();
        ValuesExtractor<Rate, Product, Set<Product>> productExtractor = Rate::getProducts;
        ValuesExtractor<Rate, Zone, Set<Zone>> zoneExtractor = Rate::getZones;
        elementGeneratorBuilder
                .withExistingElements(existingRates)
                .withSingleValueProperty("chargeCode", allChargeCodes, Rate::getChargeCode, ChargeCode::getCode)
                .withCollectionProperty("product", applicableProducts, productExtractor)
                .withCollectionProperty("zone", applicableZones, zoneExtractor)
                .withElementFactory(abstractElement -> new Rate(
                        abstractElement.get("chargeCode"),
                        singleton(abstractElement.get("product")),
                        singleton(abstractElement.get("zone"))));

        ElementGenerator<Rate> elementGenerator = elementGeneratorBuilder.build();
        Set<Rate> generatedMissingSimpleRates = elementGenerator.generateMissingElements();

        assertThat(generatedMissingSimpleRates, hasSize(7));
        assertThat(generatedMissingSimpleRates,
                containsInAnyOrder(
                        createRate("5500", "someName", XX, A),
                        createRate("5500", "someName", XX, B),
                        createRate("5510", "someName", PX, B),
                        createRate("5510", "someName", TX, A),
                        createRate("5510", "someName", TX, B),
                        createRate("5510", "someName", XX, A),
                        createRate("5510", "someName", XX, B)));
    }

    /**
     * Tests if complex objects with key properties being collections can be generated and merged.
     */
    @Test
    public void shouldGenerateAndMergeMissingElements() {
        // we don't care for zones in this test case - they also don't belong to the key properties
        List<Rate> existingRates = createRatesWithoutZones();

        List<ChargeCode> allChargeCodes = existingRates.stream()
                .map(Rate::getChargeCode)
                .collect(Collectors.toList());

        ValuesExtractor<Rate, Product, Set<Product>> productExtractor = Rate::getProducts;

        ElementGeneratorBuilder<Rate> elementGeneratorBuilder = ElementGeneratorBuilder.create();
        elementGeneratorBuilder
                .withExistingElements(existingRates)
                .withSingleValueProperty("chargeCode", allChargeCodes, Rate::getChargeCode, ChargeCode::getCode)
                .withCollectionProperty("product", applicableProducts, productExtractor)
                .withElementFactory(abstractElement -> new Rate(
                        abstractElement.get("chargeCode"),
                        singleton(abstractElement.get("product")),
                        Collections.emptySet()))
                .withReducer(Rate::getChargeCode,
                        (rate1, rate2) -> new Rate(
                                rate1.getChargeCode(),
                                chain(rate1.getProducts(), rate2.getProducts()),
                                Collections.emptySet()));

        ElementGenerator<Rate> elementGenerator = elementGeneratorBuilder.build();
        Set<Rate> generatedMissingSimpleRates = elementGenerator.generateMissingElements(MERGED);

        assertThat(generatedMissingSimpleRates, hasSize(2));
        assertThat(generatedMissingSimpleRates,
                containsInAnyOrder(
                        createRate("5500", "someName", XX),
                        createRate("5510", "someName", TX, XX)));
    }

    /**
     * Tests if complex objects with multiple key properties being collections can be generated and merged.
     */
    @Test
    public void shouldGenerateAndMergeMissingElements2() {
        // we don't care for zones in this test case - they also don't belong to the key properties
        List<Rate> existingRates = createRatesWithZones();

        List<ChargeCode> allChargeCodes = existingRates.stream()
                .map(Rate::getChargeCode)
                .collect(Collectors.toList());

        ValuesExtractor<Rate, Product, Set<Product>> productExtractor = Rate::getProducts;
        ValuesExtractor<Rate, Zone, Set<Zone>> zoneExtractor = Rate::getZones;

        ElementGeneratorBuilder<Rate> elementGeneratorBuilder = ElementGeneratorBuilder.create();
        elementGeneratorBuilder
                .withExistingElements(existingRates)
                .withSingleValueProperty("chargeCode", allChargeCodes, Rate::getChargeCode, ChargeCode::getCode)
                .withCollectionProperty("product", applicableProducts, productExtractor)
                .withCollectionProperty("zone", applicableZones, zoneExtractor)
                .withElementFactory(abstractElement -> new Rate(
                        abstractElement.get("chargeCode"),
                        singleton(abstractElement.get("product")),
                        singleton(abstractElement.get("zone"))))
                .withReducers(
                        Reducer.create(this::groupByChargeCodeAndProduct, this::mergeByZones),
                        Reducer.create(this::groupByChargeCodeAndZone, this::mergeByProducts));

        ElementGenerator<Rate> elementGenerator = elementGeneratorBuilder.build();
        Set<Rate> generatedMissingSimpleRates = elementGenerator.generateMissingElements(MERGED);

        assertThat(generatedMissingSimpleRates, hasSize(3));
        assertThat(generatedMissingSimpleRates,
                containsInAnyOrder(
                        createRate("5500", "someName", singleton(XX), asList(A, B)),
                        createRate("5510", "someName", PX, B),
                        createRate("5510", "someName", asList(TX, XX), asList(A, B))));
    }

    /**
     * Tests if complex objects with key properties being collections can be generated wiht overriden default behaviour.
     */
    @Test
    public void shouldGenerateMissingElementsWithOverridenDefaults() {
        // we don't care for zones in this test case - they also don't belong to the key properties
        List<Rate> existingRates = createRatesWithoutZones();
        // products is empty means -> rate covers all products
        existingRates.set(1, createRate("5510", "someName"));

        List<ChargeCode> allChargeCodes = existingRates.stream()
                .map(Rate::getChargeCode)
                .collect(Collectors.toList());

        ValuesExtractor<Rate, Product, Set<Product>> productExtractor = Rate::getProducts;

        ElementGeneratorBuilder<Rate> elementGeneratorBuilder = ElementGeneratorBuilder.create();
        elementGeneratorBuilder
                .withExistingElements(existingRates)
                // would fail without specifying how to obtain the unique string representation
                .withSingleValueProperty("chargeCode", allChargeCodes, Rate::getChargeCode, ChargeCode::getCode)
                .withCollectionProperty("product", applicableProducts, productExtractor)
                .withElementFactory(abstractElement -> new Rate(
                        abstractElement.get("chargeCode"),
                        singleton(abstractElement.get("product")),
                        Collections.emptySet()))
                .withOverriddenDefaults(OverriddenDefaults.<Rate> create()
                        .setIntermediateResultsMapper(this::createIntermediateResultsMapper));

        ElementGenerator<Rate> elementGenerator = elementGeneratorBuilder.build();
        Set<Rate> generatedMissingSimpleRates = elementGenerator.generateMissingElements();

        assertThat(generatedMissingSimpleRates, hasSize(1));
        assertThat(generatedMissingSimpleRates,
                containsInAnyOrder(
                        createRate("5500", "someName", XX)));
    }
}
