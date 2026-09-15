package eapli.aisafe.app.backoffice.console.presentation.usermanagement;

import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.visitor.Visitor;

@SuppressWarnings({ "squid:S106" })
public class  SystemUserPrinter implements Visitor<SystemUser> {

    @Override
    public void visit(final SystemUser visitee) {
        String status = visitee.isActive() ? "ACTIVE" : "INACTIVE";
        System.out.printf("%-20s%-20s%-20s%-35s%-40s%-15s",
                visitee.username(),
                visitee.name().firstName(),
                visitee.name().lastName(),
                visitee.email(),
                visitee.roleTypes(),
                status);
    }
}
