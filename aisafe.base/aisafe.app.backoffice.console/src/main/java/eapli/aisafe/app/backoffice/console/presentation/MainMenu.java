package eapli.aisafe.app.backoffice.console.presentation;

import eapli.aisafe.app.backoffice.console.presentation.aircontrolarea.ListAirControlAreasAction;
import eapli.aisafe.app.backoffice.console.presentation.aircontrolarea.RegisterAirControlAreaAction;
import eapli.aisafe.app.backoffice.console.presentation.collaborator.CollaboratorMenus;
import eapli.aisafe.app.backoffice.console.presentation.collaborator.LocalCollaboratorMenuActions;
import eapli.aisafe.app.backoffice.console.presentation.flightroute.CreateRouteAction;
import eapli.aisafe.app.backoffice.console.presentation.flightroute.DeactivateRouteAction;
import eapli.aisafe.app.backoffice.console.presentation.aircraftmodel.AddEngineModelToAircraftModelAction;
import eapli.aisafe.app.backoffice.console.presentation.aircraftmodel.ListAircraftModelsAction;
import eapli.aisafe.app.backoffice.console.presentation.airport.CreateAirportAction;
import eapli.aisafe.app.backoffice.console.presentation.airport.ListAirportsAction;
import eapli.aisafe.app.backoffice.console.presentation.airtransportcompany.ListAirTransportCompaniesAction;
import eapli.aisafe.app.backoffice.console.presentation.airtransportcompany.RegisterAirTransportCompanyAction;
import eapli.aisafe.app.backoffice.console.presentation.companycollaborator.AddCustomerCollaboratorUserAction;
import eapli.aisafe.app.backoffice.console.presentation.companycollaborator.ListCustomerCollaboratorUsersAction;
import eapli.aisafe.app.backoffice.console.presentation.enginemodel.ListEngineModelsAction;
import eapli.aisafe.app.backoffice.console.presentation.enginemodel.RegisterEngineModelAction;
import eapli.aisafe.app.backoffice.console.presentation.aircraftmodel.RegisterAircraftModelAction;
import eapli.aisafe.app.backoffice.console.presentation.usermanagement.ActivateDeactivateUserAction;
import eapli.aisafe.app.backoffice.console.presentation.usermanagement.AddUserAction;
import eapli.aisafe.app.backoffice.console.presentation.usermanagement.ListUsersAction;
import eapli.aisafe.app.backoffice.console.presentation.weatherdata.BulkImportWeatherDataAction;
import eapli.aisafe.app.backoffice.console.presentation.weatherdata.ConsultWeatherDataAction;
import eapli.aisafe.app.backoffice.console.presentation.weatherdata.RegisterWeatherDataAction;
import eapli.aisafe.app.common.console.presentation.authz.LogoutAction;
import eapli.aisafe.app.common.console.presentation.authz.MyUserMenu;
import eapli.aisafe.app.common.console.presentation.authz.QuitApplicationAction;
import eapli.aisafe.app.backoffice.console.presentation.flightcontrol.GenerateMonthlyStatisticsReportAction;
import eapli.aisafe.app.backoffice.console.presentation.flightcontrol.SimulateFlightsInAreaAction;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.actions.Actions;
import eapli.framework.actions.menu.Menu;
import eapli.framework.actions.menu.MenuItem;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.ShowMessageAction;
import eapli.framework.presentation.console.menu.MenuItemRenderer;
import eapli.framework.presentation.console.menu.MenuRenderer;
import eapli.framework.presentation.console.menu.VerticalMenuRenderer;

/**
 * Root menu for the AISafe console (unified entry). Sub-menus follow the eCafeteria pattern:
 * {@link MyUserMenu} first, then role-authorized sections.
 */
public class MainMenu extends AbstractUI {

    private static final int EXIT_OPTION = 0;
    private static final int MY_USER_OPTION = 1;

    private static final int COMPANY_OPTION = 2;
    private static final int AREA_OPTION = 3;
    private static final int COMPANY_COLLAB_OPTION = 4;
    private static final int AIRPORT_OPTION = 5;
    private static final int ENGINE_MODEL_OPTION = 6;
    private static final int AIRCRAFT_MODEL_OPTION = 7;

    private static final int ADMIN_USERS_OPTION = 8;
    private static final int FCO_SIM_OPTION = 9;
    private static final int FLIGHT_PLANS_OPTION = 10;
    private static final int WEATHER_OPTION = 11;
    private static final int COMPANY_FLEET_OPTION = 12;
    private static final int COMPANY_ROUTES_OPTION = 13;
    private static final int PILOT_OPTION = 14;
    private static final int QUIT_APP_OPTION = 15;

    private static final String SEPARATOR = "--------------";
    private static final String RETURN_LABEL = "Return";

    private final AuthorizationService authz = AuthzRegistry.authorizationService();

    @Override
    public String headline() {
        return authz.session()
                .map(s -> "AISafe [ @" + s.authenticatedUser().identity() + " ]")
                .orElse("AISafe [ Anonymous ]");
    }

    @Override
    protected boolean doShow() {
        final Menu menu = buildMainMenu();
        final MenuRenderer renderer = new VerticalMenuRenderer(menu, MenuItemRenderer.DEFAULT);
        return renderer.render();
    }

    private Menu buildMainMenu() {
        final Menu mainMenu = new Menu();

        mainMenu.addSubMenu(MY_USER_OPTION, new MyUserMenu());

        mainMenu.addItem(MenuItem.separator(SEPARATOR));

        if (authz.isAuthenticatedUserAuthorizedTo(AISafeRoles.BACKOFFICE_OPERATOR)) {
            mainMenu.addSubMenu(COMPANY_OPTION, buildCompanyMenu());
            mainMenu.addSubMenu(AREA_OPTION, buildAreaMenu());
            mainMenu.addSubMenu(COMPANY_COLLAB_OPTION, buildCompanyCollaboratorUserMenu());
            mainMenu.addSubMenu(AIRPORT_OPTION, buildAirportMenu());
            mainMenu.addSubMenu(ENGINE_MODEL_OPTION, buildEngineModelMenu());
            mainMenu.addSubMenu(AIRCRAFT_MODEL_OPTION, buildAircraftModelMenu());
        }

        if (authz.isAuthenticatedUserAuthorizedTo(AISafeRoles.ADMIN)) {
            mainMenu.addSubMenu(ADMIN_USERS_OPTION, buildUsersMenu());
        }

        if (authz.isAuthenticatedUserAuthorizedTo(AISafeRoles.FLIGHT_CONTROL_OPERATOR)) {
            mainMenu.addSubMenu(FCO_SIM_OPTION, buildFlightControlMenu());
        }

        if (authz.isAuthenticatedUserAuthorizedTo(AISafeRoles.PILOT)) {
            mainMenu.addSubMenu(FLIGHT_PLANS_OPTION, buildFlightsMenu());
        }

        if (authz.isAuthenticatedUserAuthorizedTo(AISafeRoles.WEATHER_PERSON)) {
            mainMenu.addSubMenu(WEATHER_OPTION, buildWeatherData());
        }

        if (authz.isAuthenticatedUserAuthorizedTo(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR)) {
            mainMenu.addSubMenu(COMPANY_FLEET_OPTION, buildCompanyFleetMenu());
            mainMenu.addSubMenu(COMPANY_ROUTES_OPTION, buildCompanyRoutesMenu());
        }

        if (authz.isAuthenticatedUserAuthorizedTo(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR)) {
            mainMenu.addSubMenu(PILOT_OPTION, buildPilotUserMenu());
        }

        mainMenu.addItem(MenuItem.separator(SEPARATOR));
        mainMenu.addItem(QUIT_APP_OPTION, "Quit application", new QuitApplicationAction());
        mainMenu.addItem(EXIT_OPTION, "Log out", new LogoutAction());
        return mainMenu;
    }

    private Menu buildFlightsMenu() {
        return CollaboratorMenus.pilotFlightsMenu(LocalCollaboratorMenuActions.INSTANCE, false);
    }

    private Menu buildCompanyMenu() {
        final Menu menu = new Menu("Air Transport Company >");
        menu.addItem(1, "Register Air Transport Company", new RegisterAirTransportCompanyAction());
        menu.addItem(2, "List Air Transport Companies", new ListAirTransportCompaniesAction());
        menu.addItem(EXIT_OPTION, RETURN_LABEL, Actions.SUCCESS);
        return menu;
    }

    private Menu buildAreaMenu() {
        final Menu menu = new Menu("Air Control Area >");
        menu.addItem(1, "Register Air Control Area (US050)", new RegisterAirControlAreaAction());
        menu.addItem(2, "List Air Control Areas", new ListAirControlAreasAction());
        menu.addItem(EXIT_OPTION, RETURN_LABEL, Actions.SUCCESS);
        return menu;
    }

    private Menu buildAirportMenu() {
        final Menu menu = new Menu("Airport >");
        menu.addItem(1, "Register Airport (US052)", new CreateAirportAction());
        menu.addItem(2, "List Airports", new ListAirportsAction());
        menu.addItem(EXIT_OPTION, RETURN_LABEL, Actions.SUCCESS);
        return menu;
    }

    private Menu buildWeatherData() {
        final Menu menu = new Menu("Weather Data >");
        menu.addItem(1, "Register Weather Data (US041)", new RegisterWeatherDataAction());
        menu.addItem(2, "Bulk Import Weather Data (US042)", new BulkImportWeatherDataAction());
        menu.addItem(3, "Consult Weather Data by Day (US043)", new ConsultWeatherDataAction());
        menu.addItem(EXIT_OPTION, RETURN_LABEL, Actions.SUCCESS);
        return menu;
    }

    private Menu buildUsersMenu() {
        final Menu menu = new Menu("Users >");
        menu.addItem(1, "Register User (US031)", new AddUserAction());
        menu.addItem(2, "Disable/Enable User (US032)", new ActivateDeactivateUserAction());
        menu.addItem(3, "List Users (US033)", new ListUsersAction());
        menu.addItem(EXIT_OPTION, RETURN_LABEL, Actions.SUCCESS);
        return menu;
    }

    private Menu buildCompanyCollaboratorUserMenu() {
        final Menu menu = new Menu("Company Collaborators >");
        menu.addItem(1, "Register Customer Collaborator (US061)", new AddCustomerCollaboratorUserAction());
        menu.addItem(3, "List Customer Collaborators (US062)", new ListCustomerCollaboratorUsersAction());
        menu.addItem(EXIT_OPTION, RETURN_LABEL, Actions.SUCCESS);
        return menu;
    }

    private Menu buildEngineModelMenu() {
        final Menu menu = new Menu("Engine Model >");
        menu.addItem(1, "Register Engine Model (US056)", new RegisterEngineModelAction());
        menu.addItem(2, "List Engine Models", new ListEngineModelsAction());
        menu.addItem(EXIT_OPTION, RETURN_LABEL, Actions.SUCCESS);
        return menu;
    }

    private Menu buildAircraftModelMenu() {
        final Menu menu = new Menu("Aircraft Model >");
        menu.addItem(1, "Register Aircraft Model (US055)", new RegisterAircraftModelAction());
        menu.addItem(2, "List Aircraft Models", new ListAircraftModelsAction());
        menu.addItem(3, "Add Certified Engine Model to Existing Aircraft Model", new AddEngineModelToAircraftModelAction());
        menu.addItem(EXIT_OPTION, RETURN_LABEL, Actions.SUCCESS);
        return menu;
    }

    private Menu buildCompanyFleetMenu() {
        return CollaboratorMenus.fleetMenu(LocalCollaboratorMenuActions.INSTANCE);
    }

    private Menu buildCompanyRoutesMenu() {
        final Menu menu = new Menu("Routes >");
        menu.addItem(1, "Create Route (US073)", new CreateRouteAction());
        menu.addItem(2, "Deactivate Route (US074)", new DeactivateRouteAction());
        menu.addItem(EXIT_OPTION, RETURN_LABEL, Actions.SUCCESS);
        return menu;
    }

    private Menu buildPilotUserMenu() {
        return CollaboratorMenus.pilotCollaboratorsMenu(LocalCollaboratorMenuActions.INSTANCE);
    }

    private Menu buildFlightControlMenu() {
        final Menu menu = new Menu("Flight Control >");
        menu.addItem(1, "Simulate Flights in Area (US100)", new SimulateFlightsInAreaAction());
        menu.addItem(2, "Generate Monthly Statistics Report (US112)", new GenerateMonthlyStatisticsReportAction());
        menu.addItem(EXIT_OPTION, RETURN_LABEL, Actions.SUCCESS);
        return menu;
    }
}
