package eapli.aisafe.rcomp.tcpclient.presentation.atcc.fleet;

import eapli.aisafe.rcomp.protocol.AtccOpcodes;
import eapli.aisafe.rcomp.protocol.ProtocolFrame;
import eapli.aisafe.rcomp.protocol.ResponseCodes;
import eapli.aisafe.rcomp.tcpclient.presentation.RemoteTcpGateway;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.io.IOException;
import java.util.List;

@SuppressWarnings("squid:S106")
public final class DecommissionAircraftRemoteUI extends AbstractUI {

    @Override
    protected boolean doShow() {
        final List<RemoteActiveAircraftEntry> choices;
        try {
            choices = loadActiveFleet();
        } catch (final IOException ex) {
            System.out.println("Connection error: " + ex.getMessage());
            return false;
        }

        if (choices.isEmpty()) {
            System.out.println("No active aircraft in your company's fleet.");
            return false;
        }

        System.out.println("Select an aircraft to decommission (registration):");
        RemoteActiveAircraftPrinter.printHeader();
        final SelectWidget<RemoteActiveAircraftEntry> selector =
                new SelectWidget<>("", choices, new RemoteActiveAircraftPrinter());
        selector.show();
        final RemoteActiveAircraftEntry chosen = selector.selectedElement();
        if (chosen == null) {
            return false;
        }

        try {
            final ProtocolFrame resp =
                    RemoteTcpGateway.request(AtccOpcodes.DECOMMISSION_AIRCRAFT, chosen.registration());
            RemoteTcpGateway.printResponse(resp);
            if (resp != null && resp.opcode() == ResponseCodes.OK) {
                System.out.println("Aircraft decommissioned successfully.");
            }
        } catch (final IOException ex) {
            System.out.println("Connection error: " + ex.getMessage());
        }
        return false;
    }

    private static List<RemoteActiveAircraftEntry> loadActiveFleet() throws IOException {
        final ProtocolFrame active = RemoteTcpGateway.request(AtccOpcodes.LIST_ACTIVE_AIRCRAFT, "");
        if (active == null || active.opcode() != ResponseCodes.OK) {
            RemoteTcpGateway.printResponse(active);
            return List.of();
        }
        return RemoteActiveAircraftEntry.parsePayload(active.payload());
    }

    @Override
    public String headline() {
        return "Decommission aircraft (US071)";
    }
}
