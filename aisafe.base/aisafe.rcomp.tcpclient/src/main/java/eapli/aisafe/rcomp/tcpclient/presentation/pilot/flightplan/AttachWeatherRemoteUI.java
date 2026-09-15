package eapli.aisafe.rcomp.tcpclient.presentation.pilot.flightplan;

import eapli.aisafe.rcomp.protocol.PilotOpcodes;
import eapli.aisafe.rcomp.protocol.ProtocolFrame;
import eapli.aisafe.rcomp.protocol.ResponseCodes;
import eapli.aisafe.rcomp.tcpclient.presentation.RemoteTcpGateway;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * US082 — same flow as backoffice {@code AttachWeatherToFlightUI} (select flight, weather id, confirm),
 * over TCP.
 */
@SuppressWarnings("squid:S106")
public final class AttachWeatherRemoteUI extends AbstractUI {

    @Override
    protected boolean doShow() {
        try {
            final String designator = chooseFlight();
            if (designator == null) {
                return false;
            }
            final long weatherId = Console.readLong("Weather Data ID:");
            final String confirm = Console.readLine(
                    "\nAttach this weather data to flight " + designator + "? (y/n):");
            if (confirm == null || !confirm.trim().equalsIgnoreCase("y")) {
                System.out.println("Operation cancelled.");
                return false;
            }
            RemoteTcpGateway.printResponse(
                    RemoteTcpGateway.request(PilotOpcodes.ATTACH_WEATHER, designator + ";" + weatherId));
        } catch (final IOException ex) {
            System.out.println("Connection error: " + ex.getMessage());
        } catch (final NumberFormatException ex) {
            System.out.println("Invalid weather data id.");
        }
        return false;
    }

    private String chooseFlight() throws IOException {
        final ProtocolFrame resp = RemoteTcpGateway.request(PilotOpcodes.LIST_MY_FLIGHTS, "");
        if (resp == null || resp.opcode() != ResponseCodes.OK) {
            RemoteTcpGateway.printResponse(resp);
            return null;
        }
        final List<String> designators = new ArrayList<>();
        for (final String line : resp.payload().split("\n")) {
            if (line.isBlank()) {
                continue;
            }
            final String[] parts = line.trim().split("\\|", -1);
            if (parts.length > 0 && !parts[0].isBlank()) {
                designators.add(parts[0].trim());
            }
        }
        if (designators.isEmpty()) {
            System.out.println("No flights assigned to you.");
            return null;
        }
        System.out.println("Select flight:");
        final SelectWidget<String> selector = new SelectWidget<>("", designators);
        selector.show();
        return selector.selectedElement();
    }

    @Override
    public String headline() {
        return "Attach Weather Data to Flight (US082)";
    }
}
