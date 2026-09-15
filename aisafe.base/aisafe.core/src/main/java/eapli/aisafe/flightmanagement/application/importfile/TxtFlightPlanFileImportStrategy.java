package eapli.aisafe.flightmanagement.application.importfile;

public final class TxtFlightPlanFileImportStrategy extends AbstractCoreFlightDslFileImportStrategy {

    @Override
    public boolean supports(final String extension) {
        return FlightPlanFileExtensions.TXT.equals(extension);
    }
}
