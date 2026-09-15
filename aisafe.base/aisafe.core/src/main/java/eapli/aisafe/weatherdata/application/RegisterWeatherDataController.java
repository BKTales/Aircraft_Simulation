package eapli.aisafe.weatherdata.application;

import eapli.aisafe.aircontrolarea.application.AirControlAreaService;
import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.time.LocalDateTime;
import java.util.List;

public class RegisterWeatherDataController {

    private final AuthorizationService authz;
    private final WeatherDataService weatherService;
    private final AirControlAreaService areaService;
    private final TransactionalContext txCtx;

    public RegisterWeatherDataController() {
        this(createDefaultDependencies());
    }

    private static ControllerDependencies createDefaultDependencies() {
        final TransactionalContext txCtx = PersistenceContext.repositories().newTransactionalContext();
        return new ControllerDependencies(
                AuthzRegistry.authorizationService(),
                new WeatherDataService(
                        PersistenceContext.repositories().airControlArea(txCtx),
                        PersistenceContext.repositories().weatherData(txCtx)),
                new AirControlAreaService(),
                txCtx);
    }

    public RegisterWeatherDataController(final AuthorizationService authz,
                                         final WeatherDataService weatherService,
                                         final AirControlAreaService areaService) {
        this(authz, weatherService, areaService, null);
    }

    RegisterWeatherDataController(final AuthorizationService authz,
                                  final WeatherDataService weatherService,
                                  final AirControlAreaService areaService,
                                  final TransactionalContext txCtx) {
        if (authz == null || weatherService == null || areaService == null) {
            throw new IllegalArgumentException("Services are required.");
        }
        this.authz = authz;
        this.weatherService = weatherService;
        this.areaService = areaService;
        this.txCtx = txCtx;
    }

    private record ControllerDependencies(AuthorizationService authz,
                                          WeatherDataService weatherService,
                                          AirControlAreaService areaService,
                                          TransactionalContext txCtx) {
    }

    private RegisterWeatherDataController(final ControllerDependencies deps) {
        this(deps.authz(), deps.weatherService(), deps.areaService(), deps.txCtx());
    }

    public Iterable<AirControlArea> availableAreas() {
        return areaService.availableAreas();
    }

    public RegisterWeatherResult registerWeatherData(final String areaCode, final List<float[]> rawCoords,
                                                     final float temp, final int direction, final float speed,
                                                     final float hum, final float press,
                                                     final LocalDateTime start, final LocalDateTime end) {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.WEATHER_PERSON);
        return weatherService.registerWeatherData(areaCode, rawCoords, temp, direction, speed, hum, press, start, end, txCtx);
    }
}
