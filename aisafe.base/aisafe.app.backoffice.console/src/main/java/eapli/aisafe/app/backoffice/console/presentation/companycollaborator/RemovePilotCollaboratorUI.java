package eapli.aisafe.app.backoffice.console.presentation.companycollaborator;

import eapli.aisafe.companycollaboratormanagment.application.DeactivatePilotResult;
import eapli.aisafe.companycollaboratormanagment.application.AddPilotCollaboratorController;
import eapli.aisafe.companycollaboratormanagment.application.RemovePilotCollaboratorController;
import eapli.aisafe.companycollaboratormanagment.dto.ResponsePilotCollaboratorDTO;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("squid:S106")
public class RemovePilotCollaboratorUI extends AbstractUI {

    private final RemovePilotCollaboratorController controller;
    private final AddPilotCollaboratorController companyContext = new AddPilotCollaboratorController();

    public RemovePilotCollaboratorUI() {
        this(new RemovePilotCollaboratorController());
    }

    public RemovePilotCollaboratorUI(final RemovePilotCollaboratorController controller) {
        this.controller = Objects.requireNonNull(controller, "controller");
    }

    @Override
    protected boolean doShow() {
        final List<ResponsePilotCollaboratorDTO> choices = new ArrayList<>();
        controller.listActivePilotsForCompany().forEach(choices::add);
        choices.sort(Comparator.comparing(ResponsePilotCollaboratorDTO::getEmail));
        if (choices.isEmpty()) {
            System.out.println("No active pilots in your company's roster.");
            return false;
        }

        System.out.println("Company: " + companyContext.currentCompany().companyName() + ">");
        System.out.println("Select a pilot to deactivate from the roster:");
        printListHeader();
        final SelectWidget<ResponsePilotCollaboratorDTO> selector =
                new SelectWidget<>("", choices, new PilotCollaboratorUserDTOPrinter());
        selector.show();
        final ResponsePilotCollaboratorDTO chosen = selector.selectedElement();
        if (chosen == null) {
            return false;
        }

        final DeactivatePilotResult result = controller.deactivatePilot(chosen.getEmail());
        switch (result.outcome()) {
            case SUCCESS -> System.out.println("Pilot deactivated successfully. They no longer appear in the active roster.");
            case NOT_FOUND -> System.out.println("Pilot not found.");
            case NOT_IN_ROSTER -> System.out.println("Pilot does not belong to your company's roster.");
            case ALREADY_INACTIVE -> System.out.println("Pilot is already inactive.");
            case HAS_ACTIVE_FLIGHTS -> System.out.println("Pilot has active flight plans and cannot be deactivated.");
        }
        return false;
    }

    private static void printListHeader() {
        System.out.printf(
                "%-30s %-15s %-15s %-15s %-10s %-15s %-15s %-5s%n",
                "EMAIL", "F. NAME", "L. NAME", "PHONE", "STATUS", "ASSESSMENT DATE", "CLEARANCE EXPIRY", "CERTS");
    }

    @Override
    public String headline() {
        return "Remove pilot from roster (US077)";
    }
}
