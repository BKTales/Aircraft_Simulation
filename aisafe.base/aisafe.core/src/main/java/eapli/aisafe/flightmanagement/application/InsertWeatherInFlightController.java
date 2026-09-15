package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.flightmanagement.domain.Flight;
import eapli.aisafe.flightmanagement.domain.FlightDesignator;
import eapli.aisafe.flightmanagement.repositories.FlightRepository;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.aisafe.weatherdata.domain.WeatherData;
import eapli.aisafe.weatherdata.repositories.WeatherDataRepository;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.util.Optional;

@UseCaseController
public class InsertWeatherInFlightController {
    private final AuthorizationService authz;
    private final FlightRepository flightRepository;
    private final WeatherDataRepository weatherDataRepository;

    public InsertWeatherInFlightController() {
        this(AuthzRegistry.authorizationService(),
                PersistenceContext.repositories().flights(),
                PersistenceContext.repositories().weatherData());
    }

    InsertWeatherInFlightController(final AuthorizationService authz,
                                    final FlightRepository flightRepository,
                                    final WeatherDataRepository weatherDataRepository) {
        if (authz == null || flightRepository == null || weatherDataRepository == null) {
            throw new IllegalArgumentException("Dependencies are required.");
        }
        this.authz = authz;
        this.flightRepository = flightRepository;
        this.weatherDataRepository = weatherDataRepository;
    }

    public Flight attachWeatherToFlight(final String flightDesignator, final Long weatherDataId) {
        authz.ensureAuthenticatedUserHasAnyOf(
                AISafeRoles.PILOT
        );
        final Flight flight = flightRepository.findByDesignator(new FlightDesignator(flightDesignator))
                .orElseThrow(() -> new IllegalArgumentException("Flight not found: " + flightDesignator));
        final WeatherData weatherData = weatherDataRepository.ofIdentity(weatherDataId)
                .orElseThrow(() -> new IllegalArgumentException("Weather data not found: " + weatherDataId));
        flight.assignWeatherData(weatherData);
        return flightRepository.save(flight);
    }

    public Optional<WeatherData> weatherDataById(final Long weatherDataId) {
        authz.ensureAuthenticatedUserHasAnyOf(
                AISafeRoles.PILOT
        );
        return weatherDataRepository.ofIdentity(weatherDataId);
    }
}
