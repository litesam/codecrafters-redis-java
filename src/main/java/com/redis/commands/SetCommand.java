package com.redis.commands;

import com.redis.DataStore;
import com.redis.RespConstants;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class SetCommand implements Command {
    private static final String NAME = "SET";
    private final DataStore dataStore;

    public SetCommand(DataStore dataStore) {
        this.dataStore = dataStore;
    }


    @Override
    public void execute(List<String> args, OutputStream outputStream) throws IOException {
        var key = args.get(0);
        var value = args.get(1);
        Long expiryMillis = null;
        for (int i = 2; i < args.size(); i++) {
            String option = args.get(i).toUpperCase();
            switch (option) {
                case "PX":
                    if (i + 1 < args.size()) {
                        expiryMillis = Long.valueOf(args.get(i+1));
                        i++;
                    }
                    break;
                default:
                    break; // Default case when there is no expiry
            }
        }

        dataStore.set(key, value, expiryMillis);

        outputStream.write(("+OK"+ RespConstants.CR_LF).getBytes());
    }

    @Override
    public String getName() {
        return NAME;
    }
}
