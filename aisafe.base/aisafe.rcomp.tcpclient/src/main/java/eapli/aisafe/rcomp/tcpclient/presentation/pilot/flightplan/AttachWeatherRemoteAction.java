package eapli.aisafe.rcomp.tcpclient.presentation.pilot.flightplan;

import eapli.framework.actions.Action;

public final class AttachWeatherRemoteAction implements Action {

    @Override
    public boolean execute() {
        return new AttachWeatherRemoteUI().show();
    }
}
