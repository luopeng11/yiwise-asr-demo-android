package com.yiwise.asr.transport;

import com.richie.easylog.ILogger;
import com.richie.easylog.LoggerFactory;
import com.yiwise.asr.common.client.log.MdcLog;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.MDC;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.yiwise.asr.common.client.protocol.Constant.HEART_BEAT;

/**
 * <p>客户端连接到服务器端后，会循环执行一个任务：随机等待几秒，然后ping一下Server端，即发送一个心跳包。</p>
 */
public class PingTrigger extends ChannelInboundHandlerAdapter {

    private static ILogger logger = LoggerFactory.getLogger(PingTrigger.class);


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        MDC.put(MdcLog.keyMdc, ctx.channel().attr(NettyAttributeKeys.taskIdAttributeKey).get());

        try {
            super.channelActive(ctx);
            ping(ctx.channel());
        } finally {
            MDC.remove(MdcLog.keyMdc);
        }
    }

    private void ping(Channel channel) {
        AtomicBoolean connectionBroken = new AtomicBoolean(false);

        ScheduledFuture<?> future = channel.eventLoop().schedule(() -> {
            MDC.put(MdcLog.keyMdc, channel.attr(NettyAttributeKeys.taskIdAttributeKey).get());
            try {
                if (channel.isActive()) {
                    logger.verbose("sending heart beat to the server");
                    channel.writeAndFlush(new TextWebSocketFrame(HEART_BEAT));
                } else {
                    String msg = "The connection had broken, cancel the task that will send a heart beat";
                    if (channel.isOpen()) {
                        logger.warn(msg);
                    } else {
                        logger.debug(msg);
                    }
                    connectionBroken.set(true);
                }
            } finally {
                MDC.remove(MdcLog.keyMdc);
            }
        }, 3, TimeUnit.SECONDS);

        future.addListener(nextFuture -> {
            if (nextFuture.isSuccess() && !connectionBroken.get()) {
                ping(channel);
            }
        });
    }
}