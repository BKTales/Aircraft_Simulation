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


import eapli.aisafe.companycollaboratormanagment.domain.CompanyCollaboratorUser;
import eapli.aisafe.companycollaboratormanagment.domain.events.NewUserRegisteredEvent;
import eapli.aisafe.companycollaboratormanagment.repositories.CompanyCollaboratorUserRepository;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;

import eapli.framework.functional.Functions;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;

import java.util.Optional;

/**
 * @author Paulo Gandra de Sousa
 */
/* package */ class AddCafeteriaUserOnSystemUserCreatedController {

	private final UserRepository repo = PersistenceContext.repositories().users();
	private final CompanyCollaboratorUserRepository cafeteriaUserRepository = PersistenceContext.repositories().collaboratorUsers();

	public CompanyCollaboratorUser addCompanyCollaboratorUser(final NewUserRegisteredEvent event) {
		final Optional<SystemUser> newUser = findUser(event);
		return newUser.map(u -> createCompanyCollaboratorUser(event, u)).orElseThrow(IllegalStateException::new);
	}

	private CompanyCollaboratorUser createCompanyCollaboratorUser(final NewUserRegisteredEvent event, SystemUser u) {
		final var cafeteriaUser = new CompanyCollaboratorUser();
		return cafeteriaUserRepository.save(cafeteriaUser);
	}

	@SuppressWarnings("squid:S1488")
	private Optional<SystemUser> findUser(final NewUserRegisteredEvent event) {
		// since we are using events, the actual user may not yet be
		// created, so lets give it a time and wait
		final Optional<SystemUser> newUser = Functions.retry(() -> repo.ofIdentity(event.username()), 500, 30);
		return newUser;
	}
}
