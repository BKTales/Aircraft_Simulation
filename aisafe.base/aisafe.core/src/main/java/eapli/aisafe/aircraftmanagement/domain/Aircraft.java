package eapli.aisafe.aircraftmanagement.domain;

import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModelId;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.aisafe.enginemodelmanagement.domain.EngineModelId;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "AIRCRAFT")
public class Aircraft implements AggregateRoot<AircraftRegistration>, Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private AircraftRegistration registration;

    @Version
    private Long version;

    @ManyToOne(optional = false)
    @JoinColumn(name = "MODEL_CODE")
    private AircraftModel aircraftModel;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ENGINE_MODEL_CODE")
    private EngineModel engineModel;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "CABIN_CONFIGURATION_ID")
    private CabinConfiguration cabinConfiguration;

    @Embedded
    private RegistrationCountry registrationCountry;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "code", column = @Column(name = "OWNER_COMPANY_IATA", nullable = false, length = 2))
    })
    private IATACode ownerCompanyIata;

    @Enumerated(EnumType.STRING)
    @Column(name = "OPERATIONAL_STATUS", nullable = false)
    private OperationalStatus operationalStatus;

    @Embedded
    private NumberOfFlightCrew numberOfFlightCrew;

    @Embedded
    private YearOfManufacture yearOfManufacture;

    protected Aircraft() {
        // ORM
    }

    public Aircraft(final AircraftRegistration registration,
                    final AircraftModel aircraftModel,
                    final EngineModel engineModel,
                    final CabinConfiguration cabinConfiguration,
                    final RegistrationCountry registrationCountry,
                    final IATACode ownerCompanyIata,
                    final OperationalStatus operationalStatus,
                    final NumberOfFlightCrew numberOfFlightCrew,
                    final YearOfManufacture yearOfManufacture) {
        if (registration == null || aircraftModel == null || engineModel == null
                || cabinConfiguration == null || registrationCountry == null || ownerCompanyIata == null
                || operationalStatus == null || numberOfFlightCrew == null || yearOfManufacture == null) {
            throw new IllegalArgumentException("All aircraft fields are required.");
        }
        this.registration = registration;
        this.aircraftModel = aircraftModel;
        this.engineModel = engineModel;
        this.cabinConfiguration = cabinConfiguration;
        this.registrationCountry = registrationCountry;
        this.ownerCompanyIata = ownerCompanyIata;
        this.operationalStatus = operationalStatus;
        this.numberOfFlightCrew = numberOfFlightCrew;
        this.yearOfManufacture = yearOfManufacture;
    }

    @Override
    public AircraftRegistration identity() {
        return registration;
    }

    @Override
    public boolean sameAs(final Object other) {
        return DomainEntities.areEqual(this, other);
    }

    public OperationalStatus operationalStatus() {
        return operationalStatus;
    }

    public IATACode ownerCompanyIata() {
        return ownerCompanyIata;
    }

    public AircraftModel aircraftModel() {
        return aircraftModel;
    }

    public EngineModel engineModel() {
        return engineModel;
    }

    public AircraftModelId aircraftModelId() {
        return aircraftModel.identity();
    }

    public EngineModelId engineModelId() {
        return engineModel.identity();
    }

    public CabinConfiguration cabinConfiguration() {
        return cabinConfiguration;
    }

    public int ageInYears() {
        return yearOfManufacture.ageInYears();
    }

    public int yearOfManufacture() {
        return yearOfManufacture.year();
    }

    public boolean isActive() {
        return operationalStatus == OperationalStatus.ACTIVE;
    }

    /** Retires this aircraft from active service. */
    public void retireFromActiveService() {
        if (operationalStatus == OperationalStatus.DECOMMISSIONED) {
            throw new IllegalStateException("Aircraft is already decommissioned.");
        }
        this.operationalStatus = OperationalStatus.DECOMMISSIONED;
    }

    /** Throws if this aircraft cannot be assigned to a new flight (e.g. decommissioned). */
    public void assertAssignableToNewFlight() {
        if (!isActive()) {
            throw new IllegalStateException("Cannot assign a decommissioned aircraft to a flight.");
        }
    }
}
