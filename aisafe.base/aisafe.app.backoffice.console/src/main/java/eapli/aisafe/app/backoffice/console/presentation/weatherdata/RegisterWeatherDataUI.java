package eapli.aisafe.app.backoffice.console.presentation.weatherdata;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicCoords;
import eapli.aisafe.weatherdata.application.RegisterWeatherDataController;
import eapli.aisafe.weatherdata.application.RegisterWeatherResult;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class RegisterWeatherDataUI extends AbstractUI {

    private final RegisterWeatherDataController theController = new RegisterWeatherDataController();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @Override
    protected boolean doShow() {
        System.out.println("\n--- Available Air Control Areas ---");
        Iterable<AirControlArea> areas = theController.availableAreas();

        if (!areas.iterator().hasNext()) {
            System.out.println("Error: No Air Control Areas found. Please register an area first.");
            return false;
        }

        for (AirControlArea area : areas) {
            System.out.println("Area Code: " + area.identity().toString());
            System.out.print("  Boundary: ");
            for (GeographicCoords coord : area.getGeographicBoundary().getGeoCords()) {
                System.out.printf("[X: %.1f, Y: %.1f] ", coord.getX(), coord.getY());
            }
            System.out.println("\n");
        }
        System.out.println("-----------------------------------");

        final String areaCode = Console.readLine("Select the Air Control Area Code (e.g., AREA-0):");

        System.out.println("\n--- Weather Information ---");
        final float temperature = (float) Console.readDouble("Temperature (ºC):");
        final float humidity = (float) Console.readDouble("Humidity (%):");
        final float pressure = (float) Console.readDouble("Pressure (hPa):");
        final int direction = Console.readInteger("Wind Direction (0-360º):");
        final float speed = (float) Console.readDouble("Wind Speed (m/s):");

        LocalDateTime start;
        LocalDateTime end;
        try {
            System.out.println("\n--- Validity Period ---");
            final String startStr = Console.readLine("Start Date/Time (dd-MM-yyyy HH:mm):");
            start = LocalDateTime.parse(startStr, formatter);

            final String endStr = Console.readLine("End Date/Time (dd-MM-yyyy HH:mm):");
            end = LocalDateTime.parse(endStr, formatter);
        } catch (DateTimeParseException e) {
            System.out.println("\nError: Invalid date format. Please use dd-MM-yyyy HH:mm.");
            return false;
        }

        final List<float[]> coordinates = new ArrayList<>();

        System.out.println("\n--- Weather Section Boundary ---");
        System.out.println("Please enter the coordinates for the boundary (min. 3 points):");
        System.out.println("WARNING: Ensure these points are inside the boundary of " + areaCode + "!");
        boolean keepAdding = true;
        int count = 1;

        while (keepAdding || coordinates.size() < 3) {
            System.out.println("Point #" + count);
            float x = (float) Console.readDouble("  Coordinate X:");
            float y = (float) Console.readDouble("  Coordinate Y:");
            coordinates.add(new float[]{x, y});

            if (coordinates.size() >= 3) {
                keepAdding = Console.readBoolean("Add another point? (y/n)");
            } else {
                System.out.println("You need at least 3 points to form an area.");
            }
            count++;
        }

        final RegisterWeatherResult result = this.theController.registerWeatherData(
                areaCode, coordinates, temperature, direction, speed, humidity, pressure, start, end);
        handleRegisterResult(result);

        return false;
    }

    private void handleRegisterResult(final RegisterWeatherResult result) {
        if (result.isSuccess()) {
            System.out.println("\nWeather Data successfully registered for Area "
                    + result.weatherData().getAirControlArea().identity() + ".");
            return;
        }

        if (result.message() != null) {
            System.out.println("\nError: " + result.message());
            return;
        }

        switch (result.outcome()) {
            case AREA_NOT_FOUND -> System.out.println("\nError: Area not found.");
            case SECTION_OUT_OF_BOUNDS -> System.out.println("\nError: Weather section is outside the area boundary.");
            case INVALID_HUMIDITY -> System.out.println("\nError: Invalid humidity value.");
            case INVALID_INPUT -> System.out.println("\nError: Invalid input.");
            default -> System.out.println("\nError: An unexpected error occurred.");
        }
    }

    @Override
    public String headline() {
        return "Register Weather Data";
    }
}