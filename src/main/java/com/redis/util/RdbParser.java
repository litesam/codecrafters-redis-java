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
        parseHeader();

        Integer databaseNumber = 0;

        while (true) {
            final var opcode = inputStream.readNBytes(1)[0];
            if (opcode == RdbConstants.END_OF_FILE) {
                System.out.println("Reached end of file for RDB");
                break;
            }

            switch (opcode) {
                case RdbConstants.METADATA_SECTION -> {
                    final var key = readString();
                    final var value = readString();
                    System.out.printf("Metadata: %s, %s\n", key, value);
                }
                case RdbConstants.DATABASE_START -> {
                    databaseNumber = readUnsignedByte();
                    System.out.printf("Database Number: %d\n", databaseNumber);
                }
                case RdbConstants.HASH_TABLE_SIZE -> {
                    final var hashTableSize = readLength();
                    System.out.printf("Hash Table Size: %d\n", hashTableSize);

                    final var expireHashTableSize = readLength();
                    System.out.printf("Expire Hash Table Size: %d\n", expireHashTableSize);
                }
                default -> {
                    if (databaseNumber == null) {
                        throw new RuntimeException("Database number is null");
                    }

                    int valueType = opcode;
                    Long expiration = null;

                    // Check if the key has an expiration timestamp
                    if (opcode == RdbConstants.P_EXPIRE_T) {
                        expiration = readUnsignedLong();
                        valueType = readUnsignedByte();
                    }

                    final var key = readString();
                    final var value = readValue(valueType);

                    // Store the key-value pair with expiration in the DataStore
                    dataStore.set(key, value, expiration);
                }
            }
        }
    }

    private String readValue(int valueType) throws IOException {
        return switch (valueType) {
            case RdbConstants.STRING_VALUE_BYTE -> readString();
            default -> throw new RuntimeException("Unsupported value type: " + valueType);
        };
    }

    public String readString() throws IOException {
        final var length = readLength();

        if (length < 0) {
            final var type = (-length) - 1;
            return switch (type) {
                case RdbConstants.STRING_INTEGER_8BIT -> String.valueOf(Byte.toUnsignedInt(inputStream.readNBytes(1)[0]));
                case RdbConstants.STRING_INTEGER_16BIT -> String.valueOf(Short.toUnsignedInt(Short.reverseBytes(readShort())));
                case RdbConstants.STRING_INTEGER_32BIT -> String.valueOf(Integer.toUnsignedLong(Integer.reverseBytes(readInt())));
                default -> throw new RuntimeException("Unsupported string type: " + type);
            };
        }

        final var content = inputStream.readNBytes(length);
        return new String(content);
    }

    public int readUnsignedByte() throws IOException {
        return Byte.toUnsignedInt(inputStream.readNBytes(1)[0]);
    }

    public long readUnsignedInteger() throws IOException {
        return Integer.toUnsignedLong(Integer.reverseBytes(readInt()));
    }

    public long readUnsignedLong() throws IOException {
        return Long.reverseBytes(readLong());
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
            default -> throw new RuntimeException("Invalid length encoding");
        };
    }

    private void parseHeader() throws IOException {
        final var header = new String(inputStream.readNBytes(5));

        if (!header.equals("REDIS")) {
            throw new RuntimeException("Invalid file format");
        }

        final var version = parseVersion();
        System.out.println("Redis server version: " + version);
    }

    private int parseVersion() throws IOException {
        final var version = new String(inputStream.readNBytes(4));
        return Integer.parseInt(version);
    }

    private short readShort() throws IOException {
        final var bytes = inputStream.readNBytes(2);
        return (short) ((bytes[1] << 8) | (bytes[0] & 0xFF));
    }

    private int readInt() throws IOException {
        final var bytes = inputStream.readNBytes(4);
        return (bytes[3] << 24) | ((bytes[2] & 0xFF) << 16) | ((bytes[1] & 0xFF) << 8) | (bytes[0] & 0xFF);
    }

    private long readLong() throws IOException {
        final var bytes = inputStream.readNBytes(8);
        return ((long) bytes[7] << 56) |
                ((long) (bytes[6] & 0xFF) << 48) |
                ((long) (bytes[5] & 0xFF) << 40) |
                ((long) (bytes[4] & 0xFF) << 32) |
                ((long) (bytes[3] & 0xFF) << 24) |
                ((long) (bytes[2] & 0xFF) << 16) |
                ((long) (bytes[1] & 0xFF) << 8) |
                ((long) (bytes[0] & 0xFF));
    }
}