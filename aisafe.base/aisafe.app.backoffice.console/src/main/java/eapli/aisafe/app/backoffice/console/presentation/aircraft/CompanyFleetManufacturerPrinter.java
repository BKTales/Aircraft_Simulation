package eapli.aisafe.app.backoffice.console.presentation.aircraft;

import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;
import eapli.framework.visitor.Visitor;

public final class CompanyFleetManufacturerPrinter implements Visitor<ManufacturerId> {

    @Override
    public void visit(final ManufacturerId manufacturerId) {
        System.out.println(manufacturerId);
    }
}
