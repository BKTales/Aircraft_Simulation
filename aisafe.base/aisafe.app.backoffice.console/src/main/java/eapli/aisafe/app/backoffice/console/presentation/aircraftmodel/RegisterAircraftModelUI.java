package eapli.aisafe.app.backoffice.console.presentation.aircraftmodel;

import eapli.aisafe.aircraftmodelmanagement.application.CreateAircraftModelController;
import eapli.aisafe.aircraftmodelmanagement.application.EngineModelNotFoundException;
import eapli.aisafe.aircraftmodelmanagement.application.AircraftModelAlreadyExistsException;
import eapli.aisafe.app.backoffice.console.presentation.enginemodel.EngineModelPrinter;
import eapli.aisafe.enginemodelmanagement.application.ManufacturerNotFoundException;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftType;
import eapli.aisafe.enginemodelmanagement.application.ListEngineModelsController;
import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.aisafe.manufacturermanagement.application.ListManufacturersController;
import eapli.aisafe.manufacturermanagement.domain.Manufacturer;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;
import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

@SuppressWarnings("squid:S106")
public class RegisterAircraftModelUI extends AbstractUI {

    private final CreateAircraftModelController controller = new CreateAircraftModelController();
    private final ListManufacturersController manufacturers = new ListManufacturersController();
    private final ListEngineModelsController engineModels = new ListEngineModelsController();

    @Override
    protected boolean doShow() {
        final String modelId = Console.readLine("Model ID");
        final String modelName = Console.readLine("Model name");
        final String manufacturerId = chooseManufacturerId();
        if (manufacturerId == null) return false;

        final AircraftType aircraftType = chooseAircraftType();
        if (aircraftType == null) return false;

        final double mtow = Console.readDouble("MTOW (kg)");
        final double mzfw = Console.readDouble("MZFW (kg)");
        final double emptyWeight = Console.readDouble("Empty weight (kg)");

        final double wingArea = Console.readDouble("Wing area (m^2)");
        final double wingSpan = Console.readDouble("Wing span (m)");

        final double cd0 = Console.readDouble("Drag coefficient (Cd0)");
        final double cl = Console.readDouble("Lift coefficient (Cl)");

        final double serviceCeiling = Console.readDouble("Service ceiling (m)");
        final double cruiseSpeed = Console.readDouble("Cruise speed (m/s)");
        final double fuelCapacity = Console.readDouble("Fuel capacity (l)");
        final double maxRange = Console.readDouble("Maximum range (km)");

        final int maxPassengerSeats = Console.readInteger("Maximum passenger seats (model type)");
        final int numberOfEngines = Console.readInteger("Number of engines");
        final List<String> engineModelIds = chooseEngineModelIds();

        try {
            controller.createAircraftModel(modelId, modelName, aircraftType, manufacturerId,
                    mtow, mzfw, emptyWeight, wingArea, wingSpan, cd0, cl,
                    serviceCeiling, cruiseSpeed, fuelCapacity, maxRange,
                    maxPassengerSeats, numberOfEngines, engineModelIds);
            System.out.println("Aircraft model registered successfully.");
        } catch (final ManufacturerNotFoundException | EngineModelNotFoundException | AircraftModelAlreadyExistsException ex) {
            System.out.println(ex.getMessage());
        } catch (final IllegalArgumentException ex) {
            System.out.println("Invalid input: " + ex.getMessage());
        } catch (final IllegalStateException ex) {
            System.out.println("Operation not allowed: " + ex.getMessage());
        }

        return false;
    }

    private String chooseManufacturerId() {
        final Iterable<Manufacturer> all = manufacturers.allManufacturers();

        if (!all.iterator().hasNext()) {
            throw new IllegalStateException("No manufacturers registered. Run bootstrap first.");
        }

        final SelectWidget<Manufacturer> selector =
                new SelectWidget<>("Select Manufacturer:", all,
                        new ManufacturerPrinter());

        selector.show();

        final Manufacturer selected = selector.selectedElement();

        if (selected == null) return null;
        return selected.identity().toString();
    }

    private AircraftType chooseAircraftType() {
        final SelectWidget<AircraftType> selector =
                new SelectWidget<>("Select Aircraft Type:",
                        List.of(AircraftType.values()),
                        new AircraftTypePrinter());

        selector.show();

        final AircraftType selected = selector.selectedElement();

        if (selected == null) return null;

        return selected;
    }

    private List<String> chooseEngineModelIds() {

        final List<EngineModel> all = StreamSupport
                .stream(engineModels.allEngineModels().spliterator(), false)
                .toList();

        if (all.isEmpty()) {
            throw new IllegalStateException("No engine models registered. Run bootstrap first.");
        }

        final List<EngineModel> available = new ArrayList<>(all);
        final List<String> selected = new ArrayList<>();

        while (!available.isEmpty()) {

            final SelectWidget<EngineModel> selector =
                    new SelectWidget<>(
                            "Select Engine Model (or cancel to finish):",
                            available,
                            new EngineModelPrinter()
                    );

            selector.show();

            final EngineModel selectedModel = selector.selectedElement();

            if (selectedModel == null) {
                break;
            }

            selected.add(selectedModel.identity().toString());
            available.remove(selectedModel);
        }

        return selected;
    }

    @Override
    public String headline() {
        return "Register Aircraft Model";
    }
}
