package com.redis.commands;

import com.redis.RespConstants;
import com.redis.replication.ReplicaManager;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class PSyncCommand implements Command {
    private static final String NAME = "PSYNC";
    private static final String DEFAULT_MASTER_REPL_ID = "8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb";
    private static final String DEFAULT_MASTER_REPL_OFFSET = "0";
    private static final byte[] EMPTY_RDB = {
        0x52, 0x45, 0x44, 0x49, 0x53, 0x30, 0x30, 0x31, 0x31, (byte) 0xfa,
        0x09, 0x72, 0x65, 0x64, 0x69, 0x73, 0x2d, 0x76, 0x65, 0x72,
        0x05, 0x37, 0x2e, 0x32, 0x2e, 0x30, (byte) 0xfa, 0x0a, 0x72, 0x65,
        0x64, 0x69, 0x73, 0x2d, 0x62, 0x69, 0x74, 0x73, (byte) 0xc0, 0x40,
        (byte) 0xfa, 0x05, 0x63, 0x74, 0x69, 0x6d, 0x65, (byte) 0xc2, 0x6d, 0x08,
        (byte) 0xbc, 0x65, (byte) 0xfa, 0x08, 0x75, 0x73, 0x65, 0x64, 0x2d, 0x6d,
        0x65, 0x6d, (byte) 0xc2, (byte) 0xb0, (byte) 0xc4, 0x10, 0x00, (byte) 0xfa, 0x08,
        0x61, 0x6f, 0x66, 0x2d, 0x62, 0x61, 0x73, 0x65, (byte) 0xc0, 0x00,
        (byte) 0xff, (byte) 0xf0, 0x6e, 0x3b, (byte) 0xfe, (byte) 0xc0, (byte) 0xff, 0x5a, (byte) 0xa2
    };

    @Override
    public void execute(List<String> args, OutputStream outputStream) throws IOException {
        String fullresync = "+FULLRESYNC " + DEFAULT_MASTER_REPL_ID + " " + DEFAULT_MASTER_REPL_OFFSET + RespConstants.CR_LF;
        outputStream.write(fullresync.getBytes());
        
        String rdbPrefix = "$" + EMPTY_RDB.length + RespConstants.CR_LF;
        outputStream.write(rdbPrefix.getBytes());
        outputStream.write(EMPTY_RDB);
        outputStream.flush();
        
        ReplicaManager.getInstance().addReplica(outputStream);
    }

    @Override
    public String getName() {
        return NAME;
    }
}

