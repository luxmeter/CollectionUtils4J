package luxmeter.receips.elementgenerator;

import luxmeter.receips.elementgenerator.model.Product;
import luxmeter.receips.elementgenerator.model.SimplifiedRate;
import luxmeter.receips.elementgenerator.model.Zone;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static luxmeter.receips.elementgenerator.model.Product.PX;
import static luxmeter.receips.elementgenerator.model.Zone.A;
import static luxmeter.receips.elementgenerator.model.Zone.B;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

public class CommonUsecasesWithSimpleObjectsTest {
    private Set<Zone> applicableZones;
    private Set<Product> applicableProducts;
    private List<SimplifiedRate> simplifiedRates;

    @Before
    public void setup() {
        applicableProducts = EnumSet.allOf(Product.class);
        applicableZones = EnumSet.allOf(Zone.class);
        simplifiedRates = Arrays.asList(
                new SimplifiedRate("5500", PX, A),
                new SimplifiedRate("5510", PX, B)
        );
    }


    private Map<String, Object> createIntermediateResultMapper(SimplifiedRate concreteElement) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("chargeCode", concreteElement.getChargeCode());
        map.put("product", concreteElement.getProduct());
        Zone zone = (concreteElement.getZone() != null) ? concreteElement.getZone() : Zone.A;
        map.put("zone", zone);
        return map;
    }

    /**
     * Tests if simple objects without any key properties being collections can be generated.
     */
    @Test
    public void shouldGenerateMissingElements() {
        List<String> allChargeCodes = simplifiedRates.stream()
                .map(SimplifiedRate::getChargeCode)
                .collect(Collectors.toList());
        ElementGeneratorBuilder<SimplifiedRate> elementGeneratorBuilder = ElementGeneratorBuilder.create();
        elementGeneratorBuilder
                .withExistingElements(simplifiedRates)
                .withSingleValueProperty("chargeCode", allChargeCodes, SimplifiedRate::getChargeCode)
                .withSingleValueProperty("product", applicableProducts, SimplifiedRate::getProduct)
                .withSingleValueProperty("zone", applicableZones, SimplifiedRate::getZone)
                .withElementFactory(abstractElement -> new SimplifiedRate(
                        abstractElement.get("chargeCode"),
                        abstractElement.get("product"),
                        abstractElement.get("zone"))
                );

        ElementGenerator<SimplifiedRate> elementGenerator = elementGeneratorBuilder.build();
        Set<SimplifiedRate> generatedMissingSimpleRates = elementGenerator.generateMissingElements();

        assertThat(generatedMissingSimpleRates, hasSize(10));
        assertThat(generatedMissingSimpleRates,
                containsInAnyOrder(
                        new SimplifiedRate("5500", Product.TX, Zone.A),
                        new SimplifiedRate("5500", Product.XX, Zone.A),
                        new SimplifiedRate("5500", Product.PX, Zone.B),
                        new SimplifiedRate("5500", Product.TX, Zone.B),
                        new SimplifiedRate("5500", Product.XX, Zone.B),
                        new SimplifiedRate("5510", Product.PX, Zone.A),
                        new SimplifiedRate("5510", Product.TX, Zone.A),
                        new SimplifiedRate("5510", Product.XX, Zone.A),
                        new SimplifiedRate("5510", Product.TX, Zone.B),
                        new SimplifiedRate("5510", Product.XX, Zone.B)
                ));
    }

    /**
     * Tests if simple objects without any key properties being collections
     * can be generated and overridden default behaviour.
     */
    @Test
    public void shouldGenerateMissingElementsWithOverridenDefaults() {
        // zone null mean -> rate covers by default Zone A
        simplifiedRates.set(1, new SimplifiedRate("5510", PX, null));
        List<String> allChargeCodes = simplifiedRates.stream()
                .map(SimplifiedRate::getChargeCode)
                .collect(Collectors.toList());
        ElementGeneratorBuilder<SimplifiedRate> elementGeneratorBuilder = ElementGeneratorBuilder.create();
        elementGeneratorBuilder
                .withExistingElements(simplifiedRates)
                .withSingleValueProperty("chargeCode", allChargeCodes, SimplifiedRate::getChargeCode)
                .withSingleValueProperty("product", applicableProducts, SimplifiedRate::getProduct)
                .withSingleValueProperty("zone", applicableZones, SimplifiedRate::getZone)
                .withElementFactory(abstractElement -> new SimplifiedRate(
                        abstractElement.get("chargeCode"),
                        abstractElement.get("product"),
                        abstractElement.get("zone"))
                )
                .withOverriddenDefaults(OverriddenDefaults.<SimplifiedRate>create()
                        .setIntermediateResultMapper(this::createIntermediateResultMapper));

        ElementGenerator<SimplifiedRate> elementGenerator = elementGeneratorBuilder.build();
        Set<SimplifiedRate> generatedMissingSimpleRates = elementGenerator.generateMissingElements();

        assertThat(generatedMissingSimpleRates, hasSize(10));
        assertThat(generatedMissingSimpleRates,
                containsInAnyOrder(
                        new SimplifiedRate("5500", Product.TX, Zone.A),
                        new SimplifiedRate("5500", Product.XX, Zone.A),
                        new SimplifiedRate("5500", Product.PX, Zone.B),
                        new SimplifiedRate("5500", Product.TX, Zone.B),
                        new SimplifiedRate("5500", Product.XX, Zone.B),
                        new SimplifiedRate("5510", Product.PX, Zone.B), // <- has been changed in comparison to #shouldGenerateMissingElements
                        new SimplifiedRate("5510", Product.TX, Zone.A),
                        new SimplifiedRate("5510", Product.XX, Zone.A),
                        new SimplifiedRate("5510", Product.TX, Zone.B),
                        new SimplifiedRate("5510", Product.XX, Zone.B)
                ));
    }

    @Test
    public void shouldGenerateWithoutExistingElements() {
        List<SimplifiedRate> empty = Collections.emptyList();
        List<String> allChargeCodes = simplifiedRates.stream()
                .map(SimplifiedRate::getChargeCode)
                .collect(Collectors.toList());
        ElementGeneratorBuilder<SimplifiedRate> elementGeneratorBuilder = ElementGeneratorBuilder.create();
        elementGeneratorBuilder
                .withExistingElements(empty)
                .withSingleValueProperty("chargeCode", allChargeCodes, SimplifiedRate::getChargeCode)
                .withSingleValueProperty("product", applicableProducts, SimplifiedRate::getProduct)
                .withSingleValueProperty("zone", applicableZones, SimplifiedRate::getZone)
                .withElementFactory(abstractElement -> new SimplifiedRate(
                        abstractElement.get("chargeCode"),
                        abstractElement.get("product"),
                        abstractElement.get("zone"))
                );

        ElementGenerator<SimplifiedRate> elementGenerator = elementGeneratorBuilder.build();
        Set<SimplifiedRate> generatedMissingSimpleRates = elementGenerator.generateMissingElements();

        assertThat(generatedMissingSimpleRates, hasSize(12));
        assertThat(generatedMissingSimpleRates,
                containsInAnyOrder(
                        new SimplifiedRate("5500", Product.TX, Zone.A),
                        new SimplifiedRate("5500", Product.XX, Zone.A),
                        new SimplifiedRate("5500", Product.PX, Zone.A),
                        new SimplifiedRate("5500", Product.PX, Zone.B),
                        new SimplifiedRate("5500", Product.TX, Zone.B),
                        new SimplifiedRate("5500", Product.XX, Zone.B),
                        new SimplifiedRate("5510", Product.PX, Zone.A),
                        new SimplifiedRate("5510", Product.PX, Zone.B),
                        new SimplifiedRate("5510", Product.TX, Zone.A),
                        new SimplifiedRate("5510", Product.XX, Zone.A),
                        new SimplifiedRate("5510", Product.TX, Zone.B),
                        new SimplifiedRate("5510", Product.XX, Zone.B)
                ));
    }
}
