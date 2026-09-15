package eapli.aisafe.flightcontroloperatormanagement.domain;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.companycollaboratormanagment.domain.SecurityClearance;
import eapli.aisafe.companycollaboratormanagment.domain.SkillsAssessment;
import eapli.aisafe.usermanagement.domain.AISafeUser;
import eapli.aisafe.usermanagement.domain.AISafeUserId;
import eapli.aisafe.usermanagement.domain.Phone;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity
public class FlightControlOperatorUser extends AISafeUser implements AggregateRoot<AISafeUserId>, Serializable {

    private static final long serialVersionUID = 1L;

    @Version
    private Long version;

    @ManyToOne(optional = false)
    @JoinColumn(name = "AIR_CONTROL_AREA_ID")
    private AirControlArea airControlArea;

    protected FlightControlOperatorUser() {
        // ORM
    }

    public FlightControlOperatorUser(final SystemUser user,
                                     final SecurityClearance securityClearance,
                                     final AirControlArea airControlArea, final Phone phoneNumber, final SkillsAssessment skillsAssessment) {

        super(user,securityClearance,phoneNumber, skillsAssessment);
        Preconditions.nonNull(airControlArea,"Air control area cannot be null");
        this.airControlArea = airControlArea;
    }

    @Override
    public boolean sameAs(final Object other) {
        return DomainEntities.areEqual(this, other);
    }

    @Override
    public AISafeUserId identity() {
        return id;
    }

    public AirControlArea airControlArea() {
        return airControlArea;
    }

}
