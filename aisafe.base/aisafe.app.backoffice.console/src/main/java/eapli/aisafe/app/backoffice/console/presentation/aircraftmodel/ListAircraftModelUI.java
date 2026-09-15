package eapli.aisafe.app.backoffice.console.presentation.aircraftmodel;

import eapli.aisafe.aircraftmodelmanagement.application.ListAircraftModelsController;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.framework.presentation.console.AbstractListUI;
import eapli.framework.visitor.Visitor;

public class ListAircraftModelUI extends AbstractListUI<AircraftModel> {

    private final ListAircraftModelsController controller = new ListAircraftModelsController();

    @Override
    public String headline() {
        return "List Aircraft Models";
    }

    @Override
    protected Iterable<AircraftModel> elements() {
        return controller.allAircraftModels();
    }

    @Override
    protected Visitor<AircraftModel> elementPrinter() {
        return new AircraftModelPrinter();
    }

    @Override
    protected String elementName() {
        return "Aircraft Model";
    }

    @Override
    protected String listHeader() {
        return String.format(
                "%-28s %-18s %-12s %-10s %-12s %-34s %-10s %-12s %-12s %-12s",
                "ID", "Name", "Manufacturer", "Engines", "Cert.Models", "Cert.Model IDs", "Cruise", "FuelCap", "Range", "Ceiling"
        );
    }

    @Override
    protected String emptyMessage() {
        return "";
    }
}
