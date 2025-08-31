package com.redis.util;

import java.util.Optional;

public class ArgsParser {
    public static final String DIR = "--dir";
    public static final String DBFILENAME = "--dbfilename";
    public static final String PORT = "--port";
    public static final String REPLICAOF = "--replicaof";

    public enum ArgsConstants {
        DIR(ArgsParser.DIR),
        DBFILENAME(ArgsParser.DBFILENAME),
        PORT(ArgsParser.PORT),
        REPLICAOF(ArgsParser.REPLICAOF);

        private final String flag;

        ArgsConstants(String flag) {
            this.flag = flag;
        }

        public Optional<String> apply(String[] args) {
            if (args == null) return Optional.empty();
            for (int i = 0; i < args.length; i++) {
                String a = args[i];
                if (a.equals(flag)) {
                    if (i + 1 < args.length) {
                        return Optional.of(args[i+1]);
                    } else {
                        return Optional.empty();
                    }
                }

                if (a.startsWith(flag+"=")) {
                    return Optional.of(a.substring(flag.length() + 1));
                }
            }
            return Optional.empty();
        }

        public String flag() {
            return flag;
        }
    }



}
