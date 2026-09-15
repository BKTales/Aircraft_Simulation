package eapli.aisafe.rcomp.tcpclient.presentation.weather;

import eapli.framework.actions.Action;

public final class ConsultWeatherRemoteAction implements Action {

    @Override
    public boolean execute() {
        return new ConsultWeatherRemoteUI().show();
    }
}
