package eapli.aisafe.app.backoffice.console.presentation.companycollaborator;

import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorRoles;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.domain.SecurityClearance;
import eapli.aisafe.companycollaboratormanagment.dto.ResponsePilotCollaboratorDTO;
import eapli.framework.visitor.Visitor;

@SuppressWarnings({ "squid:S106" })
public class PilotCollaboratorUserDTOPrinter implements Visitor<ResponsePilotCollaboratorDTO> {

    @Override
    public void visit(final ResponsePilotCollaboratorDTO visitee) {

        final String status = "ACTIVE";

        final String expiry = visitee.getSecurityClearanceExpiryDate();
        final String assessment = visitee.getSkillAssessmentDate();

        final String certificationsCount = String.valueOf(visitee.getCertificationCount());

        System.out.printf(
                "%-30s %-15s %-15s %-15s %-10s %-15s %-15s %-5s%n",
                visitee.getEmail(),
                visitee.getFirstName(),
                visitee.getLastName(),
                visitee.getPhoneNumber(),
                status,
                assessment,
                expiry,
                certificationsCount
        );
    }

}
