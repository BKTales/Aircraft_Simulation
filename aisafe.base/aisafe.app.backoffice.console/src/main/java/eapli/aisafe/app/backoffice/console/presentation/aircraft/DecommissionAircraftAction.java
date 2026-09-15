package eapli.aisafe.app.backoffice.console.presentation.aircraft;

import eapli.framework.actions.Action;

import java.util.Objects;
import java.util.function.Supplier;

public class DecommissionAircraftAction implements Action {

    private final Supplier<Boolean> uiShow;

    public DecommissionAircraftAction() {
        this(() -> new DecommissionAircraftUI().show());
    }

    DecommissionAircraftAction(final Supplier<Boolean> uiShow) {
        this.uiShow = Objects.requireNonNull(uiShow, "uiShow");
    }

    @Override
    public boolean execute() {
        return uiShow.get();
    }
}
