package eapli.aisafe.enginemodelmanagement.domain;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import static org.junit.jupiter.api.Assertions.*;

class EngineNameTest {

    @Test
    void ensureEngineNameIsCreated() {
        String name = "Trent 700";
        EngineName subject = EngineName.valueOf(name);
        assertEquals(name, subject.toString());
    }

    @Test
    void ensureConstructorValidatesInput() {
        assertThrows(IllegalArgumentException.class, () -> EngineName.valueOf(null));
        assertThrows(IllegalArgumentException.class, () -> EngineName.valueOf(""));
    }

    @Test
    void ensureEqualsAndHashCodeWork() {
        EngineName n1 = EngineName.valueOf("X");
        EngineName n2 = EngineName.valueOf("X");
        assertEquals(n1, n2);
        assertEquals(n1.hashCode(), n2.hashCode());
    }

    @Test
    void ensureProtectedConstructorForJPACoverage() throws Exception {
        Constructor<EngineName> constructor = EngineName.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertNotNull(constructor.newInstance());
    }

}