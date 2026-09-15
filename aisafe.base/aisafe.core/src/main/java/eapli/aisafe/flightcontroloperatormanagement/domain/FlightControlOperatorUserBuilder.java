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
package eapli.aisafe.flightcontroloperatormanagement.domain;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.companycollaboratormanagment.domain.*;
import eapli.aisafe.usermanagement.domain.Phone;
import eapli.framework.domain.model.DomainFactory;
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
public class FlightControlOperatorUserBuilder implements DomainFactory<FlightControlOperatorUser> {

	private SystemUser systemUser;
	private AirControlArea airControlArea;
	private String securityData;
	private String skillData;
	private String phoneNumber;

	public FlightControlOperatorUserBuilder withSystemUser(final SystemUser systemUser) {
		this.systemUser = systemUser;
		return this;
	}

	public FlightControlOperatorUserBuilder withAirControlArea(final AirControlArea airControlArea) {
		this.airControlArea = airControlArea;
		return this;
	}

	public FlightControlOperatorUserBuilder withSecurityData(final String securityData) {
		this.securityData = securityData;
		return this;
	}

	public FlightControlOperatorUserBuilder withSkillAssessment(final String skillAssessment) {
		this.skillData = skillAssessment;
		return this;
	}

	public FlightControlOperatorUserBuilder withPhoneNumber(final String phoneNumber) {
		this.phoneNumber = phoneNumber;
		return this;
	}

	@Override
	public FlightControlOperatorUser build() {
		// since the factory knows that all the parts are needed it could throw
		// an exception. however, we will leave that to the constructor
		SecurityClearance securityClearance = SecurityClearance.valueOf(LocalDate.parse(securityData));
		final SkillsAssessment skillsAssessment = SkillsAssessment.valueOf(LocalDate.parse(skillData));
		Phone phoneNumber1 = Phone.valueOf(phoneNumber);
		return new FlightControlOperatorUser(systemUser,securityClearance,airControlArea,phoneNumber1, skillsAssessment);
	}
}
