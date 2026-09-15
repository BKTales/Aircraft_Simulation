package eapli.aisafe.flightmanagement.domain;

import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.companycollaboratormanagment.domain.PilotUser;
import eapli.aisafe.routemanagement.domain.Route;
import eapli.aisafe.weatherdata.domain.WeatherData;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "FLIGHT")
public class Flight implements AggregateRoot<FlightDesignator>, Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private FlightDesignator designator;

    @Version
    private Long version;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "FLIGHT_PLAN_FLIGHT_CODE", referencedColumnName = "FLIGHT_CODE")
    private FlightPlan flightPlan;

    @Embedded
    private FlightLoad flightLoad;

    @Embedded
    private FlightSchedule schedule;

    /**
     * Route name (e.g. TP0001) or import label (e.g. LPPT-EGLL). Maps to legacy column {@code routename}.
     */
    @Column(name = "routename")
    private String routeNameKey;

    @Transient
    private Route route;

    private String aircraftRegistration;

    @ManyToOne(optional = false)
    @JoinColumn(name = "PILOT_ID", referencedColumnName = "ID")
    private PilotUser pilot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WEATHER_DATA_ID")
    private WeatherData weatherData;

    protected Flight() {
        // ORM
    }

    /**
     * Legacy/import constructor (no persisted {@link Route}).
     */
    public Flight(final FlightDesignator designator,
                  final String routeLabel,
                  final String aircraftRegistration) {
        this.designator = designator;
        this.routeNameKey = routeLabel;
        this.aircraftRegistration = aircraftRegistration;
        this.route = null;
        this.flightPlan = null;
    }

    public static Flight createDraftForRoute(final FlightDesignator designator,
                                             final Route route,
                                             final Aircraft aircraft,
                                             final PilotUser pilot,
                                             final FlightSchedule schedule,
                                             final FlightPlan flightPlan) {
        if (designator == null || route == null || aircraft == null || pilot == null
                || schedule == null || flightPlan == null) {
            throw new IllegalArgumentException("All flight draft fields are required.");
        }
        if (!flightPlan.identity().equals(FlightPlanId.of(designator))) {
            throw new IllegalArgumentException("Flight plan must belong to this flight.");
        }
        if (flightPlan.status() != FlightPlanStatus.DRAFT) {
            throw new IllegalArgumentException("New flight plans must start in DRAFT status.");
        }

        assertRoutePilotAndAircraft(route, aircraft, pilot, schedule);

        final Flight flight = new Flight();
        flight.designator = designator;
        flight.route = route;
        flight.routeNameKey = route.identity().toString();
        flight.aircraftRegistration = aircraft.identity().toString();
        flight.pilot = pilot;
        flight.schedule = schedule;
        flight.flightPlan = flightPlan;
        flight.flightLoad = null;
        return flight;
    }

    /**
     * Replaces the current flight plan on an existing flight (US080).
     */
    public void replaceFlightPlan(final Route route,
                                  final Aircraft aircraft,
                                  final PilotUser pilot,
                                  final FlightSchedule schedule,
                                  final FlightPlan newFlightPlan) {
        if (route == null || aircraft == null || pilot == null || schedule == null || newFlightPlan == null) {
            throw new IllegalArgumentException("All replacement fields are required.");
        }
        if (flightPlan == null) {
            throw new IllegalStateException("Flight has no flight plan to replace.");
        }
        if (!newFlightPlan.identity().equals(FlightPlanId.of(this.designator))) {
            throw new IllegalArgumentException("Flight plan must belong to this flight.");
        }
        if (newFlightPlan.status() != FlightPlanStatus.DRAFT) {
            throw new IllegalArgumentException("Replacement flight plans must start in DRAFT status.");
        }

        assertRoutePilotAndAircraft(route, aircraft, pilot, schedule);

        this.route = route;
        this.routeNameKey = route.identity().toString();
        this.aircraftRegistration = aircraft.identity().toString();
        this.pilot = pilot;
        this.schedule = schedule;
        this.weatherData = null;
        flightPlan.replaceDraftContent(
                newFlightPlan.fuelLoad(),
                newFlightPlan.dslContent(),
                newFlightPlan.jsonContent());
    }

    private static void assertRoutePilotAndAircraft(final Route route,
                                                    final Aircraft aircraft,
                                                    final PilotUser pilot,
                                                    final FlightSchedule schedule) {
        route.assertUsableForFlight(schedule.scheduledDeparture());
        route.assertDepartureMatchesSchedule(schedule.scheduledDeparture());

        final IATACode routeCompany = route.companyIATACode();
        if (!pilot.airTransportCompany().identity().equals(routeCompany)) {
            throw new IllegalArgumentException("Pilot must belong to the route's air transport company.");
        }

        aircraft.assertAssignableToNewFlight();
        if (!aircraft.ownerCompanyIata().equals(routeCompany)) {
            throw new IllegalArgumentException("Aircraft must belong to the route's air transport company.");
        }
        if (!pilot.isCertifiedFor(aircraft.aircraftModelId())) {
            throw new IllegalArgumentException("Pilot is not certified for the selected aircraft model.");
        }
    }

    public FlightDesignator designator() {
        return designator;
    }

    public Route route() {
        return route;
    }

    public String routeName() {
        return routeNameKey;
    }


    public String aircraftRegistration() {
        return aircraftRegistration;
    }

    public FlightPlan flightPlan() {
        return flightPlan;
    }

    public FlightLoad flightLoad() {
        return flightLoad;
    }

    public FlightSchedule schedule() {
        return schedule;
    }

    public PilotUser pilot() {
        return pilot;
    }

    public WeatherData weatherData() {
        return weatherData;
    }

    public void assignPilotId(final PilotUser pilot) {
        this.pilot = pilot;
    }

    public void assignFlightLoad(final FlightLoad flightLoad) {
        this.flightLoad = flightLoad;
    }

    public void assignSchedule(final FlightSchedule schedule) {
        this.schedule = schedule;
    }

    public void assignFlightPlan(final FlightPlan plan) {
        if (plan != null && !plan.identity().equals(FlightPlanId.of(this.designator))) {
            throw new IllegalArgumentException("Flight plan must belong to this flight.");
        }
        this.flightPlan = plan;
    }

    public void transitionFlightPlanStatus(final FlightPlanStatus newStatus) {
        if (flightPlan == null) {
            throw new IllegalStateException("Flight has no flight plan.");
        }
        flightPlan.changeStatus(newStatus);
    }

    /**
     * Attaches weather data to this flight.
     */
    public void assignWeatherData(final WeatherData weatherData) {
        if (flightPlan == null) {
            throw new IllegalStateException("Flight has no flight plan.");
        }
        if (weatherData == null) {
            throw new IllegalArgumentException("Weather data is required.");
        }
        flightPlan.resetTestOnWeatherChange();
        this.weatherData = weatherData;
    }

    public boolean hasPlanReadyForSimulation() {
        return flightPlan != null && flightPlan.hasJsonContent();
    }

    public boolean scheduleOverlaps(final LocalDateTime intervalStart, final LocalDateTime intervalEnd) {
        return schedule != null && schedule.overlaps(intervalStart, intervalEnd);
    }

    @Override
    public boolean sameAs(final Object other) {
        return DomainEntities.areEqual(this, other);
    }

    @Override
    public FlightDesignator identity() {
        return this.designator;
    }
}
