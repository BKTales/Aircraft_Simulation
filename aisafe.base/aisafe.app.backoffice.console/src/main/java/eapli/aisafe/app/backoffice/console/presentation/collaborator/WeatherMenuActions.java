package eapli.aisafe.app.backoffice.console.presentation.collaborator;

import eapli.framework.actions.Action;

public interface WeatherMenuActions {
    Action registerWeatherData();
    Action bulkImportWeatherData();
    Action consultWeatherData();
}
