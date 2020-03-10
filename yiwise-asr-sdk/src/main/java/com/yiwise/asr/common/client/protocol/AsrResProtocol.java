package com.yiwise.asr.common.client.protocol;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.yiwise.asr.common.client.utils.JsonUtils;

import java.util.HashMap;
import java.util.Map;

public class AsrResProtocol {

    public Map<String, Object> header = new HashMap<>();

    @JsonDeserialize(using = AsrResPayloadSerializer.class)
    public Map<String, Object> payload = new HashMap<>();

    private static final Integer SUCCESS = 200;


    public boolean isSuccess() {
        Integer status = getStatus();
        return SUCCESS.equals(status);
    }

    public int getStatus() {
        return (Integer) header.get(Constant.PROP_STATUS_CODE);
    }

    public String getStatusMsg() {
        return (String) header.get(Constant.PROP_STATUS_MSG);
    }

    public String getTaskId() {
        return (String) header.get(Constant.PROP_TASK_ID);
    }

    public AsrResult getAsrResult() {
        return (AsrResult) payload.get(Constant.PROP_ASR_RESULT);
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
