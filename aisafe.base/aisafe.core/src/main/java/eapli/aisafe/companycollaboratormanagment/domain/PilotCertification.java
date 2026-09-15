package eapli.aisafe.companycollaboratormanagment.domain;

import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.framework.domain.model.DomainEntity;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "PILOT_CERTIFICATION")
public class PilotCertification implements DomainEntity<PilotCertificationId>, Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private PilotCertificationId id;

    @Version
    private Long version;

    @ManyToOne(optional = false)
    @JoinColumn(name = "AIRCRAFT_MODEL_ID")
    private AircraftModel aircraftModel;

    @Embedded   
    private DueDate dueDate;

    protected PilotCertification() {
        // for ORM
    }

    public PilotCertification(final AircraftModel aircraftModel,
                              final DueDate dueDate) {
        Preconditions.nonNull(aircraftModel, "Aircraft model cannot be null");
        Preconditions.nonNull(dueDate, "Due date cannot be null");
        this.id = PilotCertificationId.newId();
        this.aircraftModel = aircraftModel;
        this.dueDate = dueDate;
    }

    public AircraftModel aircraftModel() {
        return aircraftModel;
    }

    public DueDate dueDate() {
        return dueDate;
    }

    public boolean isActive() {
        final LocalDate today = LocalDate.now();
        return !today.isBefore(dueDate.startDate()) && !today.isAfter(dueDate.endDate());
    }

    @Override
    public PilotCertificationId identity() {
        return id;
    }

    @Override
    public boolean sameAs(final Object other) {
        if (!(other instanceof PilotCertification)) {
            return false;
        }
        final PilotCertification that = (PilotCertification) other;
        return Objects.equals(id, that.id)
                && Objects.equals(aircraftModel, that.aircraftModel)
                && Objects.equals(dueDate, that.dueDate);
    }
}
