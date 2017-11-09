package com.github.browep.browerwalk;

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
    
}
