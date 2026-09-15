package eapli.aisafe.rcomp.tcpclient.presentation.atcc.pilotcollaborator;

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

@SuppressWarnings("squid:S106")
public final class AddPilotExistingRemoteUI extends AbstractUI {

    @Override
    protected boolean doShow() {
        try {
            final String email = selectEligibleEmail();
            if (email == null) {
                return false;
            }
            System.out.println("\nPilot Collaborator information >");
            final String security = Console.readLine("Security Clearance Expiry Date (YYYY-MM-DD)");
            final String phone = Console.readLine("Phone Number (+351XXXXXXXXX)");
            final String certs = RemotePilotCertificationInput.readCertifications();
            final String payload = email + ";" + security + ";" + phone + certs;
            RemoteTcpGateway.printResponse(RemoteTcpGateway.request(AtccOpcodes.ADD_PILOT_EXISTING_USER, payload));
        } catch (final IOException ex) {
            System.out.println("Connection error: " + ex.getMessage());
        }
        return false;
    }

    private String selectEligibleEmail() throws IOException {
        final ProtocolFrame resp = RemoteTcpGateway.request(AtccOpcodes.LIST_ELIGIBLE_USERS, "");
        if (resp == null || resp.opcode() != ResponseCodes.OK) {
            RemoteTcpGateway.printResponse(resp);
            return null;
        }
        final List<String> emails = Arrays.stream(resp.payload().split("\n"))
                .map(String::trim).filter(s -> !s.isEmpty()).distinct().sorted().toList();
        if (emails.isEmpty()) {
            System.out.println("No eligible System Users found.");
            return null;
        }
        System.out.println("\nSelect an existing System User >");
        final SelectWidget<String> selector = new SelectWidget<>("Select User:", emails);
        selector.show();
        return selector.selectedElement();
    }

    @Override
    public String headline() {
        return "Register pilot — existing user (US075)";
    }
}
