package com.redis;

import com.redis.commands.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandHandler {
    private final Map<String, Command> commands;
    private final DataStore dataStore;

    public CommandHandler(DataStore dataStore) {
        commands = new HashMap<>();
        this.dataStore = dataStore;
        registerCommand(new PingCommand());
        registerCommand(new EchoCommand());
        registerCommand(new SetCommand(dataStore));
        registerCommand(new GetCommand(dataStore));
    }

    private void registerCommand(Command command) {
        commands.put(command.getName().toUpperCase(), command);
    }

    public void handleCommand(List<String> commandParts, OutputStream outputStream) throws IOException {
        if (commandParts == null || commandParts.isEmpty()) {
            outputStream.write(("-ERR empty command" + RespConstants.CR_LF).getBytes());
            return;
        }

        String commandName = commandParts.getFirst().toUpperCase();
        List<String> args = commandParts.subList(1, commandParts.size());

        Command command = commands.get(commandName);
        command.execute(args, outputStream);
    }
}
