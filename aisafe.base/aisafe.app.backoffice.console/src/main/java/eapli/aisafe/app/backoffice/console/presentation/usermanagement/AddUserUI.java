package eapli.aisafe.app.backoffice.console.presentation.usermanagement;

import java.util.HashSet;
import java.util.Set;

import eapli.aisafe.usermanagement.application.AddUserController;
import eapli.aisafe.usermanagement.application.AddUserResult;
import eapli.framework.actions.menu.Menu;
import eapli.framework.actions.menu.MenuItem;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.menu.MenuItemRenderer;
import eapli.framework.presentation.console.menu.MenuRenderer;
import eapli.framework.presentation.console.menu.VerticalMenuRenderer;

public class AddUserUI extends AbstractUI {

    private final AddUserController theController = new AddUserController();

    @Override
    protected boolean doShow() {
        final String username = Console.readLine("Username");
        final String password = Console.readLine("Password");
        final String firstName = Console.readLine("First Name");
        final String lastName = Console.readLine("Last Name");
        final String email = Console.readLine("E-Mail");

        final Set<Role> roleTypes = new HashSet<>();
        boolean show;
        do {
            System.out.println("\nSelect the role:");
            show = showRoles(roleTypes);
        } while (!show);

        final AddUserResult result = this.theController.addUser(username, password, firstName, lastName, email, roleTypes);
        if (result.isSuccess()) {
            System.out.println("User " + username + " was added with success!");
        } else {
            System.out.println(result.message());
        }

        return false;
    }

    private boolean showRoles(final Set<Role> roleTypes) {
        final Menu rolesMenu = buildRolesMenu(roleTypes);
        final MenuRenderer renderer = new VerticalMenuRenderer(rolesMenu, MenuItemRenderer.DEFAULT);
        return renderer.render();
    }

    private Menu buildRolesMenu(final Set<Role> roleTypes) {
        final Menu rolesMenu = new Menu();
        int counter = 1;
        for (final Role roleType : theController.getRoleTypes()) {
            rolesMenu.addItem(MenuItem.of(counter++, roleType.toString(), () -> roleTypes.add(roleType)));
        }
        return rolesMenu;
    }

    @Override
    public String headline() {
        return "Add User";
    }
}
