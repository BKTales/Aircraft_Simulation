package eapli.aisafe.app.backoffice.console.presentation.aircraft;

import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.framework.visitor.Visitor;

public final class DecommissionFleetAircraftPrinter implements Visitor<Aircraft> {

    public static void printRegistrationHeader() {
        System.out.printf("%-18s%n", "Registration");
        System.out.println("------------------");
    }

    @Override
    public void visit(final Aircraft aircraft) {
        System.out.printf("%-18s%n", aircraft.identity());
    }
}
