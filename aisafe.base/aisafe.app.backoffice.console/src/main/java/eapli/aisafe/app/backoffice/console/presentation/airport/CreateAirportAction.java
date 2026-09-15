package eapli.aisafe.app.backoffice.console.presentation.airport;

import eapli.framework.actions.Action;

/**
 * Menu action that triggers the {@link CreateAirportUI}.
 *
 * @author aisafe team
 */
public class CreateAirportAction implements Action {

    @Override
    public boolean execute() {
        return new CreateAirportUI().show();
    }
}
