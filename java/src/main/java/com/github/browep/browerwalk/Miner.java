package com.github.browep.browerwalk;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Miner {

    private static final int SIZE_IN_MBs = 1024 * 1024 * 512;
    private static final long NUM_STEPS = (long) Math.pow(2, 19);

    private long[] path;
    private XorShifPlusRandomGenerator rng;
    private String tag;

    public Miner(String tag) {
        this.tag = tag;
    }

    public void start() {

        // slurp data from the binary file
        InputStream inputStream = ClassLoader.getSystemResourceAsStream("block_header.bin");

        try {
            byte[] headerBytes = Util.readFully(inputStream, -1, true);
            inputStream.close();

            Runnable runnable = () -> mine(headerBytes);

            new Thread(runnable).start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void mine(byte[] inputData) {

        log("started");

        try {
//            while (true) {
                mineIteration(inputData);
//            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * mine the block using the raw bytes
     * @param inputData this is the raw data, (block header) to mine.
     * @throws NoSuchAlgorithmException thrown if the SHA-256 algo is not present on the system
     */
    private void mineIteration(byte[] inputData) throws NoSuchAlgorithmException {

        // get a SHA-256 digest of the inputdata
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(inputData);
        byte[] seedBytes = md.digest();

        log("seed bytes: "+ Util.bytesToHex(seedBytes));

        log("leftArr: " + Util.bytesToHex(Arrays.copyOfRange(seedBytes, 0,8)));
        log("rightArr: " + Util.bytesToHex(Arrays.copyOfRange(seedBytes, 16,24)));
        log("leftArr: " + Util.bytesToHex(Arrays.copyOfRange(seedBytes, 8,16)));
        log("rightArr: " + Util.bytesToHex(Arrays.copyOfRange(seedBytes, 24,32)));

        // xor the first quarter with the third quarter, the second with the fourth to create the seed for the
        // random number generator
        long s0 = ByteBuffer.wrap(Util.xorByteArray(seedBytes, 0, 16, 8)).getLong();
        long s1 = ByteBuffer.wrap(Util.xorByteArray(seedBytes, 8, 24, 8)).getLong();

        log("s0: " + Long.toUnsignedString(s0));
        log("s1: " + Long.toUnsignedString(s1));

        long pathCreationStartTime = System.currentTimeMillis();

        // initialize the path ( 512 MB )
        path = new long[SIZE_IN_MBs / 8];

        // create the random number generator, seeded with the xor'ed hash
        rng = new XorShifPlusRandomGenerator(s0, s1);

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
                log(i + " next step: " + nextStep + " " + Long.toUnsignedString(newVal) +  "  " + Util.bytesToHex(byteBuffer) + " " );
            }
        }

        // hash the last steps.  this is our results and will be compared to what the difficulty determines
        byte[] finalDigest = md.digest();

        log("path creation time: " + pathCreationTime +
                " walk time: " + getTimeSinceInSeconds(walkStartTime) +
                " total time: " + getTimeSinceInSeconds(pathCreationStartTime));

        log("final result: " + Util.bytesToHex(finalDigest));

        path = null;

        System.gc();

    }

    private float getTimeSinceInSeconds(long pathCreationStartTime) {
        return ((float) (System.currentTimeMillis() - pathCreationStartTime)) / 1000;
    }

    private void log(String msg) {
        System.out.println(tag + ": " + msg);
    }

}
