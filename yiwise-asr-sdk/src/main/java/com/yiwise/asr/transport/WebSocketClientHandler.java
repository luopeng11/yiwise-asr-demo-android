package com.yiwise.asr.transport;

import com.richie.easylog.ILogger;
import com.richie.easylog.LoggerFactory;
import com.yiwise.asr.AsrRecognizerConnectionListener;
import com.yiwise.asr.common.client.log.MdcLog;

import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;

import org.slf4j.MDC;

public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {
    private static ILogger logger = LoggerFactory.getLogger(WebSocketClientHandler.class);

    private WebSocketClientHandshaker handShaker;
    private ChannelPromise handShakeFuture;
    private ConnectionListener listener;

    private String taskId;

    public WebSocketClientHandler(String taskId, WebSocketClientHandshaker handShaker, AsrRecognizerConnectionListener listener) {
        this.taskId = taskId;
        this.handShaker = handShaker;
        this.listener = listener;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        MDC.put(MdcLog.keyMdc, taskId);

        try {
            this.handShakeFuture = ctx.newPromise();
            logger.debug("handler added channelId : {}", ctx.channel().id());
        } finally {
            MDC.remove(MdcLog.keyMdc);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        MDC.put(MdcLog.keyMdc, taskId);

        try {
            handShaker.handshake(ctx.channel());
            super.channelActive(ctx);
            logger.debug("channel active channelId : {}, threadId : {}", ctx.channel().id(), Thread.currentThread().getId());
        } finally {
            MDC.remove(MdcLog.keyMdc);
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        MDC.put(MdcLog.keyMdc, taskId);

        try {
            Channel channel = ctx.channel();
            FullHttpResponse response;
            if (!this.handShaker.isHandshakeComplete()) {
                try {
                    response = (FullHttpResponse) msg;
                    // 握手协议返回，设置结束握手
                    this.handShaker.finishHandshake(channel, response);
                    // 设置成功
                    this.handShakeFuture.setSuccess();

                    listener.onOpen();
                } catch (WebSocketHandshakeException e) {
                    FullHttpResponse res = (FullHttpResponse) msg;
                    String errorMsg = String.format("WebSocket Client failed to connect, status : %s, reason : %s", res.status(), res.content().toString(CharsetUtil.UTF_8));
                    this.handShakeFuture.setFailure(new Exception(errorMsg, e));
                }
            } else if (msg instanceof FullHttpResponse) {
                response = (FullHttpResponse) msg;
                throw new IllegalStateException("Unexpected FullHttpResponse (getStatus=" + response.status() + ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
            } else {
                WebSocketFrame frame = (WebSocketFrame) msg;
                if (frame instanceof TextWebSocketFrame) {
                    TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
                    logger.debug("on message : {}", textFrame.text());
                    listener.onMessage(textFrame.text());
                } else if (frame instanceof PingWebSocketFrame) {
                    logger.debug("receive ping frame");
                    channel.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
                } else if (frame instanceof CloseWebSocketFrame) {
                    logger.info("receive close frame");
                    listener.onClose(((CloseWebSocketFrame) frame).statusCode(), ((CloseWebSocketFrame) frame).reasonText());
                    channel.close();
                } else {
                    logger.warn("unknown message type");
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            MDC.remove(MdcLog.keyMdc);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        MDC.put(MdcLog.keyMdc, taskId);

        try {
            if (ctx.channel() != null) {
                logger.debug("channelInactive : " + ctx.channel().id());
            } else {
                logger.debug("channelInactive");
            }
            if (!handShaker.isHandshakeComplete()) {
                String errorMsg;
                if (ctx.channel() != null) {
                    errorMsg = "channel inactive during handshake,connectionId:" + ctx.channel().id();
                } else {
                    errorMsg = "channel inactive during handshake";
                }
                logger.debug(errorMsg);
                handShakeFuture.setFailure(new Exception(errorMsg));
            }
            if (listener != null) {
                listener.onClose(-1, "channelInactive");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            MDC.remove(MdcLog.keyMdc);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        MDC.put(MdcLog.keyMdc, taskId);

        try {
            if (!handShakeFuture.isDone()) {
                handShakeFuture.setFailure(cause);
            }
            listener.onError(cause);
            ctx.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            MDC.remove(MdcLog.keyMdc);
        }
    }

    public ChannelFuture handShakeFuture() {
        return this.handShakeFuture;
    }
}