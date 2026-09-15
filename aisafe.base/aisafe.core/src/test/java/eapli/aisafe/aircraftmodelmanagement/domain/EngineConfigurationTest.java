package eapli.aisafe.aircraftmodelmanagement.domain;

import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.aisafe.enginemodelmanagement.domain.EngineModelId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EngineConfigurationTest {

    @Test
    void ensureEngineConfigurationIsCreated() {
        final EngineModel engineModel = mock(EngineModel.class);
        final EngineConfiguration subject = new EngineConfiguration(engineModel);

        assertNotNull(subject);
        assertEquals(engineModel, subject.engineModel());
    }

    @Test
    void ensureConstructorThrowsExceptionOnNullEngineModel() {
        assertThrows(IllegalArgumentException.class, () -> new EngineConfiguration(null));
    }

    @Test
    void ensureSameAsWorksCorrectly() {
        final EngineModelId id1 = EngineModelId.valueOf("ENG-01");
        final EngineModelId id2 = EngineModelId.valueOf("ENG-02");

        final EngineModel model1 = mock(EngineModel.class);
        when(model1.identity()).thenReturn(id1);

        final EngineModel sameModel = mock(EngineModel.class);
        when(sameModel.identity()).thenReturn(id1);

        final EngineModel differentModel = mock(EngineModel.class);
        when(differentModel.identity()).thenReturn(id2);

        final EngineConfiguration subject = new EngineConfiguration(model1);
        final EngineConfiguration same = new EngineConfiguration(sameModel);
        final EngineConfiguration different = new EngineConfiguration(differentModel);

        assertFalse(subject.sameAs(same));
        assertFalse(subject.sameAs(different));
        assertFalse(subject.sameAs(null));
        assertFalse(subject.sameAs(new Object()));
    }

    @Test
    void ensureIdentityReturnsId() {

        final EngineConfiguration subject = new EngineConfiguration(mock(EngineModel.class));
        assertNull(subject.identity());
    }

    @Test
    void ensureProtectedConstructorExistsForORM() {
        final EngineConfiguration subject = new EngineConfiguration() {};
        assertNotNull(subject);
    }
}