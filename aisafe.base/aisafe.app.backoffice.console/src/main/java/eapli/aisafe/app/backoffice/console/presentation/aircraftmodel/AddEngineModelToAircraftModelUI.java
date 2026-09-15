package eapli.aisafe.app.backoffice.console.presentation.aircraftmodel;

import eapli.aisafe.aircraftmodelmanagement.application.AddEngineModelToAircraftModelController;
import eapli.aisafe.aircraftmodelmanagement.application.AircraftModelNotFoundException;
import eapli.aisafe.aircraftmodelmanagement.application.EngineModelNotFoundException;
import eapli.aisafe.aircraftmodelmanagement.application.ListAircraftModelsController;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.app.backoffice.console.presentation.enginemodel.EngineModelPrinter;
import eapli.aisafe.enginemodelmanagement.application.ListEngineModelsController;
import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.util.List;
import java.util.stream.StreamSupport;

@SuppressWarnings("squid:S106")
public class AddEngineModelToAircraftModelUI extends AbstractUI {

    private final AddEngineModelToAircraftModelController controller = new AddEngineModelToAircraftModelController();
    private final ListAircraftModelsController aircraftModels = new ListAircraftModelsController();
    private final ListEngineModelsController engineModels = new ListEngineModelsController();

    @Override
    protected boolean doShow() {
        final AircraftModel aircraftModel = chooseAircraftModel();
        if (aircraftModel == null) {
            return false;
        }

        final EngineModel engineModel = chooseEngineModel();
        if (engineModel == null) {
            return false;
        }

        try {
            controller.addEngineModelToAircraftModel(
                    aircraftModel.identity().toString(),
                    engineModel.identity().toString());
            System.out.println("Certified engine model added to aircraft model successfully.");
        } catch (final AircraftModelNotFoundException | EngineModelNotFoundException ex) {
            System.out.println(ex.getMessage());
        } catch (final IllegalArgumentException ex) {
            System.out.println("Invalid input: " + ex.getMessage());
        } catch (final IllegalStateException ex) {
            System.out.println("Operation not allowed: " + ex.getMessage());
        }

        return false;
    }

    private AircraftModel chooseAircraftModel() {
        final List<AircraftModel> all = StreamSupport
                .stream(aircraftModels.allAircraftModels().spliterator(), false)
                .toList();

        if (all.isEmpty()) {
            throw new IllegalStateException("No aircraft models registered.");
        }

        final SelectWidget<AircraftModel> selector =
                new SelectWidget<>("Select Aircraft Model:", all, new AircraftModelPrinter());

        selector.show();
        return selector.selectedElement();
    }

    private EngineModel chooseEngineModel() {
        final List<EngineModel> all = StreamSupport
                .stream(engineModels.allEngineModels().spliterator(), false)
                .toList();

        if (all.isEmpty()) {
            throw new IllegalStateException("No engine models registered.");
        }

        final SelectWidget<EngineModel> selector =
                new SelectWidget<>("Select Engine Model:", all, new EngineModelPrinter());

        selector.show();
        return selector.selectedElement();
    }

    @Override
    public String headline() {
        return "Add Certified Engine Model to Existing Aircraft Model";
    }
}
