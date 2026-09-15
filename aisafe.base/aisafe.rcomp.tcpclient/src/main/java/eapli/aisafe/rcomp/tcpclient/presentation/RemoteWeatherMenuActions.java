package eapli.aisafe.rcomp.tcpclient.presentation;

import eapli.aisafe.app.backoffice.console.presentation.collaborator.WeatherMenuActions;
import eapli.aisafe.rcomp.tcpclient.presentation.weather.BulkImportWeatherRemoteAction;
import eapli.aisafe.rcomp.tcpclient.presentation.weather.ConsultWeatherRemoteAction;
import eapli.aisafe.rcomp.tcpclient.presentation.weather.RegisterWeatherRemoteAction;
import eapli.framework.actions.Action;

public enum RemoteWeatherMenuActions implements WeatherMenuActions {
    INSTANCE;

    @Override
    public Action registerWeatherData() {
        return new RegisterWeatherRemoteAction();
    }

    @Override
    public Action bulkImportWeatherData() {
        return new BulkImportWeatherRemoteAction();
    }

    @Override
    public Action consultWeatherData() {
        return new ConsultWeatherRemoteAction();
    }
}
