package eapli.aisafe.rcomp.tcpclient.presentation.atcc.routes;

import eapli.aisafe.rcomp.protocol.AtccOpcodes;
import eapli.aisafe.rcomp.protocol.ProtocolFrame;
import eapli.aisafe.rcomp.protocol.ResponseCodes;
import eapli.aisafe.rcomp.tcpclient.presentation.RemoteTcpGateway;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.List;

/**
 * US074 — same flow as backoffice {@link eapli.aisafe.app.backoffice.console.presentation.flightroute.DeactivateRouteUI}, over TCP.
 */
@SuppressWarnings("squid:S106")
public final class DeactivateRouteRemoteUI extends AbstractUI {

    private static final DateTimeFormatter FLEXIBLE_LOCAL_DATE = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR)
            .appendLiteral('-')
            .appendValue(ChronoField.DAY_OF_MONTH)
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT);

    @Override
    protected boolean doShow() {
        try {
            final String routeName = chooseRoute();
            if (routeName == null) {
                return false;
            }
            final LocalDate date = readLocalDate("Deactivation date (from this day onwards)");
            if (date == null) {
                return false;
            }
            final String payload = routeName + ";" + date;
            RemoteTcpGateway.printResponse(RemoteTcpGateway.request(AtccOpcodes.DEACTIVATE_ROUTE, payload));
        } catch (final IOException ex) {
            System.out.println("Connection error: " + ex.getMessage());
        }
        return false;
    }

    private String chooseRoute() throws IOException {
        final ProtocolFrame resp = RemoteTcpGateway.request(AtccOpcodes.LIST_ACTIVE_ROUTES, "");
        if (resp == null || resp.opcode() != ResponseCodes.OK) {
            RemoteTcpGateway.printResponse(resp);
            return null;
        }
        final List<String> routes = Arrays.stream(resp.payload().split("\n"))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
        if (routes.isEmpty()) {
            System.out.println("No active routes available for deactivation.");
            return null;
        }
        System.out.println("\nSelect route to deactivate >");
        final SelectWidget<String> selector = new SelectWidget<>("Route (name|company|origin|dest|type|...):", routes);
        selector.show();
        final String selected = selector.selectedElement();
        return selected == null ? null : selected.split("\\|", -1)[0].trim();
    }

    private LocalDate readLocalDate(final String prompt) {
        final String raw = Console.readLine(prompt + " (YYYY-MM-DD): ");
        if (raw == null || raw.isBlank()) {
            System.out.println("Date is required.");
            return null;
        }
        try {
            return LocalDate.parse(raw.trim(), FLEXIBLE_LOCAL_DATE);
        } catch (final DateTimeParseException ex) {
            System.out.println("Invalid date. Use YYYY-MM-DD (e.g. 2026-06-15).");
            return null;
        }
    }

    @Override
    public String headline() {
        return "Deactivate Route (US074)";
    }
}
