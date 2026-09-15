package eapli.aisafe.rcomp.tcpclient.presentation.atcc.pilotcollaborator;

import eapli.aisafe.rcomp.protocol.AtccOpcodes;
import eapli.aisafe.rcomp.protocol.ProtocolFrame;
import eapli.aisafe.rcomp.protocol.ResponseCodes;
import eapli.aisafe.rcomp.tcpclient.presentation.RemoteTcpGateway;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.io.IOException;
import java.util.List;

@SuppressWarnings("squid:S106")
public final class RemovePilotRemoteUI extends AbstractUI {

    @Override
    protected boolean doShow() {
        final List<RemotePilotRosterEntry> choices;
        try {
            choices = loadActiveRoster();
        } catch (final IOException ex) {
            System.out.println("Connection error: " + ex.getMessage());
            return false;
        }

        if (choices.isEmpty()) {
            System.out.println("No active pilots in your company's roster.");
            return false;
        }

        System.out.println("Select a pilot to deactivate from the roster:");
        RemotePilotRosterPrinter.printHeader();
        final SelectWidget<RemotePilotRosterEntry> selector =
                new SelectWidget<>("", choices, new RemotePilotRosterPrinter());
        selector.show();
        final RemotePilotRosterEntry chosen = selector.selectedElement();
        if (chosen == null) {
            return false;
        }

        try {
            final ProtocolFrame resp = RemoteTcpGateway.request(AtccOpcodes.REMOVE_PILOT, chosen.email());
            RemoteTcpGateway.printResponse(resp);
            if (resp != null && resp.opcode() == ResponseCodes.OK) {
                System.out.println("Pilot deactivated successfully. They no longer appear in the active roster.");
            }
        } catch (final IOException ex) {
            System.out.println("Connection error: " + ex.getMessage());
        }
        return false;
    }

    private static List<RemotePilotRosterEntry> loadActiveRoster() throws IOException {
        final ProtocolFrame roster = RemoteTcpGateway.request(AtccOpcodes.LIST_PILOT_ROSTER, "");
        if (roster == null || roster.opcode() != ResponseCodes.OK) {
            RemoteTcpGateway.printResponse(roster);
            return List.of();
        }
        return RemotePilotRosterEntry.parsePayload(roster.payload());
    }

    @Override
    public String headline() {
        return "Remove pilot from roster (US077)";
    }
}
