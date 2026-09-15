package eapli.aisafe.enginemodelmanagement.domain;

public enum FuelType {
    JET_A1("Jet A-1"),
    JET_A("Jet A"),
    JET_B("Jet B"),
    AVGAS_100LL("Avgas 100LL"),
    ELECTRICITY("Electricity"),
    HYDROGEN("Liquid Hydrogen");

    private final String label;

    FuelType(String label) {
        this.label = label;
    }

}