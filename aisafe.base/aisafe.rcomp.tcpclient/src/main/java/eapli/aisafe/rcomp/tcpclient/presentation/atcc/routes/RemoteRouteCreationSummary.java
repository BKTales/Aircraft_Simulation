package eapli.aisafe.rcomp.tcpclient.presentation.atcc.routes;

import eapli.aisafe.rcomp.protocol.ProtocolFrame;
import eapli.aisafe.rcomp.protocol.ResponseCodes;
import eapli.aisafe.rcomp.tcpclient.presentation.RemoteTcpGateway;

@SuppressWarnings("squid:S106")
public final class RemoteRouteCreationSummary {

    private static final String SEPARATOR = "----------------------------------------";

    private RemoteRouteCreationSummary() {}

    public static void printResponse(final ProtocolFrame resp) {
        if (resp == null || resp.opcode() != ResponseCodes.OK) {
            RemoteTcpGateway.printResponse(resp);
            return;
        }
        final String payload = resp.payload().trim();
        if (payload.isBlank()) {
            return;
        }
        final String body = payload.startsWith("OK|") ? payload.substring(3) : payload;
        final String[] fields = body.split("\\|", -1);
        if (fields.length < 7) {
            System.out.println(payload);
            return;
        }

        System.out.println();
        System.out.println("Route created successfully.");
        System.out.println(SEPARATOR);
        System.out.printf("Route name           : %s%n", fields[0]);
        System.out.printf("Company IATA code    : %s%n", fields[1]);
        System.out.printf("Origin airport       : %s%n", fields[2]);
        System.out.printf("Destination airport  : %s%n", fields[3]);
        System.out.printf("Flight type          : %s%n", fields[4]);

        if ("CHARTER".equals(fields[4]) && !"-".equals(fields[5])) {
            final String[] dates = fields[5].split("->", -1);
            if (dates.length == 2) {
                System.out.printf("Scheduled departure  : %s%n", dates[0].trim());
                System.out.printf("Scheduled arrival    : %s%n", dates[1].trim());
            } else {
                System.out.printf("Schedule             : %s%n", fields[5]);
            }
        } else if (!"-".equals(fields[6])) {
            System.out.printf("Recurring days       : %s%n", fields[6]);
        }
        System.out.println(SEPARATOR);
    }
}
