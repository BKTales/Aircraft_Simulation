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
import eapli.aisafe.flightcontroloperatormanagement.repositories.FlightControlOperatorUserRepository;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

/**
 *
 * @author losa
 */
public class ListFlightControlOperatorsUsersController {
    private final AuthorizationService authz;
    private final FlightControlOperatorUserService flightControlOperatorUserService;
    private final FlightControlOperatorUserRepository flightControlOperatorUserRepository;
    private final AirControlAreaRepository airControlAreaRepository;

    public ListFlightControlOperatorsUsersController(){
        this(AuthzRegistry.authorizationService(),
             new FlightControlOperatorUserService(),
             PersistenceContext.repositories().flightOperators(),
             PersistenceContext.repositories().airControlArea());
    }


    public ListFlightControlOperatorsUsersController(
            AuthorizationService authz,
            FlightControlOperatorUserService service,
            FlightControlOperatorUserRepository repository,
            AirControlAreaRepository areaRepo) {

        if(authz == null || service == null) {
            throw new IllegalArgumentException("Authorization service and user management service are required.");
        }
        this.authz = authz;
        this.flightControlOperatorUserService = service;
        this.flightControlOperatorUserRepository = repository;
        this.airControlAreaRepository = areaRepo;
    }

    public Iterable<AirControlArea> allAirControlAreas() {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.BACKOFFICE_OPERATOR, AISafeRoles.ADMIN);
        return airControlAreaRepository.findAll();
    }

    public Iterable<FlightControlOperatorUser> activeFlightControlOperatorsUsersForCompany(final String areaCode) {
        authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.BACKOFFICE_OPERATOR, AISafeRoles.ADMIN);
        final AirControlArea area = airControlAreaRepository
                .ofIdentity(AreaCode.valueOf(areaCode))
                .orElseThrow(() -> new IllegalArgumentException("Air control area not found: " + areaCode));

        return flightControlOperatorUserService.findActiveFlightControlOperatorsByArea(
                flightControlOperatorUserRepository, area);
    }
}
