package com.redis;

import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    private final ConcurrentHashMap<String, DataEntry> store;

    public DataStore() {
        store = new ConcurrentHashMap<>();
    }

    public void set(String key, String value, Long expiryMillis) {
        Long expiryTimeMillis = null;
        if (expiryMillis != null)
            expiryTimeMillis = System.currentTimeMillis() + expiryMillis;
        store.put(key, new DataEntry(value, expiryTimeMillis));
    }

    public String get(String key) {
        DataEntry entry = store.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.hasExpired()) {
            store.remove(key);
            return null;
        }
        return entry.getValue();
    }

    public boolean containsKey(String key) {
        DataEntry entry = store.get(key);
        if (entry == null) {
            return false;
        }
        if (entry.hasExpired()) {
            store.remove(key);
            return false;
        }
        return true;
    }
}
