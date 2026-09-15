package eapli.aisafe.aircraftmodelmanagement.domain;

import eapli.aisafe.enginemodelmanagement.domain.EngineModel;
import eapli.aisafe.enginemodelmanagement.domain.EngineModelId;
import eapli.framework.domain.model.DomainEntity;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
public class EngineConfiguration implements DomainEntity<Long>, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Long version;

    @ManyToOne
    @JoinColumn(name = "ENGINE_MODEL_ID")
    private EngineModel engineModel;


    protected EngineConfiguration() {
        // for ORM
    }

    public EngineConfiguration(final EngineModel engineModel) {
        if (engineModel == null) throw new IllegalArgumentException();
        this.engineModel = engineModel;
    }


    public EngineModel engineModel() {
        return engineModel;
    }

    @Override
    public boolean sameAs(Object other) {
        if (!(other instanceof EngineConfiguration)) return false;
        final EngineConfiguration that = (EngineConfiguration) other;
        return Objects.equals(this.engineModel, that.engineModel);
    }

    @Override
    public Long identity() {
        return this.id;
    }
}
