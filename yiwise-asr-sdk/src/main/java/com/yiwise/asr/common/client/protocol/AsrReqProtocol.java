package com.yiwise.asr.common.client.protocol;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.yiwise.asr.common.client.utils.IdGenerator;
import com.yiwise.asr.common.client.utils.JsonUtils;

import java.util.HashMap;
import java.util.Map;

public class AsrReqProtocol {
    public Map<String, String> header = new HashMap<>();

    @JsonDeserialize(using = AsrReqPayloadSerializer.class)
    public Map<String, Object> payload = new HashMap<>();

    public AsrReqProtocol() {
    }

    public AsrReqProtocol(String taskId) {
        header.put(Constant.PROP_TASK_ID, taskId);
        header.put(Constant.PROP_MESSAGE_ID, IdGenerator.genId());
        header.put(Constant.SDK_VERSION, Constant.VALUE_VERSION);
    }

    public String serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("header", header);
        if (payload != null && payload.size() > 0) {
            result.put("payload", payload);
        }
        return JsonUtils.object2String(result);
    }
}
