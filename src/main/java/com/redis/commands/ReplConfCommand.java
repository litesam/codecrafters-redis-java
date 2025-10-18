package com.redis.commands;

import com.redis.RespConstants;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ReplConfCommand implements Command {
    private static final String NAME = "REPLCONF";

    @Override
    public void execute(List<String> args, OutputStream outputStream) throws IOException {
        outputStream.write(("+OK" + RespConstants.CR_LF).getBytes());
    }

    @Override
    public String getName() {
        return NAME;
    }
}

