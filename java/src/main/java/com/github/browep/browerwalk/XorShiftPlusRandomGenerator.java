package com.github.browep.browerwalk;

public class XorShiftPlusRandomGenerator {

    long s0;
    long s1;

    public XorShiftPlusRandomGenerator(long s0, long s1) {
        this.s0 = s0;
        this.s1 = s1;
    }

    public long nextLong() {
        long s1 = this.s0;
        final long s0 = this.s1;
        this.s0 = s0;
        s1 ^= s1 << 23;
        return ( this.s1 = ( s1 ^ s0 ^ ( s1 >>> 18 ) ^ ( s0 >>> 5 ) ) ) + s0;
    }
}
