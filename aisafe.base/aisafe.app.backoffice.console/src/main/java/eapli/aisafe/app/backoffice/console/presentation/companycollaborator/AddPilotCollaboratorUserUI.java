package eapli.aisafe.app.backoffice.console.presentation.companycollaborator;

import eapli.aisafe.companycollaboratormanagment.application.AddPilotCollaboratorController;
import eapli.aisafe.companycollaboratormanagment.application.AddPilotResult;
import eapli.aisafe.companycollaboratormanagment.application.PilotCertificationSpec;
import eapli.aisafe.companycollaboratormanagment.dto.CreatePilotCollaboratorDTO;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddPilotCollaboratorUserUI extends AbstractUI {

    private final AddPilotCollaboratorController pilotController = new AddPilotCollaboratorController();

    @Override
    protected boolean doShow() {
        System.out.println("Company: " + pilotController.currentCompany().companyName() + ">");

        String[] userData = requestSystemUserData();

        createPilotCollaborator(userData[0], userData[1], userData[2], userData[3], userData[4]);
        return false;
    }

    private String[] requestSystemUserData() {
        String[] data = new String[5];

        System.out.println("New System User information >");
        data[0] = Console.readLine("Username");
        data[1] = Console.readLine("Password");
        data[2] = Console.readLine("First Name");
        data[3] = Console.readLine("Last Name");
        data[4] = Console.readLine("E-Mail");

        return data;
    }

    private void createPilotCollaborator(final String username, final String password,
                                         final String firstName, final String lastName,
                                         final String email) {
        System.out.println("\nPilot Collaborator information >");

        final String securityDate = Console.readLine("Security Clearance Expiry Date (YYYY-MM-DD)");
        final String skillDate = Console.readLine("Skill Assessment Start Date (YYYY-MM-DD)");
        final String phoneNumber = Console.readLine("Phone Number (+351XXXXXXXXX)");

        final List<PilotCertificationSpec> certifications = readCertifications();

        final Set<Role> roles = new HashSet<>();
        roles.add(AISafeRoles.PILOT);
        final CreatePilotCollaboratorDTO dto = new CreatePilotCollaboratorDTO(
                username, password, firstName, lastName,
                email, phoneNumber, securityDate, skillDate, certifications);
        final AddPilotResult result = this.pilotController.addUser(dto);
        handleAddPilotResult(result);
    }

    private void handleAddPilotResult(final AddPilotResult result) {
        if (result.isSuccess()) {
            System.out.println("Pilot created successfully.");
            return;
        }

        if (result.message() != null) {
            System.out.println("Error: " + result.message());
            return;
        }

        switch (result.outcome()) {
            case DUPLICATE_USERNAME -> System.out.println("Error: That username is already in use.");
            case AIRCRAFT_MODEL_NOT_FOUND -> System.out.println("Error: One or more aircraft models not found.");
            case NO_CERTIFICATIONS -> System.out.println("Error: Pilot must have at least one certification.");
            case INVALID_DATE_FORMAT -> System.out.println("Error: Invalid date format (use YYYY-MM-DD).");
            case INVALID_PHONE_FORMAT -> System.out.println("Error: Invalid phone number format.");
            case INVALID_SECURITY_DATE -> System.out.println("Error: Expiry date must be in the future.");
            case INVALID_SKILLS_DATE -> System.out.println("Error: Assessment date cannot be in the future.");
            case SESSION_NOT_FOUND -> System.out.println("Error: User session not found.");
            default -> System.out.println("Error: An unexpected error occurred.");
        }
    }

    private List<PilotCertificationSpec> readCertifications() {
        List<PilotCertificationSpec> certifications = new ArrayList<>();
        System.out.println("Pilot Certifications >");
        boolean addMore = true;
        while (addMore) {
            final SelectWidget<String> aircraftSelector =
                    new SelectWidget<>("Select aircraft model:", List.of(pilotController.getAircraftModelIds()));

            aircraftSelector.show();
            final String selectedModelId = aircraftSelector.selectedElement();
            final String startDate = Console.readLine("Certification Start Date (YYYY-MM-DD)");
            final String endDate = Console.readLine("Certification End Date (YYYY-MM-DD)");
            certifications.add(new PilotCertificationSpec(selectedModelId, startDate, endDate));

            final String another = Console.readLine("Add another certification? (y/n)");
            addMore = another != null && another.trim().equalsIgnoreCase("y");
        }
        return certifications;
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
        return "Add Pilot Collaborator";
    }
}
