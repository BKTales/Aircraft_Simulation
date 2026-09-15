package eapli.aisafe.companycollaboratormanagment.application;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.dto.ResponsePilotCollaboratorDTO;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.companycollaboratormanagment.repositories.PilotUserRepository;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.general.domain.model.EmailAddress;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;

@UseCaseController
public class RemovePilotCollaboratorController {

    private final AuthorizationService authz;
    private final RemovePilotCollaboratorService removePilotService;
    private final PilotCollaboratorUserService pilotListingService;
    private final CompanyCollaboratorUserService collaboratorUserService;
    private final CompanyCollaboratorUserRepository collaborators;
    private final PilotUserRepository pilotRepository;
    private final TransactionalContext txCtx;

    public RemovePilotCollaboratorController() {
        this(AuthzRegistry.authorizationService(),
                PersistenceContext.repositories().newTransactionalContext());
    }

    private RemovePilotCollaboratorController(final AuthorizationService authz,
                                              final TransactionalContext ctx) {
        this(authz,
                new RemovePilotCollaboratorService(
                        PersistenceContext.repositories().pilots(ctx),
                        PersistenceContext.repositories().flights(ctx)),
                new PilotCollaboratorUserService(),
                new CompanyCollaboratorUserService(),
                PersistenceContext.repositories().collaborators(),
                PersistenceContext.repositories().pilots(),
                ctx);
    }

    public RemovePilotCollaboratorController(final AuthorizationService authz,
                                           final RemovePilotCollaboratorService removePilotService,
                                           final PilotCollaboratorUserService pilotListingService,
                                           final CompanyCollaboratorUserService collaboratorUserService,
                                           final CompanyCollaboratorUserRepository collaborators,
                                           final PilotUserRepository pilotRepository,
                                           final TransactionalContext txCtx) {
        if (authz == null || removePilotService == null || pilotListingService == null
                || collaboratorUserService == null || collaborators == null || pilotRepository == null) {
            throw new IllegalArgumentException("All dependencies are required.");
        }
        this.authz = authz;
        this.removePilotService = removePilotService;
        this.pilotListingService = pilotListingService;
        this.collaboratorUserService = collaboratorUserService;
        this.collaborators = collaborators;
        this.pilotRepository = pilotRepository;
        this.txCtx = txCtx;
    }

    public Iterable<ResponsePilotCollaboratorDTO> listActivePilotsForCompany() {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
        return pilotListingService.findActivePilotsByCompany(pilotRepository, currentCompany()).pilots();
    }

    public DeactivatePilotResult deactivatePilot(final String emailRaw) {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
        final EmailAddress email = EmailAddress.valueOf(
                java.util.Objects.requireNonNull(emailRaw, "Pilot email is required.").trim());
        return removePilotService.deactivatePilot(email, currentCompany(), txCtx);
    }

    private AirTransportCompany currentCompany() {
        final SystemUser user = authz.session()
                .orElseThrow()
                .authenticatedUser();
        final CompanyCollaboratorUser atcc =
                collaboratorUserService.findATCC(collaborators, user.username());
        return atcc.airTransportCompany();
    }
}
