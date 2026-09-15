package eapli.aisafe.aircraftmodelmanagement.application;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.aircraftmodelmanagement.repositories.AircraftModelRepository;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListAircraftModelsControllerTest {

    @Mock private AuthorizationService authz;
    @Mock
    private AircraftModelRepository repo;

    @InjectMocks
    private ListAircraftModelsController controller;

    @Test
    void allAircraftModelsEnsuresAuthorizationAndDelegatesToRepository() {
        Iterable<AircraftModel> expected = List.of(mock(AircraftModel.class));
        when(repo.findAll()).thenReturn(expected);

        Iterable<AircraftModel> result = controller.allAircraftModels();

        assertSame(expected, result);
        verify(authz).ensureAuthenticatedUserHasAnyOf(any(Role[].class));
        verify(repo).findAll();
    }

    @Test
    void allAircraftModelsFailsWhenUnauthorized() {
        doThrow(new IllegalStateException("Unauthorized"))
                .when(authz).ensureAuthenticatedUserHasAnyOf(any(Role[].class));

        assertThrows(IllegalStateException.class, controller::allAircraftModels);

        verifyNoInteractions(repo);
    }

    @Test
    void constructorNullGuards() {
        assertThrows(IllegalArgumentException.class,
                () -> new ListAircraftModelsController(null, repo));
        assertThrows(IllegalArgumentException.class,
                () -> new ListAircraftModelsController(authz, null));
    }
}