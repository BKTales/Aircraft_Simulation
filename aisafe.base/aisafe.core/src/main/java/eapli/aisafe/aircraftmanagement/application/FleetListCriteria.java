package eapli.aisafe.aircraftmanagement.application;

import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModelId;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;

import java.util.Objects;
import java.util.Optional;

/**
 * Optional filters for listing a company's aircraft fleet (US072–US072d).
 */
public final class FleetListCriteria {

    private final Optional<AircraftModelId> modelId;
    private final Optional<ManufacturerId> manufacturerId;
    private final Optional<Integer> passengerCapacity;
    private final Optional<FleetNumericComparison> passengerCapacityComparison;
    private final Optional<Integer> ageInYears;
    private final Optional<FleetNumericComparison> ageComparison;

    private FleetListCriteria(final Optional<AircraftModelId> modelId,
                              final Optional<ManufacturerId> manufacturerId,
                              final Optional<Integer> passengerCapacity,
                              final Optional<FleetNumericComparison> passengerCapacityComparison,
                              final Optional<Integer> ageInYears,
                              final Optional<FleetNumericComparison> ageComparison) {
        this.modelId = modelId;
        this.manufacturerId = manufacturerId;
        this.passengerCapacity = passengerCapacity;
        this.passengerCapacityComparison = passengerCapacityComparison;
        this.ageInYears = ageInYears;
        this.ageComparison = ageComparison;
    }

    public static FleetListCriteria unfiltered() {
        return new FleetListCriteria(
                Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty());
    }

    public static FleetListCriteria byModel(final AircraftModelId modelId) {
        return new FleetListCriteria(
                Optional.of(Objects.requireNonNull(modelId, "modelId")),
                Optional.empty(),
                Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty());
    }

    public static FleetListCriteria byManufacturer(final ManufacturerId manufacturerId) {
        return new FleetListCriteria(
                Optional.empty(),
                Optional.of(Objects.requireNonNull(manufacturerId, "manufacturerId")),
                Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty());
    }

    public static FleetListCriteria byPassengerCapacity(final int passengerCapacity,
                                                        final FleetNumericComparison comparison) {
        if (passengerCapacity < 0) {
            throw new IllegalArgumentException("Passenger capacity cannot be negative.");
        }
        Objects.requireNonNull(comparison, "comparison");
        return new FleetListCriteria(
                Optional.empty(), Optional.empty(),
                Optional.of(passengerCapacity), Optional.of(comparison),
                Optional.empty(), Optional.empty());
    }

    public static FleetListCriteria byAge(final int ageInYears, final FleetNumericComparison comparison) {
        if (ageInYears < 0) {
            throw new IllegalArgumentException("Age cannot be negative.");
        }
        Objects.requireNonNull(comparison, "comparison");
        return new FleetListCriteria(
                Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(),
                Optional.of(ageInYears), Optional.of(comparison));
    }

    public boolean isUnfiltered() {
        return modelId.isEmpty()
                && manufacturerId.isEmpty()
                && passengerCapacity.isEmpty()
                && ageInYears.isEmpty();
    }

    public Optional<AircraftModelId> modelId() {
        return modelId;
    }

    public Optional<ManufacturerId> manufacturerId() {
        return manufacturerId;
    }

    public Optional<Integer> passengerCapacity() {
        return passengerCapacity;
    }

    public Optional<FleetNumericComparison> passengerCapacityComparison() {
        return passengerCapacityComparison;
    }

    public Optional<Integer> ageInYears() {
        return ageInYears;
    }

    public Optional<FleetNumericComparison> ageComparison() {
        return ageComparison;
    }
}
