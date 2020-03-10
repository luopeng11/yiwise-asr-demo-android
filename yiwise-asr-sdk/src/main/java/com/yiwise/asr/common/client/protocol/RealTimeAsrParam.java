package com.yiwise.asr.common.client.protocol;

public class RealTimeAsrParam extends AsrParam {
    private boolean enableIntermediateResult = false;   // 是否开启中间结果

    public boolean getEnableIntermediateResult() {
        return enableIntermediateResult;
    }

    public void setEnableIntermediateResult(boolean enableIntermediateResult) {
        this.enableIntermediateResult = enableIntermediateResult;
    }
}
