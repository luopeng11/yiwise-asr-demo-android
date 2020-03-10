package com.yiwise.asr;

import com.richie.easylog.ILogger;
import com.richie.easylog.LoggerFactory;
import com.yiwise.asr.transport.Connection;
import com.yiwise.asr.transport.NettyWebSocketClient;

import java.io.IOException;

public class AsrClient {
    private static ILogger logger = LoggerFactory.getLogger(AsrClient.class);

    private NettyWebSocketClient webSocketClient;
    private static final int DEFAULT_CONNECTION_TIMEOUT = 5000;
    private Token token;

    public AsrClient(String gatewayUrl, Token token) throws Exception {
        this.token = token;
        this.webSocketClient = new NettyWebSocketClient(gatewayUrl);
    }

    public Connection connect(AsrRecognizerConnectionListener listener) throws Exception {
        for (int i = 0; true; i++) {
            try {
                return webSocketClient.connect(token.getToken(), listener, DEFAULT_CONNECTION_TIMEOUT);
            } catch (Exception e) {
                if (i == 2) {
                    logger.error("failed to connect to server after 3 tries, error msg is : {}", e.getMessage());
                    throw e;
                }
                Thread.sleep(100);
                logger.warn("failed to connect to server the {} time, error msg is : {}, try again", i, e.getMessage());
            }
        }
    }

    /**
     * 应用最后关闭的时候调用此方法，释放资源
     *
     * @throws IOException 关闭时IO异常
     */
    public void shutdown() throws IOException {
        logger.debug("asr client shutdown");
        webSocketClient.shutdown();
    }

    public Token getToken() {
        return token;
    }
}
