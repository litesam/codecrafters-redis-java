package com.redis.commands;

import com.redis.RespConstants;
import com.redis.config.InfoStore;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class InfoCommand implements Command {
    private static final String NAME = "INFO";

    private final InfoStore infoStore;

    public InfoCommand(InfoStore infoStore) {
        this.infoStore = infoStore;
    }

    @Override
    public void execute(List<String> args, OutputStream outputStream) throws IOException {
        String subCommand = args.getFirst().toUpperCase();
        Map<String, Map<String, String>> resultMap = switch (subCommand) {
            case "REPLICATION" -> infoStore.getMatchingInfos("replication", "*");
            default -> infoStore.getMatchingInfos("*", "*");
        };
        String header = makeHeader();
        StringBuilder sb = new StringBuilder();
        sb.append(header);
        for (Map.Entry<String, Map<String, String>> entry : resultMap.entrySet()) {
            for (Map.Entry<String, String> valuesEntry : entry.getValue().entrySet()) {
                sb.append(valuesEntry.getKey()).append(":").append(valuesEntry.getValue());
                sb.append(RespConstants.CR_LF);
            }
        }
        sb.append("master_replid:")
                .append("8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb")
                .append(RespConstants.CR_LF)
                .append("master_repl_offset:0")
                .append(RespConstants.CR_LF);
        String result = RespConstants.BULK_STRING_PREFIX + String.valueOf(sb.length()) +
                RespConstants.CR_LF +
                sb + RespConstants.CR_LF;
        outputStream.write(result.getBytes());
    }

    private String makeHeader() {
        return "# Replication" + RespConstants.CR_LF;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
