package com.yiwise.asr;

import com.richie.easylog.ILogger;
import com.richie.easylog.LoggerFactory;
import com.yiwise.asr.common.client.protocol.AsrResProtocol;
import com.yiwise.asr.common.client.protocol.AsrResult;
import com.yiwise.asr.common.client.protocol.Constant;
import com.yiwise.asr.common.client.utils.JsonUtils;
import com.yiwise.asr.transport.ConnectionListener;
import org.apache.commons.lang3.StringUtils;

public class AsrRecognizerConnectionListener implements ConnectionListener {
    private static ILogger logger = LoggerFactory.getLogger(AsrRecognizerConnectionListener.class);

    private String taskId;
    private AsrRecognizer asrRecognizer;
    private AsrRecognizerListener asrRecognizerListener;

    public AsrRecognizerConnectionListener(String taskId, AsrRecognizerListener asrRecognizerListener) {
        this.taskId = taskId;
        this.asrRecognizerListener = asrRecognizerListener;
    }

    public void onOpen() {
        logger.debug("connection is ok");
    }

    public void onClose(int closeCode, String reason) {
        logger.info("connection is closed due to {}, code : {}", reason, closeCode);
    }

    public void onError(Throwable throwable) {
        logger.error("connection on Error", throwable);
    }

    public void onFail(int status, String reason) {
        logger.error("asr server fail. status : {}, reason : {}", status, reason);
    }

    public void onMessage(String message) {
        if (StringUtils.isNotEmpty(message)) {
            AsrResProtocol resProtocol = JsonUtils.string2Object(message, AsrResProtocol.class);

            if (resProtocol.isSuccess()) {
                String event = (String) resProtocol.header.get(Constant.PROP_EVENT);
                switch (event) {
                    case Constant.VALUE_EVENT_ASR_STARTED:
                        asrRecognizer.markAsrStarted();
                        break;
                    case Constant.VALUE_EVENT_ASR_RESULT:
                        AsrResult asrResult = resProtocol.getAsrResult();
                        handleAsrResultEvent(asrResult);
                        break;
                    case Constant.VALUE_EVENT_ASR_STOPPED:
                        AsrResult finalAsrResult = resProtocol.getAsrResult();
                        if (finalAsrResult != null) {
                            handleAsrResultEvent(finalAsrResult);
                        }
                        asrRecognizer.markAsrStopped();
                        break;
                    default:
                        logger.warn("unknown event type, event={}", event);
                }
            } else {
                onFail(resProtocol.getStatus(), resProtocol.getStatusMsg());
            }
        } else {
            logger.warn("get empty message");
        }
    }

    public String getTaskId() {
        return taskId;
    }

    public void setAsrRecognizer(AsrRecognizer asrRecognizer) {
        this.asrRecognizer = asrRecognizer;
    }

    private void handleAsrResultEvent(AsrResult asrResult) {
        AsrResult.Event event = asrResult.getEvent();
        switch (event) {
            case SENTENCE_BEGIN:
                asrRecognizerListener.onSentenceBegin(asrResult);
                break;
            case SENTENCE_CHANGED:
                asrRecognizerListener.onSentenceBeginChanged(asrResult);
                break;
            case SENTENCE_END:
                asrRecognizerListener.onSentenceEnd(asrResult);
                break;
            default:
                logger.warn("unknown AsrResult event type, event={}", event);
        }
    }
}
