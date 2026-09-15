package eapli.aisafe.app.backoffice.console.presentation.weatherdata;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.weatherdata.application.BulkWeatherDataController;
import eapli.aisafe.weatherdata.application.ConsultWeatherResult;
import eapli.aisafe.weatherdata.application.RegisterWeatherDataController;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@SuppressWarnings("squid:S106")
public class ConsultWeatherDataUI extends AbstractUI {

    private final BulkWeatherDataController bulkController = new BulkWeatherDataController();
    private final RegisterWeatherDataController registerController = new RegisterWeatherDataController();
    private final DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Override
    protected boolean doShow() {
        System.out.println("\n--- Available Air Control Areas ---");
        final Iterable<AirControlArea> areas = registerController.availableAreas();

        if (!areas.iterator().hasNext()) {
            System.out.println("Error: No Air Control Areas found. Please register an area first.");
            return false;
        }

        for (final AirControlArea area : areas) {
            System.out.println("Area Code: " + area.identity());
        }
        System.out.println("-----------------------------------");

        final String areaCode = Console.readLine("Select the Air Control Area Code (e.g., AREA-0):");

        final LocalDateTime day;
        try {
            final String dayStr = Console.readLine("Day to consult (dd-MM-yyyy):");
            final LocalDate date = LocalDate.parse(dayStr, dayFormatter);
            day = date.atStartOfDay();
        } catch (DateTimeParseException e) {
            System.out.println("\nError: Invalid date format. Please use dd-MM-yyyy.");
            return false;
        }

        final ConsultWeatherResult result = bulkController.consultWeatherDataForDay(areaCode, day);
        handleConsultResult(result, areaCode, day);

        return false;
    }

    private void handleConsultResult(final ConsultWeatherResult result, final String areaCode, final LocalDateTime day) {
        if (!result.isSuccess()) {
            if (result.message() != null) {
                System.out.println("\nError: " + result.message());
            } else {
                switch (result.outcome()) {
                    case INVALID_INPUT -> System.out.println("\nError: Invalid input.");
                    default -> System.out.println("\nError: An unexpected error occurred.");
                }
            }
            return;
        }

        System.out.printf("%nWeather data for area %s on %s:%n%n", areaCode, day.format(dayFormatter));

        if (result.records().isEmpty()) {
            System.out.println("No weather data found for the selected area and day.");
            return;
        }

        WeatherDataPrinter.printTableHeader();
        for (final var weather : result.records()) {
            WeatherDataPrinter.printRow(weather);
        }
        System.out.printf("%nTotal: %d record(s).%n", result.records().size());
    }

    @Override
    public String headline() {
        return "Consult Weather Data by Day (US043)";
    }
}
