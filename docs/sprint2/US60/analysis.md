## ANALYSIS

**Client Clarification / Product Owner Session**

The `AirTransportCompany` aggregate in this sprint scope holds company identity (name, IATA, ICAO) only. Collaborator users are a separate bounded context (`CompanyCollaboratorUser`) and are linked later using the company IATA; at company-registration time there are simply no collaborator rows for that airline yet.

Authorised roles for registration: **Admin** or **Backoffice Operator**.

---

## BUSINESS RULES

* The IATA code must be exactly 2 alphabetic characters
* The ICAO code must be 2 or 3 alphabetic characters
* The IATA code must be unique across all registered companies
* The ICAO code must be unique across all registered companies
* The company name must not be empty (validated by `CompanyName`)
* IATA and ICAO codes are stored in uppercase regardless of input
* No collaborator users are created as part of this use case; collaborator management is handled elsewhere (e.g. US061+)
* This registration must also be achievable through a bootstrap process

---

## TEST COVERAGE

Automated tests under `aisafe.core/src/test/java/eapli/aisafe/airtransportcompanymanagement/`:

* Domain: `CompanyNameTest`, `IATACodeTest`, `ICAOCodeTest`, `AirTransportCompanyTest`
* Application: `AirTransportCompanyServiceTest`, `RegisterAirTransportCompanyControllerTest`, `ListAirTransportCompaniesControllerTest`, `AirTransportCompanyApplicationExceptionsTest`, `AirTransportCompanyControllersDefaultConstructorCoverageTest`
