package eapli.aisafe.app.backoffice.console.presentation.airport;

import eapli.framework.actions.Action;

/**
 * Menu action that triggers the {@link ListAirportsUI}.
 *
 * @author aisafe team
 */
public class ListAirportsAction implements Action {

    @Override
    public boolean execute() {
        return new ListAirportsUI().show();
    }
}
