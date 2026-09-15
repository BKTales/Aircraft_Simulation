package eapli.aisafe.app.backoffice.console.presentation.aircontrolarea;

import eapli.aisafe.aircontrolarea.application.RegisterAirControlAreaController;
import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.util.ArrayList;
import java.util.List;

public class RegisterAirControlAreaUI extends AbstractUI {

    private final RegisterAirControlAreaController theController = new RegisterAirControlAreaController();

    @Override
    protected boolean doShow() {
        final String name = Console.readLine("Area Name:");
        final float minFuel = (float) Console.readDouble("Minimum Fuel Requirement:");

        final List<float[]> coordinates = new ArrayList<>();

        System.out.println("Please enter the coordinates for the boundary (min. 3 points):");
        boolean keepAdding = true;
        int count = 1;

        while (keepAdding || coordinates.size() < 3) {
            System.out.println("Point #" + count);
            float x = (float) Console.readDouble("  Coordinate X:");
            float y = (float) Console.readDouble("  Coordinate Y:");
            coordinates.add(new float[]{x, y});

            if (coordinates.size() >= 3) {
                keepAdding = Console.readBoolean("Add another point? (y/n)");
            } else {
                System.out.println("You need at least 3 points to form an area.");
            }
            count++;
        }

        try {
            final AirControlArea newArea = this.theController.registerAirControlArea(name, coordinates, minFuel);
            System.out.println("\nAir Control Area " + newArea.getAreaCode() + " successfully registered.");
        } catch (final Exception e) {
            System.out.println("\nError: " + e.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "Register an Air Control Area";
    }
}