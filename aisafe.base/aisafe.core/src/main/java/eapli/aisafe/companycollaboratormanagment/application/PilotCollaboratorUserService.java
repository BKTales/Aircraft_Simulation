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

import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModelId;
import eapli.aisafe.aircraftmodelmanagement.repositories.AircraftModelRepository;
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.companycollaboratormanagment.domain.*;
import eapli.aisafe.companycollaboratormanagment.dto.ResponsePilotCollaboratorDTO;
import eapli.aisafe.companycollaboratormanagment.repositories.PilotUserRepository;
import eapli.aisafe.usermanagement.domain.UserBuilderHelper;
import eapli.framework.application.ApplicationService;
import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;
@ApplicationService
public class PilotCollaboratorUserService {

    public ListPilotsResult findActivePilotsByCompany(final PilotUserRepository pilotRepository,
                                                           final AirTransportCompany company) {
        try {
            Iterable<PilotUser> activePilots = pilotRepository.findPilotByCompanyAndActive(company);
            List<ResponsePilotCollaboratorDTO> dtoList = new ArrayList<>();
            activePilots.forEach(p -> dtoList.add(p.toDTO()));

            return ListPilotsResult.success(dtoList);
        } catch (Exception e) {
            return ListPilotsResult.failure(ListPilotsResult.Outcome.ERROR, "Error retrieving pilots: " + e.getMessage());
        }
    }


    public PilotUser findPilot(
            final PilotUserRepository pilotUserRepository,
            final Username username) {
        return pilotUserRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalStateException("No pilot linked to user"));
    }

    public AddPilotResult createPilotUser(final String username, final String password, final String firstName,
                                          final String lastName, final String email, final Set<Role> roles,
                                          final Calendar createdOn, final AirTransportCompany company, final String securityData, final String skillData,
                                          final String phoneNumber, final List<PilotCertificationSpec> certifications,
                                          final UserRepository userRepository,
                                          final PilotUserRepository pilotRepository,
                                          final TransactionalContext txCtx,
                                          final AircraftModelRepository aircraftModelRepository) {

        return executeInTransaction(txCtx, () -> {
            final SystemUser newUser = registerNewUser(username, password, firstName, lastName, email, roles, createdOn, userRepository);

            createPilotCollaboratorUser(newUser, company, roles, securityData, skillData, phoneNumber, certifications,
                    pilotRepository, aircraftModelRepository);

            return AddPilotResult.success();
        });
    }


    private AddPilotResult executeInTransaction(TransactionalContext txCtx, Supplier<AddPilotResult> action) {
        if (txCtx != null) txCtx.beginTransaction();

        try {
            AddPilotResult result = action.get();
            if (txCtx != null) {
                txCtx.commit();
                txCtx.close();
            }
            return result;
        } catch (Throwable t) {
            if (txCtx != null) {
                try { txCtx.rollback(); txCtx.close(); } catch (Exception ignored) {}
            }

            return mapExceptionToResult(t);
        }
    }


    private AddPilotResult mapExceptionToResult(Throwable t) {
        Throwable cause = t;
        while (cause != null) {
            String msg = cause.getMessage();


            if (cause instanceof IntegrityViolationException ||
                    cause instanceof jakarta.persistence.RollbackException ||
                    (msg != null && msg.contains("23505"))) {
                return AddPilotResult.failure(AddPilotResult.Outcome.DUPLICATE_USERNAME);
            }

            cause = cause.getCause();
        }

        if (t instanceof InvalidPhoneFormat) {
            return AddPilotResult.failure(AddPilotResult.Outcome.INVALID_PHONE_FORMAT);
        }

        if (t instanceof InvalidSkillsAssessmentDate) {
            return AddPilotResult.failure(AddPilotResult.Outcome.INVALID_SKILLS_DATE);
        }

        if (t instanceof InvalidSecurityClearanceDate) {
            return AddPilotResult.failure(AddPilotResult.Outcome.INVALID_SECURITY_DATE);
        }

        if (t instanceof IllegalArgumentException) {
            String msg = t.getMessage();
            return (msg != null && msg.contains("role"))
                    ? AddPilotResult.failureWithMsg(AddPilotResult.Outcome.INVALID_ROLE, msg)
                    : AddPilotResult.failureWithMsg(AddPilotResult.Outcome.INVALID_INPUT, msg);
        }

        if (t instanceof EntityNotFoundException) return AddPilotResult.failure(AddPilotResult.Outcome.AIRCRAFT_MODEL_NOT_FOUND);
        if (t instanceof IllegalStateException) return AddPilotResult.failure(AddPilotResult.Outcome.NO_CERTIFICATIONS);
        if (t instanceof DateTimeParseException) return AddPilotResult.failure(AddPilotResult.Outcome.INVALID_DATE_FORMAT);

        return AddPilotResult.failureWithMsg(AddPilotResult.Outcome.ERROR, "Unexpected error: " + t.getMessage());
    }



    public String[] getAircraftModelIds(final AircraftModelRepository aircraftModelRepository) {
        return StreamSupport.stream(aircraftModelRepository.findAll().spliterator(), false)
                .map(aircraftModel -> aircraftModel.identity().toString())
                .toArray(String[]::new);
    }

    private void createPilotCollaboratorUser(final SystemUser newUser, final AirTransportCompany company,
                                              final Set<Role> roles, final String securityData,final String skillData,
                                              final String phoneNumber, final List<PilotCertificationSpec> certifications,
                                               final PilotUserRepository pilotRepository,
                                               final AircraftModelRepository aircraftModelRepository) {

        if (roles.size() != 1) {
            throw new IllegalArgumentException("Pilot can only have one role!");
        }

        final Role role = roles.iterator().next();
        if (!role.equals(CompanyCollaboratorRoles.PILOT)) {
            throw new IllegalArgumentException("Invalid role for pilot collaborator: " + role);
        }


        final PilotUserBuilder builder = new PilotUserBuilder()
                .withAirTransportCompany(company)
                .withSystemUser(newUser)
                .withPhoneNumber(phoneNumber)
                .withSecurityData(securityData)
                .withSkillAssessment(skillData);

        for (final PilotCertificationSpec spec : certifications) {
            final AircraftModelId modelId = AircraftModelId.valueOf(spec.getAircraftModelId());
            final AircraftModel aircraftModel = aircraftModelRepository
                    .ofIdentity(modelId)
                    .orElseThrow(() -> new EntityNotFoundException("Aircraft model not found: " + modelId));
            final DueDate dueDate = DueDate.valueOf(LocalDate.parse(spec.getStartDate()), LocalDate.parse(spec.getEndDate()));
            builder.withCertification(new PilotCertification(aircraftModel, dueDate));
        }

        pilotRepository.save(builder.build());
    }


    private SystemUser registerNewUser(final String username, final String rawPassword, final String firstName,
                                       final String lastName, final String email, final Set<Role> roles,
                                       final Calendar createdOn, final UserRepository userRepository) {
        final var userBuilder = UserBuilderHelper.builder();
        userBuilder.with(username, rawPassword, firstName, lastName, email).createdOn(createdOn).withRoles(roles);
        return userRepository.save(userBuilder.build());
    }
}
