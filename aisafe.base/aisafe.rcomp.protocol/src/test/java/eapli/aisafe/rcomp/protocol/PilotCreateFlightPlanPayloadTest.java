package eapli.aisafe.rcomp.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PilotCreateFlightPlanPayloadTest {

    @Test
    void encodeDecodeRoundTrip() {
        final PilotCreateFlightPlanPayload.Fields original = new PilotCreateFlightPlanPayload.Fields(
                "TP123",
                "CS-TP01",
                "pilot1",
                "2026-06-01 10:00",
                "2026-06-01 12:00",
                5000.0,
                "kg",
                120,
                10000.0,
                500.0,
                "",
                false);

        final PilotCreateFlightPlanPayload.Fields decoded =
                PilotCreateFlightPlanPayload.decode(PilotCreateFlightPlanPayload.encode(original));

        assertEquals(original, decoded);
    }

    @Test
    void rejectsInvalidFieldCount() {
        assertThrows(IllegalArgumentException.class,
                () -> PilotCreateFlightPlanPayload.decode("TP123;CS-TP01;pilot1"));
    }

    @Test
    void rejectsUnpaddedDepartureDatetime() {
        assertThrows(IllegalArgumentException.class, () -> PilotCreateFlightPlanPayload.decode(
                "TP123;CS-TP01;pilot1;2026-6-8 10:00;2026-06-08 22:00;400;kg;0;0;0;a;false"));
    }
}
