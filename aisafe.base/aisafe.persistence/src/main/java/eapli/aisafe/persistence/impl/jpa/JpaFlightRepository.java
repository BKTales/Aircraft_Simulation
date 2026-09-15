package eapli.aisafe.persistence.impl.jpa;

import eapli.aisafe.Application;
import eapli.aisafe.flightmanagement.application.FlightValidationPreview;
import eapli.aisafe.flightmanagement.domain.Flight;
import eapli.aisafe.flightmanagement.domain.FlightDesignator;
import eapli.aisafe.flightmanagement.domain.FlightPlanStatus;
import eapli.aisafe.flightmanagement.repositories.FlightRepository;
import eapli.aisafe.routemanagement.domain.RouteName;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JpaFlightRepository extends JpaAutoTxRepository<Flight, FlightDesignator, FlightDesignator>
        implements FlightRepository {

    public JpaFlightRepository(final TransactionalContext autoTx) {
        super(autoTx, "designator");
    }

    public JpaFlightRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "designator");
    }

    @Override
    public boolean existsPendingFlightForAircraft(final String aircraftRegistration, final LocalDateTime asOf) {
        final Map<String, Object> params = new HashMap<>();
        params.put("reg", aircraftRegistration);
        params.put("asOf", asOf);
        return !match(
                "UPPER(TRIM(e.aircraftRegistration)) = UPPER(TRIM(:reg))"
                        + " AND e.schedule IS NOT NULL AND e.schedule.scheduledDeparture >= :asOf",
                params).isEmpty();
    }

    @Override
    public boolean existsActiveFlightForPilot(final SystemUser pilotSystemUser, final LocalDateTime asOf) {
        final Map<String, Object> params = new HashMap<>();
        params.put("user", pilotSystemUser);
        params.put("asOf", asOf);
        params.put("draft", FlightPlanStatus.DRAFT);
        params.put("submitted", FlightPlanStatus.SUBMITTED_FOR_SIMULATION);
        params.put("approved", FlightPlanStatus.SIM_APPROVED);
        return !match(
                "e.pilot.systemUser = :user"
                        + " AND e.flightPlan IS NOT NULL"
                        + " AND e.schedule IS NOT NULL"
                        + " AND e.schedule.scheduledDeparture >= :asOf"
                        + " AND (e.flightPlan.status = :draft"
                        + " OR e.flightPlan.status = :submitted"
                        + " OR e.flightPlan.status = :approved)",
                params).isEmpty();
    }

    @Override
    public boolean existsPlannedFlightOnRouteAfter(final RouteName routeName, final LocalDate deactivationDate) {
        final Map<String, Object> params = new HashMap<>();
        params.put("routeName", routeName.toString());
        params.put("from", deactivationDate.atStartOfDay());
        params.put("draft", FlightPlanStatus.DRAFT);
        params.put("submitted", FlightPlanStatus.SUBMITTED_FOR_SIMULATION);
        params.put("approved", FlightPlanStatus.SIM_APPROVED);
        return !match(
                "UPPER(TRIM(e.routeNameKey)) = UPPER(TRIM(:routeName))"
                        + " AND e.schedule IS NOT NULL"
                        + " AND e.schedule.scheduledDeparture >= :from"
                        + " AND (e.flightPlan IS NULL"
                        + " OR e.flightPlan.status = :draft"
                        + " OR e.flightPlan.status = :submitted"
                        + " OR e.flightPlan.status = :approved)",
                params).isEmpty();
    }

    @Override
    public List<Flight> findScheduledWithFlightPlan(final LocalDateTime start, final LocalDateTime end) {
        final Map<String, Object> params = new HashMap<>();
        params.put("start", start);
        params.put("end", end);
        return match(
                "e.schedule IS NOT NULL"
                        + " AND e.flightPlan IS NOT NULL"
                        + " AND e.schedule.scheduledDeparture <= :end"
                        + " AND e.schedule.scheduledArrival >= :start",
                params);
    }

    @Override
    public List<FlightValidationPreview> findDraftFlightsForPilot(final SystemUser pilotSystemUser) {
        final List<Object[]> rows = createQuery(
                "SELECT f.designator.code, f.routeNameKey, f.aircraftRegistration,"
                        + " fp.status, f.schedule.scheduledDeparture"
                        + " FROM Flight f JOIN f.flightPlan fp JOIN f.pilot p"
                        + " WHERE p.systemUser = :user AND fp.status = :draft",
                Object[].class)
                .setParameter("user", pilotSystemUser)
                .setParameter("draft", FlightPlanStatus.DRAFT)
                .getResultList();

        return rows.stream()
                .map(JpaFlightRepository::rowToPreview)
                .toList();
    }

    private static FlightValidationPreview rowToPreview(final Object[] row) {
        return new FlightValidationPreview(
                (String) row[0],
                (String) row[1],
                null,
                null,
                (String) row[2],
                (FlightPlanStatus) row[3],
                (LocalDateTime) row[4],
                null);
    }
}
