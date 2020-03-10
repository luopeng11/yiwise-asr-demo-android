package com.yiwise.asr.common.client.protocol;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.richie.easylog.ILogger;
import com.richie.easylog.LoggerFactory;
import com.yiwise.asr.common.client.utils.JsonUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AsrResPayloadSerializer extends JsonDeserializer<Map<String, Object>> {

    private static ILogger logger = LoggerFactory.getLogger(AsrResPayloadSerializer.class);


    @Override
    public Map<String, Object> deserialize(JsonParser jp, DeserializationContext ctx) throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);

        if (node == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> next = fields.next();
            String key = next.getKey();
            JsonNode value = next.getValue();

            switch (key) {
                case Constant.PROP_ASR_RESULT:
                    AsrResult asrResult = JsonUtils.jsonNode2Object(value, AsrResult.class);
                    result.put(Constant.PROP_ASR_RESULT, asrResult);
                    break;
                default:
                    logger.warn("unhandled payload result type, please check sdk version, current version is {}", Constant.SDK_VERSION);
            }
        }

        return result;
    }
}
