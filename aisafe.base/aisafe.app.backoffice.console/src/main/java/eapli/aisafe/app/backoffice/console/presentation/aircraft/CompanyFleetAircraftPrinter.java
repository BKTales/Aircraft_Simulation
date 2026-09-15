package eapli.aisafe.app.backoffice.console.presentation.aircraft;

import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.framework.visitor.Visitor;

public final class CompanyFleetAircraftPrinter implements Visitor<Aircraft> {

    public static void printHeader() {
        System.out.printf(
                "%-16s %-10s %-16s %-10s %-6s%n",
                "Registration", "Model", "Status", "Passengers", "Age");
        System.out.println("---------------- ---------- ---------------- ---------- ------");
    }

    @Override
    public void visit(final Aircraft aircraft) {
        System.out.printf(
                "%-16s %-10s %-16s %-10d %-6d%n",
                aircraft.identity(),
                aircraft.aircraftModelId(),
                aircraft.operationalStatus(),
                aircraft.cabinConfiguration().totalSeats(),
                aircraft.ageInYears());
    }
}
