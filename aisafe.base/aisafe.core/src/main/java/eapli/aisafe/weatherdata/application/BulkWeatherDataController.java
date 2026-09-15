package eapli.aisafe.weatherdata.application;

import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.aisafe.weatherdata.application.bulk.WeatherDataBulkReaderFactory;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.nio.file.Path;
import java.time.LocalDateTime;

public class BulkWeatherDataController {

    private final AuthorizationService authz;
    private final WeatherDataService weatherService;
    private final WeatherDataBulkReaderFactory readerFactory;
    private final TransactionalContext txCtx;

    public BulkWeatherDataController() {
        this(createDefaultDependencies());
    }

    private static ControllerDependencies createDefaultDependencies() {
        final TransactionalContext txCtx = PersistenceContext.repositories().newTransactionalContext();
        return new ControllerDependencies(
                AuthzRegistry.authorizationService(),
                new WeatherDataService(
                        PersistenceContext.repositories().airControlArea(txCtx),
                        PersistenceContext.repositories().weatherData(txCtx)),
                new WeatherDataBulkReaderFactory(),
                txCtx);
    }

    public BulkWeatherDataController(final AuthorizationService authz,
                                     final WeatherDataService weatherService,
                                     final WeatherDataBulkReaderFactory readerFactory) {
        this(authz, weatherService, readerFactory, null);
    }

    BulkWeatherDataController(final AuthorizationService authz,
                              final WeatherDataService weatherService,
                              final WeatherDataBulkReaderFactory readerFactory,
                              final TransactionalContext txCtx) {
        if (authz == null || weatherService == null || readerFactory == null) {
            throw new IllegalArgumentException("Dependencies are required.");
        }
        this.authz = authz;
        this.weatherService = weatherService;
        this.readerFactory = readerFactory;
        this.txCtx = txCtx;
    }

    private record ControllerDependencies(AuthorizationService authz,
                                          WeatherDataService weatherService,
                                          WeatherDataBulkReaderFactory readerFactory,
                                          TransactionalContext txCtx) {
    }

    private BulkWeatherDataController(final ControllerDependencies deps) {
        this(deps.authz(), deps.weatherService(), deps.readerFactory(), deps.txCtx());
    }

    public BulkImportWeatherResult importFromFile(final Path file) {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.WEATHER_PERSON);
        return weatherService.importFromFile(file, readerFactory, txCtx);
    }

    public ConsultWeatherResult consultWeatherDataForDay(final String areaCode, final LocalDateTime day) {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.WEATHER_PERSON);
        return weatherService.consultWeatherDataForDay(areaCode, day);
    }
}
