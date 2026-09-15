package eapli.aisafe.app.backoffice.console.presentation.airport;

import eapli.aisafe.airportmanagement.application.CreateAirportController;
import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

/**
 * Console UI for registering a new airport (US052).
 *
 * <p>The air control area is resolved automatically from the provided coordinates.
 * No manual area selection is required.</p>
 *
 * @author aisafe team
 */
@SuppressWarnings("squid:S106")
public class CreateAirportUI extends AbstractUI {

    private final CreateAirportController controller = new CreateAirportController();

    @Override
    protected boolean doShow() {
        final String iata = Console.readLine("IATA Code (3 letters):");
        final String icao = Console.readLine("ICAO Code (4 alphanumeric):");
        final double lat = Console.readDouble("Latitude (-90 to 90):");
        final double lon = Console.readDouble("Longitude (-180 to 180):");
        final double elev = Console.readDouble("Elevation (metres above sea level, >= 0):");

        try {
            final Airport airport = controller.createAirport(iata, icao, lat, lon, elev);
            System.out.println("\nAirport " + airport + " successfully registered.");
        } catch(final Exception e) {
            System.out.println("\nError: " + e.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "Register Airport";
    }
}
