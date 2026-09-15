package eapli.aisafe.app.backoffice.console.presentation.companycollaborator;

import eapli.aisafe.companycollaboratormanagment.application.AddCompanyCollaboratorController;
import eapli.aisafe.flightcontroloperatormanagement.application.AddFlightControlOperatorController;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddCustomerCollaboratorUserUI extends AbstractUI {

    private final AddCompanyCollaboratorController companyController = new AddCompanyCollaboratorController();
    private final AddFlightControlOperatorController fcoController = new AddFlightControlOperatorController();

    private static final String[] CUSTOMER_TYPES = {
            "Air Transport Company",
            "Air Control Area"
    };

    @Override
    protected boolean doShow() {
        final SelectWidget<String> customerSelector =
                new SelectWidget<>("Customer Type:", List.of(CUSTOMER_TYPES));

        customerSelector.show();
        final String selectedType = customerSelector.selectedElement();
        if (selectedType == null) return false;

        final String[] flowOptions = {
                "Create User from scratch",
                "Use existing System User"
        };
        final SelectWidget<String> flowSelector =
                new SelectWidget<>("Operation Mode:", List.of(flowOptions));

        flowSelector.show();
        final String selectedFlow = flowSelector.selectedElement();
        if (selectedFlow == null) return false;

        boolean isNewUser = selectedFlow.equals(flowOptions[0]);

        if (selectedType.equals(CUSTOMER_TYPES[0])) {
            handleCompanyCollaboratorFlow(isNewUser);
        } else {
            handleFCOCollaboratorFlow(isNewUser);
        }

        return false;
    }

    private String[] requestSystemUserData(boolean isNewUser, List<String> eligibleEmails) {
        String[] data = new String[5]; // [0]user, [1]pass, [2]first, [3]last, [4]email

        if (isNewUser) {
            System.out.println("New System User information >");
            data[0] = Console.readLine("Username");
            data[1] = Console.readLine("Password");
            data[2] = Console.readLine("First Name");
            data[3] = Console.readLine("Last Name");
            data[4] = Console.readLine("E-Mail");
        } else {
            System.out.println("\nSelect an existing System User >");
            String email = selectExistingUserEmail(eligibleEmails);
            if (email == null) return null;
            data[4] = email;
        }
        return data;
    }


    private void handleCompanyCollaboratorFlow(boolean isNewUser) {
        String[] userData = requestSystemUserData(isNewUser, companyController.getEligibleUsers());
        if (userData != null) {
            createCompanyCollaborator(isNewUser, userData[0], userData[1], userData[2], userData[3], userData[4]);
        }
    }

    private void handleFCOCollaboratorFlow(boolean isNewUser) {
        String[] userData = requestSystemUserData(isNewUser, fcoController.getEligibleUsers());
        if (userData != null) {
            createFCOCollaborator(isNewUser, userData[0], userData[1], userData[2], userData[3], userData[4]);
        }
    }

    // ── Company Collaborator ────────────────────────────────

    private void createCompanyCollaborator(final boolean isNewUser,final String username, final String password,
                                           final String firstName, final String lastName,
                                           final String email) {

        System.out.println("\nCompany Collaborator information >");

        final SelectWidget<String> companySelector =
                new SelectWidget<>("Select Company:", List.of(companyController.getCompanyIds()));

        companySelector.show();
        final String selectedCompanyId = companySelector.selectedElement();

        if (selectedCompanyId == null) return;



        final String securityDate = Console.readLine("Security Clearance Expiry Date (YYYY-MM-DD)");
        final String skillDate = Console.readLine("Skill Assessment Start Date (YYYY-MM-DD)");
        final String phoneNumber = Console.readLine("Phone Number (+351XXXXXXXXX)");

        try {
            if (isNewUser) {
                final Set<Role> roles = new HashSet<>();
                roles.add(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
                this.companyController.addUser(username, password, firstName, lastName, email,
                        roles ,selectedCompanyId, securityDate, skillDate,phoneNumber);
            } else {
                this.companyController.addATCCToExistingUser(email, selectedCompanyId, securityDate, skillDate, phoneNumber);
            }
        } catch (IntegrityViolationException | ConcurrencyException e) {
            System.out.println("That username is already in use.");
        } catch (IllegalArgumentException e){
            System.out.println(e);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format (YYYY-MM-DD).");
        }
    }

    // ── FCO Collaborator ────────────────────────────────────

    private void createFCOCollaborator(boolean isNewUser,final String username, final String password,
                                       final String firstName, final String lastName,
                                       final String email) {

        System.out.println("\nFCO Collaborator information >");


        final SelectWidget<String> areaSelector =
                new SelectWidget<>("Select Air Control Area:", List.of(fcoController.getAreaCodes()));

        areaSelector.show();
        final String selectedArea = areaSelector.selectedElement();

        if (selectedArea == null) return;

        final Set<Role> roles = new HashSet<>();
        roles.add(AISafeRoles.FLIGHT_CONTROL_OPERATOR);

        final String securityDate = Console.readLine("Security Clearance Expiry Date (YYYY-MM-DD)");
        final String skillDate = Console.readLine("Skill Assessment Start Date (YYYY-MM-DD)");
        final String phoneNumber = Console.readLine("Phone Number (+351XXXXXXXXX)");

        try {
            if (isNewUser) {
                this.fcoController.addUser(username, password, firstName, lastName, email,roles,
                        selectedArea, securityDate, skillDate, phoneNumber);
            } else {
                this.fcoController.addFCOToExistingUser(email, selectedArea, securityDate, skillDate, phoneNumber);
            }
        } catch (IntegrityViolationException | ConcurrencyException e) {
            System.out.println("That username is already in use.");
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format (YYYY-MM-DD).");
        }
    }

    private String selectExistingUserEmail(List<String> eligibleEmails) {
        if (eligibleEmails.isEmpty()) {
            System.out.println("No eligible System Users found.");
            return null;
        }
        final SelectWidget<String> userSelector = new SelectWidget<>("Select User:", eligibleEmails);
        userSelector.show();
        return userSelector.selectedElement();
    }

    @Override
    public String headline() {
        return "Add User";
    }
}