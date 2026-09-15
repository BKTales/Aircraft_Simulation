package eapli.aisafe.dsl.model;

/**
 * Descriptor completo de uma perna (Leg) do voo.
 * Inclui os dados de fuel e rota conforme a gramática FlightPlan.g4.
 */
public class LegDescriptor {

    private final String departureAirport;
    private final String arrivalAirport;
    private final String departureTime;
    private final String arrivalTime;
    private final RouteDescriptor route;
    private final double fuelValue;
    private final String fuelUnit;

    public LegDescriptor(String departureAirport, String departureTime,
                         String arrivalAirport, String arrivalTime,
                         RouteDescriptor route,
                         double fuelValue, String fuelUnit) {
        this.departureAirport = departureAirport;
        this.departureTime = departureTime;
        this.arrivalAirport = arrivalAirport;
        this.arrivalTime = arrivalTime;
        this.route = route;
        this.fuelValue = fuelValue;
        this.fuelUnit = fuelUnit;
    }

    public String getDepartureAirport() { return departureAirport; }
    public String getArrivalAirport()   { return arrivalAirport; }
    public String getDepartureTime()    { return departureTime; }
    public String getArrivalTime()      { return arrivalTime; }
    public RouteDescriptor getRoute()   { return route; }
    public double getFuelValue()        { return fuelValue; }
    public String getFuelUnit()         { return fuelUnit; }

    @Override
    public String toString() {
        return String.format("%s@%s -> %s@%s [Fuel: %.2f%s]",
                departureAirport, departureTime, arrivalAirport, arrivalTime,
                fuelValue, fuelUnit);
    }
}