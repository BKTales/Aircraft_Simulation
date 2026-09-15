package eapli.aisafe.companycollaboratormanagment.domain;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.companycollaboratormanagment.dto.ResponsePilotCollaboratorDTO;
import eapli.aisafe.usermanagement.domain.AISafeUser;
import eapli.aisafe.usermanagement.domain.AISafeUserId;
import eapli.aisafe.usermanagement.domain.Phone;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.representations.dto.DTOable;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
public class CompanyCollaboratorUser extends AISafeUser implements AggregateRoot<AISafeUserId>, Serializable {

    private static final long serialVersionUID = 1L;

    @Version
    private Long version;


    @ManyToOne(optional = false)
    @JoinColumn(name = "AIR_TRANSPORT_COMPANY_IATA", referencedColumnName = "IATA_CODE")
    private AirTransportCompany airTransportCompany;


    protected CompanyCollaboratorUser() {
        // ORM
    }

    public CompanyCollaboratorUser(final SystemUser user,
                                   final SecurityClearance securityClearance,
                                   final AirTransportCompany airTransportCompany, final Phone phoneNumber, final SkillsAssessment skillsAssessment) {

        super(user,securityClearance,phoneNumber, skillsAssessment);
        Preconditions.nonNull(airTransportCompany, "Air transport company cannot be null");
        this.airTransportCompany = airTransportCompany;
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



}
