package eapli.aisafe.rcomp.tcpclient.presentation;

import eapli.aisafe.app.backoffice.console.presentation.collaborator.FleetMenuActions;
import eapli.aisafe.app.backoffice.console.presentation.collaborator.PilotCollaboratorMenuActions;
import eapli.aisafe.app.backoffice.console.presentation.collaborator.PilotFlightMenuActions;
import eapli.aisafe.app.backoffice.console.presentation.collaborator.RouteMenuActions;
import eapli.aisafe.rcomp.protocol.AtccOpcodes;
import eapli.aisafe.rcomp.protocol.PilotOpcodes;
import eapli.aisafe.rcomp.tcpclient.presentation.atcc.fleet.DecommissionAircraftRemoteAction;
import eapli.aisafe.rcomp.tcpclient.presentation.atcc.fleet.ListFleetRemoteAction;
import eapli.aisafe.rcomp.tcpclient.presentation.atcc.fleet.RegisterAircraftRemoteAction;
import eapli.aisafe.rcomp.tcpclient.presentation.atcc.pilotcollaborator.AddPilotCollaboratorRemoteAction;
import eapli.aisafe.rcomp.tcpclient.presentation.atcc.pilotcollaborator.RemovePilotRemoteAction;
import eapli.aisafe.rcomp.tcpclient.presentation.atcc.routes.CreateRouteRemoteAction;
import eapli.aisafe.rcomp.tcpclient.presentation.atcc.routes.DeactivateRouteRemoteAction;
import eapli.aisafe.rcomp.tcpclient.presentation.pilot.flightplan.AttachWeatherRemoteAction;
import eapli.aisafe.rcomp.tcpclient.presentation.pilot.flightplan.CreateFlightPlanRemoteAction;
import eapli.aisafe.rcomp.tcpclient.presentation.pilot.flightplan.ImportFlightPlanRemoteAction;
import eapli.aisafe.rcomp.tcpclient.presentation.pilot.flightplan.ValidateFlightPlanRemoteAction;
import eapli.framework.actions.Action;

/**
 * Remote menu wiring: {@link eapli.aisafe.app.backoffice.console.presentation.collaborator.CollaboratorMenus}
 * with TCP actions (no AuthzRegistry / DB on the client).
 */
public final class RemoteCollaboratorMenuActions implements
        FleetMenuActions, PilotCollaboratorMenuActions, PilotFlightMenuActions, RouteMenuActions {

    public static final RemoteCollaboratorMenuActions INSTANCE = new RemoteCollaboratorMenuActions();

    private RemoteCollaboratorMenuActions() {}

    @Override
    public Action registerAircraft() {
        return new RegisterAircraftRemoteAction();
    }

    @Override
    public Action decommissionAircraft() {
        return new DecommissionAircraftRemoteAction();
    }

    @Override
    public Action listFleet() {
        return new ListFleetRemoteAction();
    }

    @Override
    public Action registerPilot() {
        return new AddPilotCollaboratorRemoteAction();
    }

    @Override
    public Action listPilots() {
        return new TcpRequestAction(AtccOpcodes.LIST_PILOT_ROSTER, "");
    }

    @Override
    public Action removePilot() {
        return new RemovePilotRemoteAction();
    }

    @Override
    public Action importFlightPlan() {
        return new ImportFlightPlanRemoteAction();
    }

    @Override
    public Action attachWeather() {
        return new AttachWeatherRemoteAction();
    }

    @Override
    public Action listMyFlights() {
        return new TcpRequestAction(PilotOpcodes.LIST_MY_FLIGHTS, "");
    }

    @Override
    public Action createFlightPlan() {
        return new CreateFlightPlanRemoteAction();
    }

    @Override
    public Action validateFlightPlan() {
        return new ValidateFlightPlanRemoteAction();
    }

    @Override
    public Action createRoute() {
        return new CreateRouteRemoteAction();
    }

    @Override
    public Action deactivateRoute() {
        return new DeactivateRouteRemoteAction();
    }

    private static Action noop() {
        return () -> false;
    }
}
