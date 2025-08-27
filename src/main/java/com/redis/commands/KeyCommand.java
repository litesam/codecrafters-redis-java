package com.redis.commands;

import com.redis.DataStore;
import com.redis.RespConstants;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

public class KeyCommand implements Command {
    private static final String NAME = "KEYS";

    final DataStore dataStore;

    public KeyCommand(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public void execute(List<String> args, OutputStream outputStream) throws IOException {
        String pattern = args.getFirst();
        Set<String> matchingKeys = dataStore.getMatchingKeys(pattern);

        StringBuilder response = new StringBuilder();

        response.append(RespConstants.ARRAY_PREFIX)
                .append(matchingKeys.isEmpty() ? "-1" : matchingKeys.size())
                .append(RespConstants.CR_LF);

        for (String key : matchingKeys) {
            response.append(RespConstants.BULK_STRING_PREFIX)
                    .append(key.length())
                    .append(RespConstants.CR_LF)
                    .append(key)
                    .append(RespConstants.CR_LF);
        }

        outputStream.write(response.toString().getBytes());

    }

    @Override
    public String getName() {
        return NAME;
    }
}
