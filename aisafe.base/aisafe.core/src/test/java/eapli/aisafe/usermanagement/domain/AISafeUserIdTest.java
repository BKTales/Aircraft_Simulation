package eapli.aisafe.usermanagement.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AISafeUserIdTest {

    @Test
    void shouldCreateValidId() {
        AISafeUserId id = AISafeUserId.newId();

        assertNotNull(id);
        assertNotNull(id.id());
        assertFalse(id.id().isEmpty());
    }

    @Test
    void shouldGenerateDifferentIds() {
        AISafeUserId id1 = AISafeUserId.newId();
        AISafeUserId id2 = AISafeUserId.newId();

        assertNotEquals(id1, id2);
        assertNotEquals(id1.id(), id2.id());
    }

    @Test
    void shouldBeEqualToItself() {
        AISafeUserId id = AISafeUserId.newId();

        assertEquals(id, id);
        assertEquals(id.hashCode(), id.hashCode());
    }

    @Test
    void shouldNotBeEqualToNullOrOtherType() {
        AISafeUserId id = AISafeUserId.newId();

        assertNotEquals(id, null);
        assertNotEquals(id, "string");
    }

    @Test
    void shouldCompareDifferentIds() {
        AISafeUserId id1 = AISafeUserId.newId();
        AISafeUserId id2 = AISafeUserId.newId();

        assertNotEquals(0, id1.compareTo(id2));
    }
}