package com.redis.commands;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface Command {
    void execute(List<String> args, OutputStream outputStream) throws IOException;
    String getName();
}
