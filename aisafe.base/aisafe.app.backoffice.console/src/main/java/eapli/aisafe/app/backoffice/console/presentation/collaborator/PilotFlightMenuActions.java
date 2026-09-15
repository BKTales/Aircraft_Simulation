package eapli.aisafe.app.backoffice.console.presentation.collaborator;

import eapli.framework.actions.Action;

public interface PilotFlightMenuActions {

    Action importFlightPlan();

    Action attachWeather();

    Action listMyFlights();

    Action createFlightPlan();

    Action validateFlightPlan();
}
