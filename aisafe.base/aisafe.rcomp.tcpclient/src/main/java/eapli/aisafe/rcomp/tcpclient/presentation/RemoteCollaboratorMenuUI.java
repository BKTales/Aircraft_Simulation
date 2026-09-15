package eapli.aisafe.rcomp.tcpclient.presentation;

import eapli.aisafe.app.backoffice.console.presentation.collaborator.CollaboratorMenus;
import eapli.aisafe.rcomp.tcpclient.RemoteAppContext;
import eapli.aisafe.rcomp.tcpclient.RemoteProfile;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.menu.MenuItemRenderer;
import eapli.framework.presentation.console.menu.MenuRenderer;
import eapli.framework.presentation.console.menu.VerticalMenuRenderer;

public final class RemoteCollaboratorMenuUI extends AbstractUI {

    @Override
    protected boolean doShow() {
        final MenuRenderer renderer = new VerticalMenuRenderer(buildMenu(), MenuItemRenderer.DEFAULT);
        return renderer.render();
    }

    private eapli.framework.actions.menu.Menu buildMenu() {
        final RemoteCollaboratorMenuActions actions = RemoteCollaboratorMenuActions.INSTANCE;
        final RemoteProfile profile = RemoteAppContext.require().profile();
        if (profile == RemoteProfile.ATCC) {
            return CollaboratorMenus.atccRootMenu(actions, actions, actions, new RemoteLogoutAction());
        }
        if (profile == RemoteProfile.WEATHER) {
            return CollaboratorMenus.weatherRemoteRootMenu(RemoteWeatherMenuActions.INSTANCE, new RemoteLogoutAction());
        }
        return CollaboratorMenus.pilotRemoteRootMenu(actions, new RemoteLogoutAction());
    }

    @Override
    public String headline() {
        final var ctx = RemoteAppContext.require();
        if (ctx.profile() == RemoteProfile.ATCC) {
            return "AISafe Remote [ @" + ctx.loggedInUser() + " | " + ctx.profile().loginToken() + " ]";
        }
        if (ctx.profile() == RemoteProfile.WEATHER) {
            return "AISafe Remote Weather [ @" + ctx.loggedInUser() + " ]";
        }
        return "AISafe Remote Pilot [ @" + ctx.loggedInUser() + " ]";
    }
}
