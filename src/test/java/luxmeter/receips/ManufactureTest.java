package luxmeter.receips;

import luxmeter.collectionutils.CollectionUtils;
import luxmeter.receips.ManufactureBuilder.Manufacture;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static luxmeter.collectionutils.CollectionUtils.chain;
import static luxmeter.collectionutils.CollectionUtils.println;
import static luxmeter.receips.ManufactureBuilder.IntermediateResultGenerationInstruction.GENERATE_PRO_VALUES;
import static luxmeter.receips.ManufactureBuilder.MergeType.MERGED;
import static luxmeter.receips.ManufactureTest.Product.PX;
import static luxmeter.receips.ManufactureTest.Product.TX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

public class ManufactureTest {
    public void shouldGenerateMissingCharges() {
        List<Rate> rates = Arrays.asList(new Rate("5500", EnumSet.of(PX, TX)), new Rate("5510", EnumSet.of(PX)));
        Set<Product> allProducts = EnumSet.allOf(Product.class);

        // what do i expect?
        List<String> chargeCodes = rates.stream().map(Rate::getChargeCode).collect(Collectors.toList());
        List<Pair<String, Product>> chargeCodePerProduct = CollectionUtils.product(chargeCodes, allProducts);
        // goal -> abstraction of the current collection (type A)
        List<RateKey> allRatesKeys = chargeCodePerProduct.stream().map(pair -> new RateKey(pair.getLeft(), pair.getRight())).collect(Collectors.toList());

        // map current collection (type T) to the abstraction (type A)
        List<RateKey> existingRateKeys = rates.stream().flatMap(r -> r.getProducts().stream().map(p -> new RateKey(r.getChargeCode(), p))).collect(Collectors.toList());

        // subtract from the goal the existing elements to determine the missing elements
        List<RateKey> missingRateKeys = CollectionUtils.removeAll(allRatesKeys, existingRateKeys);
        missingRateKeys.sort(null); // not needed, only for visualization
        println(missingRateKeys);
        System.out.println();

        // revert the abstract -> generate concrete elements
        Function<RateKey, Rate> inverseAbstractionMapper = rateKey -> new Rate(rateKey.getChargeCode(), EnumSet.of(rateKey.getProduct()));
        List<Rate> generatedMissingRateKeys = missingRateKeys.stream().map(inverseAbstractionMapper).collect(Collectors.toList());
        println(generatedMissingRateKeys);
        System.out.println();


        // it may happen that more elements have been generated than needed
        // this happens when similar entries can be merged together
        // -> group missing elements by similarity
        // -> merge similar entries together to one

        // grouping key
        Function<Rate, String> groupingKey = Rate::getChargeCode;
        Map<String, List<Rate>> groupedGeneratedMissingRates = generatedMissingRateKeys.stream().collect(Collectors.groupingBy(groupingKey));

        // and reduce -> merge
        BinaryOperator<Rate> reducer = (a, b) -> new Rate(a.getChargeCode(), chain(a.getProducts(), b.getProducts()));
        groupedGeneratedMissingRates.entrySet().forEach(entry -> {
            List<Rate> reducedList = Collections.singletonList(entry.getValue().stream()
                    .collect(Collectors.reducing(entry.getValue().get(0), reducer)));
            entry.setValue(reducedList);
        });

        // finish: flatten the result
        List<Rate> generatedMissingRates = groupedGeneratedMissingRates.values().stream().map(r -> r.get(0)).collect(Collectors.toList());
        println(generatedMissingRates);
        System.out.println();
    }

    @Test
    public void shouldGenerateMissingRates() {
        List<Rate> rates = Arrays.asList(new Rate("5500", EnumSet.of(PX, TX)), new Rate("5510", EnumSet.of(PX)));
        Set<Product> allProducts = EnumSet.allOf(Product.class);
        List<String> chargeCodes = rates.stream().map(Rate::getChargeCode).collect(Collectors.toList());

        ManufactureBuilder<Rate> manufactureBuilder = ManufactureBuilder.create();
        manufactureBuilder
                .withExistingElements(rates)
                .withKeyProperty("chargeCode", chargeCodes, Rate::getChargeCode)
                .withKeyProperty("product", allProducts, Rate::getProducts, GENERATE_PRO_VALUES)
                .withElementConstructor(
                        key -> new Rate(key.get("chargeCode"), Collections.singleton(key.get("product"))));

        Manufacture<Rate> manufacture = manufactureBuilder.build();
        Set<Rate> generatedMissingRates = manufacture.generateMissingElements();

        assertThat(generatedMissingRates, hasSize(3));
        assertThat(generatedMissingRates.stream().map(Rate::toString).collect(Collectors.toList()),
                containsInAnyOrder("Rate{ChargeCode='5500', products=[XX]}",
                        "Rate{ChargeCode='5510', products=[XX]}",
                        "Rate{ChargeCode='5510', products=[TX]}"));
    }

    @Test
    public void shouldGenerateMissingRatesAndMerge() {
        List<Rate> rates = Arrays.asList(new Rate("5500", EnumSet.of(PX, TX)), new Rate("5510", EnumSet.of(PX)));
        Set<Product> allProducts = EnumSet.allOf(Product.class);

        // what do i expect?
        List<String> chargeCodes = rates.stream().map(Rate::getChargeCode).collect(Collectors.toList());
        List<Pair<String, Product>> chargeCodePerProduct = CollectionUtils.product(chargeCodes, allProducts);

        ManufactureBuilder<Rate> manufactureBuilder = ManufactureBuilder.create();
        manufactureBuilder
                .withExistingElements(rates)
                .withKeyProperty("chargeCode", chargeCodes, Rate::getChargeCode)
                .withKeyProperty("product", allProducts, Rate::getProducts, GENERATE_PRO_VALUES)
                .withElementConstructor(key -> new Rate(key.get("chargeCode"), Collections.singleton(key.get("product"))))
                .withReducer(Rate::getChargeCode,
                        (a, b) -> new Rate(a.getChargeCode(), chain(a.getProducts(), b.getProducts())));

        Manufacture<Rate> manufacture = manufactureBuilder.build();
        Set<Rate> generatedMissingRates = manufacture.generateMissingElements(MERGED);

        assertThat(generatedMissingRates, hasSize(2));
        assertThat(generatedMissingRates.stream().map(Rate::toString).collect(Collectors.toList()),
                containsInAnyOrder("Rate{ChargeCode='5500', products=[XX]}",
                        "Rate{ChargeCode='5510', products=[TX, XX]}"));
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
        private String ChargeCode;
        private Set<Product> products;

        public Rate(String chargeCode, Set<Product> products) {
            this.products = products;
            ChargeCode = chargeCode;
        }

        public Set<Product> getProducts() {
            return products;
        }

        public String getChargeCode() {
            return ChargeCode;
        }

        @Override
        public String toString() {
            ArrayList<Product> sorted = new ArrayList<>(this.products);
            sorted.sort(Comparator.comparing(Product::name));
            return "Rate{" +
                    "ChargeCode='" + ChargeCode + '\'' +
                    ", products=" + sorted +
                    '}';
        }
    }

    public enum Product {
        PX, TX, XX
    }
}