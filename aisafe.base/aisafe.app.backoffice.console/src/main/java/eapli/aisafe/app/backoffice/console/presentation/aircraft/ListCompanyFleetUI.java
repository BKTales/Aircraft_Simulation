package eapli.aisafe.app.backoffice.console.presentation.aircraft;

import eapli.aisafe.aircraftmanagement.application.FleetListCriteria;
import eapli.aisafe.aircraftmanagement.application.FleetNumericComparison;
import eapli.aisafe.aircraftmanagement.application.ListFleetController;
import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * US072–US072d — list the collaborator company's aircraft fleet with optional filters.
 */
@SuppressWarnings("squid:S106")
public class ListCompanyFleetUI extends AbstractUI {

    private final ListFleetController controller;

    public ListCompanyFleetUI() {
        this(new ListFleetController());
    }

    public ListCompanyFleetUI(final ListFleetController controller) {
        this.controller = Objects.requireNonNull(controller, "controller");
    }

    @Override
    protected boolean doShow() {
        System.out.println("1 - Full fleet (US072)");
        System.out.println("2 - Filter by aircraft model (US072a)");
        System.out.println("3 - Filter by maker (US072b)");
        System.out.println("4 - Filter by passenger capacity (US072c)");
        System.out.println("5 - Filter by age (US072d)");
        System.out.println("0 - Cancel");

        final int option = Console.readInteger("Option");
        if (option == 0) {
            return false;
        }

        final FleetListCriteria criteria = buildCriteria(option);
        if (criteria == null) {
            return false;
        }

        printFleet(controller.listFleet(criteria));
        return false;
    }

    private void printFleet(final Iterable<Aircraft> fleet) {
        final List<Aircraft> sorted = new ArrayList<>();
        fleet.forEach(sorted::add);
        sorted.sort(Comparator.comparing(a -> a.identity().toString()));

        if (sorted.isEmpty()) {
            System.out.println("No aircraft match the selected criteria.");
            return;
        }

        CompanyFleetAircraftPrinter.printHeader();
        final CompanyFleetAircraftPrinter printer = new CompanyFleetAircraftPrinter();
        sorted.forEach(printer::visit);
    }

    private FleetListCriteria buildCriteria(final int option) {
        return switch (option) {
            case 1 -> FleetListCriteria.unfiltered();
            case 2 -> criteriaByModel();
            case 3 -> criteriaByMaker();
            case 4 -> criteriaByPassengerCapacity();
            case 5 -> criteriaByAge();
            default -> {
                System.out.println("Invalid option.");
                yield null;
            }
        };
    }

    private FleetListCriteria criteriaByModel() {
        final List<AircraftModel> models = new ArrayList<>();
        controller.modelsUsedInCompanyFleet().forEach(models::add);
        if (models.isEmpty()) {
            System.out.println("No aircraft models in your company's fleet.");
            return null;
        }

        System.out.println("Select aircraft model used in your fleet:");
        System.out.printf("%-12s %s%n", "Model ID", "Name");
        final SelectWidget<AircraftModel> selector =
                new SelectWidget<>("", models, new CompanyFleetModelPrinter());
        selector.show();
        final AircraftModel chosen = selector.selectedElement();
        if (chosen == null) {
            return null;
        }
        return FleetListCriteria.byModel(chosen.identity());
    }

    private FleetListCriteria criteriaByMaker() {
        final List<ManufacturerId> makers = new ArrayList<>();
        controller.manufacturersUsedInCompanyFleet().forEach(makers::add);
        if (makers.isEmpty()) {
            System.out.println("No makers found for aircraft in your company's fleet.");
            return null;
        }

        System.out.println("Select maker of aircraft models used in your fleet:");
        final SelectWidget<ManufacturerId> selector =
                new SelectWidget<>("", makers, new CompanyFleetManufacturerPrinter());
        selector.show();
        final ManufacturerId chosen = selector.selectedElement();
        if (chosen == null) {
            return null;
        }
        return FleetListCriteria.byManufacturer(chosen);
    }

    private FleetListCriteria criteriaByPassengerCapacity() {
        final int capacity = Console.readInteger("Total passenger capacity (seats)");
        final FleetNumericComparison comparison = readNumericComparison();
        if (comparison == null) {
            return null;
        }
        return FleetListCriteria.byPassengerCapacity(capacity, comparison);
    }

    private FleetListCriteria criteriaByAge() {
        final int age = Console.readInteger("Aircraft age in years");
        final FleetNumericComparison comparison = readNumericComparison();
        if (comparison == null) {
            return null;
        }
        return FleetListCriteria.byAge(age, comparison);
    }

    private FleetNumericComparison readNumericComparison() {
        System.out.println("1 - More than");
        System.out.println("2 - Less than");
        System.out.println("3 - Exactly");
        return switch (Console.readInteger("Option")) {
            case 1 -> FleetNumericComparison.GREATER_THAN;
            case 2 -> FleetNumericComparison.LESS_THAN;
            case 3 -> FleetNumericComparison.EQUAL;
            default -> {
                System.out.println("Invalid option.");
                yield null;
            }
        };
    }

    @Override
    public String headline() {
        return "List company fleet (US072)";
    }
}
