package eapli.aisafe.aircraftmanagement.application;

import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.aisafe.aircraftmanagement.domain.AircraftRegistration;
import eapli.aisafe.aircraftmanagement.repositories.AircraftRepository;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.flightmanagement.repositories.FlightRepository;

import java.time.LocalDateTime;
import java.util.Objects;

public class DecommissionAircraftService {

    private final AircraftRepository aircraft;
    private final FlightRepository flights;

    public DecommissionAircraftService(final AircraftRepository aircraft, final FlightRepository flights) {
        if (aircraft == null || flights == null) {
            throw new IllegalArgumentException("Repositories are required.");
        }
        this.aircraft = aircraft;
        this.flights = flights;
    }

    public Iterable<Aircraft> listActiveFleet(final IATACode ownerCompanyIata) {
        Objects.requireNonNull(ownerCompanyIata, "Owner company is required.");
        return aircraft.findActiveByOwnerCompany(ownerCompanyIata);
    }

    public Aircraft decommission(final String registrationRaw,
                                 final IATACode ownerCompanyIata,
                                 final LocalDateTime now) {
        Objects.requireNonNull(ownerCompanyIata, "Owner company is required.");
        Objects.requireNonNull(now, "Reference time is required.");
        final AircraftRegistration reg = new AircraftRegistration(
                Objects.requireNonNull(registrationRaw, "registration"));
        final Aircraft entity = aircraft.findByRegistration(reg)
                .orElseThrow(() -> new AircraftNotFoundException("Unknown aircraft registration."));
        if (!entity.ownerCompanyIata().equals(ownerCompanyIata)) {
            throw new AircraftNotInCompanyFleetException();
        }
        if (!entity.isActive()) {
            throw new AircraftAlreadyDecommissionedException();
        }
        if (flights.existsPendingFlightForAircraft(reg.toString(), now)) {
            throw new AircraftHasPendingFlightsException();
        }
        entity.retireFromActiveService();
        return aircraft.save(entity);
    }
}
