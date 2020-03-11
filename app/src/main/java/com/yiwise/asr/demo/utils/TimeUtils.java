package com.yiwise.asr.demo.utils;


import java.util.concurrent.TimeUnit;

public class TimeUtils {

    public static void sleepMilliseconds(long timeout) {
        try {
            TimeUnit.MILLISECONDS.sleep(timeout);
        } catch (Exception ignore) {
        }
    }

    public static void sleepSeconds(long timeout) {
        try {
            TimeUnit.SECONDS.sleep(timeout);
        } catch (Exception ignore) {
        }
    }
}
