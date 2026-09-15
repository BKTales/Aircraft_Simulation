package eapli.aisafe.app.backoffice.console.presentation.companycollaborator;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.framework.visitor.Visitor;

public class AirControlAreaPrinter implements Visitor<AirControlArea> {

    @Override
    public void visit(final AirControlArea area) {
        System.out.printf("%-8s %-24s",
                area.identity(),
                area.getName().getName());
    }
}