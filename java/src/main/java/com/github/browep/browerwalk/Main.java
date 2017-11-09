package com.github.browep.browerwalk;

//import it.unimi.dsi.util.XorShift1024StarRandom;

public class Main {


    public static void main(String[] args) {
        Miner miner = new Miner("miner_1");
        miner.start();
        while (true) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
