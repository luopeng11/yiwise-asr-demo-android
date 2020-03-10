package com.yiwise.asr.common.client.protocol;

import java.util.ResourceBundle;

public class Constant {
    public static String VALUE_VERSION;

    static {
        //ResourceBundle rb = ResourceBundle.getBundle("asr-sdk");
        VALUE_VERSION = "1.0.8-android";
    }

    public static String SDK_VERSION = "sdk-version";
    public static final String HEADER_TOKEN = "X-ASR-Token";
    public static final String HEADER_TICKET = "X-ASR-Ticket";
    public static final String HEADER_TASK_ID = "X-Task-Id";
    public static final String HEADER_REQUEST_ID = "X-Request-Id";

    public static final String PROP_EVENT = "EVENT";
    public static final String PROP_TASK_ID = "TASK_ID";
    public static final String PROP_STATUS_MSG = "STATUS_MSG";
    public static final String PROP_STATUS_CODE = "STATUS_CODE";
    public static final String PROP_MESSAGE_ID = "MESSAGE_ID";
    public static final String PROP_ASR_RESULT = "ASR_RESULT";

    // 参数列表
    public static final String PROP_ASR_PARAM = "ASR_PARAM";

    // 心跳标记
    public static final String HEART_BEAT = "HB";

    public static final String VALUE_EVENT_ASR_RESULT = "ASR_RESULT";
    public static final String VALUE_EVENT_START_ASR = "START_ASR";
    public static final String VALUE_EVENT_STOP_ASR = "STOP_ASR";

    public static final String VALUE_EVENT_ASR_STARTED = "ASR_STARTED";
    public static final String VALUE_EVENT_ASR_STOPPED = "ASR_STOPPED";
}
