package eapli.aisafe.app.backoffice.console.presentation.aircraftmodel;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftType;
import eapli.framework.visitor.Visitor;

public class AircraftTypePrinter implements Visitor<AircraftType> {

    @Override
    public void visit(final AircraftType type) {
        System.out.printf("%s", type);
    }
}