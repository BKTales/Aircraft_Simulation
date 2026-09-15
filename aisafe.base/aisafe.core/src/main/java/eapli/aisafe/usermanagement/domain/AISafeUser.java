package eapli.aisafe.usermanagement.domain;


import eapli.aisafe.companycollaboratormanagment.domain.SecurityClearance;
import eapli.aisafe.companycollaboratormanagment.domain.SkillsAssessment;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AISafeUser implements Serializable{

    private static final long serialVersionUID = 1L;


    @Version
    private Long version;

    @EmbeddedId
    protected AISafeUserId id;

    @OneToOne(optional = false)
    private SystemUser systemUser;

    @Embedded
    protected Phone phoneNumber;

    @Embedded
    protected SecurityClearance securityClearance;

    @Embedded
    protected SkillsAssessment skillsAssessment;

    protected AISafeUser() {
        // ORM
    }

    protected AISafeUser(final SystemUser user, final SecurityClearance securityClearance, final Phone phoneNumber, final SkillsAssessment skillsAssessment) {
        Preconditions.nonNull(user,"Respective system user cannot be null");
        Preconditions.nonNull(securityClearance,"Security Clearance cannot be null");
        Preconditions.nonNull(phoneNumber, "Phone number cannot be null");
        Preconditions.nonNull(skillsAssessment, "Skills assessment cannot be null");
        this.id = AISafeUserId.newId();
        this.systemUser = user;
        this.securityClearance = securityClearance;
        this.phoneNumber = phoneNumber;
        this.skillsAssessment = skillsAssessment;
    }

    public SystemUser systemUser(){return systemUser;}
    public SecurityClearance securityClearance() {
        return securityClearance;
    }
    public Phone phoneNumber(){return phoneNumber;}
    public SkillsAssessment skillsAssessment(){return skillsAssessment;}

    public boolean hasActiveClearance() {
        return securityClearance != null && securityClearance.isActive();
    }

    public boolean hasValidAssessment() {
        return skillsAssessment != null && skillsAssessment.isValid();
    }

    public void updateSecurityClearance(final SecurityClearance securityClearance) {
        Preconditions.nonNull(securityClearance, "Security Clearance cannot be null");
        this.securityClearance = securityClearance;
    }

    public void updateSkillsAssessment(final SkillsAssessment skillsAssessment) {
        Preconditions.nonNull(skillsAssessment, "Skills Assessment cannot be null");
        this.skillsAssessment = skillsAssessment;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AISafeUser{" +
                "id=" + id +
                ", systemUser=" + systemUser +
                ", phoneNumber=" + phoneNumber +
                ", securityClearance=" + securityClearance +
                ", skillsAssessment=" + skillsAssessment +
                '}';
    }

}