package eapli.aisafe.rcomp.tcpclient;

import eapli.aisafe.rcomp.protocol.CommonOpcodes;
import eapli.aisafe.rcomp.protocol.ProtocolConstants;
import eapli.aisafe.rcomp.protocol.ProtocolFrame;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public final class TcpSession implements AutoCloseable {

    private final Socket socket;
    private final DataOutputStream out;
    private final DataInputStream in;

    public TcpSession(final String host, final int port) throws IOException {
        this.socket = new Socket(host, port);
        this.out = new DataOutputStream(socket.getOutputStream());
        this.in = new DataInputStream(socket.getInputStream());
    }

    public ProtocolFrame request(final byte opcode, final String payload) throws IOException {
        new ProtocolFrame(ProtocolConstants.VERSION, opcode, payload == null ? "" : payload).write(out);
        if (opcode == CommonOpcodes.LOGOUT) {
            return ProtocolFrame.readOrNullOnEof(in);
        }
        return ProtocolFrame.read(in);
    }

    public boolean isSuccess(final ProtocolFrame response) {
        return response != null && response.opcode() >= 0;
    }

    public boolean isOk(final ProtocolFrame response) {
        return response != null && response.opcode() == 0;
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
