package eapli.aisafe.app.backoffice.console.presentation.usermanagement;

import eapli.aisafe.usermanagement.application.ListUsersController;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.presentation.console.AbstractListUI;
import eapli.framework.visitor.Visitor;

@SuppressWarnings({ "squid:S106" })
public class ListUsersUI extends AbstractListUI<SystemUser> {
    private final ListUsersController theController = new ListUsersController();

    @Override
    public String headline() {
        return "List Users";
    }

    @Override
    protected String emptyMessage() {
        return "No data.";
    }

    @Override
    protected Iterable<SystemUser> elements() {
        return theController.allUsers();
    }

    @Override
    protected Visitor<SystemUser> elementPrinter() {
        return new SystemUserPrinter();
    }

    @Override
    protected String elementName() {
        return "User";
    }

    @Override
    protected String listHeader() {
        return String.format("#  %-20s%-20s%-20s%-35s%-40s%-15s", "USERNAME", "F. NAME", "L. NAME", "E-MAIL", "ROLE", "STATUS");
    }
}
