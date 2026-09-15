package eapli.aisafe.rcomp.tcpclient.presentation.pilot.flightplan;

import eapli.framework.actions.Action;

public final class ImportFlightPlanRemoteAction implements Action {

    @Override
    public boolean execute() {
        return new ImportFlightPlanRemoteUI().show();
    }
}
