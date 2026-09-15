package eapli.aisafe.flightmanagement.infrastructure.simulator;

public final class SimulatorFlightId {

    private SimulatorFlightId() {}

    public static int fromDesignator(final String flightDesignator) {
        if (flightDesignator == null) {
            return 0;
        }
        long h = 1125899906842597L;
        for (int i = 0; i < flightDesignator.length(); i++) {
            h = 31 * h + flightDesignator.charAt(i);
        }
        return (int) (Math.abs(h) % Integer.MAX_VALUE);
    }
}
