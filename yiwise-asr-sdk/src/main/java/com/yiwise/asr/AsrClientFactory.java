package com.yiwise.asr;

import com.fasterxml.jackson.databind.JsonNode;
import com.richie.easylog.ILogger;
import com.richie.easylog.LoggerFactory;
import com.yiwise.asr.common.client.factory.AsyncHttpClientFactory;
import com.yiwise.asr.common.client.utils.JsonUtils;

import java.util.concurrent.atomic.AtomicReference;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AsrClientFactory {
    private static ILogger logger = LoggerFactory.getLogger(AsrClientFactory.class);

    private static final String DEFAULT_GATEWAY_ADDRESS = "https://asr-gateway.yiwise.com";
    private final static AtomicReference<AsrClient> asrClient = new AtomicReference<>();
    private static String gatewayUrl;

    private final static AtomicReference<Token> token = new AtomicReference<>(new Token(null, 0));

    private static String accessKeyId;
    private static String accessKeySecret;

    private static OkHttpClient okHttpClient = AsyncHttpClientFactory.getAsyncHttpClient();

    public static void init(String accessKeyId, String accessKeySecret) {
        init(DEFAULT_GATEWAY_ADDRESS, accessKeyId, accessKeySecret);
    }

    public static void init(String gatewayUrl, String accessKeyId, String accessKeySecret) {
        AsrClientFactory.gatewayUrl = gatewayUrl;
        AsrClientFactory.accessKeyId = accessKeyId;
        AsrClientFactory.accessKeySecret = accessKeySecret;
    }

    public static AsrClient getAsrClient() throws Exception {
        AsrClient asrClient = AsrClientFactory.asrClient.get();
        if (asrClient == null || asrClient.getToken().isDue()) {
            asrClient = new AsrClient(getGatewayUrl(), getToken());
            AsrClientFactory.asrClient.set(asrClient);
        }
        return asrClient;
    }

    public static Token getToken() throws Exception {
        Token tmp = AsrClientFactory.token.get();
        if (tmp.isDue()) {
            synchronized (AsrClientFactory.class) {
                if (tmp.isDue()) {
                    doRefreshToken();
                }
            }
        }
        return AsrClientFactory.token.get();
    }

    private static void doRefreshToken() throws Exception {
        FormBody formBody = new FormBody.Builder()
                .add("accessKeyId", accessKeyId)
                .add("accessKeySecret", accessKeySecret)
                .build();

        Request request = new Request.Builder().url(getGatewayUrl() + "/apiToken/getTokenByAccessKey")
                .post(formBody)
                .build();

        Call call = okHttpClient.newCall(request);

        Response response = call.execute();
        if (response.isSuccessful()) {
            String responseBody = response.body().string();
            logger.debug("get token info : {}", responseBody);

            JsonNode jsonNode = JsonUtils.string2JsonNode(responseBody);
            AsyncHttpClientFactory.checkResponse(response, jsonNode);

            JsonNode dataNode = jsonNode.get("data");
            token.set(new Token(dataNode.get("token").asText(), dataNode.get("expireTime").asLong()));
        } else {
            // TODO
            System.out.println("TODO");
        }

    }


    public static String getGatewayUrl() {
        return gatewayUrl;
    }

    public static OkHttpClient getAsyncHttpClient() {
        return okHttpClient;
    }
}
