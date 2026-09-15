package eapli.aisafe.rcomp.tcpclient.presentation.atcc.fleet;

import eapli.aisafe.rcomp.protocol.AtccOpcodes;
import eapli.aisafe.rcomp.protocol.ProtocolFrame;
import eapli.aisafe.rcomp.protocol.ResponseCodes;
import eapli.aisafe.rcomp.tcpclient.presentation.RemoteTcpGateway;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * US070 — same flow as backoffice {@code RegisterAircraftUI} (model + certified engine via
 * {@link SelectWidget}), over TCP.
 */
@SuppressWarnings("squid:S106")
public final class RegisterAircraftRemoteUI extends AbstractUI {

    @Override
    protected boolean doShow() {
        try {
            final String registration = Console.readLine(
                    "Aircraft registration (tail number, 3-14 characters: A-Z, 0-9, hyphen; e.g. CS-TST)");

            final RemoteAircraftModelEntry model = chooseModel();
            if (model == null) {
                return false;
            }

            final String engineId = chooseCertifiedEngine(model.id());
            if (engineId == null) {
                return false;
            }

            final int economy = Console.readInteger("Economy seats");
            final int business = Console.readInteger("Business seats");
            final int firstClass = Console.readInteger("First class seats");
            final String country = Console.readLine("Registration country (ISO 3166-1 alpha-2, e.g. PT)");
            final int crew = Console.readInteger("Number of flight crew");
            final int year = Console.readInteger("Year of manufacture (e.g. 2015)");

            final String payload = String.join(";",
                    registration, model.id(), engineId,
                    String.valueOf(economy), String.valueOf(business), String.valueOf(firstClass),
                    country, String.valueOf(crew), String.valueOf(year));
            RemoteTcpGateway.printResponse(RemoteTcpGateway.request(AtccOpcodes.REGISTER_AIRCRAFT, payload));
        } catch (final IOException ex) {
            System.out.println("Connection error: " + ex.getMessage());
        }
        return false;
    }

    private RemoteAircraftModelEntry chooseModel() throws IOException {
        final ProtocolFrame resp = RemoteTcpGateway.request(AtccOpcodes.LIST_AIRCRAFT_MODELS, "");
        if (resp == null || resp.opcode() != ResponseCodes.OK) {
            RemoteTcpGateway.printResponse(resp);
            return null;
        }
        final List<RemoteAircraftModelEntry> models = RemoteAircraftModelEntry.parsePayload(resp.payload());
        if (models.isEmpty()) {
            System.out.println("No aircraft models registered.");
            return null;
        }
        System.out.println("Select aircraft model:");
        RemoteAircraftModelPrinter.printHeader();
        final SelectWidget<RemoteAircraftModelEntry> selector =
                new SelectWidget<>("", models, new RemoteAircraftModelPrinter());
        selector.show();
        return selector.selectedElement();
    }

    private String chooseCertifiedEngine(final String modelId) throws IOException {
        final ProtocolFrame resp = RemoteTcpGateway.request(AtccOpcodes.LIST_CERTIFIED_ENGINES, modelId);
        if (resp == null || resp.opcode() != ResponseCodes.OK) {
            RemoteTcpGateway.printResponse(resp);
            return null;
        }
        final List<String> engines = Arrays.stream(resp.payload().split("\n"))
                .map(String::trim).filter(s -> !s.isEmpty()).distinct().sorted().toList();
        if (engines.isEmpty()) {
            System.out.println("This aircraft model has no certified engine models.");
            return null;
        }
        System.out.println("Select engine model (certified for this aircraft model):");
        final SelectWidget<String> selector = new SelectWidget<>("", engines);
        selector.show();
        return selector.selectedElement();
    }

    @Override
    public String headline() {
        return "Register Aircraft (US070)";
    }
}
