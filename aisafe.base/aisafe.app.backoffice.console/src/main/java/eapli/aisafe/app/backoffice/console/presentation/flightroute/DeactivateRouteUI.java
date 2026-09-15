package eapli.aisafe.app.backoffice.console.presentation.flightroute;

import eapli.aisafe.routemanagement.application.ActiveRouteOption;
import eapli.aisafe.routemanagement.application.DeactivateRouteController;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("squid:S106")
public class DeactivateRouteUI extends AbstractUI {

    private static final DateTimeFormatter FLEXIBLE_LOCAL_DATE = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR)
            .appendLiteral('-')
            .appendValue(ChronoField.DAY_OF_MONTH)
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT);

    private final DeactivateRouteController controller;

    public DeactivateRouteUI() {
        this(new DeactivateRouteController());
    }

    public DeactivateRouteUI(final DeactivateRouteController controller) {
        this.controller = Objects.requireNonNull(controller, "controller");
    }

    @Override
    protected boolean doShow() {
        try {
            final List<ActiveRouteOption> options = controller.listActiveRouteOptions();
            if (options.isEmpty()) {
                System.out.println("No active routes available for deactivation.");
                return false;
            }

            final ActiveRouteOption selected = chooseRoute(options);
            if (selected == null) {
                return false;
            }

            final LocalDate deactivationDate = readLocalDate("Deactivation date (from this day onwards)");
            final Route deactivated = controller.deactivateRoute(
                    selected.route().identity().toString(), deactivationDate);
            RoutePrinter.printDeactivationSummary(deactivated);
            return true;
        } catch (final RuntimeException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    private ActiveRouteOption chooseRoute(final List<ActiveRouteOption> options) {
        System.out.println("Select route to deactivate:");
        DeactivateRouteListPrinter.printTableHeaderWithRule();
        final SelectWidget<ActiveRouteOption> selector =
                new SelectWidget<>("", options, new DeactivateRouteListPrinter());
        selector.show();
        return selector.selectedElement();
    }

    private LocalDate readLocalDate(final String prompt) {
        while (true) {
            final String raw = Console.readLine(prompt + " (YYYY-MM-DD): ");
            if (raw == null || raw.isBlank()) {
                throw new IllegalArgumentException("Date is required.");
            }
            try {
                return LocalDate.parse(raw.trim(), FLEXIBLE_LOCAL_DATE);
            } catch (final DateTimeParseException ex) {
                System.out.println("Invalid date. Use YYYY-MM-DD (e.g. 2026-06-15 or 2026-6-15).");
            }
        }
    }

    @Override
    public String headline() {
        return "Deactivate Route (US074)";
    }
}
