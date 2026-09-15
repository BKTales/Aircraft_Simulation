/*
 * Copyright (c) 2013-2024 the original author or authors.
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package eapli.aisafe.flightcontroloperatormanagement.application;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.aircontrolarea.repositories.AirControlAreaRepository;
import eapli.aisafe.flightcontroloperatormanagement.domain.FlightControlOperatorUser;
import eapli.aisafe.flightcontroloperatormanagement.domain.FlightControlOperatorUserBuilder;
import eapli.aisafe.flightcontroloperatormanagement.repositories.FlightControlOperatorUserRepository;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.aisafe.usermanagement.domain.UserBuilderHelper;
import eapli.framework.application.ApplicationService;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.general.domain.model.EmailAddress;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author mcn
 */
@ApplicationService
public class FlightControlOperatorUserService {

    public Iterable<FlightControlOperatorUser> findActiveFlightControlOperatorsByArea(
            FlightControlOperatorUserRepository flightControlOperatorUserRepository,
            AirControlArea airControlArea) {
        return flightControlOperatorUserRepository.findByAreaAndActive(airControlArea);
    }

    public void createCollaboratorUser(final String username, final String password, final String firstName,
                                       final String lastName, final String email, final Set<Role> roles,
                                       final Calendar createdOn, final String areaCode, final String securityData, final String skillsData,
                                       final String phoneNumber, final UserRepository userRepository,
                                       final FlightControlOperatorUserRepository operatorRepository,
                                       final TransactionalContext txCtx,
                                       final AirControlAreaRepository airControlAreaRepository) {
        if (txCtx != null) txCtx.beginTransaction();

        final SystemUser newUser = registerNewUser(username, password, firstName, lastName, email, roles, createdOn, userRepository);
        createFlightControlOperatorUser(newUser, areaCode, securityData, skillsData, phoneNumber, operatorRepository, airControlAreaRepository);

        if (txCtx != null) { txCtx.commit(); txCtx.close(); }
    }

    public void createFCOOnly(final String email, final String areaCode, final String securityData, final String skillsData,
                              final String phoneNumber, final List<SystemUser> eligibleUsers,
                              final FlightControlOperatorUserRepository operatorRepository,
                              final TransactionalContext txCtx,
                              final AirControlAreaRepository airControlAreaRepository) {
        if (txCtx != null) txCtx.beginTransaction();

        final SystemUser existingUser = eligibleUsers.stream()
                .filter(u -> u.email().toString().equals(email))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));

        createFlightControlOperatorUser(existingUser, areaCode, securityData, skillsData, phoneNumber,
                operatorRepository, airControlAreaRepository);

        if (txCtx != null) { txCtx.commit(); txCtx.close(); }
    }

    public List<SystemUser> findEligibleUsersForFCO(final UserRepository userRepository,
                                                    final FlightControlOperatorUserRepository flightControlOperatorUserRepository) {
        final Iterable<SystemUser> activeUsers = userRepository.findByActive(true);

        final Set<EmailAddress> alreadyRegistered = StreamSupport
                .stream(flightControlOperatorUserRepository.findAll().spliterator(), false)
                .map(fco -> fco.systemUser().email())
                .collect(Collectors.toSet());

        return StreamSupport.stream(activeUsers.spliterator(), false)
                .filter(u -> u.hasAny(AISafeRoles.FLIGHT_CONTROL_OPERATOR))
                .filter(u -> !alreadyRegistered.contains(u.email()))
                .collect(Collectors.toList());
    }

    private void createFlightControlOperatorUser(final SystemUser newUser, final String areaCode,
                                                 final String securityData, final String skillsData, final String phoneNumber,
                                                 final FlightControlOperatorUserRepository operatorRepository,
                                                 final AirControlAreaRepository airControlAreaRepository) {
        final AirControlArea airControlArea = airControlAreaRepository
                .ofIdentity(AreaCode.valueOf(areaCode))
                .orElseThrow(() -> new EntityNotFoundException("Air control area not found: " + areaCode));
        final var flightControlOperatorUser = new FlightControlOperatorUserBuilder();
        flightControlOperatorUser.withAirControlArea(airControlArea);
        flightControlOperatorUser.withSystemUser(newUser);
        flightControlOperatorUser.withSecurityData(securityData);
        flightControlOperatorUser.withPhoneNumber(phoneNumber);
        flightControlOperatorUser.withSkillAssessment(skillsData);
        FlightControlOperatorUser user = flightControlOperatorUser.build();
        operatorRepository.save(user);
    }

    private SystemUser registerNewUser(final String username, final String rawPassword, final String firstName,
                                       final String lastName, final String email, final Set<Role> roles,
                                       final Calendar createdOn, final UserRepository userRepository) {
        final var userBuilder = UserBuilderHelper.builder();
        userBuilder.with(username, rawPassword, firstName, lastName, email).createdOn(createdOn).withRoles(roles);
        return userRepository.save(userBuilder.build());
    }

}
