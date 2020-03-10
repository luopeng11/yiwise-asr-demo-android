package com.yiwise.asr.common.client.message;

import com.richie.easylog.ILogger;
import com.richie.easylog.LoggerFactory;
import com.yiwise.asr.common.client.log.MdcLog;
import com.yiwise.asr.common.client.protocol.AsrReqProtocol;
import com.yiwise.asr.common.client.protocol.AsrResProtocol;
import com.yiwise.asr.common.client.protocol.Constant;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.MDC;

import java.nio.channels.ClosedChannelException;

public class NettySignalMsgSendListener<F extends Future<?>> implements GenericFutureListener<F> {

    private static ILogger logger = LoggerFactory.getLogger(NettySignalMsgSendListener.class);

    private String title;
    private String taskId;
    private String messageId;
    private long startTime = System.currentTimeMillis();

    public NettySignalMsgSendListener(String title, AsrReqProtocol request) {
        this.title = title;
        this.taskId = request.header.get(Constant.PROP_TASK_ID);
        this.messageId = request.header.get(Constant.PROP_MESSAGE_ID);
    }

    public NettySignalMsgSendListener(String title, AsrResProtocol result) {
        this.title = title;
        this.taskId = (String) result.header.get(Constant.PROP_TASK_ID);
        this.messageId = (String) result.header.get(Constant.PROP_MESSAGE_ID);
    }

    @Override
    public void operationComplete(Future future) {
        MDC.put(MdcLog.keyMdc, taskId);

        try {
            long currentTimeMillis = System.currentTimeMillis();
            long takeTime = currentTimeMillis - startTime;

            if (future.isSuccess()) {
                logger.verbose("{} send message success, messageId={}, take time {} ms", title, messageId, takeTime);
            } else {
                if (null != future.cause() && future.cause() instanceof ClosedChannelException) {
                    logger.info("{} send message failure, channel is closed, messageId={}, take time {} ms", title, messageId, takeTime);
                } else {
                    logger.error("{} send message failure, messageId={}, take time {} ms", title, messageId, takeTime, future.cause());
                }
            }
        } finally {
            MDC.remove(MdcLog.keyMdc);
        }
    }
}

