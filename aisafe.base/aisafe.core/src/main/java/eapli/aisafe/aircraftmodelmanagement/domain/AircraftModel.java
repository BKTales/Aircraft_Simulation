package eapli.aisafe.aircraftmodelmanagement.domain;


import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.aisafe.enginemodelmanagement.domain.EngineModelId;
import eapli.aisafe.manufacturermanagement.domain.Manufacturer;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "aircraft_model", uniqueConstraints = {
        @UniqueConstraint(
                name = "UK_MANUFACTURER_MODEL_NAME",
                columnNames = {"MANUFACTURER_ID", "MODEL_NAME"}
        )
})
public class AircraftModel implements AggregateRoot<AircraftModelId>, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private AircraftModelId modelCode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "MANUFACTURER_ID")
    private Manufacturer manufacturer;

    @Version
    private Long version;

    @Embedded
    private ModelName name;

    @Enumerated(EnumType.STRING)
    private AircraftType aircraftType; // passenger | cargo | mixed
    
    @Embedded
    private WeightSpecification weights;

    @Embedded
    private WingGeometry wingGeometry;

    @Embedded
    private AerodynamicCoefficients aerodynamics;

    @Embedded
    private PerformanceSpec performanceSpec;

    @Embedded
    private NumberOfEngines numberOfEngines;

    @Embedded
    private NumberOfSeats numberOfSeats;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "AIRCRAFT_MODEL_ID")
    private List<EngineConfiguration> certifiedConfigurations = new ArrayList<>();

    protected AircraftModel() {
        // for ORM
    }

    public AircraftModel(final AircraftModelId modelId, final ModelName name, final AircraftType aircraftType,
                         final Manufacturer manufacturer, final WeightSpecification weights, final WingGeometry geometry,
                         final AerodynamicCoefficients aerodynamics, final PerformanceSpec performanceSpec,
                         final NumberOfEngines numberOfEngines, final NumberOfSeats numberOfSeats) {

        if (modelId == null || name == null  ||
                weights == null || geometry == null || aerodynamics == null ||
                performanceSpec == null || numberOfEngines == null || numberOfSeats == null) {
            throw new IllegalArgumentException("All domain components must be provided.");
        }

        this.modelCode = modelId;
        this.name = name;
        this.aircraftType = aircraftType;
        this.manufacturer = manufacturer;
        this.weights = weights;
        this.wingGeometry = geometry;
        this.aerodynamics = aerodynamics;
        this.performanceSpec = performanceSpec;
        this.numberOfEngines = numberOfEngines;
        this.numberOfSeats = numberOfSeats;
    }



    @Override
    public boolean sameAs(Object other) {
        return DomainEntities.areEqual(this, other);
    }

    @Override
    public AircraftModelId identity() {
        return this.modelCode;
    }

    public ModelName name() {
        return name;
    }

    public Manufacturer manufacturer() {
        return manufacturer;
    }

    public AircraftType aircraftType() {
        return aircraftType;
    }

    public WeightSpecification weights() {
        return weights;
    }

    public WingGeometry wingGeometry() {
        return wingGeometry;
    }

    public AerodynamicCoefficients aerodynamics() {
        return aerodynamics;
    }

    public PerformanceSpec performanceSpec() {
        return performanceSpec;
    }

    public NumberOfEngines numberOfEngines() {
        return numberOfEngines;
    }

    public NumberOfSeats numberOfSeats() {
        return numberOfSeats;
    }

    public List<EngineConfiguration> engineCertifiedConfigurations() {
        return certifiedConfigurations;
    }


    public void addEngineConfiguration(final EngineModel engineModel) {
        if (engineModel == null) {
            throw new IllegalArgumentException("Engine model and its identity are required.");
        }


        for (final EngineConfiguration cfg : certifiedConfigurations) {
            if (cfg != null && cfg.engineModel() != null && engineModel.equals(cfg.engineModel())) {
                throw new IllegalArgumentException("Engine model already certified for this aircraft model.");
            }
        }

        certifiedConfigurations.add(new EngineConfiguration(engineModel));
    }

    public void ensureCertifiedEngineCount() {
        if (certifiedConfigurations.isEmpty()) {
            throw new IllegalArgumentException("At least one engine model must be certified.");
        }
    }

}
