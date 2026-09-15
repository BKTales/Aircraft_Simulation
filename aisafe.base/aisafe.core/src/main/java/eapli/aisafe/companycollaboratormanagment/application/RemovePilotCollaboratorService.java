package eapli.aisafe.companycollaboratormanagment.application;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.companycollaboratormanagment.domain.PilotUser;
import eapli.aisafe.companycollaboratormanagment.repositories.PilotUserRepository;
import eapli.aisafe.flightmanagement.repositories.FlightRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.general.domain.model.EmailAddress;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Objects;
import java.util.Optional;

public class RemovePilotCollaboratorService {

    private final PilotUserRepository pilots;
    private final FlightRepository flights;

    public RemovePilotCollaboratorService(final PilotUserRepository pilots, final FlightRepository flights) {
        if (pilots == null || flights == null) {
            throw new IllegalArgumentException("Repositories are required.");
        }
        this.pilots = pilots;
        this.flights = flights;
    }

    public DeactivatePilotResult deactivatePilot(final EmailAddress email,
                                                 final AirTransportCompany company,
                                                 final TransactionalContext txCtx) {
        return deactivatePilot(email, company, LocalDateTime.now(), txCtx);
    }

    public DeactivatePilotResult deactivatePilot(final EmailAddress email,
                                                 final AirTransportCompany company,
                                                 final LocalDateTime referenceTime,
                                                 final TransactionalContext txCtx) {
        Objects.requireNonNull(email, "Pilot email is required.");
        Objects.requireNonNull(company, "Company is required.");
        Objects.requireNonNull(referenceTime, "Reference time is required.");

        if (txCtx != null) {
            txCtx.beginTransaction();
        }

        try {
            final Optional<PilotUser> found = pilots.findByEmailWithLock(email, company);
            if (found.isEmpty()) {
                return finish(txCtx, pilots.findByEmail(email).isPresent()
                        ? DeactivatePilotResult.failure(DeactivatePilotResult.Outcome.NOT_IN_ROSTER)
                        : DeactivatePilotResult.failure(DeactivatePilotResult.Outcome.NOT_FOUND));
            }
            final PilotUser pilot = found.get();

            if (!pilot.systemUser().isActive()) {
                return finish(txCtx, DeactivatePilotResult.failure(DeactivatePilotResult.Outcome.ALREADY_INACTIVE));
            }
            if (flights.existsActiveFlightForPilot(pilot.systemUser(), referenceTime)) {
                return finish(txCtx, DeactivatePilotResult.failure(DeactivatePilotResult.Outcome.HAS_ACTIVE_FLIGHTS));
            }

            pilot.deactivateFromRoster(Calendar.getInstance());
            final PilotUser saved = pilots.save(pilot);
            return finish(txCtx, DeactivatePilotResult.success(saved), true);
        } catch (final RuntimeException ex) {
            if (txCtx != null) {
                txCtx.rollback();
            }
            throw ex;
        } finally {
            if (txCtx != null) {
                txCtx.close();
            }
        }
    }

    private DeactivatePilotResult finish(final TransactionalContext txCtx,
                                         final DeactivatePilotResult result) {
        return finish(txCtx, result, false);
    }

    private DeactivatePilotResult finish(final TransactionalContext txCtx,
                                         final DeactivatePilotResult result,
                                         final boolean commit) {
        if (txCtx != null) {
            if (commit) {
                txCtx.commit();
            } else {
                txCtx.rollback();
            }
        }
        return result;
    }
}
