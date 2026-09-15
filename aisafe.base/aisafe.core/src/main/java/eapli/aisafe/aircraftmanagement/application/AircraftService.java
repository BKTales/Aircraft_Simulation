package eapli.aisafe.aircraftmanagement.application;

import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.aisafe.aircraftmanagement.domain.YearOfManufacture;
import eapli.aisafe.aircraftmanagement.domain.AircraftRegistration;
import eapli.aisafe.aircraftmanagement.domain.CabinConfiguration;
import eapli.aisafe.aircraftmanagement.domain.NumberOfFlightCrew;
import eapli.aisafe.aircraftmanagement.domain.OperationalStatus;
import eapli.aisafe.aircraftmanagement.domain.RegistrationCountry;
import eapli.aisafe.aircraftmanagement.repositories.AircraftRepository;
import eapli.aisafe.aircraftmodelmanagement.application.AircraftModelNotFoundException;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModelId;
import eapli.aisafe.aircraftmodelmanagement.repositories.AircraftModelRepository;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.aircraftmodelmanagement.domain.EngineConfiguration;
import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.aisafe.enginemodelmanagement.domain.EngineModelId;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class AircraftService {

    private final AircraftRepository aircraft;
    private final AircraftModelRepository aircraftModels;

    public AircraftService(final AircraftRepository aircraft, final AircraftModelRepository aircraftModels) {
        if (aircraft == null || aircraftModels == null) {
            throw new IllegalArgumentException("Repositories are required.");
        }
        this.aircraft = aircraft;
        this.aircraftModels = aircraftModels;
    }

    public Aircraft registerAircraft(final String registration,
                                     final String modelId,
                                     final String engineModelId,
                                     final CabinConfiguration cabin,
                                     final String registrationCountryIso2,
                                     final int flightCrewCount,
                                     final int yearOfManufacture,
                                     final IATACode ownerCompanyIata) {

        Objects.requireNonNull(ownerCompanyIata, "Owner company is required.");

        final AircraftRegistration reg = new AircraftRegistration(
                Objects.requireNonNull(registration, "registration"));
        if (aircraft.existsByRegistration(reg)) {
            throw new DuplicateAircraftRegistrationException("Registration already exists.");
        }

        final AircraftModelId mid = AircraftModelId.valueOf(Objects.requireNonNull(modelId, "modelId").trim());
        final var modelOpt = aircraftModels.findByID(mid);
        if (modelOpt.isEmpty()) {
            throw new AircraftModelNotFoundException("Invalid aircraft model.");
        }

        final AircraftModel model = modelOpt.get();
        final EngineModelId resolvedEngineId =
                EngineModelId.valueOf(Objects.requireNonNull(engineModelId, "engineModelId").trim());
        final EngineModel engine = resolveCertifiedEngine(resolvedEngineId, model);
        validateCabinAgainstModel(cabin, model);

        final Aircraft created = new Aircraft(
                reg,
                model,
                engine,
                cabin,
                new RegistrationCountry(registrationCountryIso2.trim()),
                ownerCompanyIata,
                OperationalStatus.ACTIVE,
                NumberOfFlightCrew.valueOf(flightCrewCount),
                YearOfManufacture.valueOf(yearOfManufacture));

        return aircraft.save(created);
    }

    public Iterable<Aircraft> listFleet(final IATACode ownerCompanyIata, final FleetListCriteria criteria) {
        Objects.requireNonNull(ownerCompanyIata, "Owner company is required.");
        Objects.requireNonNull(criteria, "criteria");

        if (criteria.isUnfiltered()) {
            return aircraft.findByOwnerCompany(ownerCompanyIata);
        }
        if (criteria.modelId().isPresent()) {
            return aircraft.findByOwnerCompanyAndModel(ownerCompanyIata, criteria.modelId().get());
        }
        if (criteria.manufacturerId().isPresent()) {
            return filterByManufacturer(ownerCompanyIata, criteria.manufacturerId().get());
        }
        if (criteria.passengerCapacity().isPresent()) {
            return filterByPassengerCapacity(
                    ownerCompanyIata,
                    criteria.passengerCapacity().get(),
                    criteria.passengerCapacityComparison().orElse(FleetNumericComparison.EQUAL));
        }
        if (criteria.ageInYears().isPresent()) {
            return filterByAge(
                    ownerCompanyIata,
                    criteria.ageInYears().get(),
                    criteria.ageComparison().orElse(FleetNumericComparison.EQUAL));
        }
        return aircraft.findByOwnerCompany(ownerCompanyIata);
    }

    /**
     * Distinct aircraft models present in the company's fleet (active and decommissioned).
     */
    public List<AircraftModel> modelsUsedInFleet(final IATACode ownerCompanyIata) {
        Objects.requireNonNull(ownerCompanyIata, "Owner company is required.");
        final Set<AircraftModelId> modelIds = new TreeSet<>(Comparator.comparing(AircraftModelId::toString));
        for (final Aircraft a : aircraft.findByOwnerCompany(ownerCompanyIata)) {
            modelIds.add(a.aircraftModelId());
        }
        final List<AircraftModel> models = new ArrayList<>();
        for (final AircraftModelId id : modelIds) {
            aircraftModels.findByID(id).ifPresent(models::add);
        }
        models.sort(Comparator.comparing(m -> m.identity().toString()));
        return models;
    }

    /**
     * Distinct manufacturers of aircraft models used in the company's fleet.
     */
    public List<ManufacturerId> manufacturersUsedInFleet(final IATACode ownerCompanyIata) {
        Objects.requireNonNull(ownerCompanyIata, "Owner company is required.");
        final Set<ManufacturerId> makerIds = new LinkedHashSet<>();
        for (final AircraftModelId modelId : distinctModelIdsInFleet(ownerCompanyIata)) {
            aircraftModels.findByID(modelId).ifPresent(m -> makerIds.add(m.manufacturer().identity()));
        }
        final List<ManufacturerId> sorted = new ArrayList<>(makerIds);
        sorted.sort(Comparator.comparing(ManufacturerId::toString));
        return sorted;
    }

    private Set<AircraftModelId> distinctModelIdsInFleet(final IATACode ownerCompanyIata) {
        final Set<AircraftModelId> modelIds = new TreeSet<>(Comparator.comparing(AircraftModelId::toString));
        for (final Aircraft a : aircraft.findByOwnerCompany(ownerCompanyIata)) {
            modelIds.add(a.aircraftModelId());
        }
        return modelIds;
    }

    private Iterable<Aircraft> filterByPassengerCapacity(final IATACode ownerCompanyIata,
                                                         final int capacity,
                                                         final FleetNumericComparison comparison) {
        final List<Aircraft> result = new ArrayList<>();
        for (final Aircraft a : aircraft.findByOwnerCompany(ownerCompanyIata)) {
            if (matchesNumericComparison(a.cabinConfiguration().totalSeats(), capacity, comparison)) {
                result.add(a);
            }
        }
        return result;
    }

    private Iterable<Aircraft> filterByAge(final IATACode ownerCompanyIata,
                                           final int ageInYears,
                                           final FleetNumericComparison comparison) {
        final List<Aircraft> result = new ArrayList<>();
        for (final Aircraft a : aircraft.findByOwnerCompany(ownerCompanyIata)) {
            if (matchesNumericComparison(a.ageInYears(), ageInYears, comparison)) {
                result.add(a);
            }
        }
        return result;
    }

    private static boolean matchesNumericComparison(final int actual,
                                                    final int threshold,
                                                    final FleetNumericComparison comparison) {
        return switch (comparison) {
            case GREATER_THAN -> actual > threshold;
            case LESS_THAN -> actual < threshold;
            case EQUAL -> actual == threshold;
        };
    }

    private Iterable<Aircraft> filterByManufacturer(final IATACode ownerCompanyIata,
                                                    final ManufacturerId manufacturerId) {
        final List<Aircraft> result = new ArrayList<>();
        for (final Aircraft a : aircraft.findByOwnerCompany(ownerCompanyIata)) {
            final var model = aircraftModels.findByID(a.aircraftModelId());
            if (model.isPresent() && model.get().manufacturer().identity().equals(manufacturerId)) {
                result.add(a);
            }
        }
        return result;
    }

    private static EngineModel resolveCertifiedEngine(final EngineModelId engineModelId, final AircraftModel model) {
        if (engineModelId == null || model == null) {
            throw new IllegalArgumentException("Engine model and aircraft model are required.");
        }
        return model.engineCertifiedConfigurations().stream()
                .map(EngineConfiguration::engineModel)
                .filter(engine -> engine.identity().equals(engineModelId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "The selected engine model is not certified for this aircraft model."));
    }

    private static void validateCabinAgainstModel(final CabinConfiguration cabin, final AircraftModel model) {
        if (cabin == null || model == null) {
            throw new IllegalArgumentException("Cabin configuration and aircraft model are required.");
        }
        if (cabin.totalSeats() > model.numberOfSeats().seats()) {
            throw new IllegalArgumentException(
                    "Total seats exceed the maximum passenger capacity of the aircraft model.");
        }
        if (cabin.totalSeats() < 1) {
            throw new IllegalArgumentException("The aircraft must have at least one passenger seat.");
        }
    }
}
