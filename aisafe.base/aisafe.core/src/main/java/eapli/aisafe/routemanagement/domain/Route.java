package eapli.aisafe.routemanagement.domain;

import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.airportmanagement.domain.AirportIATACode;
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.flightmanagement.domain.FlightType;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.persistence.Column;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ROUTE")
public class Route implements AggregateRoot<RouteName>, Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private RouteName routeName;

    @Version
    private Long version;

    @ManyToOne(optional = false)
    @JoinColumn(name = "AIR_TRANSPORT_COMPANY_IATA", referencedColumnName = "IATA_CODE")
    private AirTransportCompany airTransportCompany;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ORIGIN_AIRPORT_IATA", referencedColumnName = "AIRPORT_IATA_CODE")
    private Airport originAirport;

    @ManyToOne(optional = false)
    @JoinColumn(name = "DESTINATION_AIRPORT_IATA", referencedColumnName = "AIRPORT_IATA_CODE")
    private Airport destinationAirport;

    @Enumerated(EnumType.STRING)
    @Column(name = "FLIGHT_TYPE", nullable = false, length = 10)
    private FlightType flightType;

    @Embedded
    private RouteSchedule routeSchedule;

    @Embedded
    private RouteRecurringSchedule routeRecurringSchedule;

    @Embedded
    private DeactivationDate deactivationDate;

    protected Route() {
        // for ORM
    }

    private Route(final RouteName routeName,
                  final AirTransportCompany airTransportCompany,
                  final Airport originAirport,
                  final Airport destinationAirport,
                  final FlightType flightType,
                  final RouteSchedule routeSchedule,
                  final RouteRecurringSchedule routeRecurringSchedule,
                  final DeactivationDate deactivationDate) {
        if (routeName == null || airTransportCompany == null || originAirport == null || destinationAirport == null) {
            throw new IllegalArgumentException("Route name, company and airports are required.");
        }
        if (DomainEntities.areEqual(originAirport, destinationAirport)) {
            throw new IllegalArgumentException("Origin and destination airports must be different.");
        }
        if (flightType == null) {
            throw new IllegalArgumentException("Flight type is required.");
        }
        if (flightType == FlightType.CHARTER) {
            if (routeSchedule == null || routeRecurringSchedule != null) {
                throw new IllegalArgumentException("Charter route requires route schedule and no recurring schedule.");
            }
        }
        if (flightType == FlightType.REGULAR) {
            if (routeRecurringSchedule == null || routeSchedule != null) {
                throw new IllegalArgumentException("Regular route requires recurring schedule and no charter schedule.");
            }
        }

        this.routeName = routeName;
        this.airTransportCompany = airTransportCompany;
        this.originAirport = originAirport;
        this.destinationAirport = destinationAirport;
        this.flightType = flightType;
        this.routeSchedule = routeSchedule;
        this.routeRecurringSchedule = routeRecurringSchedule;
        this.deactivationDate = deactivationDate;
    }

    public static Route charterRoute(final RouteName routeName,
                                     final AirTransportCompany airTransportCompany,
                                     final Airport originAirport,
                                     final Airport destinationAirport,
                                     final RouteSchedule routeSchedule) {
        return new Route(routeName, airTransportCompany, originAirport, destinationAirport,
                FlightType.CHARTER, routeSchedule, null, null);
    }

    public static Route regularRoute(final RouteName routeName,
                                     final AirTransportCompany airTransportCompany,
                                     final Airport originAirport,
                                     final Airport destinationAirport,
                                     final RouteRecurringSchedule routeRecurringSchedule) {
        return new Route(routeName, airTransportCompany, originAirport, destinationAirport,
                FlightType.REGULAR, null, routeRecurringSchedule, null);
    }

    public static Route regularRouteDeactivated(final RouteName routeName,
                                                final AirTransportCompany airTransportCompany,
                                                final Airport originAirport,
                                                final Airport destinationAirport,
                                                final RouteRecurringSchedule routeRecurringSchedule,
                                                final LocalDate deactivationDate) {
        return new Route(routeName, airTransportCompany, originAirport, destinationAirport,
                FlightType.REGULAR, null, routeRecurringSchedule, DeactivationDate.valueOf(deactivationDate));
    }

    @Override
    public RouteName identity() {
        return routeName;
    }

    @Override
    public boolean sameAs(final Object other) {
        return DomainEntities.areEqual(this, other);
    }

    public AirTransportCompany airTransportCompany() {
        return airTransportCompany;
    }

    public Airport originAirport() {
        return originAirport;
    }

    public Airport destinationAirport() {
        return destinationAirport;
    }

    public IATACode companyIATACode() {
        return airTransportCompany.identity();
    }

    public AirportIATACode originAirportIATACode() {
        return originAirport.identity();
    }

    public AirportIATACode destinationAirportIATACode() {
        return destinationAirport.identity();
    }

    public FlightType flightType() {
        return flightType;
    }

    public RouteSchedule routeSchedule() {
        return routeSchedule;
    }

    public RouteRecurringSchedule routeRecurringSchedule() {
        return routeRecurringSchedule;
    }

    public DeactivationDate deactivationDate() {
        return deactivationDate;
    }

    public boolean isActive() {
        return deactivationDate == null;
    }

    /**
     * {@code true} when the route is not deactivated on the given calendar date.
     */
    public boolean isActiveOn(final LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date is required.");
        }
        return deactivationDate == null || date.isBefore(deactivationDate.value());
    }

    public void deactivate(final DeactivationDate date) {
        if (deactivationDate != null) {
            throw new IllegalStateException("Route is already deactivated.");
        }
        if (date == null) {
            throw new IllegalArgumentException("Deactivation date is required.");
        }
        this.deactivationDate = date;
    }

    public void assertUsableForFlight(final LocalDateTime departure) {
        if (departure == null) {
            throw new IllegalArgumentException("Departure date/time is required.");
        }
        if (!isActiveOn(departure.toLocalDate())) {
            throw new IllegalStateException("Route is deactivated for the selected departure date.");
        }
    }

    public void assertDepartureMatchesSchedule(final LocalDateTime departure) {
        if (departure == null) {
            throw new IllegalArgumentException("Departure date/time is required.");
        }
        if (flightType == FlightType.CHARTER) {
            assertCharterDeparture(departure.toLocalDate());
        } else {
            assertRegularDeparture(departure);
        }
    }

    private void assertCharterDeparture(final LocalDate departureDate) {
        if (routeSchedule == null) {
            throw new IllegalStateException("Charter route has no schedule.");
        }
        final LocalDate from = routeSchedule.scheduledDeparture();
        final LocalDate to = routeSchedule.scheduledArrival();
        if (departureDate.isBefore(from) || departureDate.isAfter(to)) {
            throw new IllegalArgumentException(
                    "Departure date must be within charter route window " + from + " to " + to + ".");
        }
    }

    private void assertRegularDeparture(final LocalDateTime departure) {
        if (routeRecurringSchedule == null) {
            throw new IllegalStateException("Regular route has no recurring schedule.");
        }
        final DayOfWeek day = departure.getDayOfWeek();
        final boolean allowed = routeRecurringSchedule.entries().stream()
                .anyMatch(e -> e.dayOfWeek() == day);
        if (!allowed) {
            throw new IllegalArgumentException(
                    "Departure day " + day + " is not allowed for this regular route.");
        }
    }
}
