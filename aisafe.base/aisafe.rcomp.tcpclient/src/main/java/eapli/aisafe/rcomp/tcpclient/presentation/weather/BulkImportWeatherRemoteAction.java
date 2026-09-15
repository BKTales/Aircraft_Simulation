package eapli.aisafe.rcomp.tcpclient.presentation.weather;

import eapli.framework.actions.Action;

public final class BulkImportWeatherRemoteAction implements Action {

    @Override
    public boolean execute() {
        return new BulkImportWeatherRemoteUI().show();
    }
}
