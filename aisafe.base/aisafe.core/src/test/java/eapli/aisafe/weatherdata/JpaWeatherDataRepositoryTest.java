package eapli.aisafe.weatherdata;

import eapli.aisafe.weatherdata.repositories.JpaWeatherDataRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class JpaWeatherDataRepositoryTest {

    @Test
    void testConstructorWithTransactionalContext() {
        TransactionalContext tx = mock(TransactionalContext.class);
        assertThrows(IllegalArgumentException.class, () -> new JpaWeatherDataRepository(tx));
    }

    @Test
    void testConstructorWithPersistenceUnitName() {
        assertThrows(PersistenceException.class, () -> new JpaWeatherDataRepository("aisafe.persistence"));
    }
}
