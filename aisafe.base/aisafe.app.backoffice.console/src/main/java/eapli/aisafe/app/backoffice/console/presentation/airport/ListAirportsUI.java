package eapli.aisafe.app.backoffice.console.presentation.airport;

import eapli.aisafe.airportmanagement.application.ListAirportsController;
import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.framework.presentation.console.AbstractUI;

/**
 * Console UI for listing all registered airports.
 *
 * @author aisafe team
 */
@SuppressWarnings("squid:S106")
public class ListAirportsUI extends AbstractUI {

    private final ListAirportsController controller = new ListAirportsController();

    @Override
    protected boolean doShow() {
        final Iterable<Airport> airports = controller.allAirports();

        System.out.printf("%-6s %-6s %-10s %-11s %-10s %-12s%n",
                "IATA", "ICAO", "Latitude", "Longitude", "Elev (m)", "Area Code");
        System.out.println("-".repeat(58));

        boolean any = false;
        for(final Airport a : airports){
            System.out.printf("%-6s %-6s %-10.4f %-11.4f %-10.1f %-12s%n",
                    a.identity(),
                    a.icaoCode(),
                    a.coordinates().latitude(),
                    a.coordinates().longitude(),
                    a.coordinates().elevationMeters(),
                    a.airControlAreaCode());
            any = true;
        }

        if(!any){
            System.out.println("No airports registered.");
        }

        return false;
    }

    @Override
    public String headline() {
        return "List Airports";
    }
}
