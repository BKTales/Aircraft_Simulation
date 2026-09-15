package eapli.aisafe.rcomp.protocol;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProtocolFrameTest {

    @Test
    void roundTripPreservesPayload() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DataOutputStream out = new DataOutputStream(baos);
        new ProtocolFrame(ProtocolConstants.VERSION, AtccOpcodes.LIST_FLEET, "UNFILTERED").write(out);

        final DataInputStream in = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
        final ProtocolFrame frame = ProtocolFrame.read(in);

        assertEquals(ProtocolConstants.VERSION, frame.version());
        assertEquals(AtccOpcodes.LIST_FLEET, frame.opcode());
        assertEquals("UNFILTERED", frame.payload());
    }
}
