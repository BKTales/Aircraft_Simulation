package eapli.aisafe.rcomp.tcpclient.presentation.atcc.pilotcollaborator;

import eapli.aisafe.rcomp.protocol.AtccOpcodes;
import eapli.aisafe.rcomp.protocol.ResponseCodes;
import eapli.aisafe.rcomp.tcpclient.presentation.RemoteTcpGateway;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.SelectWidget;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("squid:S106")
public final class RemotePilotCertificationInput {

    private RemotePilotCertificationInput() {}

    /** Same flow as backoffice {@code AddPilotCollaboratorUserUI#readCertifications} (SelectWidget model). */
    public static String readCertifications() throws IOException {
        final List<String> modelIds = loadModelIds();
        final StringBuilder sb = new StringBuilder();
        System.out.println("Pilot Certifications >");
        boolean more = true;
        while (more) {
            final String model = chooseModel(modelIds);
            if (model == null) {
                break;
            }
            final String from = Console.readLine("Certification Start Date (YYYY-MM-DD)");
            final String to = Console.readLine("Certification End Date (YYYY-MM-DD)");
            sb.append(";CERT:").append(model).append(',').append(from).append(',').append(to);

            final String another = Console.readLine("Add another certification? (y/n)");
            more = another != null && another.trim().equalsIgnoreCase("y");
        }
        return sb.toString();
    }

    private static String chooseModel(final List<String> modelIds) {
        if (modelIds.isEmpty()) {
            return Console.readLine("Model id");
        }
        final SelectWidget<String> selector = new SelectWidget<>("Select aircraft model:", modelIds);
        selector.show();
        return selector.selectedElement();
    }

    private static List<String> loadModelIds() throws IOException {
        final var resp = RemoteTcpGateway.request(AtccOpcodes.LIST_AIRCRAFT_MODEL_IDS, "");
        if (resp == null || resp.opcode() != ResponseCodes.OK) {
            return List.of();
        }
        return Arrays.stream(resp.payload().split("\n"))
                .map(String::trim).filter(s -> !s.isEmpty()).distinct().sorted().toList();
    }
}
