package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.aircraftmanagement.domain.AircraftRegistration;
import eapli.aisafe.aircraftmanagement.repositories.AircraftRepository;
import eapli.aisafe.aircraftmodelmanagement.repositories.AircraftModelRepository;
import eapli.aisafe.airportmanagement.repositories.AirportRepository;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.companycollaboratormanagment.domain.PilotUser;
import eapli.aisafe.dsl.model.FlightPlanDescriptor;
import eapli.aisafe.enginemodelmanagement.repositories.EngineModelRepository;
import eapli.aisafe.flightmanagement.domain.Flight;
import eapli.aisafe.flightmanagement.domain.FlightDesignator;
import eapli.aisafe.flightmanagement.repositories.FlightRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class ImportFlightPlanService {

    private final FlightRepository flights;
    private final AircraftRepository aircraft;
    private final SimulatorFlightPlanJsonBuilder jsonBuilder;

    public ImportFlightPlanService(final FlightRepository flights,
                                   final AircraftRepository aircraft,
                                   final AircraftModelRepository aircraftModels,
                                   final EngineModelRepository engineModels,
                                   final AirportRepository airports) {
        if (flights == null || aircraft == null || aircraftModels == null
                || engineModels == null || airports == null) {
            throw new IllegalArgumentException("Repositories are required.");
        }
        this.flights = flights;
        this.aircraft = aircraft;
        this.jsonBuilder = new SimulatorFlightPlanJsonBuilder(aircraftModels, engineModels, airports);
    }

    public List<String> listActiveAircraftRegistrations() {
        final List<String> regs = new ArrayList<>();
        for (final var a : aircraft.findAll()) {
            if (a.isActive()) {
                regs.add(a.identity().toString());
            }
        }
        regs.sort(Comparator.naturalOrder());
        return regs;
    }

    public List<String> listActiveAircraftRegistrations(final IATACode ownerCompany) {
        Objects.requireNonNull(ownerCompany, "ownerCompany");
        final List<String> regs = new ArrayList<>();
        for (final var a : aircraft.findActiveByOwnerCompany(ownerCompany)) {
            regs.add(a.identity().toString());
        }
        regs.sort(Comparator.naturalOrder());
        return regs;
    }

    public ImportFlightPlanResult importFlightPlan(final FlightPlanDescriptor descriptor,
                                                    final String dslContent,
                                                    final PilotUser pilot,
                                                    final String aircraftRegistration) {
        if (descriptor == null || dslContent == null || dslContent.isBlank()) {
            return ImportFlightPlanResult.failure("Flight plan content is required.");
        }
        if (pilot == null) {
            return ImportFlightPlanResult.failure("Pilot identity is required.");
        }
        if (aircraftRegistration == null || aircraftRegistration.isBlank()) {
            return ImportFlightPlanResult.failure("Aircraft registration is required.");
        }

        final FlightDesignator designator = new FlightDesignator(descriptor.getFlightId());
        if (flights.findByDesignator(designator).isPresent()) {
            return ImportFlightPlanResult.failure(
                    "A flight with designator " + designator + " already exists.");
        }

        final var registration = AircraftRegistration.valueOf(aircraftRegistration.trim());
        final var aircraftOpt = aircraft.findByRegistration(registration);
        if (aircraftOpt.isEmpty()) {
            return ImportFlightPlanResult.failure("Unknown aircraft registration: " + aircraftRegistration);
        }

        try {
            final Flight flight = FlightPlanDescriptorMapper.toFlight(
                    descriptor, dslContent, pilot, aircraftOpt.get(), jsonBuilder);
            flights.save(flight);
            return ImportFlightPlanResult.success(designator);
        } catch (final IllegalArgumentException | IllegalStateException ex) {
            return ImportFlightPlanResult.failure(ex.getMessage());
        }
    }
}
