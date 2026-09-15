package eapli.aisafe.app.backoffice.console.presentation.airtransportcompany;

import eapli.aisafe.airtransportcompanymanagement.application.ListAirTransportCompaniesController;
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.framework.presentation.console.AbstractUI;

/**
 * Console UI for listing all registered air transport companies (US062-adjacent).
 */
@SuppressWarnings("squid:S106")
public class ListAirTransportCompaniesUI extends AbstractUI {

    private final ListAirTransportCompaniesController controller = new ListAirTransportCompaniesController();

    @Override
    protected boolean doShow() {
        final Iterable<AirTransportCompany> companies = controller.allCompanies();

        System.out.printf("%-30s %-6s %-6s%n", "Name", "IATA", "ICAO");
        System.out.println("-".repeat(44));

        boolean any = false;
        for(final AirTransportCompany c : companies) {
            System.out.printf("%-30s %-6s %-6s%n",
                    c.companyName(),
                    c.identity(),
                    c.icaoCode());
            any = true;
        }

        if(!any){
            System.out.println("No air transport companies registered.");
        }

        return false;
    }

    @Override
    public String headline() {
        return "List Air Transport Companies";
    }
}
