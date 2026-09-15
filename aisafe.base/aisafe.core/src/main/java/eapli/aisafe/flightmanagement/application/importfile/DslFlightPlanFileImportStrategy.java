package eapli.aisafe.flightmanagement.application.importfile;

public final class DslFlightPlanFileImportStrategy extends AbstractCoreFlightDslFileImportStrategy {

    @Override
    public boolean supports(final String extension) {
        return FlightPlanFileExtensions.DSL.equals(extension);
    }
}
