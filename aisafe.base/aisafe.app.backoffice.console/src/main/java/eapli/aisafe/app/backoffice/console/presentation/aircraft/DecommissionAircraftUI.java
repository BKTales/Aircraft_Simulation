package eapli.aisafe.app.backoffice.console.presentation.aircraft;

import eapli.aisafe.aircraftmanagement.application.AircraftAlreadyDecommissionedException;
import eapli.aisafe.aircraftmanagement.application.AircraftHasPendingFlightsException;
import eapli.aisafe.aircraftmanagement.application.AircraftNotFoundException;
import eapli.aisafe.aircraftmanagement.application.AircraftNotInCompanyFleetException;
import eapli.aisafe.aircraftmanagement.application.DecommissionAircraftController;
import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("squid:S106")
public class DecommissionAircraftUI extends AbstractUI {

    private final DecommissionAircraftController controller;

    public DecommissionAircraftUI() {
        this(new DecommissionAircraftController());
    }

    public DecommissionAircraftUI(final DecommissionAircraftController controller) {
        this.controller = Objects.requireNonNull(controller, "controller");
    }

    @Override
    protected boolean doShow() {
        final Iterable<Aircraft> fleet = controller.listActiveCompanyAircraft();
        final List<Aircraft> choices = new ArrayList<>();
        fleet.forEach(choices::add);
        choices.sort(Comparator.comparing(a -> a.identity().toString()));
        if (choices.isEmpty()) {
            System.out.println("No active aircraft in your company's fleet.");
            return false;
        }

        System.out.println("Select an aircraft to decommission (registration):");
        DecommissionFleetAircraftPrinter.printRegistrationHeader();
        final SelectWidget<Aircraft> selector =
                new SelectWidget<>("", choices, new DecommissionFleetAircraftPrinter());
        selector.show();
        final Aircraft chosen = selector.selectedElement();
        if (chosen == null) {
            return false;
        }

        try {
            controller.decommissionAircraft(chosen.identity().toString());
            System.out.println("Aircraft decommissioned successfully. It can no longer be assigned to new flights.");
        } catch (final AircraftNotFoundException | AircraftNotInCompanyFleetException
                       | AircraftAlreadyDecommissionedException | AircraftHasPendingFlightsException ex) {
            System.out.println(ex.getMessage());
        } catch (final IllegalArgumentException | IllegalStateException ex) {
            System.out.println(ex.getMessage());
        }
        return false;
    }

    @Override
    public String headline() {
        return "Decommission aircraft (US071)";
    }
}
