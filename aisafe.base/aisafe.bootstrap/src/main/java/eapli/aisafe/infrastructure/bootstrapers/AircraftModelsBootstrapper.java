package eapli.aisafe.infrastructure.bootstrapers;

import eapli.aisafe.aircraftmodelmanagement.application.AircraftModelAlreadyExistsException;
import eapli.aisafe.aircraftmodelmanagement.application.AircraftModelService;
import eapli.aisafe.aircraftmodelmanagement.application.EngineModelNotFoundException;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftType;
import eapli.aisafe.enginemodelmanagement.application.ManufacturerNotFoundException;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.actions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AircraftModelsBootstrapper implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(AircraftModelsBootstrapper.class);

    @Override
    public boolean execute() {
        final var service = new AircraftModelService(
                PersistenceContext.repositories().aircraftModels(),
                PersistenceContext.repositories().manufacturers(),
                PersistenceContext.repositories().engineModels());

        register(service,
                "A320",
                "Airbus A320",
                AircraftType.PASSENGER,
                "MAN02",
                78000, 60000, 42000,
                122.6, 35.8,
                0.02, 1.5,
                12000, 230, 24000, 5200,
                180,
                2,
                List.of("MAN02-PW1100G"));

        register(service,
                "A321",
                "Airbus A321",
                AircraftType.PASSENGER,
                "MAN02",
                89000, 68000, 48000,
                128.0, 36.0,
                0.021, 1.48,
                12500, 235, 26000, 5400,
                200,
                2,
                List.of("MAN02-PW1100G"));

        register(service,
                "B737",
                "Boeing 737-800",
                AircraftType.PASSENGER,
                "MAN04",
                79000, 61000, 41400,
                125.0, 35.8,
                0.022, 1.52,
                12500, 225, 26000, 5000,
                189,
                2,
                List.of("MAN04-GEnx"));

        register(service,
                "A350",
                "Airbus A350-900",
                AircraftType.PASSENGER,
                "MAN03",
                280000, 220000, 142000,
                440.0, 64.8,
                0.018, 1.6,
                13100, 250, 138000, 15000,
                325,
                2,
                List.of("MAN03-Trent700"));

        register(service,
                "ATR72",
                "ATR 72-600",
                AircraftType.PASSENGER,
                "MAN05",
                23000, 19500, 13000,
                61.0, 27.1,
                0.028, 1.4,
                7600, 185, 5500, 1500,
                78,
                2,
                List.of("MAN01-CFM56-7B"));

        register(service,
                "A380",
                "Airbus A380",
                AircraftType.PASSENGER,
                "MAN02",
                575000, 340800, 276800,
                845.0, 79.75,
                0.020, 1.6,
                13100, 250, 320000, 15000,
                500,
                4,
                List.of("MAN03-Trent970"));

        return true;
    }

    private void register(final AircraftModelService service,
                          final String modelId,
                          final String modelName,
                          final AircraftType aircraftType,
                          final String manufacturerId,
                          final double mtow,
                          final double mzfw,
                          final double emptyWeight,
                          final double wingArea,
                          final double wingSpan,
                          final double cd0,
                          final double cl,
                          final double serviceCeiling,
                          final double cruiseSpeed,
                          final double fuelCapacity,
                          final double maxRange,
                          final int maxPassengerSeats,
                          final int numberOfEngines,
                          final List<String> engineModelIds) {
        try {
            service.createAircraftModel(modelId, modelName, aircraftType, manufacturerId,
                    mtow, mzfw, emptyWeight, wingArea, wingSpan, cd0, cl,
                    serviceCeiling, cruiseSpeed, fuelCapacity, maxRange,
                    maxPassengerSeats, numberOfEngines, engineModelIds);
        } catch (final AircraftModelAlreadyExistsException ex) {
            LOGGER.debug("Assuming aircraft model already exists: {} / {}", modelId, manufacturerId);
        } catch (final ManufacturerNotFoundException | EngineModelNotFoundException | IllegalArgumentException ex) {
            LOGGER.warn("Could not bootstrap aircraft model {} / {}: {}", modelId, manufacturerId, ex.getMessage());
        }
    }
}
