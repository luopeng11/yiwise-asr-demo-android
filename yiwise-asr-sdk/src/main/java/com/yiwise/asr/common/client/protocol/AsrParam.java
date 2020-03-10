package com.yiwise.asr.common.client.protocol;

public class AsrParam {
    private Long hotWordId;                             // 热词id

    private Long selfLearningModelId;                   // 自学习模型id
    private Float selfLearningRatio;                    // 自学习模型比率

    private boolean enablePunctuation = false;          // 是否打标点
    private boolean enableInverseTextNormalization = false;     // 开启逆文本规整

    public AsrParam() {
    }

    public Long getHotWordId() {
        return hotWordId;
    }

    public void setHotWordId(Long hotWordId) {
        this.hotWordId = hotWordId;
    }

    public Long getSelfLearningModelId() {
        return selfLearningModelId;
    }

    public void setSelfLearningModelId(Long selfLearningModelId) {
        this.selfLearningModelId = selfLearningModelId;
    }

    public Float getSelfLearningRatio() {
        return selfLearningRatio;
    }

    public void setSelfLearningRatio(Float selfLearningRatio) {
        this.selfLearningRatio = selfLearningRatio;
    }

    public boolean getEnablePunctuation() {
        return enablePunctuation;
    }

    public void setEnablePunctuation(boolean enablePunctuation) {
        this.enablePunctuation = enablePunctuation;
    }

    public boolean getEnableInverseTextNormalization() {
        return enableInverseTextNormalization;
    }

    public void setEnableInverseTextNormalization(boolean enableInverseTextNormalization) {
        this.enableInverseTextNormalization = enableInverseTextNormalization;
    }
}
