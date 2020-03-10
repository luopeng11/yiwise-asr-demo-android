package com.yiwise.asr.common.client.message;

import com.richie.easylog.ILogger;
import com.richie.easylog.LoggerFactory;
import com.yiwise.asr.common.client.log.MdcLog;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.nio.channels.ClosedChannelException;

public class NettyAudioMsgSendListener<F extends Future<?>> implements GenericFutureListener<F> {

    private static ILogger logger = LoggerFactory.getLogger(NettyAudioMsgSendListener.class);

    private int audioIndex;
    private int length;

    private String title;
    private String taskId;

    private long startTime = System.currentTimeMillis();

    public NettyAudioMsgSendListener(String title, String taskId, int audioIndex, int length) {
        this.title = title;
        this.taskId = taskId;
        this.audioIndex = audioIndex;
        this.length = length;
    }

    @Override
    public void operationComplete(Future future) {
        MDC.put(MdcLog.keyMdc, taskId);

        try {
            String audioIndexStr = StringUtils.leftPad(String.valueOf(audioIndex), 5);
            long currentTimeMillis = System.currentTimeMillis();

            long takeTime = currentTimeMillis - startTime;

            if (future.isSuccess()) {
                logger.verbose("{} send audio message success, audioIndex={}, Length={}, take time {} ms", title, audioIndexStr, length, takeTime);
            } else {
                if (null != future.cause() && future.cause() instanceof ClosedChannelException) {
                    logger.info("{} send audio message failure, channel is closed, audioIndex={}, Length={}, take time {} ms", title, audioIndexStr, length, takeTime);
                } else {
                    logger.error("{} send audio message failure, audioIndex={}, Length={}, take time {} ms", title, audioIndexStr, length, takeTime);
                }
            }
        } finally {
            MDC.remove(MdcLog.keyMdc);
        }
    }
}

