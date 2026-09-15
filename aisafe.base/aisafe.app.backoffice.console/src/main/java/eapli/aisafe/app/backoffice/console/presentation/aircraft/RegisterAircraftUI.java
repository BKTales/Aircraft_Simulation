package eapli.aisafe.app.backoffice.console.presentation.aircraft;

import eapli.aisafe.aircraftmanagement.application.DuplicateAircraftRegistrationException;
import eapli.aisafe.aircraftmanagement.application.RegisterAircraftController;
import eapli.aisafe.aircraftmodelmanagement.application.AircraftModelNotFoundException;
import eapli.aisafe.aircraftmodelmanagement.application.ListAircraftModelsController;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.aircraftmodelmanagement.domain.EngineConfiguration;
import eapli.aisafe.enginemodelmanagement.domain.EngineModelId;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.util.List;
import java.util.stream.Collectors;

/**
 * US070 — add an aircraft to the collaborator's company fleet.
 * Flow: registration → aircraft model → one certified engine model (same type for every engine position on that model)
 * → cabin seats (total ≤ model capacity) → registration country → flight crew.
 * Operational status is {@link eapli.aisafe.aircraftmanagement.domain.OperationalStatus#ACTIVE} by default (set in application layer, not prompted).
 */
@SuppressWarnings("squid:S106")
public class RegisterAircraftUI extends AbstractUI {

    private final RegisterAircraftController controller = new RegisterAircraftController();
    private final ListAircraftModelsController aircraftModels = new ListAircraftModelsController();

    @Override
    protected boolean doShow() {
        final String registration = Console.readLine(
                "Aircraft registration (tail number, 3-14 characters: A-Z, 0-9, hyphen; e.g. CS-TST)");

        final AircraftModel model = chooseModel();
        if (model == null) {
            return false;
        }

        final EngineModelId engineModelId = chooseCertifiedEngine(model);
        if (engineModelId == null) {
            return false;
        }

        final int economy = Console.readInteger("Economy seats");
        final int business = Console.readInteger("Business seats");
        final int firstClass = Console.readInteger("First class seats");

        final String country = Console.readLine("Registration country (ISO 3166-1 alpha-2, e.g. PT)");

        final int crew = Console.readInteger("Number of flight crew");
        final int yearOfManufacture = Console.readInteger("Year of manufacture (e.g. 2015)");

        try {
            controller.registerAircraft(registration, model.identity().toString(), engineModelId.toString(),
                    economy, business, firstClass, country, crew, yearOfManufacture);
            System.out.println("Aircraft registered successfully. Operational status: Active.");
        } catch (final DuplicateAircraftRegistrationException | AircraftModelNotFoundException ex) {
            System.out.println(ex.getMessage());
        } catch (final IllegalArgumentException | IllegalStateException ex) {
            System.out.println(ex.getMessage());
        }

        return false;
    }

    private AircraftModel chooseModel() {
        final Iterable<AircraftModel> all = aircraftModels.allAircraftModels();
        if (!all.iterator().hasNext()) {
            throw new IllegalStateException("No aircraft models registered.");
        }
        System.out.println("Select aircraft model:");
        RegisterAircraftPrinter.printAircraftModelsTableHeaderWithRule();
        final SelectWidget<AircraftModel> selector =
                new SelectWidget<>("", all, new RegisterAircraftPrinter());
        selector.show();
        return selector.selectedElement();
    }

    private EngineModelId chooseCertifiedEngine(final AircraftModel model) {
        final List<EngineModelId> certified = model.engineCertifiedConfigurations().stream()
                .filter(cfg -> cfg != null && cfg.engineModel() != null)
                .map(cfg -> cfg.engineModel().identity())
                .distinct()
                .collect(Collectors.toList());
        if (certified.isEmpty()) {
            throw new IllegalStateException("This aircraft model has no certified engine models.");
        }
        System.out.println(
                "Select engine model (certified for this aircraft model). One choice — same engine type for all "
                        + model.numberOfEngines() + " engine position(s):");
        RegisterAircraftPrinter.printCertifiedEngineModelIdsHeaderWithRule();
        final SelectWidget<EngineModelId> selector = new SelectWidget<>(
                "", certified, new RegisterAircraftPrinter.CertifiedEngineModelIdPrinter());
        selector.show();
        return selector.selectedElement();
    }

    @Override
    public String headline() {
        return "Register Aircraft (US070)";
    }
}
