package eapli.aisafe.app.backoffice.console.presentation.aircontrolarea;

import eapli.framework.actions.Action;

/**
 * Menu action that launches the {@link ListAirControlAreasUI}.
 *
 * @author aisafe team
 */
public class ListAirControlAreasAction implements Action {

    @Override
    public boolean execute() {
        return new ListAirControlAreasUI().show();
    }
}
