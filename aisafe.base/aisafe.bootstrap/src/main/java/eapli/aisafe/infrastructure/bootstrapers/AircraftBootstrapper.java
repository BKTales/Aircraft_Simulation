package eapli.aisafe.infrastructure.bootstrapers;

import eapli.aisafe.aircraftmanagement.application.AircraftService;
import eapli.aisafe.aircraftmanagement.application.DuplicateAircraftRegistrationException;
import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.aisafe.aircraftmanagement.domain.CabinConfiguration;
import eapli.aisafe.aircraftmanagement.repositories.AircraftRepository;
import eapli.aisafe.aircraftmodelmanagement.application.AircraftModelNotFoundException;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.actions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Seeds demo aircraft in the fleet (US070), after models and engine certifications exist.
 * Includes extra TAP aircraft for US071 (decommission) and US072 (fleet list / filters).
 */
public class AircraftBootstrapper implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(AircraftBootstrapper.class);

    @Override
    public boolean execute() {
        final var aircraftRepo = PersistenceContext.repositories().aircraft();
        final var service = new AircraftService(
                aircraftRepo,
                PersistenceContext.repositories().aircraftModels());

        // A320 max 180 seats
        register(service,
                "CS-DEMO",
                "A320",
                "MAN02-PW1100G",
                CabinConfiguration.ofEconomyBusinessFirst(150, 30, 0),
                "PT",
                2,
                2021,
                IATACode.valueOf("TP"));

        register(service,
                "CS-FR01",
                "A320",
                "MAN02-PW1100G",
                CabinConfiguration.ofEconomyBusinessFirst(170, 10, 0),
                "PT",
                2,
                2014,
                IATACode.valueOf("FR"));

        // A321 max 200 seats
        register(service,
                "CS-LH01",
                "A321",
                "MAN02-PW1100G",
                CabinConfiguration.ofEconomyBusinessFirst(175, 20, 0),
                "DE",
                2,
                2019,
                IATACode.valueOf("LH"));

        // B737 max 189 seats
        register(service,
                "CS-VY01",
                "B737",
                "MAN04-GEnx",
                CabinConfiguration.ofEconomyBusinessFirst(165, 15, 0),
                "ES",
                2,
                2018,
                IATACode.valueOf("VY"));

        // A350 max 325 seats
        register(service,
                "CS-IB01",
                "A350",
                "MAN03-Trent700",
                CabinConfiguration.ofEconomyBusinessFirst(270, 40, 8),
                "ES",
                2,
                2020,
                IATACode.valueOf("IB"));

        // US071 — active TAP aircraft without pending flights (safe to decommission in demo)
        register(service,
                "CS-TP02",
                "A320",
                "MAN02-PW1100G",
                CabinConfiguration.ofEconomyBusinessFirst(120, 20, 0),
                "PT",
                2,
                2018,
                IATACode.valueOf("TP"));

        register(service,
                "CS-A380",
                "A380",
                "MAN03-Trent970",
                CabinConfiguration.ofEconomyBusinessFirst(400, 80, 14),
                "PT",
                2,
                2019,
                IATACode.valueOf("TP"));

        // US072 — older capacity / age; seeded as decommissioned to exercise full-fleet listing
        registerDecommissioned(service, aircraftRepo,
                "CS-TP03",
                "A320",
                "MAN02-PW1100G",
                CabinConfiguration.ofEconomyBusinessFirst(100, 0, 0),
                "PT",
                2,
                2008,
                IATACode.valueOf("TP"));

        return true;
    }

    private void register(final AircraftService service,
                          final String registration,
                          final String modelId,
                          final String engineModelId,
                          final CabinConfiguration cabin,
                          final String registrationCountryIso2,
                          final int flightCrewCount,
                          final int yearOfManufacture,
                          final IATACode ownerCompanyIata) {
        try {
            service.registerAircraft(registration, modelId, engineModelId, cabin,
                    registrationCountryIso2, flightCrewCount, yearOfManufacture, ownerCompanyIata);
        } catch (final DuplicateAircraftRegistrationException ex) {
            LOGGER.debug("Assuming aircraft already exists: {}", registration);
        } catch (final AircraftModelNotFoundException ex) {
            LOGGER.warn("Could not bootstrap aircraft {}: model '{}' not found (bootstrap aircraft models first).",
                    registration, modelId);
        } catch (final IllegalArgumentException ex) {
            LOGGER.warn("Could not bootstrap aircraft {}: {}", registration, ex.getMessage());
        }
    }

    private void registerDecommissioned(final AircraftService service,
                                        final AircraftRepository aircraftRepo,
                                        final String registration,
                                        final String modelId,
                                        final String engineModelId,
                                        final CabinConfiguration cabin,
                                        final String registrationCountryIso2,
                                        final int flightCrewCount,
                                        final int yearOfManufacture,
                                        final IATACode ownerCompanyIata) {
        try {
            final Aircraft created = service.registerAircraft(registration, modelId, engineModelId, cabin,
                    registrationCountryIso2, flightCrewCount, yearOfManufacture, ownerCompanyIata);
            created.retireFromActiveService();
            aircraftRepo.save(created);
        } catch (final DuplicateAircraftRegistrationException ex) {
            LOGGER.debug("Assuming aircraft already exists: {}", registration);
        } catch (final AircraftModelNotFoundException ex) {
            LOGGER.warn("Could not bootstrap aircraft {}: model '{}' not found (bootstrap aircraft models first).",
                    registration, modelId);
        } catch (final IllegalArgumentException ex) {
            LOGGER.warn("Could not bootstrap aircraft {}: {}", registration, ex.getMessage());
        }
    }
}
