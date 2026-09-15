package eapli.aisafe.rcomp.tcpclient.presentation.weather;

import eapli.aisafe.rcomp.protocol.ProtocolFrame;
import eapli.aisafe.rcomp.protocol.ResponseCodes;
import eapli.aisafe.rcomp.protocol.WeatherConsultPayload;
import eapli.aisafe.rcomp.protocol.WeatherOpcodes;
import eapli.aisafe.rcomp.tcpclient.presentation.RemoteTcpGateway;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("squid:S106")
public final class ConsultWeatherRemoteUI extends AbstractUI {

    private final DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Override
    protected boolean doShow() {
        try {
            final ProtocolFrame areasResp = RemoteTcpGateway.request(WeatherOpcodes.LIST_AREAS, "");
            if (areasResp == null || areasResp.opcode() != ResponseCodes.OK) {
                RemoteTcpGateway.printResponse(areasResp);
                return false;
            }
            printAreaCodes(areasResp.payload());

            final String areaCode = Console.readLine("Select the Air Control Area Code (e.g., AREA-0):");

            final LocalDate day;
            try {
                day = LocalDate.parse(Console.readLine("Day to consult (dd-MM-yyyy):"), dayFormatter);
            } catch (final DateTimeParseException e) {
                System.out.println("\nError: Invalid date format. Please use dd-MM-yyyy.");
                return false;
            }

            final ProtocolFrame resp = RemoteTcpGateway.request(
                    WeatherOpcodes.CONSULT_BY_DAY,
                    WeatherConsultPayload.encode(areaCode, day));
            if (resp == null || resp.opcode() != ResponseCodes.OK) {
                System.out.println("\nError: " + RemoteTcpGateway.messageFrom(resp, "Consult failed."));
                return false;
            }

            final List<String> rows = Arrays.stream(resp.payload().split("\n"))
                    .map(String::trim).filter(s -> !s.isEmpty()).toList();
            System.out.printf("%nWeather data for area %s on %s:%n%n", areaCode, day.format(dayFormatter));
            if (rows.isEmpty()) {
                System.out.println("No weather data found for the selected area and day.");
                return false;
            }

            RemoteWeatherDataPrinter.printTableHeader();
            for (final String row : rows) {
                RemoteWeatherDataPrinter.printRow(row);
            }
            System.out.printf("%nTotal: %d record(s).%n", rows.size());
        } catch (final IOException ex) {
            System.out.println("Connection error: " + ex.getMessage());
        }
        return false;
    }

    private static void printAreaCodes(final String payload) {
        final List<String> lines = Arrays.stream(payload.split("\n"))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
        if (lines.isEmpty()) {
            System.out.println("Error: No Air Control Areas found. Please register an area first.");
            return;
        }
        System.out.println("\n--- Available Air Control Areas ---");
        for (final String line : lines) {
            System.out.println("Area Code: " + line.split("\\|", 2)[0]);
        }
        System.out.println("-----------------------------------");
    }

    @Override
    public String headline() {
        return "Consult Weather Data by Day (US043)";
    }
}
