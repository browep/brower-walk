package com.github.browep.browerwalk;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

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

    /**
     * Read up to <code>length</code> of bytes from <code>in</code>
     * until EOF is detected.
     * @param in input stream, must not be null
     * @param length number of bytes to read, -1 or Integer.MAX_VALUE means
     *        read as much as possible
     * @param readAll if true, an EOFException will be thrown if not enough
     *        bytes are read. Ignored when length is -1 or Integer.MAX_VALUE
     * @return bytes read
     * @throws IOException Any IO error or a premature EOF is detected
     */
    public static byte[] readFully(InputStream is, int length, boolean readAll)
            throws IOException {
        byte[] output = {};
        if (length == -1) length = Integer.MAX_VALUE;
        int pos = 0;
        while (pos < length) {
            int bytesToRead;
            if (pos >= output.length) { // Only expand when there's no room
                bytesToRead = Math.min(length - pos, output.length + 1024);
                if (output.length < pos + bytesToRead) {
                    output = Arrays.copyOf(output, pos + bytesToRead);
                }
            } else {
                bytesToRead = output.length - pos;
            }
            int cc = is.read(output, pos, bytesToRead);
            if (cc < 0) {
                if (readAll && length != Integer.MAX_VALUE) {
                    throw new EOFException("Detect premature EOF");
                } else {
                    if (output.length != pos) {
                        output = Arrays.copyOf(output, pos);
                    }
                    break;
                }
            }
            pos += cc;
        }
        return output;
    }
    
}
