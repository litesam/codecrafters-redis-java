package com.redis;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class RespParser {
    private final InputStream inputStream;

    public RespParser(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    private String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        int b;
        while ((b = inputStream.read()) != -1) {
            sb.append((char) b);
            if (sb.length() >= 2 && sb.substring(sb.length() - 2).equals(RespConstants.CR_LF)) {
                return sb.substring(0, sb.length() - 2);
            }
        }
        throw new IOException("End of stream, without CR_LF.");
    }

    private String readBytes(int length) throws IOException {
        byte[] buffer = new byte[length];
        int bytesRead = inputStream.readNBytes(buffer, 0, length);
        if (bytesRead != length) {
            throw new IOException("Failed to read all bytes. Expected " + length + ", got " + bytesRead);
        }
        return new String(buffer);
    }

    public List<String> parseCommand() throws IOException {
        int firstByte = inputStream.read();
        if (firstByte == -1) return null;

        String arrayLengthStr = readLine();
        int arrayLength = Integer.parseInt(arrayLengthStr);
        List<String> commandParts = new ArrayList<>(arrayLength);
        for (int i = 0; i < arrayLength; i++) {
            int bulkStringPrefix = inputStream.read();
            String bulkStringLengthStr = readLine();
            int bulkStringLength = Integer.parseInt(bulkStringLengthStr);

            String bulkString = readBytes(bulkStringLength);
            commandParts.add(bulkString);

            String crLf = readLine();
        }

        return commandParts;
    }
}
