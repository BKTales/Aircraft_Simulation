package eapli.aisafe.rcomp.tcpclient.presentation.weather;

import eapli.aisafe.rcomp.protocol.ProtocolFrame;
import eapli.aisafe.rcomp.protocol.ResponseCodes;
import eapli.aisafe.rcomp.protocol.WeatherOpcodes;
import eapli.aisafe.rcomp.protocol.WeatherRegisterPayload;
import eapli.aisafe.rcomp.tcpclient.presentation.RemoteTcpGateway;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("squid:S106")
public final class RegisterWeatherRemoteUI extends AbstractUI {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @Override
    protected boolean doShow() {
        try {
            final ProtocolFrame areasResp = RemoteTcpGateway.request(WeatherOpcodes.LIST_AREAS, "");
            if (areasResp == null || areasResp.opcode() != ResponseCodes.OK) {
                RemoteTcpGateway.printResponse(areasResp);
                return false;
            }
            printAreas(areasResp.payload());

            final String areaCode = Console.readLine("Select the Air Control Area Code (e.g., AREA-0):");

            System.out.println("\n--- Weather Information ---");
            final float temperature = (float) Console.readDouble("Temperature (ºC):");
            final float humidity = (float) Console.readDouble("Humidity (%):");
            final float pressure = (float) Console.readDouble("Pressure (hPa):");
            final int direction = Console.readInteger("Wind Direction (0-360º):");
            final float speed = (float) Console.readDouble("Wind Speed (m/s):");

            final LocalDateTime start;
            final LocalDateTime end;
            try {
                System.out.println("\n--- Validity Period ---");
                start = LocalDateTime.parse(Console.readLine("Start Date/Time (dd-MM-yyyy HH:mm):"), formatter);
                end = LocalDateTime.parse(Console.readLine("End Date/Time (dd-MM-yyyy HH:mm):"), formatter);
            } catch (final DateTimeParseException e) {
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
                final float x = (float) Console.readDouble("  Coordinate X:");
                final float y = (float) Console.readDouble("  Coordinate Y:");
                coordinates.add(new float[]{x, y});
                if (coordinates.size() >= 3) {
                    keepAdding = Console.readBoolean("Add another point? (y/n)");
                } else {
                    System.out.println("You need at least 3 points to form an area.");
                }
                count++;
            }

            final String payload = WeatherRegisterPayload.encode(new WeatherRegisterPayload.Fields(
                    areaCode, temperature, humidity, pressure, direction, speed, start, end, coordinates));
            final ProtocolFrame resp = RemoteTcpGateway.request(WeatherOpcodes.REGISTER_WEATHER, payload);
            if (resp != null && resp.opcode() == ResponseCodes.OK) {
                System.out.println("\nWeather Data successfully registered for Area " + areaCode + ".");
            } else {
                System.out.println("\nError: " + RemoteTcpGateway.messageFrom(resp, "Registration failed."));
            }
        } catch (final IOException ex) {
            System.out.println("Connection error: " + ex.getMessage());
        }
        return false;
    }

    private static void printAreas(final String payload) {
        final List<String> lines = Arrays.stream(payload.split("\n"))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
        if (lines.isEmpty()) {
            System.out.println("Error: No Air Control Areas found. Please register an area first.");
            return;
        }
        System.out.println("\n--- Available Air Control Areas ---");
        for (final String line : lines) {
            final String[] parts = line.split("\\|", 2);
            System.out.println("Area Code: " + parts[0]);
            if (parts.length == 2 && !parts[1].isBlank()) {
                System.out.print("  Boundary: ");
                for (final String point : parts[1].split(",")) {
                    final String[] xy = point.split(":");
                    if (xy.length == 2) {
                        System.out.printf("[X: %s, Y: %s] ", xy[0], xy[1]);
                    }
                }
                System.out.println();
            }
            System.out.println();
        }
        System.out.println("-----------------------------------");
    }

    @Override
    public String headline() {
        return "Register Weather Data (US041)";
    }
}
