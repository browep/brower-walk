package com.github.browep.browerwalk;

public class Logger {

    public static LOG_LEVEL LEVEL = LOG_LEVEL.INFO;

    public enum LOG_LEVEL {

        VERBOSE(0),
        INFO(1),
        ERROR(2);

        int levelInt;

        LOG_LEVEL(int levelInt) {
            this.levelInt = levelInt;
        }
    }

    public static void log(LOG_LEVEL logLevel, String msg) {
        if(logLevel.levelInt >= LEVEL.levelInt) {
            System.out.println(msg);
        }
    }
}
