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
package eapli.aisafe.companycollaboratormanagment.application;
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.airtransportcompanymanagement.repositories.AirTransportCompanyRepository;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorRoles;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUserBuilder;
import eapli.aisafe.companycollaboratormanagment.domain.PilotUser;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;

import eapli.aisafe.companycollaboratormanagment.repositories.PilotUserRepository;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.aisafe.usermanagement.domain.UserBuilderHelper;
import eapli.framework.application.ApplicationService;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.general.domain.model.EmailAddress;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@ApplicationService
public class CompanyCollaboratorUserService {

    public Iterable<CompanyCollaboratorUser> findActiveCollaboratorsByCompany(
            final CompanyCollaboratorUserRepository collaboratorRepository,
            final AirTransportCompany airTransportCompany) {
        return collaboratorRepository.findATCCByCompanyAndActive(airTransportCompany);
    }

    public CompanyCollaboratorUser findATCC(
            final CompanyCollaboratorUserRepository collaboratorRepository,
            final Username username) {
        return collaboratorRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalStateException("No collaborator linked to user"));
    }

    public void createCollaboratorUser(final String username, final String password, final String firstName,
                                       final String lastName, final String email, final Set<Role> roles,
                                       final Calendar createdOn, final String companyId, final String securityData, final String skillsData,
                                       final String phoneNumber, final UserRepository userRepository,
                                       final CompanyCollaboratorUserRepository collaboratorRepository,
                                       final TransactionalContext txCtx,
                                       final AirTransportCompanyRepository airTransportCompanyRepository) {
        if (txCtx != null) txCtx.beginTransaction();

        final SystemUser newUser = registerNewUser(username, password, firstName, lastName, email, roles, createdOn, userRepository);
        createCompanyCollaboratorUser(newUser, companyId, roles, securityData, skillsData, phoneNumber, collaboratorRepository, airTransportCompanyRepository);

        if (txCtx != null) { txCtx.commit(); txCtx.close(); }
    }

    public void createATCCOnly(final String email, final String companyId, final String securityData, final String skillsData,
                               final String phoneNumber, final List<SystemUser> eligibleUsers,
                               final CompanyCollaboratorUserRepository collaboratorRepository,
                               final TransactionalContext txCtx,
                               final AirTransportCompanyRepository airTransportCompanyRepository) {
        if (txCtx != null) txCtx.beginTransaction();

        final SystemUser existingUser = eligibleUsers.stream()
                .filter(u -> u.email().toString().equals(email))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));

        final Set<Role> roles = new HashSet<>(existingUser.roleTypes());
        createCompanyCollaboratorUser(existingUser, companyId, roles, securityData, skillsData, phoneNumber, collaboratorRepository, airTransportCompanyRepository);

        if (txCtx != null) { txCtx.commit(); txCtx.close(); }
    }

    public List<SystemUser> findEligibleUsersForATCC(final UserRepository userRepository,
                                                     final CompanyCollaboratorUserRepository collaboratorRepository) {
        final Iterable<SystemUser> activeUsers = userRepository.findByActive(true);

        final Set<EmailAddress> alreadyRegistered = StreamSupport
                .stream(collaboratorRepository.findAll().spliterator(), false)
                .map(atcc -> atcc.systemUser().email())
                .collect(Collectors.toSet());

        return StreamSupport.stream(activeUsers.spliterator(), false)
                .filter(u -> u.hasAny(AISafeRoles.PILOT, AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR))
                .filter(u -> !alreadyRegistered.contains(u.email()))
                .collect(Collectors.toList());
    }

    private void createCompanyCollaboratorUser(final SystemUser newUser, final String companyId,
                                               final Set<Role> roles, final String securityData, final String skillsData,
                                               final String phoneNumber,
                                               final CompanyCollaboratorUserRepository collaboratorRepository,
                                               final AirTransportCompanyRepository airTransportCompanyRepository) {
        if (roles.size() != 1) {
            throw new IllegalArgumentException("ATCC can only have one role!");
        }

        final AirTransportCompany company = airTransportCompanyRepository
                .ofIdentity(IATACode.valueOf(companyId))
                .orElseThrow(() -> new EntityNotFoundException("Air transport company not found: " + companyId));

        final Role role = roles.iterator().next();
        if (!role.equals(CompanyCollaboratorRoles.ATCC)) {
            throw new IllegalArgumentException("Invalid role for company collaborator: " + role);
        }

        final CompanyCollaboratorUser user = new CompanyCollaboratorUserBuilder()
                .withAirTransportCompany(company)
                .withSystemUser(newUser)
                .withPhoneNumber(phoneNumber)
                .withSecurityData(securityData)
                .withSkillsData(skillsData)
                .build();

        collaboratorRepository.save(user);
    }

    private SystemUser registerNewUser(final String username, final String rawPassword, final String firstName,
                                       final String lastName, final String email, final Set<Role> roles,
                                       final Calendar createdOn, final UserRepository userRepository) {
        final var userBuilder = UserBuilderHelper.builder();
        userBuilder.with(username, rawPassword, firstName, lastName, email).createdOn(createdOn).withRoles(roles);
        return userRepository.save(userBuilder.build());
    }
}
