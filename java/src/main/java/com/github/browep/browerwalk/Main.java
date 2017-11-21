package com.github.browep.browerwalk;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Main {


    private static final List<Interval> times = Collections.synchronizedList(new LinkedList<>());

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
            long startTime = System.currentTimeMillis();
            miner.mineIteration(headerBytes);
            long endTime = System.currentTimeMillis();

            synchronized (times) {

                times.add(new Interval(startTime, endTime));

                long timeOverAll = endTime - times.get(0).start;

                long millisPerHash = timeOverAll / times.size();

                System.out.println("millisPerHash: " + millisPerHash);

                float hashesPerMin = 1/((float)millisPerHash) * 1000 * 60;

                System.out.println(hashesPerMin + " H/m");
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static class Interval {
        long start;
        long end;

        public Interval(long start, long end) {
            this.start = start;
            this.end = end;
        }
    }
}
