package eapli.aisafe.app.backoffice.console.presentation.collaborator;

import eapli.aisafe.app.backoffice.console.presentation.aircraft.DecommissionAircraftAction;
import eapli.aisafe.app.backoffice.console.presentation.aircraft.ListCompanyFleetAction;
import eapli.aisafe.app.backoffice.console.presentation.aircraft.RegisterAircraftAction;
import eapli.aisafe.app.backoffice.console.presentation.companycollaborator.AddPilotCollaboratorUserAction;
import eapli.aisafe.app.backoffice.console.presentation.companycollaborator.ListPilotUsersAction;
import eapli.aisafe.app.backoffice.console.presentation.companycollaborator.RemovePilotCollaboratorAction;
import eapli.aisafe.app.backoffice.console.presentation.flight.AttachWeatherToFlightAction;
import eapli.aisafe.app.backoffice.console.presentation.flight.CreateFlightPlanAction;
import eapli.aisafe.app.backoffice.console.presentation.flight.ImportFlightPlanFromFileAction;
import eapli.aisafe.app.backoffice.console.presentation.flight.ValidateFlightPlanAction;
import eapli.framework.actions.Action;
import eapli.framework.actions.Actions;

public final class LocalCollaboratorMenuActions implements
        FleetMenuActions, PilotCollaboratorMenuActions, PilotFlightMenuActions, RouteMenuActions {

    public static final LocalCollaboratorMenuActions INSTANCE = new LocalCollaboratorMenuActions();

    private LocalCollaboratorMenuActions() {}

    @Override
    public Action registerAircraft() {
        return new RegisterAircraftAction();
    }

    @Override
    public Action decommissionAircraft() {
        return new DecommissionAircraftAction();
    }

    @Override
    public Action listFleet() {
        return new ListCompanyFleetAction();
    }

    @Override
    public Action registerPilot() {
        return new AddPilotCollaboratorUserAction();
    }

    @Override
    public Action listPilots() {
        return new ListPilotUsersAction();
    }

    @Override
    public Action removePilot() {
        return new RemovePilotCollaboratorAction();
    }

    @Override
    public Action importFlightPlan() {
        return new ImportFlightPlanFromFileAction();
    }

    @Override
    public Action attachWeather() {
        return new AttachWeatherToFlightAction();
    }

    @Override
    public Action listMyFlights() {
        return noop();
    }

    @Override
    public Action createFlightPlan() {
        return new CreateFlightPlanAction();
    }

    @Override
    public Action validateFlightPlan() {
        return new ValidateFlightPlanAction();
    }

    @Override
    public Action createRoute() {
        return () -> {
            System.out.println("US073 Create route is not available on the local console.");
            return false;
        };
    }

    @Override
    public Action deactivateRoute() {
        return () -> {
            System.out.println("US074 Deactivate route is not available on the local console.");
            return false;
        };
    }

    private static Action noop() {
        return () -> false;
    }
}
