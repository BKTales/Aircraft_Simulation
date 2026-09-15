package eapli.aisafe.app.backoffice.console.presentation.weatherdata;

import eapli.framework.actions.Action;

public class BulkImportWeatherDataAction implements Action {
    @Override
    public boolean execute() {
        return new BulkImportWeatherDataUI().show();
    }
}
