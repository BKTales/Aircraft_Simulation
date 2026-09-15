package eapli.aisafe.app.backoffice.console.presentation.aircraft;

import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.framework.visitor.Visitor;

public final class CompanyFleetModelPrinter implements Visitor<AircraftModel> {

    @Override
    public void visit(final AircraftModel model) {
        System.out.printf("%-12s %s%n", model.identity(), model.name());
    }
}
