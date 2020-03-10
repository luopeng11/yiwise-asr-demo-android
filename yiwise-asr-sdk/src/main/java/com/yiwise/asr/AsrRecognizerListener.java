package com.yiwise.asr;

import com.yiwise.asr.common.client.protocol.AsrRecognizerResult;

public interface AsrRecognizerListener {

    void onSentenceBegin(AsrRecognizerResult result);

    void onSentenceBeginChanged(AsrRecognizerResult result);

    void onSentenceEnd(AsrRecognizerResult result);
}
