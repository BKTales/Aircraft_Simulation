package eapli.aisafe.aircraftmanagement.domain;

import org.junit.jupiter.api.Test;

import java.time.Year;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YearOfManufactureTest {

    @Test
    void valueOfAcceptsValidYear() {
        final int year = Year.now().getValue() - 10;
        final YearOfManufacture subject = YearOfManufacture.valueOf(year);

        assertEquals(year, subject.year());
        assertEquals(10, subject.ageInYears());
    }

    @Test
    void valueOfAcceptsCurrentYear() {
        final int currentYear = Year.now().getValue();
        final YearOfManufacture subject = YearOfManufacture.valueOf(currentYear);

        assertEquals(currentYear, subject.year());
        assertEquals(0, subject.ageInYears());
    }

    @Test
    void valueOfAcceptsMinimumYear() {
        final YearOfManufacture subject = YearOfManufacture.valueOf(1900);

        assertEquals(1900, subject.year());
        assertTrue(subject.ageInYears() >= Year.now().getValue() - 1900);
    }

    @Test
    void valueOfRejectsFutureYear() {
        assertThrows(IllegalArgumentException.class,
                () -> YearOfManufacture.valueOf(Year.now().getValue() + 1));
    }

    @Test
    void valueOfRejectsYearBeforeMinimum() {
        assertThrows(IllegalArgumentException.class, () -> YearOfManufacture.valueOf(1899));
    }

    @Test
    void equalsAndHashCode() {
        final YearOfManufacture a = YearOfManufacture.valueOf(2015);
        final YearOfManufacture sameYear = YearOfManufacture.valueOf(2015);
        final YearOfManufacture otherYear = YearOfManufacture.valueOf(2010);

        assertTrue(a.equals(a));
        assertTrue(a.equals(sameYear));
        assertEquals(a.hashCode(), sameYear.hashCode());
        assertFalse(a.equals(otherYear));
        assertFalse(a.equals(null));
        assertFalse(a.equals("2015"));
    }

    @Test
    void protectedConstructorExistsForOrm() throws Exception {
        final var ctor = YearOfManufacture.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertNotNull(ctor.newInstance());
    }
}
