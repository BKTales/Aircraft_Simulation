package eapli.aisafe.rcomp.tcpclient.presentation.atcc.fleet;

import eapli.framework.visitor.Visitor;

@SuppressWarnings("squid:S106")
public final class RemoteActiveAircraftPrinter implements Visitor<RemoteActiveAircraftEntry> {

    public static void printHeader() {
        System.out.printf(
                "%-12s %-10s %-12s %-8s %-5s%n",
                "REGISTRATION", "MODEL", "STATUS", "SEATS", "AGE");
    }

    @Override
    public void visit(final RemoteActiveAircraftEntry aircraft) {
        System.out.printf(
                "%-12s %-10s %-12s %-8d %-5d%n",
                aircraft.registration(),
                aircraft.modelId(),
                aircraft.status(),
                aircraft.totalSeats(),
                aircraft.ageInYears());
    }
}
