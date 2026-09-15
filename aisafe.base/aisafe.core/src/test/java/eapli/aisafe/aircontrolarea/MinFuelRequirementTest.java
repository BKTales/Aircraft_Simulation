package eapli.aisafe.aircontrolarea;

import eapli.aisafe.aircontrolarea.application.exceptions.NegativeFuelException;
import eapli.aisafe.aircontrolarea.domain.MinFuelRequirement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MinFuelRequirementTest {

    @Test
    public void ensureNegativeFuelIsInvalid() {
        assertThrows(NegativeFuelException.class, () -> {
            MinFuelRequirement.valueOf(-1.0f);
        });
    }

    @Test
    public void ensureValidFuelIsAccepted() {
        MinFuelRequirement fuel = MinFuelRequirement.valueOf(100.0f);
        assertNotNull(fuel);
    }

    @Test
    public void protectedConstructorCoverage() {
        class ProtectedFuel extends MinFuelRequirement { ProtectedFuel() { super(); } }
        assertNotNull(new ProtectedFuel());
    }
}
