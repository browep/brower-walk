package com.github.browep.browerwalk;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Miner {



    private static final int SIZE_IN_MBs = 1024 * 1024 * 512;
    public static final long NUM_STEPS = (long) Math.pow(2, 20);

    private long[] path;
    private XorShifPlusRandomGenerator rng;
    private String tag;

    public Miner(String tag) {
        this.tag = tag;
    }

    public void start() {

        // slurp data from the binary file

        Runnable runnable = () -> mine();

        new Thread(runnable).start();

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

        long s0 = ByteBuffer.wrap(Util.xorByteArray(seedBytes, 0, 128, 64)).getLong();
        long s1 = ByteBuffer.wrap(Util.xorByteArray(seedBytes, 64, 192, 64)).getLong();

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

        log("path creation time: " + pathCreationTime +
                " walk time: " + getTimeSinceInSeconds(walkStartTime) +
                " total time: " + getTimeSinceInSeconds(pathCreationStartTime));

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
