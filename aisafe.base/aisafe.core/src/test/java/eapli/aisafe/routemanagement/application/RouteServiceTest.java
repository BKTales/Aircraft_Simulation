package eapli.aisafe.routemanagement.application;

import eapli.aisafe.airportmanagement.domain.AirportIATACode;
import eapli.aisafe.airportmanagement.repositories.AirportRepository;
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.routemanagement.RouteTestFixtures;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.aisafe.routemanagement.domain.RouteName;
import eapli.aisafe.routemanagement.repositories.RouteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    private RouteRepository routes;
    @Mock
    private AirportRepository airports;
    @Mock
    private AirTransportCompany company;

    @Test
    void constructorRejectsNullRepositories() {
        assertThrows(IllegalArgumentException.class, () -> new RouteService(null, airports));
        assertThrows(IllegalArgumentException.class, () -> new RouteService(routes, null));
    }

    @Test
    void validateRouteNameReturnsNormalizedValue() {
        when(routes.existsByName(RouteName.valueOf("TP123"))).thenReturn(false);
        final RouteService service = new RouteService(routes, airports);

        assertEquals("TP123", service.validateRouteName("tp123"));
    }

    @Test
    void validateRouteNameFailsWhenDuplicate() {
        when(routes.existsByName(RouteName.valueOf("TP123"))).thenReturn(true);
        final RouteService service = new RouteService(routes, airports);

        assertThrows(RouteAlreadyExistsException.class, () -> service.validateRouteName("TP123"));
    }

    @Test
    void createCharterRoutePersistsWhenValid() {
        final RouteService service = new RouteService(routes, airports);
        when(routes.existsByName(RouteName.valueOf("TP123"))).thenReturn(false);
        when(airports.ofIdentity(AirportIATACode.valueOf("OPO"))).thenReturn(Optional.of(RouteTestFixtures.AIRPORT_OPO));
        when(airports.ofIdentity(AirportIATACode.valueOf("LIS"))).thenReturn(Optional.of(RouteTestFixtures.AIRPORT_LIS));
        when(routes.save(any(Route.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final Route created = service.createCharterRoute(
                "TP123", "OPO", "LIS", RouteTestFixtures.COMPANY_TP,
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2));

        assertNotNull(created);
        assertEquals("TP123", created.identity().toString());

        final ArgumentCaptor<Route> captor = ArgumentCaptor.forClass(Route.class);
        verify(routes).save(captor.capture());
        assertEquals(RouteTestFixtures.COMPANY_TP, captor.getValue().airTransportCompany());
    }

    @Test
    void createRegularRoutePersistsWhenValid() {
        final RouteService service = new RouteService(routes, airports);
        when(routes.existsByName(RouteName.valueOf("TP100"))).thenReturn(false);
        when(airports.ofIdentity(AirportIATACode.valueOf("OPO"))).thenReturn(Optional.of(RouteTestFixtures.AIRPORT_OPO));
        when(airports.ofIdentity(AirportIATACode.valueOf("LIS"))).thenReturn(Optional.of(RouteTestFixtures.AIRPORT_LIS));
        when(routes.save(any(Route.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final Route created = service.createRegularRoute(
                "TP100", "OPO", "LIS", RouteTestFixtures.COMPANY_TP, List.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY));

        assertNotNull(created);
        assertEquals(2, created.routeRecurringSchedule().entries().size());
        verify(routes).save(created);
    }

    @Test
    void createRouteFailsWhenNameExists() {
        final RouteService service = new RouteService(routes, airports);
        when(routes.existsByName(RouteName.valueOf("TP123"))).thenReturn(true);

        assertThrows(RouteAlreadyExistsException.class,
                () -> service.createCharterRoute("TP123", "OPO", "LIS", RouteTestFixtures.COMPANY_TP,
                        LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2)));
    }

    @Test
    void createRouteFailsWhenNamePrefixDoesNotMatchCompany() {
        final RouteService service = new RouteService(routes, airports);

        assertThrows(IllegalArgumentException.class,
                () -> service.createCharterRoute("FR123", "OPO", "LIS", RouteTestFixtures.COMPANY_TP,
                        LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2)));
    }

    @Test
    void createRouteFailsWhenOriginAndDestinationAreEqual() {
        final RouteService service = new RouteService(routes, airports);
        when(routes.existsByName(RouteName.valueOf("TP123"))).thenReturn(false);
        when(airports.ofIdentity(AirportIATACode.valueOf("OPO"))).thenReturn(Optional.of(RouteTestFixtures.AIRPORT_OPO));

        assertThrows(IllegalArgumentException.class,
                () -> service.createRegularRoute("TP123", "OPO", "OPO", RouteTestFixtures.COMPANY_TP, List.of(DayOfWeek.MONDAY)));
    }

    @Test
    void createRouteFailsWhenAirportMissing() {
        final RouteService service = new RouteService(routes, airports);
        when(routes.existsByName(RouteName.valueOf("TP123"))).thenReturn(false);
        when(airports.ofIdentity(AirportIATACode.valueOf("OPO"))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.createCharterRoute("TP123", "OPO", "LIS", RouteTestFixtures.COMPANY_TP,
                        LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2)));
    }

    @Test
    void createRegularRouteFailsWhenNoRecurringDays() {
        final RouteService service = new RouteService(routes, airports);

        assertThrows(IllegalArgumentException.class,
                () -> service.createRegularRoute("TP123", "OPO", "LIS", RouteTestFixtures.COMPANY_TP, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> service.createRegularRoute("TP123", "OPO", "LIS", RouteTestFixtures.COMPANY_TP, null));
    }

    @Test
    void createRouteFailsWhenCompanyNull() {
        final RouteService service = new RouteService(routes, airports);

        assertThrows(NullPointerException.class,
                () -> service.createCharterRoute("TP123", "OPO", "LIS", null,
                        LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2)));
    }

    @Test
    void listAirportsDelegatesToRepository() {
        final RouteService service = new RouteService(routes, airports);
        when(airports.findAll()).thenReturn(List.of(RouteTestFixtures.AIRPORT_OPO));

        assertEquals(List.of(RouteTestFixtures.AIRPORT_OPO), service.listAirports());
    }
}
