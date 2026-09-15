package eapli.aisafe.airportmanagement.application;

import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.airportmanagement.domain.AirportICAOCode;
import eapli.aisafe.airportmanagement.domain.AirportIATACode;
import eapli.aisafe.airportmanagement.domain.Coordinates;
import eapli.aisafe.airportmanagement.repositories.AirportRepository;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListAirportsControllerTest {

    private static class FakeAuthorizationService extends AuthorizationService {
        private final boolean deny;

        FakeAuthorizationService(final boolean deny) {
            this.deny = deny;
        }

        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... roles) {
            if (deny) {
                throw new IllegalStateException("Unauthorized");
            }
        }
    }

    @Mock
    private AirportRepository repository;

    @Test
    void constructorRejectsNullAuthz() {
        assertThrows(IllegalArgumentException.class,
                () -> new ListAirportsController(null, repository));
    }

    @Test
    void constructorRejectsNullRepository() {
        assertThrows(IllegalArgumentException.class,
                () -> new ListAirportsController(new FakeAuthorizationService(false), null));
    }

    @Test
    void allAirportsReturnsSavedAirportsWhenAuthorized() {
        final Airport airport = new Airport(
                AirportIATACode.valueOf("LIS"),
                AirportICAOCode.valueOf("LPPT"),
                Coordinates.valueOf(0, 0, 10),
                new AreaCode());
        when(repository.findAll()).thenReturn(List.of(airport));

        final ListAirportsController controller =
                new ListAirportsController(new FakeAuthorizationService(false), repository);

        final List<Airport> out = new ArrayList<>();
        controller.allAirports().forEach(out::add);
        assertEquals(1, out.size());
        assertEquals(airport, out.get(0));
        verify(repository).findAll();
    }

    @Test
    void allAirportsUnauthorizedThrows() {
        final ListAirportsController controller =
                new ListAirportsController(new FakeAuthorizationService(true), repository);

        assertThrows(IllegalStateException.class, controller::allAirports);
    }

    @Test
    void allAirportsReturnsEmptyWhenNoData() {
        when(repository.findAll()).thenReturn(List.of());

        final ListAirportsController controller =
                new ListAirportsController(new FakeAuthorizationService(false), repository);

        final List<Airport> out = new ArrayList<>();
        controller.allAirports().forEach(out::add);
        assertTrue(out.isEmpty());
    }
}
