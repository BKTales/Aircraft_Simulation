package eapli.aisafe.airtransportcompanymanagement.application;

import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.CompanyName;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.airtransportcompanymanagement.domain.ICAOCode;
import eapli.aisafe.airtransportcompanymanagement.repositories.AirTransportCompanyRepository;
import eapli.aisafe.usermanagement.domain.AISafeRoles;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListAirTransportCompaniesControllerTest {

    @Mock
    private AuthorizationService authz;

    @Mock
    private AirTransportCompanyRepository repository;

    @Test
    void constructorRejectsNullAuthorizationService() {
        assertThrows(IllegalArgumentException.class,
                () -> new ListAirTransportCompaniesController(null, repository));
    }

    @Test
    void constructorRejectsNullRepository() {
        assertThrows(IllegalArgumentException.class,
                () -> new ListAirTransportCompaniesController(authz, null));
    }

    @Test
    void allCompaniesEnsuresAuthorizationAndReturnsRepositoryData() {
        final AirTransportCompany company = new AirTransportCompany(
                CompanyName.valueOf("TAP"), IATACode.valueOf("TP"), ICAOCode.valueOf("TAP"));
        when(repository.findAll()).thenReturn(List.of(company));

        final ListAirTransportCompaniesController controller = new ListAirTransportCompaniesController(authz, repository);

        final Iterable<AirTransportCompany> result = controller.allCompanies();

        verify(authz).ensureAuthenticatedUserHasAnyOf(AISafeRoles.ADMIN, AISafeRoles.BACKOFFICE_OPERATOR);
        assertEquals(company, result.iterator().next());
    }
}
