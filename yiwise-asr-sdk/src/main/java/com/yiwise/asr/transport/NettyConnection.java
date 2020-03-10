package com.yiwise.asr.transport;

import com.richie.easylog.ILogger;
import com.richie.easylog.LoggerFactory;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class NettyConnection implements Connection {

    private static ILogger logger = LoggerFactory.getLogger(NettyConnection.class);
    private Channel channel;

    public NettyConnection(Channel channel) {
        this.channel = channel;
    }

    public String getId() {
        if (channel != null) {
            return channel.id().toString();
        }
        return null;
    }

    public boolean isActive() {
        return channel != null && channel.isActive();
    }

    public void close() {
        channel.close();
    }

    public ChannelFuture sendText(final String payload) {
        if (channel == null || !channel.isActive()) {
            if (channel == null) {
                throw new RuntimeException("the channel is null");
            } else {
                throw new RuntimeException("the channel is inactive");
            }
        }

        logger.debug("thread:{}, send:{}", Thread.currentThread().getId(), payload);
        TextWebSocketFrame frame = new TextWebSocketFrame(payload);
        return channel.writeAndFlush(frame);
    }

    public ChannelFuture sendBinary(byte[] payload) {
        if (channel == null || !channel.isActive()) {
            return null;
        }

        BinaryWebSocketFrame frame = new BinaryWebSocketFrame(Unpooled.wrappedBuffer(payload));
        return channel.writeAndFlush(frame);
    }
}
