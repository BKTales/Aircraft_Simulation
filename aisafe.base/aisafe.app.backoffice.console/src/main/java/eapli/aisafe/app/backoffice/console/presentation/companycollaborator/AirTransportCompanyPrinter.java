package eapli.aisafe.app.backoffice.console.presentation.companycollaborator;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.framework.visitor.Visitor;

public class AirTransportCompanyPrinter implements Visitor<AirTransportCompany> {

    @Override
    public void visit(final AirTransportCompany company) {
        System.out.printf("%-8s %-24s",
                company.identity(),
                company.companyName());
    }
}