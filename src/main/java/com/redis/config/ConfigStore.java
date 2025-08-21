package com.redis.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ConfigStore {
    private final ConcurrentHashMap<String, String> configMap;

    public ConfigStore() {
        this.configMap = new ConcurrentHashMap<>();
        this.configMap.put("dir", "/tmp/redis-data");
        this.configMap.put("dbfilename", "dump.rdb");
    }

    public String get(String key) {
        return configMap.get(key.toLowerCase());
    }

    public void set(String key, String value) {
        configMap.put(key.toLowerCase(), value);
    }

    public Map<String, String> getMatchingConfigs(String pattern) {
        String regex = pattern.toLowerCase()
                .replace(".", "\\.")
                .replace("*", ".*");

        Pattern compilePattern;
        try {
            compilePattern = Pattern.compile(regex);
        } catch (PatternSyntaxException pse) {
            System.err.println("Invalid pattern: " + pattern + " -> " + regex);
            return Collections.emptyMap();
        }
        LinkedHashMap<String, String> matchingConfigs = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : configMap.entrySet()) {
            if (compilePattern.matcher(entry.getKey()).matches()) {
                matchingConfigs.put(entry.getKey(), entry.getValue());
            }
        }
        return matchingConfigs;
    }
}
