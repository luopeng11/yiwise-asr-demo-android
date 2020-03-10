package com.yiwise.asr.transport;

import com.yiwise.asr.common.client.protocol.Constant;
import io.netty.util.AttributeKey;

public class NettyAttributeKeys {
    public static final AttributeKey<String> taskIdAttributeKey = AttributeKey.newInstance(Constant.PROP_TASK_ID);
}
