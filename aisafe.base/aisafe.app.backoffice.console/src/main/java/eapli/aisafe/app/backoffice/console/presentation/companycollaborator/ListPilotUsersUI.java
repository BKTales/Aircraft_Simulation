package eapli.aisafe.app.backoffice.console.presentation.companycollaborator;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.companycollaboratormanagment.application.AddPilotCollaboratorController;
import eapli.aisafe.companycollaboratormanagment.application.ListCompanyCollaboratorUsersController;
import eapli.aisafe.companycollaboratormanagment.application.ListPilotUsersController;
import eapli.aisafe.companycollaboratormanagment.application.ListPilotsResult;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.dto.ResponsePilotCollaboratorDTO;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.presentation.console.AbstractListUI;
import eapli.framework.presentation.console.SelectWidget;
import eapli.framework.visitor.Visitor;

import java.util.Collections;

@SuppressWarnings({ "squid:S106" })
public class ListPilotUsersUI extends AbstractListUI<ResponsePilotCollaboratorDTO> {
    private final ListPilotUsersController listPilotController = new ListPilotUsersController();
    private final AddPilotCollaboratorController pilotController = new AddPilotCollaboratorController();

    @Override
    public String headline() {
        return "List Users";
    }

    @Override
    protected String emptyMessage() {
        return "No data.";
    }

    @Override
    protected Iterable<ResponsePilotCollaboratorDTO> elements() {
        System.out.println("Company: " + pilotController.currentCompany().companyName() + ">");

        final ListPilotsResult result = listPilotController.activePilotUsersForCompany();

        if (result.isSuccess()) {
            return result.pilots();
        } else {
            System.out.println("Error: " + result.message());
            return Collections.emptyList();
        }
    }

    @Override
    protected Visitor<ResponsePilotCollaboratorDTO> elementPrinter() {
        return new PilotCollaboratorUserDTOPrinter();
    }

    @Override
    protected String elementName() {
        return "User";
    }

    @Override
    protected String listHeader() {
        return String.format(
                "#  %-30s %-15s %-15s %-15s %-10s %-15s %-15s %-5s",
                "EMAIL", "F. NAME", "L. NAME", "PHONE", "STATUS", "ASSESSMENT DATE", "CLEARANCE EXPIRY", "CERTS"
        );
    }
}
