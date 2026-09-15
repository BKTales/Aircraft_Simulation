package eapli.aisafe.aircontrolarea;

import eapli.aisafe.aircontrolarea.domain.AirControlAreaName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AirControlAreaNameTest {

    @Test
    void valueOfCreatesName() {
        final AirControlAreaName name = AirControlAreaName.valueOf("Lisbon FIR");
        assertEquals("Lisbon FIR", name.getName());
    }
}
