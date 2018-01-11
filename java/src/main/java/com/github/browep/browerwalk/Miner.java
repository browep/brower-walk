package com.github.browep.browerwalk;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static com.github.browep.browerwalk.Logger.*;
import static com.github.browep.browerwalk.Logger.LOG_LEVEL.*;

public class Miner {

    private static final int SIZE_IN_MBs = 1024 * 1024 * 512;
    private static final long NUM_STEPS = (long) Math.pow(2, 19);

    private long[] path;
    private XorShiftPlusRandomGenerator rng;
    private String tag;

    public Miner(String tag) {
        this.tag = tag;
    }

    /**
     * mine the block using the raw bytes
     * @param inputData this is the raw data, (block header) to mine.
     * @throws NoSuchAlgorithmException thrown if the SHA-256 algo is not present on the system
     */
    public byte[] mineIteration(byte[] inputData) throws NoSuchAlgorithmException {

        // get a SHA-256 digest of the inputdata
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(inputData);
        byte[] seedBytes = md.digest();

        log(VERBOSE, "seed bytes: "+ Util.bytesToHex(seedBytes));

        log(VERBOSE, "leftArr: " + Util.bytesToHex(Arrays.copyOfRange(seedBytes, 0,8)));
        log(VERBOSE, "rightArr: " + Util.bytesToHex(Arrays.copyOfRange(seedBytes, 16,24)));
        log(VERBOSE,"leftArr: " + Util.bytesToHex(Arrays.copyOfRange(seedBytes, 8,16)));
        log(VERBOSE, "rightArr: " + Util.bytesToHex(Arrays.copyOfRange(seedBytes, 24,32)));

        // xor the first quarter with the third quarter, the second with the fourth to create the seed for the
        // random number generator
        long s0 = ByteBuffer.wrap(Util.xorByteArray(seedBytes, 0, 16, 8)).getLong();
        long s1 = ByteBuffer.wrap(Util.xorByteArray(seedBytes, 8, 24, 8)).getLong();

        log(VERBOSE, "s0: " + Long.toUnsignedString(s0));
        log(VERBOSE, "s1: " + Long.toUnsignedString(s1));

        long pathCreationStartTime = System.currentTimeMillis();

        // initialize the path ( 512 MB )
        path = new long[SIZE_IN_MBs / 8];

        // create the random number generator, seeded with the xor'ed hash
        rng = new XorShiftPlusRandomGenerator(s0, s1);

        // fill the path
        for (int i = 0; i < path.length; i++) {
            path[i] = rng.nextLong();
        }

        float pathCreationTime = getTimeSinceInSeconds(pathCreationStartTime);

        long walkStartTime = System.currentTimeMillis();

        // walk the path
        md.reset();
        byte[] byteBuffer = new byte[8];
        int nextStep = path.length-1;
        for (int i = 0; i < NUM_STEPS; i++) {
            long val = path[nextStep];
            long newVal = (val << 1) + (i % 2);
            path[nextStep] = newVal;

            Util.writeLong(byteBuffer, newVal);
            md.update(byteBuffer);

            nextStep = (int) Long.remainderUnsigned(val, path.length);

            if (i < 10 || i > NUM_STEPS - 10) {
                log(VERBOSE,i + " next step: " + nextStep + " " + Long.toUnsignedString(newVal) +  "  " + Util.bytesToHex(byteBuffer) + " " );
            }
        }


        path = null;

        System.gc();

        // hash the last steps.  this is our results and will be compared to what the difficulty determines
        byte[] finalDigest = md.digest();

        log(VERBOSE,"final result: " + Util.bytesToHex(finalDigest));

        log(INFO, tag + ": path creation time: " + pathCreationTime +
                " walk time: " + getTimeSinceInSeconds(walkStartTime) +
                " total time: " + getTimeSinceInSeconds(pathCreationStartTime));

        return finalDigest;

    }

    private float getTimeSinceInSeconds(long timeSince) {
        return ((float) (System.currentTimeMillis() - timeSince)) / 1000;
    }


}
