package eapli.aisafe.enginemodelmanagement.domain;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import static org.junit.jupiter.api.Assertions.*;

class EngineModelIdTest {

    @Test
    void ensureEngineModelIdIsCreatedAndReturnsValue() {
        String code = "CFM56-7B";
        EngineModelId subject = EngineModelId.valueOf(code);
        assertEquals(code, subject.toString());
    }

    @Test
    void ensureConstructorThrowsExceptionForInvalidInputs() {
        assertThrows(IllegalArgumentException.class, () -> EngineModelId.valueOf(null));
        assertThrows(IllegalArgumentException.class, () -> EngineModelId.valueOf(""));
        assertThrows(IllegalArgumentException.class, () -> EngineModelId.valueOf("   "));
    }

    @Test
    void ensureEqualsAndHashCodeWork() {
        EngineModelId id1 = EngineModelId.valueOf("A");
        EngineModelId id2 = EngineModelId.valueOf("A");
        EngineModelId id3 = EngineModelId.valueOf("B");


        assertEquals(id1, id2);
        assertNotEquals(id1, id3);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void ensureCompareToWorks() {
        EngineModelId a = EngineModelId.valueOf("A");
        EngineModelId b = EngineModelId.valueOf("B");
        assertTrue(a.compareTo(b) < 0);
        assertEquals(0, a.compareTo(EngineModelId.valueOf("A")));
    }

    @Test
    void ensureProtectedConstructorForJPACoverage() throws Exception {
        Constructor<EngineModelId> constructor = EngineModelId.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertNotNull(constructor.newInstance());
    }


}