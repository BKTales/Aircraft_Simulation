package eapli.aisafe.app.backoffice.console.presentation.aircraft;

import eapli.framework.actions.Action;

import java.util.Objects;
import java.util.function.Supplier;

public class ListCompanyFleetAction implements Action {

    private final Supplier<Boolean> uiShow;

    public ListCompanyFleetAction() {
        this(() -> new ListCompanyFleetUI().show());
    }

    private ListCompanyFleetAction(final Supplier<Boolean> uiShow) {
        this.uiShow = Objects.requireNonNull(uiShow, "uiShow");
    }

    public ListCompanyFleetAction(final ListCompanyFleetUI ui) {
        this(() -> Objects.requireNonNull(ui, "ui").show());
    }

    @Override
    public boolean execute() {
        return uiShow.get();
    }
}
