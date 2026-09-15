package eapli.aisafe.app.backoffice.console.presentation.aircraftmodel;

import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.framework.visitor.Visitor;

import java.util.stream.Collectors;

public class AircraftModelPrinter implements Visitor<AircraftModel> {

    private static final int CERTIFIED_IDS_MAX_LEN = 34;

    @Override
    public void visit(final AircraftModel m) {

        final var perf = m.performanceSpec();
        final int certifiedEngineModels = m.engineCertifiedConfigurations().size();
        final String certifiedEngineIds = m.engineCertifiedConfigurations().stream()
                .map(cfg -> cfg.engineModel().identity().toString())
                .collect(Collectors.joining(","));
        final String certifiedEngineIdsDisplay = truncate(certifiedEngineIds, CERTIFIED_IDS_MAX_LEN);

        System.out.printf(
                "%-28s %-18s %-12s %-10s %-12s %-34s %-10.1f %-12.1f %-12.1f %-12.1f",
                m.identity(),
                m.name(),
                m.manufacturer(),
                m.numberOfEngines(),
                certifiedEngineModels,
                certifiedEngineIdsDisplay,
                perf.cruiseSpeed(),
                perf.fuelCapacity(),
                perf.maxRange(),
                perf.serviceCeiling()
        );
    }

    private static String truncate(final String value, final int maxLen) {
        if (value == null || value.length() <= maxLen) {
            return value;
        }
        return value.substring(0, maxLen - 3) + "...";
    }
}
