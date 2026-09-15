package eapli.aisafe.aircraftmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.Year;
import java.util.Objects;

@Embeddable
public class YearOfManufacture implements ValueObject {

    private static final int MIN_YEAR = 1900;

    @Column(name = "YEAR_OF_MANUFACTURE", nullable = false)
    private int year;

    protected YearOfManufacture() {
        // ORM
    }

    private YearOfManufacture(final int year) {
        final int currentYear = Year.now().getValue();
        if (year < MIN_YEAR || year > currentYear) {
            throw new IllegalArgumentException(
                    "Year of manufacture must be between " + MIN_YEAR + " and " + currentYear + ".");
        }
        this.year = year;
    }

    public static YearOfManufacture valueOf(final int year) {
        return new YearOfManufacture(year);
    }

    public int year() {
        return year;
    }

    /** Aircraft age in whole years, based on the current calendar year. */
    public int ageInYears() {
        return Year.now().getValue() - year;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof YearOfManufacture)) {
            return false;
        }
        final YearOfManufacture that = (YearOfManufacture) o;
        return year == that.year;
    }

    @Override
    public int hashCode() {
        return Objects.hash(year);
    }
}
