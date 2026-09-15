package eapli.aisafe.app.backoffice.console.presentation.airtransportcompany;

import eapli.framework.actions.Action;

public class RegisterAirTransportCompanyAction implements Action {
    @Override
    public boolean execute() {
        return new RegisterAirTransportCompanyUI().show();
    }
}
