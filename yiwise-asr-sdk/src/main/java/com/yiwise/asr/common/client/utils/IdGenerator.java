package com.yiwise.asr.common.client.utils;

import java.util.UUID;

public class IdGenerator {

    public static String genId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
