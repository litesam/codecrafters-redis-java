package com.redis.commands;

import com.redis.RespConstants;
import com.redis.config.ConfigStore;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigCommand implements Command {
    private static final String NAME = "CONFIG";

    private final ConfigStore configStore;

    public ConfigCommand(ConfigStore configStore) {
        this.configStore = configStore;
    }

    @Override
    public void execute(List<String> args, OutputStream outputStream) throws IOException {
        if (args.isEmpty()) {
            return;
        }
        String subCommand = args.getFirst().toUpperCase();
        switch (subCommand) {
            case "GET":
                handleConfigGet(args.subList(1, args.size()), outputStream);
                break;
            default:
                break;
        }
    }

    private void handleConfigGet(List<String> getArgs, OutputStream outputStream) throws IOException {
        List<String> responseEls = new ArrayList<>();
        for (String argPattern : getArgs) {
            Map<String, String> matchingConfigs = configStore.getMatchingConfigs(argPattern);
            for (Map.Entry<String, String> entry : matchingConfigs.entrySet()) {
                responseEls.add(entry.getKey());
                responseEls.add(entry.getValue());
            }
        }

        StringBuilder response = new StringBuilder();
        response.append(RespConstants.ARRAY_PREFIX)
                .append(responseEls.size())
                .append(RespConstants.CR_LF);
        for (String el : responseEls) {
            response.append(RespConstants.BULK_STRING_PREFIX)
                    .append(el.length())
                    .append(RespConstants.CR_LF)
                    .append(el)
                    .append(RespConstants.CR_LF);
        }
        outputStream.write(response.toString().getBytes());
    }

    @Override
    public String getName() {
        return NAME;
    }
}
