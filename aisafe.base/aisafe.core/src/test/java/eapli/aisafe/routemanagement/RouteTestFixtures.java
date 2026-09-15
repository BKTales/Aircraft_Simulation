package eapli.aisafe.routemanagement;

import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.airportmanagement.domain.Airport;
import eapli.aisafe.airportmanagement.domain.AirportIATACode;
import eapli.aisafe.airportmanagement.domain.AirportICAOCode;
import eapli.aisafe.airportmanagement.domain.Coordinates;
import eapli.aisafe.airtransportcompanymanagement.domain.AirTransportCompany;
import eapli.aisafe.airtransportcompanymanagement.domain.CompanyName;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.aisafe.airtransportcompanymanagement.domain.ICAOCode;
import eapli.aisafe.routemanagement.domain.RouteName;
import eapli.aisafe.routemanagement.domain.RouteRecurringSchedule;
import eapli.aisafe.routemanagement.domain.RouteSchedule;
import eapli.aisafe.routemanagement.domain.RecurringScheduleEntry;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public final class RouteTestFixtures {

    public static final Coordinates COORDS_OPO = Coordinates.valueOf(41.24, -8.68, 69.0);
    public static final Coordinates COORDS_LIS = Coordinates.valueOf(38.77, -9.13, 113.0);
    public static final AirTransportCompany COMPANY_TP = company("TAP", "TP", "TAP");
    public static final Airport AIRPORT_OPO = airport("OPO", "LPPR", COORDS_OPO);
    public static final Airport AIRPORT_LIS = airport("LIS", "LPPT", COORDS_LIS);
    public static final RouteName ROUTE_NAME_TP123 = RouteName.valueOf("TP123");

    private RouteTestFixtures() {
    }

    public static AirTransportCompany company(final String name, final String iata, final String icao) {
        return new AirTransportCompany(
                CompanyName.valueOf(name), IATACode.valueOf(iata), ICAOCode.valueOf(icao));
    }

    public static Airport airport(final String iata, final String icao) {
        return airport(iata, icao, COORDS_LIS);
    }

    public static Airport airport(final String iata, final String icao, final Coordinates coordinates) {
        return new Airport(
                AirportIATACode.valueOf(iata),
                AirportICAOCode.valueOf(icao),
                coordinates,
                new AreaCode());
    }

    public static RouteSchedule charterSchedule() {
        return new RouteSchedule(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2));
    }

    public static RouteRecurringSchedule mondaySchedule() {
        return new RouteRecurringSchedule(List.of(RecurringScheduleEntry.of(DayOfWeek.MONDAY)));
    }
}
