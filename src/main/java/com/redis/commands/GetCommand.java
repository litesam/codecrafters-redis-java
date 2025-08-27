package com.redis.commands;

import com.redis.DataStore;
import com.redis.RespConstants;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class GetCommand implements Command {
    private static final String NAME = "GET";
    private final DataStore dataStore;

    public GetCommand(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public void execute(List<String> args, OutputStream outputStream) throws IOException {
        var key = args.getFirst();
        var value = dataStore.get(key);
        if (value == null) {
            String response = String.valueOf(RespConstants.BULK_STRING_PREFIX) + "-1" + RespConstants.CR_LF;
            outputStream.write(response.getBytes());
        } else {
            String response = String.valueOf(RespConstants.BULK_STRING_PREFIX) + value.length()
                    + RespConstants.CR_LF + value + RespConstants.CR_LF;
            outputStream.write(response.getBytes());
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
