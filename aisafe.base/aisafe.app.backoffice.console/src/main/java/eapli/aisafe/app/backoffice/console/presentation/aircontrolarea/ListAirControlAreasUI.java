package eapli.aisafe.app.backoffice.console.presentation.aircontrolarea;

import eapli.aisafe.aircontrolarea.application.ListAirControlAreasController;
import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.framework.presentation.console.AbstractUI;

/**
 * Console UI for listing all registered air control areas.
 *
 * @author aisafe team
 */
@SuppressWarnings("squid:S106")
public class ListAirControlAreasUI extends AbstractUI {

    private final ListAirControlAreasController controller = new ListAirControlAreasController();

    @Override
    protected boolean doShow() {
        final Iterable<AirControlArea> areas = controller.allAirControlAreas();

        System.out.printf("%-12s %-30s%n", "Code", "Name");
        System.out.println("-".repeat(44));

        boolean any = false;
        for(final AirControlArea area : areas){
            System.out.printf("%-12s %-30s%n",
                    area.getAreaCode(),
                    area.getName().getName());
            any = true;
        }

        if(!any){
            System.out.println("No air control areas registered.");
        }

        return false;
    }

    @Override
    public String headline() {
        return "List Air Control Areas";
    }
}
