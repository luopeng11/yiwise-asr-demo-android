package com.yiwise.asr.common.client.protocol;

public class AsrRecognizerResult {
    String taskId;          // 会话请求id
    Long beginTime;         // 本句子开始时间
    Long time;              // 当前已处理音频时长
    String resultText;      // 识别文本结果
    Long sentenceIndex;     // 句子Index

    public String getTaskId() {
        return taskId;
    }

    public Long getBeginTime() {
        return beginTime;
    }

    public Long getTime() {
        return time;
    }

    public String getResultText() {
        return resultText;
    }

    public Long getSentenceIndex() {
        return sentenceIndex;
    }

    @Override
    public String toString() {
        return "taskId='" + taskId + '\'' +
                ", beginTime=" + beginTime +
                ", time=" + time +
                ", sentenceIndex=" + sentenceIndex +
                ", resultText='" + resultText + '\'';
    }
}
