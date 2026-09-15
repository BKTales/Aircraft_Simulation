package eapli.aisafe.dsl.model;

public final class AltitudeSlotDescriptor {

    private final int altitudeMetres;
    private final int widthMetres;

    public AltitudeSlotDescriptor(final int altitudeMetres, final int widthMetres) {
        this.altitudeMetres = altitudeMetres;
        this.widthMetres = widthMetres;
    }

    public int altitudeMetres() {
        return altitudeMetres;
    }

    public int widthMetres() {
        return widthMetres;
    }
}

