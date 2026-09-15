/*
 * Copyright (c) 2013-2024 the original author or authors.
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package eapli.aisafe.companycollaboratormanagment.domain;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.usermanagement.domain.Phone;
import eapli.framework.domain.model.DomainFactory;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;

import java.time.LocalDate;

/**
 * A factory for User entities.
 *
 * This class demonstrates the use of the factory (DDD) pattern using a fluent
 * interface. It acts as a Builder (GoF).
 *
 * @author Jorge Santos ajs@isep.ipp.pt 02/04/2016
 */
public class CompanyCollaboratorUserBuilder implements DomainFactory<CompanyCollaboratorUser> {

	private SystemUser systemUser;
	private AirTransportCompany airTransportCompany;
	private String securityData;
	private String skillsData;
	private String phoneNumber;

	public CompanyCollaboratorUserBuilder withSystemUser(final SystemUser systemUser) {
		this.systemUser = systemUser;
		return this;
	}

	public CompanyCollaboratorUserBuilder withAirTransportCompany(final AirTransportCompany airTransportCompany) {
		this.airTransportCompany = airTransportCompany;
		return this;
	}

	public CompanyCollaboratorUserBuilder withSecurityData(final String securityData) {
		this.securityData = securityData;
		return this;
	}

	public CompanyCollaboratorUserBuilder withSkillsData(final String skillsData) {
		this.skillsData = skillsData;
		return this;
	}

	public CompanyCollaboratorUserBuilder withPhoneNumber(final String phoneNumber) {
		this.phoneNumber = phoneNumber;
		return this;
	}

	@Override
	public CompanyCollaboratorUser build() {
		// since the factory knows that all the parts are needed it could throw
		// an exception. however, we will leave that to the constructor
		SecurityClearance securityClearance = SecurityClearance.valueOf(LocalDate.parse(securityData));
		SkillsAssessment skillsAssessment = SkillsAssessment.valueOf(LocalDate.parse(skillsData));
		Phone phoneNumber1 = Phone.valueOf(phoneNumber);
		return new CompanyCollaboratorUser(systemUser,securityClearance,airTransportCompany,phoneNumber1, skillsAssessment);
	}
}
