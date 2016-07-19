package luxmeter.receips.elementgenerator.model;

import java.util.*;

/**
 * Model for testing purposes representing a charge that's applicable for some zones and products.
 */
public final class Rate {
    private ChargeCode chargeCode;
    private Set<Product> products;
    private Set<Zone> zones;

    // default constructor for reflection based ElementConstructor (see test)
    public Rate() {
    }

    public Rate(ChargeCode chargeCode, Collection<Product> products, Collection<Zone> zones) {
        this.products = new HashSet<>(products);
        this.chargeCode = chargeCode;
        this.zones = new HashSet<>(zones);
    }

    public Set<Product> getProducts() {
        return products;
    }

    public ChargeCode getChargeCode() {
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
                ", zones=" + sortedZones +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rate rate = (Rate) o;
        return Objects.equals(chargeCode, rate.chargeCode) &&
                Objects.equals(products, rate.products) &&
                Objects.equals(zones, rate.zones);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chargeCode, products, zones);
    }
}
