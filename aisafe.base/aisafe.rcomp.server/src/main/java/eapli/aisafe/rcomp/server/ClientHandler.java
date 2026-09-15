package eapli.aisafe.rcomp.server;

import eapli.aisafe.infrastructure.authz.AuthenticationCredentialHandler;
import eapli.aisafe.rcomp.protocol.CommonOpcodes;
import eapli.aisafe.rcomp.protocol.ProtocolConstants;
import eapli.aisafe.rcomp.protocol.ProtocolFrame;
import eapli.aisafe.rcomp.protocol.ResponseCodes;
import eapli.aisafe.rcomp.server.atcc.AtccCommandHandler;
import eapli.aisafe.rcomp.server.pilot.PilotCommandHandler;
import eapli.aisafe.rcomp.server.weather.WeatherCommandHandler;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.Role;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Optional;

/**
 * TCP session handler for remote access (US078 ATCC, US086 Pilot, US044 Weather).
 */
@SuppressWarnings("squid:S106")
public final class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final AtccCommandHandler atccCommands = new AtccCommandHandler(this);
    private final PilotCommandHandler pilotCommands = new PilotCommandHandler(this);
    private final WeatherCommandHandler weatherCommands = new WeatherCommandHandler(this);
    private final AuthenticationCredentialHandler authHandler = new AuthenticationCredentialHandler();

    private DataOutputStream out;
    private DataInputStream in;
    private boolean authenticated;
    private String loggedInUser;
    private Role sessionRole;

    private String clientIp;
    private int clientPort;
    private boolean explicitLogout;

    public ClientHandler(final Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            out = new DataOutputStream(clientSocket.getOutputStream());
            in = new DataInputStream(clientSocket.getInputStream());

            clientIp   = clientSocket.getInetAddress().getHostAddress();
            clientPort = clientSocket.getPort();

            ProtocolFrame frame;
            while ((frame = ProtocolFrame.readOrNullOnEof(in)) != null) {
                if (frame.version() != ProtocolConstants.VERSION) {
                    ProtocolFrame.writeResponse(out, CommonOpcodes.FAILED_LOGIN,
                            "Unsupported protocol version: " + frame.version());
                    break;
                }
                if (!dispatch(frame)) {
                    break;
                }
            }
        } catch (final IOException e) {
            System.err.println("Client handler I/O error: " + e.getMessage());
        } finally {
            AuthzRegistry.authorizationService().clearSession();
            closeConnection();
        }
    }

    private boolean dispatch(final ProtocolFrame frame) throws IOException {
        final byte code = frame.opcode();
        if (code == CommonOpcodes.LOGIN) {
            processLogin(frame.payload());
            return true;
        }
        if (code == CommonOpcodes.LOGOUT) {
            processLogout();
            return false;
        }
        if (!authenticated) {
            ProtocolFrame.writeResponse(out, ResponseCodes.UNAUTHORIZED, "Login required.");
            return true;
        }
        if (code >= 20 && code <= 39) {
            if (!isAtccSession()) {
                ProtocolFrame.writeResponse(out, ResponseCodes.FORBIDDEN, "ATCC role required.");
                return true;
            }
            atccCommands.handle(code, frame.payload(), out);
            return true;
        }
        if (code >= 40 && code <= 49) {
            if (!isPilotSession()) {
                ProtocolFrame.writeResponse(out, ResponseCodes.FORBIDDEN, "Pilot role required.");
                return true;
            }
            pilotCommands.handle(code, frame.payload(), out);
            return true;
        }
        if (code >= 50 && code <= 59) {
            if (!isWeatherSession()) {
                ProtocolFrame.writeResponse(out, ResponseCodes.FORBIDDEN, "Weather Person role required.");
                return true;
            }
            weatherCommands.handle(code, frame.payload(), out);
            return true;
        }
        ProtocolFrame.writeResponse(out, ResponseCodes.BAD_REQUEST, "Unknown opcode: " + code);
        return true;
    }

    private boolean isAtccSession() {
        return sessionRole != null && sessionRole.equals(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
    }

    private boolean isPilotSession() {
        return sessionRole != null && sessionRole.equals(AISafeRoles.PILOT);
    }

    private boolean isWeatherSession() {
        return sessionRole != null && sessionRole.equals(AISafeRoles.WEATHER_PERSON);
    }

    private void processLogin(final String credentials) throws IOException {
        final Optional<LoginCredentialsParser.ParsedLogin> parsed = LoginCredentialsParser.parse(credentials);
        if (parsed.isEmpty()) {
            ProtocolFrame.writeResponse(out, CommonOpcodes.INVALID_CREDENTIALS, "INVALID CREDENTIALS");
            RemoteAccessLogger.log(RemoteAccessLogger.Event.LOGIN_FAIL,
                    null, clientIp, clientPort, "UNKNOWN", null);
            return;
        }
        final LoginCredentialsParser.ParsedLogin login = parsed.get();
        final boolean valid = authHandler.authenticate(
                login.username(), login.password(), login.requiredRole());

        if (valid) {
            if (SessionManager.login(login.username())) {
                authenticated = true;
                loggedInUser = login.username();
                sessionRole = login.requiredRole();
                ProtocolFrame.writeResponse(out, CommonOpcodes.SUCCESS_LOGIN, "LOGIN SUCCESSFUL");
                logAction(RemoteAccessLogger.Event.LOGIN_OK, null);
            } else {
                ProtocolFrame.writeResponse(out, CommonOpcodes.FAILED_LOGIN, "ALREADY LOGGED IN");
                RemoteAccessLogger.log(RemoteAccessLogger.Event.LOGIN_FAIL,
                        login.username(), clientIp, clientPort,
                        resolveService(login.requiredRole()), null);
            }
        } else {
            ProtocolFrame.writeResponse(out, CommonOpcodes.FAILED_LOGIN, "LOGIN FAILED");
            RemoteAccessLogger.log(RemoteAccessLogger.Event.LOGIN_FAIL,
                    login.username(), clientIp, clientPort,
                    resolveService(login.requiredRole()), null);
        }
    }

    private void processLogout() throws IOException {
        logAction(RemoteAccessLogger.Event.LOGOUT, null);

        SessionManager.logout(loggedInUser);
        explicitLogout = true;
        AuthzRegistry.authorizationService().clearSession();
        authenticated = false;
        loggedInUser = null;
        sessionRole = null;
        ProtocolFrame.writeResponse(out, CommonOpcodes.SUCCESS_LOGOUT, "LOGOUT SUCCESSFUL");
    }

    private void closeConnection() {
        if (loggedInUser != null) {
            SessionManager.logout(loggedInUser);
        }
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                if (!explicitLogout && loggedInUser != null) {
                    logAction(RemoteAccessLogger.Event.DISCONNECT, null);
                }
                try {
                    clientSocket.shutdownOutput();
                } catch (final IOException ignored) {}
                clientSocket.close();
                System.out.println("Connection closed for: " + loggedInUser);
            }
        } catch (final IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }

    public void logAction(final RemoteAccessLogger.Event event, final String operationName) {
        RemoteAccessLogger.log(event, loggedInUser, clientIp, clientPort, resolveService(), operationName);
    }

    private String resolveService() {
        return resolveService(sessionRole);
    }

    private String resolveService(final Role role) {
        if (role == null) return "UNKNOWN";
        if (role.equals(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR)) return "US78";
        if (role.equals(AISafeRoles.PILOT)) return "US86";
        if (role.equals(AISafeRoles.WEATHER_PERSON)) return "US44";
        return "UNKNOWN";
    }
}