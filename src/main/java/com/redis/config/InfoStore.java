package com.redis.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class InfoStore {
    private final ConcurrentHashMap<String, Map<String, String>> infoMap;

    public InfoStore() {
        this.infoMap = new ConcurrentHashMap<>();
    }

    public void set(String info, String key, String value) {
        infoMap.putIfAbsent(info, new HashMap<>());
        infoMap.get(info).putIfAbsent(key, value);
    }

    public Map<String, String> get(String info, String key) {
        return infoMap.get(key.toLowerCase());
    }

    public Map<String, Map<String, String>> getMatchingInfos(String infoPattern, String pattern) {
        String regex = infoPattern.toLowerCase()
                .replace(".", "\\.")
                .replace("*", ".*");
        Pattern compilePattern;
        try {
            compilePattern = Pattern.compile(regex);
        } catch (PatternSyntaxException pe) {
            System.err.println("Invalid pattern: " + infoPattern + " -> " + regex);
            return Collections.emptyMap();
        }
        LinkedHashMap<String, Map<String, String>> matchingInfos = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, String>> entry : infoMap.entrySet()) {
            if (compilePattern.matcher(entry.getKey()).matches()) {
                matchingInfos.put(entry.getKey(), entry.getValue());
            }
        }
        return matchingInfos;
    }
}
