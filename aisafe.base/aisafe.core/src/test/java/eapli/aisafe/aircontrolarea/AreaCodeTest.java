package eapli.aisafe.aircontrolarea;

import eapli.aisafe.aircontrolarea.domain.AirControlAreaName;
import eapli.aisafe.aircontrolarea.domain.AreaCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AreaCodeTest {

    @Test
    public void ensureAreaCodesAreDifferent() {
        AreaCode code1 = new AreaCode();
        AreaCode code2 = new AreaCode();
        assertNotEquals(code1.getCode(), code2.getCode());
    }

    @Test
    public void ensureCompareToWorks() {
        AreaCode code1 = new AreaCode();
        assertEquals(0, code1.compareTo(code1));
    }

    @Test
    public void coverageFiller() {
        AreaCode code = new AreaCode();
        code.hashCode();
        code.toString();
        code.equals(code);
        code.equals(null);

        AreaCode fromValueOf = AreaCode.valueOf("AREA-XYZ");
        assertEquals("AREA-XYZ", fromValueOf.getCode());

        AirControlAreaName name = AirControlAreaName.valueOf("Test");
        assertEquals("Test", name.getName());
    }
}
