package com.yiwise.asr;

import com.richie.easylog.ILogger;
import com.richie.easylog.LoggerFactory;
import com.yiwise.asr.common.client.log.MdcLog;
import com.yiwise.asr.common.client.message.NettyAudioMsgSendListener;
import com.yiwise.asr.common.client.message.NettySignalMsgSendListener;
import com.yiwise.asr.common.client.protocol.AsrReqProtocol;
import com.yiwise.asr.common.client.protocol.Constant;
import com.yiwise.asr.common.client.protocol.RealTimeAsrParam;
import com.yiwise.asr.common.client.utils.IdGenerator;
import com.yiwise.asr.transport.Connection;

import org.slf4j.MDC;

import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.util.concurrent.Future;

public class AsrRecognizer {
    private static ILogger logger = LoggerFactory.getLogger(AsrRecognizer.class);

    private String currentTaskId;
    private Connection connection;

    private CountDownLatch readyLatch;
    private CountDownLatch completeLatch;

    private AtomicInteger audioCount = new AtomicInteger(0);

    // 参数列表
    private RealTimeAsrParam asrParam = new RealTimeAsrParam();

    public AsrRecognizer(AsrClient asrClient, AsrRecognizerListener asrRecognizerListener) throws Exception {
        this(IdGenerator.genId(), asrClient, asrRecognizerListener);
    }

    /**
     * @param taskId                asr识别任务id，如果传入taskId，请保证taskId对于业务的唯一性（服务端不会进行唯一性校验）
     * @param asrClient             asr识别客户端（可复用）
     * @param asrRecognizerListener asr识别结果监听器
     * @throws Exception 连接错误
     */
    public AsrRecognizer(String taskId, AsrClient asrClient, AsrRecognizerListener asrRecognizerListener) throws Exception {
        MDC.put(MdcLog.keyMdc, taskId);

        AsrRecognizerConnectionListener connectionListener = new AsrRecognizerConnectionListener(taskId, asrRecognizerListener);
        connectionListener.setAsrRecognizer(this);

        this.currentTaskId = taskId;
        this.connection = asrClient.connect(connectionListener);
    }

    /**
     * 开始asr识别
     *
     * @throws Exception 连接错误
     */
    public void startAsr() throws Exception {
        AsrReqProtocol request = new AsrReqProtocol(currentTaskId);
        request.header.put(Constant.PROP_EVENT, Constant.VALUE_EVENT_START_ASR);
        request.payload.put(Constant.PROP_ASR_PARAM, asrParam);

        completeLatch = new CountDownLatch(1);
        readyLatch = new CountDownLatch(1);

        Future channelFuture = connection.sendText(request.serialize());
        channelFuture.addListener(new NettySignalMsgSendListener<>("signal", request));

        boolean result = readyLatch.await(10, TimeUnit.SECONDS);
        if (!result) {
            String msg = String.format("timeout after 10 seconds waiting for start complete taskId=%s,", currentTaskId);
            logger.error(msg);
            throw new Exception(msg);
        }
    }

    /**
     * 发送时需要控制速率，使单位时间内发送的数据大小接近单位时间原始语音数据存储的大小
     * <ul>
     * <li>对于8k pcm 编码数据，建议每发送4800字节 sleep 300 ms</li>
     * <li>对于16k pcm 编码数据，建议每发送9600字节 sleep 300 ms</li>
     * </ul>
     *
     * @param data 音频数据
     */
    public void sendAudio(byte[] data) {
        if (data == null) {
            throw new NullPointerException("data can't be null");
        }
        Future channelFuture = connection.sendBinary(data);
        int index = audioCount.incrementAndGet();
        channelFuture.addListener(new NettyAudioMsgSendListener<>("signal", currentTaskId, index, data.length));
    }

    /**
     * 语音数据来自文件，发送时需要控制速率，使单位时间内发送的数据大小接近单位时间原始语音数据存储的大小
     *
     * @param inputStream   离线音频文件流
     * @param batchSize     每次发送到服务端的数据大小
     * @param sleepInterval 数据发送的间隔，即用于控制发送数据的速率，每次发送batchSize大小的数据后需要sleep的时间
     * @throws Exception 数据流读取异常，sleep异常
     */
    public void sendAudio(InputStream inputStream, int batchSize, int sleepInterval) throws Exception {
        byte[] bytes = new byte[batchSize];
        int length;
        while ((length = inputStream.read(bytes)) > 0) {
            sendAudio(Arrays.copyOfRange(bytes, 0, length));
            Thread.sleep(sleepInterval);
        }
    }

    /**
     * 结束语音识别
     *
     * @throws Exception 连接错误
     */
    public void stopAsr() throws Exception {
        stopAsr(10);
    }

    /**
     * 结束语音识别
     * 发送停止识别后，最后的识别结果返回可能有一定延迟
     *
     * @param waitTime 结束等待超时时间
     * @throws Exception 连接错误
     */
    public void stopAsr(long waitTime) throws Exception {
        try {
            if (connection.isActive()) {
                AsrReqProtocol request = new AsrReqProtocol(currentTaskId);
                request.header.put(Constant.PROP_EVENT, Constant.VALUE_EVENT_STOP_ASR);
                Future channelFuture = connection.sendText(request.serialize());
                channelFuture.addListener(new NettySignalMsgSendListener<>("signal", request));

                // 等待最后一帧数据（最后一次的识别结果）发送完成，然后关闭
                boolean result = completeLatch.await(waitTime, TimeUnit.SECONDS);
                if (!result) {
                    String msg = String.format("timeout after 10 seconds waiting for stop complete taskId=%s,", currentTaskId);
                    logger.error(msg);
                    throw new Exception(msg);
                }
            }
        } finally {
            close();
        }
    }

    /**
     * 设置热词id
     *
     * @param hotWordId 热词id
     */
    public void setHotWordId(Long hotWordId) {
        asrParam.setHotWordId(hotWordId);
    }

    /**
     * 是否打标点
     *
     * @param enablePunctuation true: 开启; false: 关闭
     */
    public void setEnablePunctuation(boolean enablePunctuation) {
        asrParam.setEnablePunctuation(enablePunctuation);
    }

    /**
     * 是否开启中间识别结果
     *
     * @param enableIntermediateResult true: 开启; false: 关闭
     */
    public void setEnableIntermediateResult(boolean enableIntermediateResult) {
        asrParam.setEnableIntermediateResult(enableIntermediateResult);
    }

    /**
     * 是否开启逆文本规整
     *
     * @param enableInverseTextNormalization true: 开启; false: 关闭
     */
    public void setEnableInverseTextNormalization(boolean enableInverseTextNormalization) {
        asrParam.setEnableInverseTextNormalization(enableInverseTextNormalization);
    }

    /**
     * 启用自学习模型
     *
     * @param selfLearningModelId 自学习模型id
     */
    public void setSelfLearningModelId(Long selfLearningModelId) {
        asrParam.setSelfLearningModelId(selfLearningModelId);
    }

    /**
     * 自学习模型比率
     *
     * @param ratio 自学习模型比率
     */
    public void setSelfLearningRatio(Float ratio) {
        asrParam.setSelfLearningRatio(ratio);
    }

    /**
     * 关闭netty连接
     */
    protected void close() {
        connection.close();
        logger.debug("close connection");
    }

    void markAsrStopped() {
        if (completeLatch != null) {
            completeLatch.countDown();
        } else {
            logger.error("completeLatch is null");
        }
    }

    void markAsrStarted() {
        if (readyLatch != null) {
            readyLatch.countDown();
        } else {
            logger.error("readyLatch is null");
        }
    }
}
