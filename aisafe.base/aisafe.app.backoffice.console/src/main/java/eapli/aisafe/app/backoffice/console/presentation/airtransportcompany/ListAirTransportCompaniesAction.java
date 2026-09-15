package eapli.aisafe.app.backoffice.console.presentation.airtransportcompany;

import eapli.framework.actions.Action;

/**
 * Menu action that triggers the {@link ListAirTransportCompaniesUI}.
 */
public class ListAirTransportCompaniesAction implements Action {

    @Override
    public boolean execute() {
        return new ListAirTransportCompaniesUI().show();
    }
}
