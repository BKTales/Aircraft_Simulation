/*
 * Copyright (c) 2013-2024 the original author or authors.
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package eapli.aisafe.app.backoffice.console.presentation.usermanagement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import eapli.aisafe.usermanagement.application.ActivateDeactivateUserController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

/**
 *
 * @author Fernando
 */
@SuppressWarnings("squid:S106")
public class ActivateDeactivateUserUI extends AbstractUI {
    private static final Logger LOGGER = LoggerFactory.getLogger(ActivateDeactivateUserUI.class);

    private final ActivateDeactivateUserController theController = new ActivateDeactivateUserController();

    @Override
    protected boolean doShow() {
        System.out.println("1. Deactivate an active user");
        System.out.println("2. Activate a deactivated user");
        System.out.println("0. Return");
        final int operation = Console.readInteger("\nPlease choose an option");

        if (operation == 0) {
            System.out.println("Operation cancelled.");
            return true;
        }
        if (operation != 1 && operation != 2) {
            System.out.println("Invalid option.");
            return false;
        }

        final boolean deactivating = (operation == 1);
        final Iterable<SystemUser> iterable = deactivating
                ? this.theController.activeUsers()
                : this.theController.nonActiveUsers();

        final Optional<SystemUser> currentUser = deactivating
                ? this.theController.currentUser()
                : Optional.empty();

        final List<SystemUser> list = new ArrayList<>();
        for (final SystemUser user : iterable) {
            if (deactivating && currentUser.isPresent() && user.sameAs(currentUser.get())) {
                continue;
            }
            list.add(user);
        }

        if (list.isEmpty()) {
            System.out.println(deactivating
                    ? "There are no active users to deactivate."
                    : "There are no deactivated users to activate.");
            return true;
        }

        final String action = deactivating ? "deactivate" : "activate";
        System.out.printf("%n%-6s%-20s%-20s%-20s%-12s%n", "Nº:", "Username", "Firstname", "Lastname", "Status");
        int cont = 1;
        for (final SystemUser user : list) {
            System.out.printf("%-6d%-20s%-20s%-20s%-12s%n",
                    cont,
                    user.username(),
                    user.name().firstName(),
                    user.name().lastName(),
                    user.isActive() ? "Active" : "Inactive");
            cont++;
        }

        final int option = Console.readInteger("Enter user nº to " + action + " or 0 to cancel ");
        if (option == 0) {
            System.out.println("No user selected.");
            return true;
        }
        if (option < 1 || option > list.size()) {
            System.out.println("Invalid selection.");
            return false;
        }

        try {
            final SystemUser target = list.get(option - 1);
            if (deactivating) {
                this.theController.deactivateUser(target);
                System.out.printf("User '%s' has been deactivated.%n", target.username());
            } else {
                this.theController.activateUser(target);
                System.out.printf("User '%s' has been activated.%n", target.username());
            }
        } catch (IntegrityViolationException | ConcurrencyException ex) {
            LOGGER.error("Error performing the operation", ex);
            System.out.println("An unexpected error occurred. Please try again or contact your system administrator.");
        }

        return true;
    }

    @Override
    public String headline() {
        return "Activate/Deactivate User";
    }
}
