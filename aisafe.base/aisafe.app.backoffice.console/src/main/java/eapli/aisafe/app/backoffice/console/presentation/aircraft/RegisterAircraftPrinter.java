package eapli.aisafe.app.backoffice.console.presentation.aircraft;

import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.enginemodelmanagement.domain.EngineModelId;
import eapli.framework.visitor.Visitor;

import java.util.stream.Collectors;

/**
 * US070 console: aircraft model table (header + rows) and certified engine id column.
 */
public class RegisterAircraftPrinter implements Visitor<AircraftModel> {

    private static final int CERTIFIED_IDS_MAX_LEN = 34;

    private static final String MODEL_LINE_FORMAT =
            "%-28s %-18s %-12s %-10s %-12s %-34s %-10s %-12s %-12s %-12s %-8s";

    public static final int AIRCRAFT_MODEL_TABLE_PRINT_WIDTH =
            28 + 18 + 12 + 10 + 12 + 34 + 10 + 12 + 12 + 12 + 8;

    private static final int CERTIFIED_ENGINE_ID_COL_WIDTH = 40;

    public static String aircraftModelsTableHeaderLine() {
        return String.format(
                MODEL_LINE_FORMAT,
                "ID",
                "Name",
                "Manufacturer",
                "Engines",
                "Cert.Models",
                "Cert.Model IDs",
                "Cruise",
                "FuelCap",
                "Range",
                "Ceiling",
                "MaxSeats");
    }

    public static void printAircraftModelsTableHeaderWithRule() {
        System.out.println(aircraftModelsTableHeaderLine());
        System.out.println("-".repeat(AIRCRAFT_MODEL_TABLE_PRINT_WIDTH));
    }

    @Override
    public void visit(final AircraftModel m) {
        final var perf = m.performanceSpec();
        final int certifiedEngineModels = m.engineCertifiedConfigurations().size();
        final String certifiedEngineIds = m.engineCertifiedConfigurations().stream()
                .map(cfg -> cfg.engineModel().identity().toString())
                .collect(Collectors.joining(","));
        final String certifiedEngineIdsDisplay = truncate(certifiedEngineIds, CERTIFIED_IDS_MAX_LEN);

        System.out.printf(
                "%-28s %-18s %-12s %-10s %-12s %-34s %-10.1f %-12.1f %-12.1f %-12.1f %-8d%n",
                m.identity(),
                m.name(),
                m.manufacturer(),
                m.numberOfEngines(),
                certifiedEngineModels,
                certifiedEngineIdsDisplay,
                perf.cruiseSpeed(),
                perf.fuelCapacity(),
                perf.maxRange(),
                perf.serviceCeiling(),
                m.numberOfSeats().seats());
    }

    public static void printCertifiedEngineModelIdsHeaderWithRule() {
        System.out.printf("%-" + CERTIFIED_ENGINE_ID_COL_WIDTH + "s%n", "Certified engine model ID");
        System.out.println("-".repeat(CERTIFIED_ENGINE_ID_COL_WIDTH));
    }

    public static final class CertifiedEngineModelIdPrinter implements Visitor<EngineModelId> {
        @Override
        public void visit(final EngineModelId id) {
            System.out.printf("%-" + CERTIFIED_ENGINE_ID_COL_WIDTH + "s%n", id);
        }
    }

    private static String truncate(final String value, final int maxLen) {
        if (value == null || value.length() <= maxLen) {
            return value;
        }
        return value.substring(0, maxLen - 3) + "...";
    }
}
