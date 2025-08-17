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

        dataStore.set(key, value);
        outputStream.write(("+OK"+ RespConstants.CR_LF).getBytes());
    }

    @Override
    public String getName() {
        return NAME;
    }
}
