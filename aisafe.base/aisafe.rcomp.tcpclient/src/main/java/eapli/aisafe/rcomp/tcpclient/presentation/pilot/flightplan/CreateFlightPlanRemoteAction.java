package eapli.aisafe.rcomp.tcpclient.presentation.pilot.flightplan;

import eapli.framework.actions.Action;

public final class CreateFlightPlanRemoteAction implements Action {

    @Override
    public boolean execute() {
        return new CreateFlightPlanRemoteUI().show();
    }
}
