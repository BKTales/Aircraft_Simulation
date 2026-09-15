package eapli.aisafe.app.backoffice.console.presentation.aircraftmodel;
import eapli.aisafe.manufacturermanagement.domain.Manufacturer;
import eapli.framework.visitor.Visitor;

public class ManufacturerPrinter implements Visitor<Manufacturer> {

    @Override
    public void visit(final Manufacturer m) {
        System.out.printf("%-10s %-30s %-10s",
                m.identity(),
                m.name(),
                m.countryCode().code());
    }
}