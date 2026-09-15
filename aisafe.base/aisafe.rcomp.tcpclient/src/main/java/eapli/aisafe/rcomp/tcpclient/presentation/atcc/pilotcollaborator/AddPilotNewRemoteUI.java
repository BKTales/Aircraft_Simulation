package eapli.aisafe.rcomp.tcpclient.presentation.atcc.pilotcollaborator;

import eapli.aisafe.rcomp.protocol.AtccOpcodes;
import eapli.aisafe.rcomp.tcpclient.presentation.RemoteTcpGateway;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.io.IOException;

@SuppressWarnings("squid:S106")
public final class AddPilotNewRemoteUI extends AbstractUI {

    @Override
    protected boolean doShow() {
        try {
            System.out.println("New System User information >");
            final String username = Console.readLine("Username");
            final String password = Console.readLine("Password");
            final String first = Console.readLine("First Name");
            final String last = Console.readLine("Last Name");
            final String email = Console.readLine("E-Mail");
            System.out.println("\nPilot Collaborator information >");
            final String security = Console.readLine("Security Clearance Start Date (YYYY-MM-DD)");
            final String phone = Console.readLine("Phone Number (+351XXXXXXXXX)");
            final String certs = RemotePilotCertificationInput.readCertifications();
            final String payload = String.join(";",
                    username, password, first, last, email, security, phone) + certs;
            RemoteTcpGateway.printResponse(RemoteTcpGateway.request(AtccOpcodes.ADD_PILOT_NEW_USER, payload));
        } catch (final IOException ex) {
            System.out.println("Connection error: " + ex.getMessage());
        }
        return false;
    }

    @Override
    public String headline() {
        return "Register pilot — new user (US075)";
    }
}
