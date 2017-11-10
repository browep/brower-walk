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

    private void mineIteration(byte[] inputData) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");

        md.update(inputData);
        byte[] seedBytes = md.digest();

        long s0 = ByteBuffer.wrap(Util.xorByteArray(seedBytes, 0, 16, 8)).getLong();
        long s1 = ByteBuffer.wrap(Util.xorByteArray(seedBytes, 8, 24, 8)).getLong();

        long pathCreationStartTime = System.currentTimeMillis();

        path = new long[SIZE_IN_MBs / 8];

        rng = new XorShifPlusRandomGenerator(s0, s1);

        for (int i = 0; i < path.length; i++) {
            path[i] = rng.nextLong();
        }

        float pathCreationTime = getTimeSinceInSeconds(pathCreationStartTime);

        long walkStartTime = System.currentTimeMillis();

        int nextStep = 0;

        for (int i = 0; i < NUM_STEPS; i++) {
            long val = path[nextStep];
            path[nextStep] = (val << 1) + (i % 2);
            nextStep = (int) Long.remainderUnsigned(val, path.length);
        }

        md.reset();

        byte[] byteBuffer = new byte[8];

        // last steps calculation
        for (int i = 0; i < RESULT_TO_HASH_SIZE; i++) {
            long val = path[nextStep];
            Util.writeLong(byteBuffer, val);
            md.update(byteBuffer);
            nextStep = (int) Long.remainderUnsigned(val, path.length);
        }

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
