package luxmeter.receips.elementgenerator.model;

import java.util.Objects;

/**
 * Model for testing purposes representing a charge that's applicable for some zone and product.
 * In constrast to {@link Rate}s only simple properties are used without any collections.
 *
 */
public final class SimplifiedRate {
    private String chargeCode;
    private Product product;
    private Zone zone;

    // default constructor for reflection based ElementConstructor (see test)
    public SimplifiedRate() {
    }

    public SimplifiedRate(String chargeCode, Product product, Zone zone) {
        this.chargeCode = chargeCode;
        this.product = product;
        this.zone = zone;
    }

    public String getChargeCode() {
        return chargeCode;
    }

    public Product getProduct() {
        return product;
    }

    public Zone getZone() {
        return zone;
    }

    @Override
    public String toString() {
        return "SimplifiedRate{" +
                "chargeCode='" + chargeCode + '\'' +
                ", product=" + product +
                ", zone=" + zone +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimplifiedRate that = (SimplifiedRate) o;
        return Objects.equals(chargeCode, that.chargeCode) &&
                product == that.product &&
                zone == that.zone;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chargeCode, product, zone);
    }
}
