package com.redis;

import com.redis.commands.*;
import com.redis.config.ConfigStore;
import com.redis.config.InfoStore;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandHandler {
    private final Map<String, Command> commands;

    public CommandHandler(DataStore dataStore, ConfigStore configStore, InfoStore infoStore) {
        commands = new HashMap<>();
        registerCommand(new PingCommand());
        registerCommand(new EchoCommand());
        registerCommand(new SetCommand(dataStore));
        registerCommand(new GetCommand(dataStore));
        registerCommand(new ConfigCommand(configStore));
        registerCommand(new KeyCommand(dataStore));
        registerCommand(new InfoCommand(infoStore));
        registerCommand(new ReplConfCommand());
        registerCommand(new PSyncCommand());
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
