package eapli.aisafe.rcomp.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** [version:byte][opcode:byte][length:int][payload:utf8] */
public record ProtocolFrame(byte version, byte opcode, String payload) {

    public static ProtocolFrame read(final DataInputStream in) throws IOException {
        final byte version = in.readByte();
        final byte opcode = in.readByte();
        final int length = in.readInt();
        if (length < 0) {
            throw new IOException("Invalid frame length: " + length);
        }
        final byte[] bytes = new byte[length];
        if (length > 0) {
            in.readFully(bytes);
        }
        return new ProtocolFrame(version, opcode, new String(bytes, StandardCharsets.UTF_8));
    }

    public static ProtocolFrame readOrNullOnEof(final DataInputStream in) throws IOException {
        try {
            return read(in);
        } catch (final EOFException e) {
            return null;
        }
    }

    public void write(final DataOutputStream out) throws IOException {
        final byte[] bytes = payload == null ? new byte[0] : payload.getBytes(StandardCharsets.UTF_8);
        out.writeByte(version);
        out.writeByte(opcode);
        out.writeInt(bytes.length);
        if (bytes.length > 0) {
            out.write(bytes);
        }
        out.flush();
    }

    public static void writeResponse(final DataOutputStream out, final byte opcode, final String message)
            throws IOException {
        new ProtocolFrame(ProtocolConstants.VERSION, opcode, message == null ? "" : message).write(out);
    }
}
