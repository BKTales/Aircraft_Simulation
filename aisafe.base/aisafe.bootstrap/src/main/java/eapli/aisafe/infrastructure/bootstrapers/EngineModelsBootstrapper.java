package eapli.aisafe.infrastructure.bootstrapers;

import eapli.aisafe.enginemodelmanagement.application.EngineModelAlreadyExistsException;
import eapli.aisafe.enginemodelmanagement.application.EngineModelService;
import eapli.aisafe.enginemodelmanagement.application.ManufacturerNotFoundException;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.actions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EngineModelsBootstrapper implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(EngineModelsBootstrapper.class);

    @Override
    public boolean execute() {
        final var service = new EngineModelService(
                PersistenceContext.repositories().engineModels(),
                PersistenceContext.repositories().manufacturers());

        register(service, "CFM56-7B", "MAN01", "turbofan", 121.0, 103.0, "JET_A1", 0.55);
        register(service, "PW1100G", "MAN02", "turbofan", 150.0, 135.0, "JET_A1", 0.50);
        register(service, "Trent700", "MAN03", "turbofan", 320.0, 280.0, "JET_A1", 0.58);
        register(service, "LEAP-1A", "MAN01", "turbofan", 130.0, 115.0, "JET_A1", 0.52);
        register(service, "GEnx", "MAN04", "turbofan", 515.0, 450.0, "JET_A1", 0.57);
        register(service, "Trent970", "MAN03", "turbofan", 348.0, 300.0, "JET_A1", 0.58);
        return true;
    }

    private void register(final EngineModelService service,
                          final String name, final String man,
                          final String mot, final double ts, final double tc,
                          final String fuel, final double tsfc) {
        try {
            service.createEngineModel(name, man, mot, ts, tc, fuel, tsfc);
        } catch (final EngineModelAlreadyExistsException ex) {
            LOGGER.debug("Assuming engine model already exists: {} / {}", name, man);
        } catch (final ManufacturerNotFoundException | IllegalArgumentException ex) {
            LOGGER.warn("Could not bootstrap engine model {} / {}: {}", name, man, ex.getMessage());
        }
    }
}
