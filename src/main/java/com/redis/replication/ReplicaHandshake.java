package com.redis.replication;

import com.redis.RespConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public final class ReplicaHandshake {

    private ReplicaHandshake() {}

    public static void start(String masterHost, int masterPort, int listeningPort) {
        Thread.ofVirtual()
                .start(() -> {
                    Socket socket = null;
                    try {
                        socket = new Socket(masterHost, masterPort);
                        socket.setTcpNoDelay(true);

                        InputStream in = socket.getInputStream();
                        OutputStream out = socket.getOutputStream();

                        writeArray(out, "PING");
                        readOneLine(in);

                        writeArray(out, "REPLCONF", "listening-port", String.valueOf(listeningPort));
                        readOneLine(in);

					writeArray(out, "REPLCONF", "capa", "psync2");
					readOneLine(in);

                        writeArray(out, "PSYNC", "?", "-1");

                        readOneLine(in);

                        byte[] sink = new byte[8192];
                        while (true) {
                            int n = in.read(sink);
                            if (n == -1) break;
                        }

                    } catch (Exception e) {
                        System.err.println("Replica handshake failed: " + e.getMessage());
                    }
                });
    }

    private static void writeArray(OutputStream out, String... params) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(RespConstants.ARRAY_PREFIX).append(params.length).append(RespConstants.CR_LF);

        for (String p : params) {
            sb.append(RespConstants.BULK_STRING_PREFIX)
            .append(p.length())
            .append(RespConstants.CR_LF)
            .append(p)
            .append(RespConstants.CR_LF);
        }
        out.write(sb.toString().getBytes());
        out.flush();
    }

    private static String readOneLine(InputStream in) throws IOException {
        ByteArrayOutputStream line = new ByteArrayOutputStream();
        while (true) {
            int b = in.read();
            if (b == -1) break;
            if (b == '\r') {
                int next = in.read();
                if (next == '\n') break;
                line.write(b);
                line.write(next);
            } else {
                line.write(b);
            }
        }
        return line.toString();
    }
}
