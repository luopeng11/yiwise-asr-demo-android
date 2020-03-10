package com.yiwise.asr.transport;

import io.netty.channel.ChannelFuture;

public interface Connection {
    void close();

    ChannelFuture sendText(final String payload);

    ChannelFuture sendBinary(byte[] payload);

    String getId();

    boolean isActive();
}
