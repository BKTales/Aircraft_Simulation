package eapli.aisafe.rcomp.tcpclient.presentation.weather;

import eapli.framework.actions.Action;

public final class RegisterWeatherRemoteAction implements Action {

    @Override
    public boolean execute() {
        return new RegisterWeatherRemoteUI().show();
    }
}
