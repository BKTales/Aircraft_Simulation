package eapli.aisafe.app.backoffice.console.presentation.flightcontrol;

import eapli.aisafe.flightmanagement.application.GenerateMonthlyStatisticsReportController;
import eapli.aisafe.flightmanagement.application.exceptions.NoMonthlySimulationDataException;
import eapli.aisafe.flightmanagement.application.reporting.MonthlyReportResult;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

@SuppressWarnings("squid:S106")
public class GenerateMonthlyStatisticsReportUI extends AbstractUI {

    private final GenerateMonthlyStatisticsReportController controller =
            new GenerateMonthlyStatisticsReportController();

    @Override
    protected boolean doShow() {
        final String areaCode;
        try {
            areaCode = controller.assignedAreaCode();
        } catch (final IllegalStateException ex) {
            System.out.println("\nError: " + ex.getMessage());
            return false;
        }

        System.out.println("\nAssigned Air Control Area: " + areaCode);

        final int year;
        final int month;
        try {
            year = Integer.parseInt(Console.readLine("Year (e.g. 2026):").trim());
            month = Integer.parseInt(Console.readLine("Month (1-12):").trim());
        } catch (final NumberFormatException ex) {
            System.out.println("Invalid year or month.");
            return false;
        }

        if (month < 1 || month > 12) {
            System.out.println("Month must be between 1 and 12.");
            return false;
        }

        try {
            final MonthlyReportResult result = controller.generate(year, month);
            System.out.println("\nMonthly statistics report generated.\n");
            System.out.println(result.formattedContent());
            System.out.println("Report saved to: " + result.reportPath());
        } catch (final NoMonthlySimulationDataException ex) {
            System.out.println("\nError: " + ex.getMessage());
        } catch (final IllegalArgumentException | IllegalStateException ex) {
            System.out.println("\nError: " + ex.getMessage());
        }
        return false;
    }

    @Override
    public String headline() {
        return "Generate Monthly Statistics Report (US112)";
    }
}
