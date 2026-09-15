package eapli.aisafe.rcomp.tcpclient.presentation.atcc.routes;

import eapli.framework.actions.Action;

public final class DeactivateRouteRemoteAction implements Action {

    @Override
    public boolean execute() {
        return new DeactivateRouteRemoteUI().show();
    }
}
