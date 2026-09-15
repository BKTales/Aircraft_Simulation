package eapli.aisafe.airportmanagement.application;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.aircontrolarea.domain.AirControlAreaName;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicBoundary;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicCoords;
import eapli.aisafe.aircontrolarea.domain.MinFuelRequirement;
import eapli.aisafe.aircontrolarea.repositories.InMemoryAirControlAreaRepository;
import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.airportmanagement.repositories.InMemoryAirportRepository;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateAirportControllerTest {

    private static final double LAT_INSIDE = 38.5;
    private static final double LON_INSIDE = -8.5;
    private static final double LAT_OUTSIDE = -50.0;
    private static final double LON_OUTSIDE = -50.0;

    private static class FakeAuthorizationService extends AuthorizationService {
        private final boolean shouldThrow;

        FakeAuthorizationService(final boolean shouldThrow) {
            this.shouldThrow = shouldThrow;
        }

        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... roles) {
            if(shouldThrow){
                throw new IllegalStateException("Unauthorized");
            }
        }
    }

    private InMemoryAirportRepository airportRepo;
    private InMemoryAirControlAreaRepository areaRepo;

    @BeforeEach
    void setUp() {
        airportRepo = new InMemoryAirportRepository();
        areaRepo = new InMemoryAirControlAreaRepository();

        final AirControlArea area = new AirControlArea(
                new AirControlAreaName("Test Area"),
                new GeographicBoundary(List.of(
                        new GeographicCoords(38.0f, -9.0f),
                        new GeographicCoords(39.0f, -9.0f),
                        new GeographicCoords(39.0f, -8.0f),
                        new GeographicCoords(38.0f, -8.0f))),
                new MinFuelRequirement(250.0f));
        areaRepo.save(area);
    }

    private CreateAirportController buildController(final boolean unauthorized) {
        final FakeAuthorizationService authz = new FakeAuthorizationService(unauthorized);
        final AirportService service = new AirportService(airportRepo, areaRepo);
        return new CreateAirportController(authz, service);
    }

    @Test
    void ensureAirportIsCreatedWhenCoordinatesAreInsideArea() {
        final CreateAirportController controller = buildController(false);

        final Airport airport = controller.createAirport("TST", "TSTA", LAT_INSIDE, LON_INSIDE, 100.0);

        assertNotNull(airport);
    }

    @Test
    void ensureAirportAreaIsAutoAssignedFromCoordinates() {
        final CreateAirportController controller = buildController(false);

        final Airport airport = controller.createAirport("ASN", "ASNA", LAT_INSIDE, LON_INSIDE, 100.0);

        assertNotNull(airport.airControlAreaCode());
    }

    @Test
    void ensureCoordinatesOutsideAllAreasThrows() {
        final CreateAirportController controller = buildController(false);

        assertThrows(NoAreaFoundForCoordinatesException.class, new Executable() {
            @Override
            public void execute() {
                controller.createAirport("OUT", "OUTA", LAT_OUTSIDE, LON_OUTSIDE, 0.0);
            }
        });
    }

    @Test
    void ensureUnauthorizedUserCannotCreateAirport() {
        final CreateAirportController controller = buildController(true);

        assertThrows(IllegalStateException.class, new Executable() {
            @Override
            public void execute() {
                controller.createAirport("UAT", "UATX", LAT_INSIDE, LON_INSIDE, 100.0);
            }
        });
    }

    @Test
    void ensureConstructorRejectsNullAuthz() {
        final AirportService service = new AirportService(airportRepo, areaRepo);

        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                new CreateAirportController(null, service);
            }
        });
    }

    @Test
    void ensureConstructorRejectsNullService() {
        final FakeAuthorizationService authz = new FakeAuthorizationService(false);

        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                new CreateAirportController(authz, null);
            }
        });
    }

    @Test
    void ensureDuplicateIataCodeThrows() {
        final CreateAirportController controller = buildController(false);
        controller.createAirport("DIA", "DIA1", LAT_INSIDE, LON_INSIDE, 100.0);

        assertThrows(AirportIATACodeAlreadyExistsException.class, new Executable() {
            @Override
            public void execute() {
                controller.createAirport("DIA", "DIA2", LAT_INSIDE, LON_INSIDE, 100.0);
            }
        });
    }

    @Test
    void ensureDuplicateIcaoCodeThrows() {
        final CreateAirportController controller = buildController(false);
        controller.createAirport("DIC", "DICO", LAT_INSIDE, LON_INSIDE, 100.0);

        assertThrows(AirportICAOCodeAlreadyExistsException.class, new Executable() {
            @Override
            public void execute() {
                controller.createAirport("DID", "DICO", LAT_INSIDE, LON_INSIDE, 100.0);
            }
        });
    }
}
