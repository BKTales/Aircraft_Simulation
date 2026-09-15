package eapli.aisafe.aircraftmodelmanagement.application;

import eapli.aisafe.aircraftmodelmanagement.domain.*;
import eapli.aisafe.enginemodelmanagement.domain.*;
import eapli.aisafe.manufacturermanagement.domain.*;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
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
class CreateAircraftModelControllerTest {

    @Mock
    private AuthorizationService authz;
    @Mock private AircraftModelService service;

    @InjectMocks
    private CreateAircraftModelController controller;

    @Test
    void ensureCreateAircraftModelDelegatesToServiceCorrectly() {
        AircraftModel expected = mock(AircraftModel.class);
        when(service.createAircraftModel(any(), any(), any(), any(),
                anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                anyDouble(), anyInt(), anyInt(), any()))
                .thenReturn(expected);

        AircraftModel result = controller.createAircraftModel("A320", "Airbus A320", AircraftType.PASSENGER,
                "MAN01", 78000, 60000, 42000, 122.6, 35.8, 0.02, 1.5,
                12000, 230, 24000, 5200, 180, 2, List.of("ENG1"));

        assertSame(expected, result);
        verify(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.ADMIN, AISafeRoles.BACKOFFICE_OPERATOR);
        verify(service).createAircraftModel(eq("A320"), any(), any(), any(),
                anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                anyDouble(), anyInt(), anyInt(), any());
    }

    @Test
    void ensureCreateAircraftModelFailsWhenUnauthorized() {
        doThrow(new IllegalStateException("Unauthorized"))
                .when(authz).ensureAuthenticatedUserHasAnyOf(any(Role[].class));

        assertThrows(IllegalStateException.class,
                () -> controller.createAircraftModel("A1", "N", AircraftType.PASSENGER,
                        "M1", 1, 1, 1, 1, 1, 0.1, 1, 1, 1, 1, 1, 1, 1, List.of("E1")));

        verifyNoInteractions(service);
    }

    @Test
    void ensureConstructorNullGuardsWork() {
        assertThrows(IllegalArgumentException.class,
                () -> new CreateAircraftModelController(null, service));
        assertThrows(IllegalArgumentException.class,
                () -> new CreateAircraftModelController(authz, null));
    }

    @Test
    void ensureServiceExceptionsArePropagated() {
        doThrow(new AircraftModelAlreadyExistsException("Conflict"))
                .when(service).createAircraftModel(any(), any(), any(), any(),
                        anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                        anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                        anyDouble(), anyInt(), anyInt(), any());

        assertThrows(AircraftModelAlreadyExistsException.class,
                () -> controller.createAircraftModel("A1", "N", AircraftType.PASSENGER,
                        "M1", 1, 1, 1, 1, 1, 0.1, 1, 1, 1, 1, 1, 1, 1, List.of("E1")));

    }
}