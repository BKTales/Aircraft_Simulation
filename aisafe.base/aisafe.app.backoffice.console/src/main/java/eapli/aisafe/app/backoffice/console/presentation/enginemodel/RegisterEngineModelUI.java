package eapli.aisafe.app.backoffice.console.presentation.enginemodel;

import eapli.aisafe.enginemodelmanagement.application.CreateEngineModelController;
import eapli.aisafe.enginemodelmanagement.application.EngineModelAlreadyExistsException;
import eapli.aisafe.enginemodelmanagement.application.ManufacturerNotFoundException;
import eapli.aisafe.enginemodelmanagement.domain.FuelType;
import eapli.aisafe.enginemodelmanagement.domain.MotorizationType;
import eapli.aisafe.manufacturermanagement.application.ListManufacturersController;
import eapli.aisafe.manufacturermanagement.domain.Manufacturer;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

public class RegisterEngineModelUI extends AbstractUI {

    private final CreateEngineModelController controller = new CreateEngineModelController();
    private final ListManufacturersController manufacturers = new ListManufacturersController();

    @Override
    protected boolean doShow() {
        final String name = Console.readLine("Engine model name");
        final String manufacturerId = chooseManufacturerId();
        final String motorization = chooseMotorization();
        final double thrustAtStatic = Console.readDouble("Thrust at static (kN)");
        final double thrustAtCruise = Console.readDouble("Thrust at cruise (kN)");

        final String fuelType = chooseFuelType();

        final double tsfc = Console.readDouble("TSFC (N/N/s) [e.g. 0.55; typical range ~0.3-1.2]");

        try {
            controller.createEngineModel(name, manufacturerId, motorization, thrustAtStatic, thrustAtCruise, fuelType, tsfc);
            System.out.println("Engine model registered successfully.");
        } catch (final ManufacturerNotFoundException | EngineModelAlreadyExistsException ex) {
            System.out.println(ex.getMessage());
        } catch (final IllegalArgumentException ex) {
            System.out.println("Invalid input: " + ex.getMessage());
        } catch (final IllegalStateException ex) {
            System.out.println("Operation not allowed: " + ex.getMessage());
        }

        return false;
    }

    private String chooseManufacturerId() {
        final var all = manufacturers.allManufacturers();
        System.out.println("Manufacturers:");
        int i = 0;
        for (final Manufacturer m : all) {
            i++;
            System.out.printf("%d) %-8s %-24s %-6s%n",
                    i, m.identity(), m.name(), m.countryCode().code());
        }
        if (i == 0) {
            throw new IllegalStateException("No manufacturers registered. Run bootstrap first.");
        }
        final int choice = Console.readInteger("Choose manufacturer (1-" + i + "):");
        if (choice < 1 || choice > i) throw new IllegalArgumentException("Invalid manufacturer option.");

        int j = 0;
        for (final Manufacturer m : manufacturers.allManufacturers()) {
            j++;
            if (j == choice) return m.identity().toString();
        }
        throw new IllegalStateException("Manufacturer selection failed.");
    }

    private String chooseMotorization() {
        final MotorizationType[] opts = MotorizationType.values();
        System.out.println("Motorization types:");
        for (int i = 0; i < opts.length; i++) {
            System.out.println((i + 1) + ") " + opts[i].label());
        }
        final int choice = Console.readInteger("Choose motorization (1-" + opts.length + "):");
        if (choice < 1 || choice > opts.length) {
            throw new IllegalArgumentException("Invalid motorization option.");
        }
        return opts[choice - 1].label();
    }

    private String chooseFuelType() {
        final FuelType[] values = FuelType.values();
        System.out.println("Fuel types:");
        for (int i = 0; i < values.length; i++) {
            System.out.println((i + 1) + ") " + values[i]);
        }
        final int choice = Console.readInteger("Choose fuel type (1-" + values.length + "):");
        if (choice < 1 || choice > values.length) {
            throw new IllegalArgumentException("Invalid fuel type option.");
        }
        return values[choice - 1].name();
    }

    @Override
    public String headline() {
        return "Register Engine Model";
    }
}

