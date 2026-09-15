## ANALYSIS

**Client Clarification / Product Owner Session**

The airport’s air control area is not chosen manually: the system resolves it from geographic coordinates by finding the registered air control area whose boundary contains the point. Registration is available from the backoffice console and from bootstrap.

---

## BUSINESS RULES

* At least one air control area must exist whose geographic boundary contains the submitted latitude and longitude; otherwise registration fails
* The IATA code must be exactly 3 alphabetic characters
* The ICAO code must be exactly 4 alphanumeric characters
* The IATA code must be unique across all registered airports
* The ICAO code must be unique across all registered airports
* IATA and ICAO codes are stored in uppercase regardless of input
* Latitude must be between -90 and 90; longitude between -180 and 180
* Elevation is expressed in metres above sea level and must be greater than or equal to zero
* Each airport is associated with exactly one air control area (the one that contains its coordinates), referenced by area code only (cross-aggregate reference)
* Only users with the **Backoffice Operator** role may register airports through the application controller
* This registration must also be achievable through a bootstrap process

---

## TEST COVERAGE

Automated tests live under `aisafe.core/src/test/java/eapli/aisafe/airportmanagement/`:

* Domain: `AirportIATACodeTest`, `AirportICAOCodeTest`, `CoordinatesTest`, `AirportTest`
* Application: `AirportServiceTest`, `CreateAirportControllerTest`, `ListAirportsControllerTest`, `AirportManagementApplicationExceptionsTest`, `AirportControllersDefaultConstructorCoverageTest`
