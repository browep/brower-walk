package com.github.browep.browerwalk;

//import it.unimi.dsi.util.XorShift1024StarRandom;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Main {


    private static List<Float> times = Collections.synchronizedList(new LinkedList<Float>());

    public static void main(String[] args) {

        int numThreads = 1;
        int i = 0;
        while (i < args.length) {
            if (args[i].startsWith("-") && (i < args.length - 1 && !args[i + 1].startsWith("-"))) {
                // check for filled args "-t <ARG>"
                switch (args[i]) {
                    case "-t":
                        numThreads = Integer.parseInt(args[i + 1]);
                        break;
                }
                i++;
            } else {

            }
            i++;

        }

        // slurp data from the binary file
        InputStream inputStream = ClassLoader.getSystemResourceAsStream("block_header.bin");

        try {
            byte[] headerBytes = Util.readFully(inputStream, -1, true);
            inputStream.close();

            for (int j = 0; j < numThreads; j++) {
                Miner miner = new Miner("miner_" + j);
                int finalNumThreads = numThreads;
                new Thread(() -> {
                    while (true) {
                        doMine(headerBytes, miner, finalNumThreads);
                    }
                }).start();
            }

            while (true) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static void doMine(byte[] headerBytes, Miner miner, int finalNumThreads) {
        try {
            float sum;
            synchronized (times) {
                float totalTime = miner.mineIteration(headerBytes);
                times.add(totalTime);

                sum = 0;

                for (Float time : times) {
                    sum += time;
                }

                sum /= times.size();
            }

            BigDecimal secondsPerHash = BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(finalNumThreads), new MathContext(3, RoundingMode.HALF_UP));
            BigDecimal hashesPerMin = BigDecimal.ONE.divide(secondsPerHash, 3, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(60));
            String hashRate = MessageFormat.format("{0} seconds per hash\n{1} H/m", secondsPerHash.toPlainString(), hashesPerMin.toPlainString());
            System.out.println(hashRate);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
