package eapli.aisafe.app.backoffice.console.presentation.enginemodel;

import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.framework.visitor.Visitor;

public class EngineModelPrinter implements Visitor<EngineModel> {

    @Override
    public void visit(final EngineModel m) {

        System.out.printf(
                "%-28s %-18s %-15s %-20s",
                m.identity(),
                m.name(),
                m.manufacturerId(),
                m.motorization()
        );
    }
}