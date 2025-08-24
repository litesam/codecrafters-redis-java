package com.redis.util;

import com.redis.DataStore;

import java.io.IOException;
import java.io.InputStream;

public class RdbParser {

    private final InputStream inputStream;
    private final DataStore dataStore;

    public RdbParser(InputStream inputStream, DataStore dataStore) {
        this.inputStream = inputStream;
        this.dataStore = dataStore;
    }

    public void load() throws IOException {
        // Header section
        // Metadata section
        // Database section
        // End of file section
        parseHeader();
        final var redisVersion = parseVersion();
        System.out.println("version: " + redisVersion);

        Integer databaseNumber = 0;

        while (true) {
            final var opcode = inputStream.readNBytes(1)[0];
            if (opcode == RdbConstants.END_OF_FILE) {
                System.out.println("Reached end of file for rdb");
            }

            switch (opcode) {
                case RdbConstants.METADATA_SECTION -> {
                    final var key = readString();
                    final var value = readString();
                    System.out.printf("metadata: %s, %s\n", key, value);
                }
                case RdbConstants.DATABASE_START -> {
                    databaseNumber = readUnsignedByte();
                    System.out.printf("databaseNumber: %d\n", databaseNumber);
                }
                default -> {
                    if (databaseNumber == null) {
                        //?
                    }
                    int valueType = opcode;
                    long expiration = -1;
                    if (opcode == RdbConstants.P_EXPIRE_T) {
                        expiration = readUnsignedLong();
                        valueType = readUnsignedByte();
                    }

                    final var key = readString();
                    final var value = readValue(valueType);
                    //TODO: Fill DataStore
                    dataStore.set(key, value, expiration);
                }
            }
        }
    }

    private String readValue(int valueType) throws IOException {
        return switch (valueType) {
            case RdbConstants.STRING_VALUE_BYTE -> readString();
            default -> throw new RuntimeException("unsupported value type: " + valueType);
        };
    }

    public String readString() throws IOException {
        final var length = readLength();

        if (length < 0) {
            final var type = (-length) - 1;
            return switch(type) {
                case RdbConstants.STRING_INTEGER_8BIT -> String.valueOf(Byte.toUnsignedInt(inputStream.readNBytes(1)[0]));
                case RdbConstants.STRING_INTEGER_16BIT -> String.valueOf(Short.toUnsignedInt(Short.reverseBytes(inputStream.readNBytes(2)[0])));
                case RdbConstants.STRING_INTEGER_32BIT -> Integer.toUnsignedString(Integer.reverseBytes(inputStream.readNBytes(4)[0]));
                default -> throw new RuntimeException("No type found");
            };
        }

        final var content = inputStream.readNBytes(length);
        return new String(content);
    }

    public int readUnsignedByte() throws IOException {
        return Byte.toUnsignedInt(inputStream.readNBytes(1)[0]);
    }

    public long readUnsignedInteger() throws IOException {
        return Integer.toUnsignedLong(Integer.reverseBytes(inputStream.readNBytes(4)[0]));
    }

    public long readUnsignedLong() throws IOException {
        return Long.reverseBytes(inputStream.readNBytes(8)[0]);
    }

    public int readLength() throws IOException {
        final var first = readUnsignedByte();

        final var encoding = first >> 6;
        final var value = first & 0b0011_1111;

        return switch (encoding) {
            case RdbConstants.LENGTH_6BIT -> value;
            case RdbConstants.LENGTH_14BIT -> {
                final var second = readUnsignedByte();
                yield (value << 8) | second;
            }
            case RdbConstants.LENGTH_SPECIAL -> -(value + 1);
            default -> throw new RuntimeException("no value found");
        };
    }

    private void parseHeader() throws IOException {
        final var header = new String(inputStream.readNBytes(5));

        if (!header.equals(("REDIS"))) {
            throw new RuntimeException("Invalid file format");
        }

        final var version = parseVersion();
        System.out.println("Redis server version: " + version);
    }

    private int parseVersion() throws IOException {
        final var version = new String(inputStream.readNBytes(3));
        return Integer.parseInt(version);
    }
}
