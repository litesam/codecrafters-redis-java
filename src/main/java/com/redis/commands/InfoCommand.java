package com.redis.commands;

import com.redis.RespConstants;
import com.redis.config.InfoStore;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class InfoCommand implements Command {
    private static final String NAME = "INFO";
    private static final String SUBCOMMAND_REPLICATION = "REPLICATION";
    private static final String DEFAULT_MASTER_REPL_ID = "8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb";
    private static final String DEFAULT_MASTER_REPL_OFFSET = "0";

    private final InfoStore infoStore;

    public InfoCommand(InfoStore infoStore) {
        this.infoStore = infoStore;
    }

    @Override
    public void execute(List<String> args, OutputStream outputStream) throws IOException {
        String subCommand = args.getFirst().toUpperCase();
        Map<String, Map<String, String>> infos = fetchInfos(subCommand);
        byte[] bodyBytes = buildBodyBytes(subCommand, infos);
        byte[] prefixBytes = buildRespBulkPrefix(bodyBytes.length);
        outputStream.write(prefixBytes);
        outputStream.write(bodyBytes);
        outputStream.write(RespConstants.CR_LF.getBytes());
    }

    private Map<String, Map<String, String>> fetchInfos(String subCommand) {
        return switch (subCommand) {
            case SUBCOMMAND_REPLICATION -> infoStore.getMatchingInfos("replication", "*");
            default -> infoStore.getMatchingInfos("*", "*");
        };
    }

    private byte[] buildBodyBytes(String subCommand, Map<String, Map<String, String>> infos) {

        StringBuilder sb = new StringBuilder();
        sb.append(makeHeader(subCommand));

        if (infos != null) {
            for (Map<String, String> section : infos.values()) {
                for (Map.Entry<String, String> e : section.entrySet()) {
                    sb.append(e.getKey())
                            .append(':')
                            .append(e.getValue())
                            .append(RespConstants.CR_LF);
                }
            }
        }

        sb.append("master_replid:")
                .append(DEFAULT_MASTER_REPL_ID)
                .append(RespConstants.CR_LF)
                .append("master_repl_offset:")
                .append(DEFAULT_MASTER_REPL_OFFSET)
                .append(RespConstants.CR_LF);

        return sb.toString().getBytes();
    }

    private byte[] buildRespBulkPrefix(int bodyByteLength) {
        String prefix = "%c%d%s".formatted(
                RespConstants.BULK_STRING_PREFIX,
                bodyByteLength,
                RespConstants.CR_LF);
        return prefix.getBytes();
    }

    private String makeHeader(String subCommand) {
        return switch (subCommand) {
            case SUBCOMMAND_REPLICATION -> "# Replication" + RespConstants.CR_LF;
            default -> "# Replication" + RespConstants.CR_LF;
        };
    }

    @Override
    public String getName() {
        return NAME;
    }
}
