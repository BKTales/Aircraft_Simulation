package eapli.aisafe.app.backoffice.console.presentation.companycollaborator;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.companycollaboratormanagment.application.ListCompanyCollaboratorUsersController;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractListUI;
import eapli.framework.presentation.console.SelectWidget;
import eapli.framework.visitor.Visitor;

import java.util.List;
import java.util.stream.StreamSupport;

@SuppressWarnings({ "squid:S106" })
public class ListCompanyCollaboratorUsersUI extends AbstractListUI<CompanyCollaboratorUser> {
    private final ListCompanyCollaboratorUsersController theController = new ListCompanyCollaboratorUsersController();

    @Override
    public String headline() {
        return "List Users";
    }

    @Override
    protected String emptyMessage() {
        return "No data.";
    }

    @Override
    protected Iterable<CompanyCollaboratorUser> elements() {

        final Iterable<AirTransportCompany> companies = theController.allAirTransportCompanies();

        if (!companies.iterator().hasNext()) {
            throw new IllegalStateException("No air transport companies registered.");
        }

        final SelectWidget<AirTransportCompany> selector =
                new SelectWidget<>("Select Company:", companies,
                        new AirTransportCompanyPrinter());

        selector.show();
        final AirTransportCompany selected = selector.selectedElement();

        if (selected == null) {
            throw new IllegalStateException("No company selected.");
        }

        return theController.activeATCCUsersForCompany(
                selected.identity().toString()
        );
    }

    @Override
    protected Visitor<CompanyCollaboratorUser> elementPrinter() {
        return new CompanyCollaboratorUserPrinter();
    }

    @Override
    protected String elementName() {
        return "User";
    }

    @Override
    protected String listHeader() {
        return String.format(
                "#  %-30s %-15s %-15s %-15s %-10s %-15s %-15s",
                "EMAIL", "F. NAME", "L. NAME", "PHONE", "STATUS", "ASSESSMENT DATE", "CLEARANCE EXPIRY"
        );
    }
}
