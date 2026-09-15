package eapli.aisafe.rcomp.tcpclient.presentation.atcc.fleet;

import eapli.framework.visitor.Visitor;

@SuppressWarnings("squid:S106")
public final class RemoteAircraftModelPrinter implements Visitor<RemoteAircraftModelEntry> {

    public static void printHeader() {
        System.out.printf("%-12s %-24s %-16s %-10s%n", "MODEL ID", "NAME", "MANUFACTURER", "TYPE");
    }

    @Override
    public void visit(final RemoteAircraftModelEntry model) {
        System.out.printf("%-12s %-24s %-16s %-10s%n",
                model.id(), model.name(), model.manufacturer(), model.type());
    }
}
