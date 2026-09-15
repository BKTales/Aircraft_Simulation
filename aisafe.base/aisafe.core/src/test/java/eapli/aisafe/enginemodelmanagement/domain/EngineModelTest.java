package eapli.aisafe.enginemodelmanagement.domain;

import eapli.aisafe.manufacturermanagement.domain.CountryCode;
import eapli.aisafe.manufacturermanagement.domain.Manufacturer;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerName;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import static org.junit.jupiter.api.Assertions.*;

class EngineModelTest {

    @Test
    void ensureEngineModelIsCreatedAndSameAsWorks() {
        EngineModelId id = EngineModelId.valueOf("ENG01");
        final Manufacturer manufacturer = new Manufacturer(
                ManufacturerId.valueOf("MAN01"),
                new ManufacturerName("Acme"),
                new CountryCode("PT"));
        EngineModel subject = new EngineModel(
                id,
                EngineName.valueOf("Name"),
                TSFC.valueOf(0.5),
                FuelType.JET_A1,
                ThrustProfile.valueOf(100, 20),
                manufacturer,
                MotorizationType.TURBOFAN
        );

        assertEquals(id, subject.identity());
        assertEquals(EngineName.valueOf("Name"), subject.name());
        assertEquals(ManufacturerId.valueOf("MAN01"), subject.manufacturerId());
        assertEquals(MotorizationType.TURBOFAN, subject.motorization());
        assertEquals(FuelType.JET_A1, subject.fuelType());
        assertEquals(0.5, subject.tsfc().value(), 0.000001);
        assertEquals(100.0, subject.thrustProfile().thrustAtStatic(), 0.000001);
        assertTrue(subject.sameAs(subject));
        assertFalse(subject.sameAs(null));
        assertFalse(subject.sameAs(new Object()));
    }

    @Test
    void ensureConstructorValidatesAllParams() {
        assertThrows(IllegalArgumentException.class, () ->
                new EngineModel(null, null, null, null, null, null, null));
    }

    @Test
    void ensureProtectedConstructorForJPACoverage() throws Exception {
        Constructor<EngineModel> constructor = EngineModel.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertNotNull(constructor.newInstance());
    }

}