package com.redis.util;

public class RdbConstants {

    public static final byte DATABASE_START = (byte) 0xFE;
    public static final byte END_OF_FILE = (byte) 0xFF;
    public static final byte EXPIRE_T = (byte) 0xFD;
    public static final byte P_EXPIRE_T = (byte) 0xFC;
    public static final byte HASH_TABLE_SIZE = (byte) 0xFB;
    public static final byte METADATA_SECTION = (byte) 0xFA;


    public static final byte LENGTH_6BIT = 0b00;
    public static final byte LENGTH_14BIT = 0b01;
    public static final byte LENGTH_32BIT = 0b10;
    public static final byte LENGTH_SPECIAL = 0b11;

    public static final byte STRING_INTEGER_8BIT = 0;
    public static final byte STRING_INTEGER_16BIT = 1;
    public static final byte STRING_INTEGER_32BIT = 2;

    public static final byte STRING_VALUE_BYTE = 0;

}
