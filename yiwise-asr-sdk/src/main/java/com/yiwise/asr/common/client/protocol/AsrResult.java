package com.yiwise.asr.common.client.protocol;


public class AsrResult extends AsrRecognizerResult {
    Event event;            // 事件类型

    public Event getEvent() {
        return event;
    }

    public enum Event {
        SENTENCE_BEGIN,     // 句子开始
        SENTENCE_CHANGED,   // 中间识别结果
        SENTENCE_END,       // 句子结束
        UNRECOGNIZED;       // 未知
    }
}
