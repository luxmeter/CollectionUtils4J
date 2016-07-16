package luxmeter.receips.elementgenerator;

import luxmeter.collectionutils.CollectionUtils;
import luxmeter.receips.elementgenerator.ElementGeneratorBuilder.ElementGenerator;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static luxmeter.collectionutils.CollectionUtils.chain;
import static luxmeter.collectionutils.CollectionUtils.println;
import static luxmeter.receips.elementgenerator.MergeType.MERGED;
import static luxmeter.receips.elementgenerator.ElementGeneratorTest.Product.PX;
import static luxmeter.receips.elementgenerator.ElementGeneratorTest.Product.TX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

public class ElementGeneratorTest {
    public void shouldGenerateMissingCharges() {
        List<Rate> rates = Arrays.asList(new Rate("5500", EnumSet.of(PX, TX)), new Rate("5510", EnumSet.of(PX)));
        Set<Product> allProducts = EnumSet.allOf(Product.class);

        // what do i expect?
        List<String> chargeCodes = rates.stream().map(Rate::getChargeCode).collect(Collectors.toList());
        List<Pair<String, Product>> chargeCodePerProduct = CollectionUtils.product(chargeCodes, allProducts);
        // goal -> abstraction of the current collection (type A)
        List<RateKey> allRatesKeys = chargeCodePerProduct.stream()
                .map(pair -> new RateKey(pair.getLeft(), pair.getRight()))
                .collect(Collectors.toList());

        // map current collection (type T) to the abstraction (type A)
        List<RateKey> existingRateKeys = rates.stream()
                .flatMap(r -> r.getProducts().stream().map(p -> new RateKey(r.getChargeCode(), p)))
                .collect(Collectors.toList());

        // subtract from the goal the existing elements to determine the missing elements
        List<RateKey> missingRateKeys = CollectionUtils.removeAll(allRatesKeys, existingRateKeys);
        missingRateKeys.sort(null); // not needed, only for visualization
        println(missingRateKeys);
        System.out.println();

        // revert the abstract -> generate concrete elements
        Function<RateKey, Rate> inverseAbstractionMapper =
                rateKey -> new Rate(rateKey.getChargeCode(), EnumSet.of(rateKey.getProduct()));
        List<Rate> generatedMissingRateKeys = missingRateKeys.stream()
                .map(inverseAbstractionMapper)
                .collect(Collectors.toList());
        println(generatedMissingRateKeys);
        System.out.println();


        // it may happen that more elements have been generated than needed
        // this happens when similar entries can be merged together
        // -> group missing elements by similarity
        // -> merge similar entries together to one

        // grouping key
        Function<Rate, String> groupingKey = Rate::getChargeCode;
        Map<String, List<Rate>> groupedGeneratedMissingRates =
                generatedMissingRateKeys.stream().collect(Collectors.groupingBy(groupingKey));

        // and reduce -> merge
        BinaryOperator<Rate> reducer = (a, b) -> new Rate(a.getChargeCode(), chain(a.getProducts(), b.getProducts()));
        groupedGeneratedMissingRates.entrySet().forEach(entry -> {
            List<Rate> reducedList = Collections.singletonList(entry.getValue().stream()
                    .collect(Collectors.reducing(entry.getValue().get(0), reducer)));
            entry.setValue(reducedList);
        });

        // finish: flatten the result
        List<Rate> generatedMissingRates = groupedGeneratedMissingRates.values().stream()
                .map(r -> r.get(0)).collect(Collectors.toList());
        println(generatedMissingRates);
        System.out.println();
    }

    @Test
    public void shouldGenerateMissingRates() {
        List<Rate> rates = Arrays.asList(new Rate("5500", EnumSet.of(PX, TX)), new Rate("5510", EnumSet.of(PX)));
        Set<Product> allProducts = EnumSet.allOf(Product.class);
        List<String> chargeCodes = rates.stream().map(Rate::getChargeCode).collect(Collectors.toList());

        ElementGeneratorBuilder<Rate> elementGeneratorBuilder = ElementGeneratorBuilder.create();
        elementGeneratorBuilder
                .withExistingElements(rates)
                .withKeyProperty("chargeCode", chargeCodes, Rate::getChargeCode)
                .withKeyProperty("product", allProducts, Rate::getProducts)
                .withElementConstructor(
                        key -> new Rate(key.get("chargeCode"), Collections.singleton(key.get("product"))));

        ElementGenerator<Rate> elementGenerator = elementGeneratorBuilder.build();
        Set<Rate> generatedMissingRates = elementGenerator.generateMissingElements();

        assertThat(generatedMissingRates, hasSize(3));
        assertThat(generatedMissingRates.stream().map(Rate::toString).collect(Collectors.toList()),
                containsInAnyOrder(
                        "Rate{chargeCode='5500', products=[XX], zones=[]}",
                        "Rate{chargeCode='5510', products=[XX], zones=[]}",
                        "Rate{chargeCode='5510', products=[TX], zones=[]}"));
    }

    @Test
    public void shouldGenerateMissingRatesAndMerge() {
        List<Rate> rates = Arrays.asList(new Rate("5500", EnumSet.of(PX, TX)), new Rate("5510", EnumSet.of(PX)));
        Set<Product> allProducts = EnumSet.allOf(Product.class);

        // what do i expect?
        List<String> allChargeCodes = rates.stream().map(Rate::getChargeCode).collect(Collectors.toList());

        ElementGeneratorBuilder<Rate> elementGeneratorBuilder = ElementGeneratorBuilder.create();
        elementGeneratorBuilder
                .withExistingElements(rates)
                .withKeyProperty("chargeCode", allChargeCodes, Rate::getChargeCode)
                .withKeyProperty("product", allProducts, Rate::getProducts)
                .withElementConstructor(key ->
                        new Rate(key.get("chargeCode"), Collections.singleton(key.get("product"))))
                .withReducer(Rate::getChargeCode,
                        (a, b) -> new Rate(a.getChargeCode(), chain(a.getProducts(), b.getProducts())));

        ElementGenerator<Rate> elementGenerator = elementGeneratorBuilder.build();
        Set<Rate> generatedMissingRates = elementGenerator.generateMissingElements(MERGED);

        assertThat(generatedMissingRates, hasSize(2));
        assertThat(generatedMissingRates.stream().map(Rate::toString).collect(Collectors.toList()),
                containsInAnyOrder(
                        "Rate{chargeCode='5500', products=[XX], zones=[]}",
                        "Rate{chargeCode='5510', products=[TX, XX], zones=[]}"));
    }

    @Test
    public void shouldGenerateMissingRatesWithZones() {
        List<Rate> rates = Arrays.asList(
                new Rate("5500", EnumSet.of(PX, TX), EnumSet.of(Zone.A, Zone.B)),
                new Rate("5510", EnumSet.of(PX), EnumSet.of(Zone.A)));
        Set<Product> allProducts = EnumSet.allOf(Product.class);
        Set<Zone> allZones = EnumSet.allOf(Zone.class);

        // what do i expect?
        List<String> allChargeCodes = rates.stream().map(Rate::getChargeCode).collect(Collectors.toList());

        ElementGeneratorBuilder<Rate> elementGeneratorBuilder = ElementGeneratorBuilder.create();
        elementGeneratorBuilder
                .withExistingElements(rates)
                .withKeyProperty("chargeCode", allChargeCodes, Rate::getChargeCode)
                .withKeyProperty("product", allProducts, Rate::getProducts)
                .withKeyProperty("zone", allZones, Rate::getZones)
                .withElementConstructor(key -> new Rate(key.get("chargeCode"),
                        Collections.singleton(key.get("product")),
                        Collections.singleton(key.get("zone"))));


        ElementGenerator<Rate> elementGenerator = elementGeneratorBuilder.build();
        Set<Rate> generatedMissingRates = elementGenerator.generateMissingElements();

        assertThat(generatedMissingRates, hasSize(7));
        assertThat(generatedMissingRates.stream().map(Rate::toString).collect(Collectors.toList()),
                containsInAnyOrder(
                        "Rate{chargeCode='5500', products=[XX], zones=[A]}",
                        "Rate{chargeCode='5500', products=[XX], zones=[B]}",
                        "Rate{chargeCode='5510', products=[XX], zones=[B]}",
                        "Rate{chargeCode='5510', products=[XX], zones=[A]}",
                        "Rate{chargeCode='5510', products=[PX], zones=[B]}",
                        "Rate{chargeCode='5510', products=[TX], zones=[B]}",
                        "Rate{chargeCode='5510', products=[TX], zones=[A]}"
                ));
    }

    public static final class RateKey implements Comparable<RateKey> {
        private String chargeCode;
        private Product product;

        public RateKey(String chargeCode, Product product) {
            this.chargeCode = chargeCode;
            this.product = product;
        }

        public String getChargeCode() {
            return chargeCode;
        }

        public Product getProduct() {
            return product;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RateKey rateKey = (RateKey) o;
            return Objects.equals(chargeCode, rateKey.chargeCode) &&
                    product == rateKey.product;
        }

        @Override
        public int hashCode() {
            return Objects.hash(chargeCode, product);
        }

        @Override
        public String toString() {
            return "RateKey{" +
                    "chargeCode='" + chargeCode + '\'' +
                    ", product=" + product +
                    '}';
        }

        @Override
        public int compareTo(RateKey o) {
            return Comparator.comparing(RateKey::getChargeCode).thenComparing(RateKey::getProduct).compare(this, o);
        }
    }

    public static final class Rate {
        private String chargeCode;
        private Set<Product> products;
        private Set<Zone> zones;

        public Rate(String chargeCode, Set<Product> products) {
            this.products = products;
            this.chargeCode = chargeCode;
            zones = Collections.emptySet();
        }

        public Rate(String chargeCode, Set<Product> products, Set<Zone> zones) {
            this.products = products;
            this.chargeCode = chargeCode;
            this.zones = zones;
        }

        public Set<Product> getProducts() {
            return products;
        }

        public String getChargeCode() {
            return chargeCode;
        }

        public Set<Zone> getZones() {
            return zones;
        }

        @Override
        public String toString() {
            ArrayList<Product> sortedProducts = new ArrayList<>(this.products);
            sortedProducts.sort(Comparator.comparing(Product::name));
            ArrayList<Zone> sortedZones = new ArrayList<>(this.zones);
            sortedZones.sort(Comparator.comparing(Zone::name));
            return "Rate{" +
                    "chargeCode='" + chargeCode + '\'' +
                    ", products=" + sortedProducts +
                    ", zones=" +  sortedZones +
                    '}';
        }
    }

    public enum Product {
        PX, TX, XX
    }

    public enum Zone {
        A, B
    }
}