package com.github.browep.browerwalk;

import sun.misc.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Miner {


    private static final int SIZE_IN_MBs = 1024 * 1024 * 512;
    public static final long NUM_STEPS = (long) Math.pow(2, 20);
    public static final int RESULT_TO_HASH_SIZE = 32;

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
            byte[] headerBytes = IOUtils.readFully(inputStream, -1, true);
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
            while (true) {
                mineIteration(inputData);
            }
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

        // xor the first quarter with the third quarter, the second with the fourth to create the seed for the
        // random number generator
        long s0 = ByteBuffer.wrap(Util.xorByteArray(seedBytes, 0, 16, 8)).getLong();
        long s1 = ByteBuffer.wrap(Util.xorByteArray(seedBytes, 8, 24, 8)).getLong();

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
        int nextStep = 0;
        for (int i = 0; i < NUM_STEPS; i++) {
            long val = path[nextStep];
            path[nextStep] = (val << 1) + (i % 2);
            nextStep = (int) Long.remainderUnsigned(val, path.length);
        }

        // take 32 last steps ( 256 bits ) but dont modify the array
        md.reset();
        byte[] byteBuffer = new byte[8];
        for (int i = 0; i < RESULT_TO_HASH_SIZE; i++) {
            long val = path[nextStep];
            Util.writeLong(byteBuffer, val);
            md.update(byteBuffer);
            nextStep = (int) Long.remainderUnsigned(val, path.length);
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
