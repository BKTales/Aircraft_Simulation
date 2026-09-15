package eapli.aisafe.enginemodelmanagement.domain;

import eapli.aisafe.manufacturermanagement.domain.Manufacturer;
import eapli.aisafe.manufacturermanagement.domain.ManufacturerId;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity
public class EngineModel implements AggregateRoot<EngineModelId>, Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private EngineModelId identity;

    @Version
    private Long version;

    @Embedded
    private EngineName name;

    @Embedded
    private TSFC tsfc;

    @Enumerated(EnumType.STRING)
    @Column(name = "FUEL_TYPE")
    private FuelType fuelType;

    @Embedded
    private ThrustProfile thrustProfile;

    @ManyToOne(optional = false)
    @JoinColumn(name = "MANUFACTURER_ID")
    private Manufacturer manufacturer;

    @Enumerated(EnumType.STRING)
    @Column(name = "MOTORIZATION_TYPE")
    private MotorizationType motorization;

    protected EngineModel() {
        // for ORM
    }

    public EngineModel(final EngineModelId id, final EngineName name, final TSFC tsfc,
                       final FuelType fuelType, final ThrustProfile thrustProfile,
                       final Manufacturer manufacturer, final MotorizationType motorization) {

        if (id == null || name == null || tsfc == null || fuelType == null ||
                thrustProfile == null || manufacturer == null || motorization == null) {
            throw new IllegalArgumentException("All engine components must be provided.");
        }

        this.identity = id;
        this.name = name;
        this.tsfc = tsfc;
        this.fuelType = fuelType;
        this.thrustProfile = thrustProfile;
        this.manufacturer = manufacturer;
        this.motorization = motorization;
    }

    @Override
    public boolean sameAs(Object other) {
        return DomainEntities.areEqual(this, other);
    }

    @Override
    public EngineModelId identity() {
        return this.identity;
    }

    public EngineName name() {
        return name;
    }

    public Manufacturer manufacturer() {
        return manufacturer;
    }

    public ManufacturerId manufacturerId() {
        return manufacturer.identity();
    }

    public MotorizationType motorization() {
        return motorization;
    }

    public FuelType fuelType() {
        return fuelType;
    }

    public TSFC tsfc() {
        return tsfc;
    }

    public ThrustProfile thrustProfile() {
        return thrustProfile;
    }
}
