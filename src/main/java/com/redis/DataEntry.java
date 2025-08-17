package com.redis;

public class DataEntry {
    private final String value;
    private final Long expiryTimeMillis;

    public DataEntry(String value, Long expiryTimeMillis) {
        this.value = value;
        this.expiryTimeMillis = expiryTimeMillis;
    }

    public String getValue() {
        return value;
    }

    public Long getExpiryTimeMillis() {
        return expiryTimeMillis;
    }

    public boolean hasExpired() {
        if (expiryTimeMillis == null) {
            return false;
        }
        return System.currentTimeMillis() > expiryTimeMillis;
    }
}
