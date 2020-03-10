package com.yiwise.asr.transport;

import com.fasterxml.jackson.databind.JsonNode;
import com.richie.easylog.ILogger;
import com.richie.easylog.LoggerFactory;
import com.yiwise.asr.AsrRecognizerConnectionListener;
import com.yiwise.asr.common.client.factory.AsyncHttpClientFactory;
import com.yiwise.asr.common.client.utils.JsonUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.io.IOException;
import java.net.URI;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.yiwise.asr.common.client.protocol.Constant.HEADER_REQUEST_ID;
import static com.yiwise.asr.common.client.protocol.Constant.HEADER_TASK_ID;
import static com.yiwise.asr.common.client.protocol.Constant.HEADER_TICKET;
import static com.yiwise.asr.common.client.protocol.Constant.HEADER_TOKEN;

public class NettyWebSocketClient {
    private static ILogger logger = LoggerFactory.getLogger(NettyWebSocketClient.class);

    private URI gatewayURI;
    private SslContext sslCtx;
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(0, new BasicThreadFactory.Builder().namingPattern("AsrWorker-%d").build());
    private OkHttpClient okHttpClient = AsyncHttpClientFactory.getAsyncHttpClient();

    public NettyWebSocketClient(final String gatewayUrl) throws Exception {
        this.gatewayURI = new URI(gatewayUrl);

        if (StringUtils.equals("https", gatewayURI.getScheme())) {
            sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        }
    }

    public Connection connect(String token, AsrRecognizerConnectionListener listener, int connectionTimeout) throws Exception {
        logger.debug("start to get connection");
        String taskId = listener.getTaskId();

        // 获取实际要连接的服务器地址
        AsrRecognizerServerInfo asrServerInfo = getAsrServerInfo(token, taskId);

        URI asrServerURI = new URI(asrServerInfo.getScheme() + "://" + asrServerInfo.getIp() + ":" + asrServerInfo.getPort());

        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        httpHeaders.set(HEADER_TOKEN, token);
        httpHeaders.set(HEADER_TICKET, asrServerInfo.getTicketValue());
        httpHeaders.set(HEADER_TASK_ID, taskId);
        WebSocketClientHandshaker handShaker = WebSocketClientHandshakerFactory.newHandshaker(asrServerURI, WebSocketVersion.V13, null, true, httpHeaders);

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
                .option(ChannelOption.TCP_NODELAY, true)
                .group(eventLoopGroup)
                .handler(new LoggingHandler(LogLevel.INFO))
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel socketChannel) {
                        socketChannel.attr(NettyAttributeKeys.taskIdAttributeKey).set(taskId);

                        ChannelPipeline pipeline = socketChannel.pipeline();
                        if (StringUtils.equals("wss", asrServerInfo.getScheme())) {
                            pipeline.addFirst(sslCtx.newHandler(socketChannel.alloc(), asrServerURI.getHost(), asrServerURI.getPort()));
                        }
                        pipeline.addLast(new HttpClientCodec(), new HttpObjectAggregator(8192));
                        pipeline.addLast(new PingTrigger());
                        pipeline.addLast("hookedHandler", new WebSocketClientHandler(taskId, handShaker, listener));
                    }
                });

        ChannelFuture channelFuture = bootstrap.connect(asrServerURI.getHost(), asrServerURI.getPort()).sync();

        NioSocketChannel channel = (NioSocketChannel) channelFuture.channel();
        logger.debug("WebSocket channel is established after sync, connectionId : {}", channel.id());

        WebSocketClientHandler handler = (WebSocketClientHandler) channel.pipeline().get("hookedHandler");
        handler.handShakeFuture().sync();

        logger.debug("WebSocket connection is established after handshake, connectionId : {}", channel.id());
        return new NettyConnection(channel);
    }

    public void shutdown() throws IOException {
        eventLoopGroup.shutdownGracefully();
    }

    private AsrRecognizerServerInfo getAsrServerInfo(String token, String taskId) throws Exception {
        Request request = new Request.Builder().url(gatewayURI.toString() + "/apiAsrRecognizer/getServerInfo")
                .header(HEADER_TOKEN, token)
                .header(HEADER_REQUEST_ID, taskId)
                .header("Referer", gatewayURI.toString())
                .build();
        Call call = okHttpClient.newCall(request);

        Response response = call.execute();
        if (response.isSuccessful()) {
            String responseBody = response.body().string();

            logger.debug("get asr server info : {}", responseBody);

            JsonNode jsonNode = JsonUtils.string2JsonNode(responseBody);
            AsyncHttpClientFactory.checkResponse(response, jsonNode);

            JsonNode dataNode = jsonNode.get("data");
            String ip = dataNode.get("ip").asText();
            int port = dataNode.get("port").asInt();
            String scheme = dataNode.get("scheme").asText();
            String ticketValue = dataNode.get("ticketValue").asText();
            return new AsrRecognizerServerInfo(scheme, ip, port, ticketValue);
        } else {
            // TODO
            System.out.println("TODO");
            return null;
        }
    }
}
