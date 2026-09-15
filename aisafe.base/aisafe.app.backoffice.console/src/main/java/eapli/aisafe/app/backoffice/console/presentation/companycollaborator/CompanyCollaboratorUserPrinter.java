package eapli.aisafe.app.backoffice.console.presentation.companycollaborator;

import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorRoles;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.domain.SecurityClearance;
import eapli.aisafe.companycollaboratormanagment.domain.SkillsAssessment;
import eapli.framework.visitor.Visitor;

@SuppressWarnings({ "squid:S106" })
public class CompanyCollaboratorUserPrinter implements Visitor<CompanyCollaboratorUser> {

    @Override
    public void visit(final CompanyCollaboratorUser visitee) {

        final String status = visitee.systemUser().isActive() ? "ACTIVE" : "INACTIVE";

        final SecurityClearance sc = visitee.securityClearance();

        final String expiry = sc != null && sc.expiryDate() != null
                ? sc.expiryDate().toString()
                : "";

        final SkillsAssessment sa = visitee.skillsAssessment();

        final String assessment = sa != null && sa.date() != null
                ? sa.date().toString()
                : "";


        System.out.printf(
                "%-30s %-15s %-15s %-15s %-10s %-15s %-15s %n",
                visitee.systemUser().email(),
                visitee.systemUser().name().firstName(),
                visitee.systemUser().name().lastName(),
                visitee.phoneNumber(),
                status,
                assessment,
                expiry
        );
    }

}
