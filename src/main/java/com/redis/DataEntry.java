package com.redis;

public record DataEntry(String value, Long expiryTimeMillis) {

    public boolean hasExpired() {
        if (expiryTimeMillis == null) {
            return false;
        }
        return System.currentTimeMillis() > expiryTimeMillis;
    }
}
