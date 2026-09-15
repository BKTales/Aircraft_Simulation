package eapli.aisafe.rcomp.tcpclient.presentation.atcc.fleet;

import eapli.framework.actions.Action;

public final class DecommissionAircraftRemoteAction implements Action {

    @Override
    public boolean execute() {
        return new DecommissionAircraftRemoteUI().show();
    }
}
