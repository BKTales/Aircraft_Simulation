package eapli.aisafe.app.backoffice.console.presentation.collaborator;

import eapli.framework.actions.Action;
import eapli.framework.actions.Actions;
import eapli.framework.actions.menu.Menu;
import eapli.framework.actions.menu.MenuItem;

public final class CollaboratorMenus {

    private static final int EXIT_OPTION = 0;

    private CollaboratorMenus() {}

    public static Menu fleetMenu(final FleetMenuActions actions) {
        final Menu menu = new Menu("Fleet >");
        menu.addItem(1, "Register Aircraft (US070)", actions.registerAircraft());
        menu.addItem(2, "Decommission Aircraft (US071)", actions.decommissionAircraft());
        menu.addItem(3, "List company fleet (US072)", actions.listFleet());
        menu.addItem(EXIT_OPTION, "Return", Actions.SUCCESS);
        return menu;
    }

    public static Menu pilotCollaboratorsMenu(final PilotCollaboratorMenuActions actions) {
        final Menu menu = new Menu("Pilot Collaborators >");
        menu.addItem(4, "Register Pilot Collaborator (US075)", actions.registerPilot());
        menu.addItem(5, "List Pilot Collaborator (US076)", actions.listPilots());
        menu.addItem(6, "Remove Pilot from Roster (US077)", actions.removePilot());
        menu.addItem(EXIT_OPTION, "Return", Actions.SUCCESS);
        return menu;
    }

    public static Menu routesMenu(final RouteMenuActions actions) {
        final Menu menu = new Menu("Routes (US073/US074) >");
        menu.addItem(1, "Create route (US073)", actions.createRoute());
        menu.addItem(2, "Deactivate route (US074)", actions.deactivateRoute());
        menu.addItem(EXIT_OPTION, "Return", Actions.SUCCESS);
        return menu;
    }

    public static Menu pilotFlightsMenu(final PilotFlightMenuActions actions, final boolean remote) {
        final Menu menu = new Menu("Flights >");
        int option = 1;
        menu.addItem(option++, "Create flight plan (US080)", actions.createFlightPlan());
        menu.addItem(option++, "Import flight plan from file (US081/US121)", actions.importFlightPlan());
        if (remote) {
            menu.addItem(option++, "List my flights", actions.listMyFlights());
        }
        menu.addItem(option++, "Attach Weather Data to Flight (US082)", actions.attachWeather());
        menu.addItem(option++, "Validate flight plan (US085)", actions.validateFlightPlan());
        menu.addItem(EXIT_OPTION, "Return", Actions.SUCCESS);
        return menu;
    }

    public static Menu pilotRemoteRootMenu(final PilotFlightMenuActions actions, final Action logoutAction) {
        final Menu menu = pilotFlightsMenu(actions, true);
        menu.addItem(98, "Logout", logoutAction);
        return menu;
    }

    public static Menu weatherDataMenu(final WeatherMenuActions actions) {
        final Menu menu = new Menu("Weather Data >");
        menu.addItem(1, "Register Weather Data (US041)", actions.registerWeatherData());
        menu.addItem(2, "Bulk Import Weather Data (US042)", actions.bulkImportWeatherData());
        menu.addItem(3, "Consult Weather Data by Day (US043)", actions.consultWeatherData());
        menu.addItem(EXIT_OPTION, "Return", Actions.SUCCESS);
        return menu;
    }

    public static Menu weatherRemoteRootMenu(final WeatherMenuActions actions, final Action logoutAction) {
        final Menu menu = weatherDataMenu(actions);
        menu.addItem(98, "Logout", logoutAction);
        return menu;
    }

    public static Menu atccRootMenu(final FleetMenuActions fleet,
                                    final PilotCollaboratorMenuActions pilots,
                                    final RouteMenuActions routes,
                                    final Action logoutAction) {
        final Menu menu = new Menu();
        menu.addSubMenu(1, fleetMenu(fleet));
        menu.addSubMenu(2, pilotCollaboratorsMenu(pilots));
        menu.addSubMenu(3, routesMenu(routes));
        menu.addItem(4, "Logout", logoutAction);
        menu.addItem(EXIT_OPTION, "Return", Actions.SUCCESS);
        return menu;
    }
}
