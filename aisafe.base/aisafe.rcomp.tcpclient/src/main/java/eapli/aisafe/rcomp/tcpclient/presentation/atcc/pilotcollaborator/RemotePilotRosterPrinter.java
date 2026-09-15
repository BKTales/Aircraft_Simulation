package eapli.aisafe.rcomp.tcpclient.presentation.atcc.pilotcollaborator;

import eapli.framework.visitor.Visitor;

@SuppressWarnings("squid:S106")
public final class RemotePilotRosterPrinter implements Visitor<RemotePilotRosterEntry> {

    public static void printHeader() {
        System.out.printf(
                "%-30s %-15s %-15s %-10s %-5s%n",
                "EMAIL", "F. NAME", "L. NAME", "STATUS", "CERTS");
    }

    @Override
    public void visit(final RemotePilotRosterEntry pilot) {
        System.out.printf(
                "%-30s %-15s %-15s %-10s %-5d%n",
                pilot.email(),
                pilot.firstName(),
                pilot.lastName(),
                pilot.status(),
                pilot.certificationCount());
    }
}
