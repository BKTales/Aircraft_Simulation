package eapli.aisafe.app.backoffice.console.presentation.companycollaborator;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.flightcontroloperatormanagement.application.ListFlightControlOperatorsUsersController;
import eapli.aisafe.flightcontroloperatormanagement.domain.FlightControlOperatorUser;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractListUI;
import eapli.framework.presentation.console.SelectWidget;
import eapli.framework.visitor.Visitor;

import java.util.List;
import java.util.stream.StreamSupport;

@SuppressWarnings({ "squid:S106" })
public class ListFlightControlOperatorUsersUI extends AbstractListUI<FlightControlOperatorUser> {
    private final ListFlightControlOperatorsUsersController theController = new ListFlightControlOperatorsUsersController();

    @Override
    public String headline() {
        return "List Users";
    }

    @Override
    protected String emptyMessage() {
        return "No data.";
    }

    @Override
    protected Iterable<FlightControlOperatorUser> elements() {

        final Iterable<AirControlArea> areas = theController.allAirControlAreas();

        if (!areas.iterator().hasNext()) {
            throw new IllegalStateException("No air control areas registered.");
        }

        final SelectWidget<AirControlArea> selector =
                new SelectWidget<>("Select Air Control Area:", areas,
                        new AirControlAreaPrinter());

        selector.show();
        final AirControlArea selected = selector.selectedElement();

        if (selected == null) {
            throw new IllegalStateException("No area selected.");
        }

        return theController.activeFlightControlOperatorsUsersForCompany(
                selected.identity().toString()
        );
    }

    @Override
    protected Visitor<FlightControlOperatorUser> elementPrinter() {
        return new FlightControlOperatorUserPrinter();
    }

    @Override
    protected String elementName() {
        return "User";
    }

    @Override
    protected String listHeader() {
        return String.format(
                "#  %-30s %-15s %-15s %-10s %-12s %-12s",
                "EMAIL", "F. NAME", "L. NAME", "STATUS", "ACCESS", "EXPIRY"
        );
    }
}
