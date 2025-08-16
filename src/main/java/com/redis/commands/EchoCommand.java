package com.redis.commands;

import com.redis.RespConstants;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class EchoCommand implements Command {
    private static final String NAME = "ECHO";

    @Override
    public void execute(List<String> args, OutputStream outputStream) throws IOException {
        if (args.isEmpty()) {
            outputStream.write(("-ERR wrong number of arguments for 'echo' command" + RespConstants.CR_LF).getBytes());
            return;
        }
        String message = args.get(0);
        String response = RespConstants.BULK_STRING_PREFIX + String.valueOf(message.length()) +
                RespConstants.CR_LF + message + RespConstants.CR_LF;
        outputStream.write(response.getBytes());
    }

    @Override
    public String getName() {
        return NAME;
    }
}
