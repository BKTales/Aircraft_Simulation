package eapli.aisafe.companycollaboratormanagment.domain;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.usermanagement.domain.Phone;
import eapli.framework.domain.model.DomainFactory;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PilotUserBuilder implements DomainFactory<PilotUser> {

    private SystemUser systemUser;
    private AirTransportCompany airTransportCompany;
    private String securityData;
    private String skillData;
    private String phoneNumber;
    private final List<PilotCertification> certifications = new ArrayList<>();

    public PilotUserBuilder withSystemUser(final SystemUser systemUser) {
        this.systemUser = systemUser;
        return this;
    }

    public PilotUserBuilder withAirTransportCompany(final AirTransportCompany airTransportCompany) {
        this.airTransportCompany = airTransportCompany;
        return this;
    }

    public PilotUserBuilder withSecurityData(final String securityData) {
        this.securityData = securityData;
        return this;
    }

    public PilotUserBuilder withSkillAssessment(final String skillAssessment) {
        this.skillData = skillAssessment;
        return this;
    }

    public PilotUserBuilder withPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public PilotUserBuilder withCertification(final PilotCertification certification) {
        if (certification == null) {
            throw new IllegalArgumentException("Certification cannot be null");
        }
        this.certifications.add(certification);
        return this;
    }

    @Override
    public PilotUser build() {
        if (certifications.isEmpty()) {
            throw new IllegalStateException("A pilot must have at least one certification.");
        }
        final SecurityClearance securityClearance = SecurityClearance.valueOf(LocalDate.parse(securityData));
        final Phone phone = Phone.valueOf(phoneNumber);
        final SkillsAssessment skillsAssessment = SkillsAssessment.valueOf(LocalDate.parse(skillData));
        final PilotUser pilot = new PilotUser(systemUser, securityClearance, airTransportCompany, phone, skillsAssessment);
        certifications.forEach(pilot::addPilotCertification);
        return pilot;
    }
}