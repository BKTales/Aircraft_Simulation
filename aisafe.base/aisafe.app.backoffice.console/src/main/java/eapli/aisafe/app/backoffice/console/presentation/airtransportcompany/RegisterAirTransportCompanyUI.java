package eapli.aisafe.app.backoffice.console.presentation.airtransportcompany;

import eapli.aisafe.airtransportcompanymanagement.application.ICAOCodeAlreadyExistsException;
import eapli.aisafe.airtransportcompanymanagement.application.IATACodeAlreadyExistsException;
import eapli.aisafe.airtransportcompanymanagement.application.RegisterAirTransportCompanyController;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

public class RegisterAirTransportCompanyUI extends AbstractUI {

    private final RegisterAirTransportCompanyController controller = new RegisterAirTransportCompanyController();

    @Override
    protected boolean doShow() {
        final String name = Console.readLine("Company name");
        final String iata = Console.readLine("IATA code (2 letters)");
        final String icao = Console.readLine("ICAO code (2-3 letters)");

        try {
            controller.registerCompany(name, iata, icao);
            System.out.println("Air transport company registered successfully.");
        } catch (final IATACodeAlreadyExistsException | ICAOCodeAlreadyExistsException ex) {
            System.out.println(ex.getMessage());
        } catch (final IllegalArgumentException ex) {
            System.out.println("Invalid input: " + ex.getMessage());
        } catch (final IllegalStateException ex) {
            System.out.println("Operation not allowed: " + ex.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "Register Air Transport Company";
    }
}
