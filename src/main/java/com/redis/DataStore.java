package com.redis;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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

    public void set(String key, DataEntry entry) {
        store.put(key, entry);
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

    public Set<String> getMatchingKeys(String pattern) {
        String regex = pattern
                .replace(".", "\\.")
                .replace("*", ".*")
                .replace("?", ".");

        Pattern compiledPattern;
        try {
            compiledPattern = Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            System.err.println("Invalid regex pattern derived for KEYS: " + regex);
            return Collections.emptySet();
        }

        Set<String> matchingKeys = new LinkedHashSet<>();

        for (Map.Entry<String, DataEntry> entry : store.entrySet()) {
            String key = entry.getKey();
            DataEntry dataEntry = entry.getValue();

            if (dataEntry.hasExpired()) {
                store.remove(key);
                continue;
            }

            if (compiledPattern.matcher(key).matches()) {
                matchingKeys.add(key);
            }
        }
        return matchingKeys;
    }
}
