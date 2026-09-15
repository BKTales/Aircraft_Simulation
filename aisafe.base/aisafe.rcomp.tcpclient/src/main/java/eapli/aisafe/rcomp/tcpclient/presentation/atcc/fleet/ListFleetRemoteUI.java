package eapli.aisafe.rcomp.tcpclient.presentation.atcc.fleet;

import eapli.aisafe.rcomp.protocol.AtccOpcodes;
import eapli.aisafe.rcomp.protocol.ProtocolFrame;
import eapli.aisafe.rcomp.protocol.ResponseCodes;
import eapli.aisafe.rcomp.tcpclient.presentation.RemoteTcpGateway;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * US072 — same flow as backoffice {@code ListCompanyFleetUI} (numbered filters,
 * {@link SelectWidget} for model/maker, comparison submenu), over TCP.
 */
@SuppressWarnings("squid:S106")
public final class ListFleetRemoteUI extends AbstractUI {

    @Override
    protected boolean doShow() {
        System.out.println("1 - Full fleet (US072)");
        System.out.println("2 - Filter by aircraft model (US072a)");
        System.out.println("3 - Filter by maker (US072b)");
        System.out.println("4 - Filter by passenger capacity (US072c)");
        System.out.println("5 - Filter by age (US072d)");
        System.out.println("0 - Cancel");

        final int option = Console.readInteger("Option");
        if (option == 0) {
            return false;
        }

        try {
            final String criteria = buildCriteria(option);
            if (criteria == null) {
                return false;
            }
            RemoteTcpGateway.printResponse(RemoteTcpGateway.request(AtccOpcodes.LIST_FLEET, criteria));
        } catch (final IOException ex) {
            System.out.println("Connection error: " + ex.getMessage());
        }
        return false;
    }

    private String buildCriteria(final int option) throws IOException {
        return switch (option) {
            case 1 -> "UNFILTERED";
            case 2 -> criteriaByModel();
            case 3 -> criteriaByMaker();
            case 4 -> criteriaByPassengerCapacity();
            case 5 -> criteriaByAge();
            default -> {
                System.out.println("Invalid option.");
                yield null;
            }
        };
    }

    private String criteriaByModel() throws IOException {
        final String chosen = selectFrom(AtccOpcodes.LIST_FLEET_MODELS,
                "Select aircraft model used in your fleet:", "No aircraft models in your company's fleet.");
        return chosen == null ? null : "MODEL;" + chosen;
    }

    private String criteriaByMaker() throws IOException {
        final String chosen = selectFrom(AtccOpcodes.LIST_FLEET_MANUFACTURERS,
                "Select maker of aircraft models used in your fleet:",
                "No makers found for aircraft in your company's fleet.");
        return chosen == null ? null : "MANUFACTURER;" + chosen;
    }

    private String criteriaByPassengerCapacity() {
        final int capacity = Console.readInteger("Total passenger capacity (seats)");
        final String comparison = readNumericComparison();
        return comparison == null ? null : "PASSENGERS;" + capacity + ";" + comparison;
    }

    private String criteriaByAge() {
        final int age = Console.readInteger("Aircraft age in years");
        final String comparison = readNumericComparison();
        return comparison == null ? null : "AGE;" + age + ";" + comparison;
    }

    private String readNumericComparison() {
        System.out.println("1 - More than");
        System.out.println("2 - Less than");
        System.out.println("3 - Exactly");
        return switch (Console.readInteger("Option")) {
            case 1 -> "GT";
            case 2 -> "LT";
            case 3 -> "EQ";
            default -> {
                System.out.println("Invalid option.");
                yield null;
            }
        };
    }

    private String selectFrom(final byte opcode, final String prompt, final String emptyMessage)
            throws IOException {
        final ProtocolFrame resp = RemoteTcpGateway.request(opcode, "");
        if (resp == null || resp.opcode() != ResponseCodes.OK) {
            RemoteTcpGateway.printResponse(resp);
            return null;
        }
        final List<String> values = Arrays.stream(resp.payload().split("\n"))
                .map(String::trim).filter(s -> !s.isEmpty()).distinct().sorted().toList();
        if (values.isEmpty()) {
            System.out.println(emptyMessage);
            return null;
        }
        System.out.println(prompt);
        final SelectWidget<String> selector = new SelectWidget<>("", values);
        selector.show();
        return selector.selectedElement();
    }

    @Override
    public String headline() {
        return "List company fleet (US072)";
    }
}
