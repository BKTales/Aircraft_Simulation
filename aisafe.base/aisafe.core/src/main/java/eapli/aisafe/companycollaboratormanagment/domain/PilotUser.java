package eapli.aisafe.companycollaboratormanagment.domain;

import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModelId;
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.companycollaboratormanagment.dto.ResponsePilotCollaboratorDTO;
import eapli.aisafe.usermanagement.domain.AISafeUser;
import eapli.aisafe.usermanagement.domain.AISafeUserId;
import eapli.aisafe.usermanagement.domain.Phone;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.representations.dto.DTOable;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Entity
public class PilotUser extends AISafeUser implements AggregateRoot<AISafeUserId>, DTOable<ResponsePilotCollaboratorDTO>, Serializable {

    private static final long serialVersionUID = 1L;

    @Version
    private Long version;


    @ManyToOne(optional = false)
    @JoinColumn(name = "AIR_TRANSPORT_COMPANY_IATA", referencedColumnName = "IATA_CODE")
    private AirTransportCompany airTransportCompany;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "PILOT_COLLABORATOR_ID")
    private List<PilotCertification> pilotCertifications = new ArrayList<>();

    protected PilotUser() {
        // ORM
    }

    public PilotUser(final SystemUser user,
                     final SecurityClearance securityClearance,
                     final AirTransportCompany airTransportCompany, final Phone phoneNumber, final SkillsAssessment skillsAssessment) {

        super(user,securityClearance,phoneNumber,skillsAssessment);
        Preconditions.nonNull(airTransportCompany, "Air transport company cannot be null");
        this.airTransportCompany = airTransportCompany;
    }

    @Override
    public ResponsePilotCollaboratorDTO toDTO() {
        return new ResponsePilotCollaboratorDTO(
                systemUser().email().toString(),
                systemUser().name().firstName(),
                systemUser().name().lastName(),
                phoneNumber.toString(),
                securityClearance().expiryDate().toString(),
                skillsAssessment().date().toString(),
                pilotCertifications.size()
        );
    }

    @Override
    public boolean sameAs(final Object other) {
        return DomainEntities.areEqual(this, other);
    }

    @Override
    public AISafeUserId identity() {
        return id;
    }


    public AirTransportCompany airTransportCompany() {
        return airTransportCompany;
    }


    public List<PilotCertification> pilotCertifications() {
        return pilotCertifications;
    }

    public void addPilotCertification(final PilotCertification certification) {
        pilotCertifications.add(certification);
    }

    public boolean isCertifiedFor(final AircraftModelId modelId) {
        if (modelId == null) {
            return false;
        }
        return pilotCertifications.stream()
                .filter(PilotCertification::isActive)
                .anyMatch(c -> c.aircraftModel().identity().equals(modelId));
    }

    public void deactivateFromRoster(final Calendar when) {
        if (!systemUser().isActive()) {
            throw new IllegalStateException("Pilot is already inactive.");
        }
        systemUser().deactivate(when);
    }

}
