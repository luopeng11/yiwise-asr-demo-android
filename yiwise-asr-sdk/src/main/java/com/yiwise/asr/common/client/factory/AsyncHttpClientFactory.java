package com.yiwise.asr.common.client.factory;

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Response;

public class AsyncHttpClientFactory {
    private static OkHttpClient asyncHttpClient = new OkHttpClient.Builder()
            .readTimeout(3, TimeUnit.SECONDS)       // 设置读取超时时间
            .writeTimeout(3, TimeUnit.SECONDS)      // 设置写的超时时间
            .connectTimeout(3, TimeUnit.SECONDS)    // 设置连接超时时间
            .build();

    public static OkHttpClient getAsyncHttpClient() {
        return asyncHttpClient;
    }

    public static void checkResponse(Response response, JsonNode jsonNode) {
        if (!StringUtils.equals("true", response.header("apiSuccess"))) {
            checkResponse(jsonNode);
        }
    }

    public static void checkResponse(Response response) {
        int statusCode = response.code();
        if (200 != statusCode) {
            throw new RuntimeException("调用后端服务错误，code=" + response.code() + "，ResponseBody=" + response.body());
        }
    }

    public static void checkResponse(JsonNode jsonNode) {
        if (!Objects.equals(200, jsonNode.get("code").asInt())) {
            String resultMsg = jsonNode.get("resultMsg").asText();
            String errorStackTrace = jsonNode.get("errorStackTrace").asText();
            String message = StringUtils.isEmpty(errorStackTrace) ? resultMsg : errorStackTrace;
            throw new RuntimeException(message);
        }
    }
}
