package eapli.aisafe.flightmanagement.application.importfile;

public final class ExtensionlessFlightPlanFileImportStrategy extends AbstractCoreFlightDslFileImportStrategy {

    @Override
    public boolean supports(final String extension) {
        return extension.isEmpty();
    }
}
