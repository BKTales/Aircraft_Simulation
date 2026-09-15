package eapli.aisafe.rcomp.tcpclient.presentation;

import eapli.aisafe.rcomp.protocol.CommonOpcodes;
import eapli.aisafe.rcomp.protocol.ProtocolFrame;
import eapli.aisafe.rcomp.tcpclient.RemoteAppContext;
import eapli.aisafe.rcomp.tcpclient.RemoteProfile;
import eapli.framework.presentation.console.AbstractUI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@SuppressWarnings("squid:S106")
public final class RemoteLoginUI extends AbstractUI {

    final int MAX_PROFILE_ATTEMPTS = 3;
    final int MAX_LOGIN_ATTEMPTS = 3;

    private static final BufferedReader STDIN =
            new BufferedReader(new InputStreamReader(System.in));

    @Override
    protected boolean doShow() {
        System.out.println("Profile: 1=ATCC (US078)  2=Pilot (US086)  3=Weather (US044)");

        RemoteProfile profile = null;
        int profile_attempts = 0;
        while(profile_attempts < MAX_PROFILE_ATTEMPTS){
            profile = RemoteProfile.fromChoice(readInline("> "));
            profile_attempts++;

            if (profile != null) break;

            System.out.println("Invalid profile (attempts remaining: " + (MAX_PROFILE_ATTEMPTS - profile_attempts) + ")");
            if(profile_attempts == MAX_PROFILE_ATTEMPTS){
                System.out.println("Maximum profile attempts exceeded!");
                return false;
            }
        }

        int login_attempts = 0;
        while(login_attempts < MAX_LOGIN_ATTEMPTS){
            final String username = readInline("Username: ");
            final String password = readInline("Password: ");
            final String payload = username + ";" + password + ";" + profile.loginToken();

            try {
                final ProtocolFrame resp = RemoteTcpGateway.request(CommonOpcodes.LOGIN, payload);
                if (resp != null && resp.opcode() == CommonOpcodes.SUCCESS_LOGIN) {
                    RemoteAppContext.require().setLoggedIn(username, profile);
                    return true;
                }
                System.out.println("Login failed (attempts remaining: " + (MAX_LOGIN_ATTEMPTS - ++login_attempts) + ")");
            } catch (final IOException ex) {
                System.out.println("Connection error: " + ex.getMessage());
                break;
            }
        }
        if(login_attempts == MAX_LOGIN_ATTEMPTS){
            System.out.println("Maximum login attempts exceeded!");
        }
        return false;
    }

    @Override
    public String headline() {
        return "Remote login";
    }

    private static String readInline(final String prompt) {
        System.out.print(prompt);
        System.out.flush();
        try {
            final String line = STDIN.readLine();
            return line == null ? "" : line.trim();
        } catch (final IOException ex) {
            return "";
        }
    }
}
