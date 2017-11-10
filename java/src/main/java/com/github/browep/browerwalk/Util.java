package com.github.browep.browerwalk;

import java.io.IOException;

public class Util {
    /**
     * do logical XOR of byte arry
     * @param array array containing data to xor
     * @param leftStart start of index for left pointer
     * @param rightStart start of index for right pointer
     * @param count number of pointer moves
     * @return xor'ed resulting array
     */
    public static byte[] xorByteArray(byte[] array, int leftStart, int rightStart, int count) {
        byte[] result = new byte[count];

        for (int i = 0; i < count; i++) {
            result[i] = (byte) (array[i + leftStart] ^ array[i + rightStart]);
        }

        return result;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte byt : bytes) result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }

    public static void writeLong(byte[] writeBuffer, long l) {
        writeBuffer[0] = (byte)(l >>> 56);
        writeBuffer[1] = (byte)(l >>> 48);
        writeBuffer[2] = (byte)(l >>> 40);
        writeBuffer[3] = (byte)(l >>> 32);
        writeBuffer[4] = (byte)(l >>> 24);
        writeBuffer[5] = (byte)(l >>> 16);
        writeBuffer[6] = (byte)(l >>>  8);
        writeBuffer[7] = (byte)(l);
    }
    
}
