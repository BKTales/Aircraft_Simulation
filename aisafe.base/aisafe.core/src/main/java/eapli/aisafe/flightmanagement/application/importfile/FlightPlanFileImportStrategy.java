package eapli.aisafe.flightmanagement.application.importfile;

import java.nio.file.Path;

public interface FlightPlanFileImportStrategy {

    boolean supports(String extension);

    FlightPlanFileImportResult parse(Path path);
}
