package eapli.aisafe.app.backoffice.console.presentation.enginemodel;

import eapli.aisafe.enginemodelmanagement.application.ListEngineModelsController;
import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.framework.presentation.console.AbstractUI;

@SuppressWarnings("squid:S106")
public class ListEngineModelsUI extends AbstractUI {

    private final ListEngineModelsController controller = new ListEngineModelsController();

    @Override
    protected boolean doShow() {
        final Iterable<EngineModel> models = controller.allEngineModels();

        System.out.printf("%-28s %-18s %-10s %-20s %-10s %-8s %-8s%n",
                "ID", "Name", "Mfr", "Motorization", "Fuel", "TSFC", "Thrust");
        System.out.println("-".repeat(108));

        boolean any = false;
        for (final EngineModel m : models) {
            System.out.printf("%-28s %-18s %-10s %-20s %-10s %-8.3f %4.0f/%-4.0f%n",
                    m.identity(),
                    m.name(),
                    m.manufacturerId(),
                    m.motorization(),
                    m.fuelType(),
                    m.tsfc().value(),
                    m.thrustProfile().thrustAtStatic(),
                    m.thrustProfile().thrustAtCruise());
            any = true;
        }

        if (!any) System.out.println("No engine models registered.");
        return false;
    }

    @Override
    public String headline() {
        return "List Engine Models";
    }
}

