package com.redis.replication;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ReplicaManager {
    private static final ReplicaManager INSTANCE = new ReplicaManager();
    private final List<OutputStream> replicas = new CopyOnWriteArrayList<>();

    private ReplicaManager() {}

    public static ReplicaManager getInstance() {
        return INSTANCE;
    }

    public void addReplica(OutputStream outputStream) {
        replicas.add(outputStream);
    }

    public void removeReplica(OutputStream outputStream) {
        replicas.remove(outputStream);
    }

    public void propagateCommand(byte[] command) {
        for (OutputStream replica : replicas) {
            try {
                replica.write(command);
                replica.flush();
            } catch (IOException e) {
                removeReplica(replica);
            }
        }
    }
}

