package com.yiwise.asr.demo.handler;

import android.content.Context;

import com.yiwise.asr.AsrClient;
import com.yiwise.asr.AsrClientFactory;
import com.yiwise.asr.AsrRecognizer;
import com.yiwise.asr.AsrRecognizerListener;
import com.yiwise.asr.common.client.protocol.AsrRecognizerResult;
import com.yiwise.asr.demo.utils.PropertiesLoader;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public class AsrHandler {
    private static Logger logger = LoggerFactory.getLogger(AsrHandler.class);

    private String gatewayUrl;
    private String accessKeyId;
    private String accessKeySecret;
    public String audioFileName;
    private Long hotWordId;
    private Float selfLearningRatio;
    private Long selfLearningModelId;
    private boolean enablePunctuation;
    private boolean enableIntermediateResult;
    private boolean enableInverseTextNormalization;

    private AsrRecognizer asrRecognizer;

    public AsrHandler(Context context) {

        try (InputStream stream = context.getResources().getAssets().open("config.properties")) {
            Properties properties = PropertiesLoader.loadProperties(stream);

            gatewayUrl = properties.getProperty("gatewayUrl", "http://127.0.0.1:6060");
            accessKeyId = properties.getProperty("accessKeyId");
            accessKeySecret = properties.getProperty("accessKeySecret");
            audioFileName = properties.getProperty("audioFileName", "test.wav");
            hotWordId = StringUtils.isEmpty(properties.getProperty("hotWordId")) ? null : Long.valueOf(properties.getProperty("hotWordId"));
            selfLearningRatio = StringUtils.isEmpty(properties.getProperty("selfLearningRatio")) ? null : Float.valueOf(properties.getProperty("selfLearningRatio"));
            selfLearningModelId = StringUtils.isEmpty(properties.getProperty("selfLearningModelId")) ? null : Long.valueOf(properties.getProperty("selfLearningModelId"));
            enablePunctuation = Boolean.valueOf(properties.getProperty("enablePunctuation", "false"));
            enableIntermediateResult = Boolean.valueOf(properties.getProperty("enableIntermediateResult", "false"));
            enableInverseTextNormalization = Boolean.valueOf(properties.getProperty("enableInverseTextNormalization", "true"));

            // 初始化客户端
            AsrClientFactory.init(gatewayUrl, accessKeyId, accessKeySecret);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public AsrRecognizer start(ResultViewHandlerInterface resultViewHandler) throws Exception {
        AsrClient asrClient = AsrClientFactory.getAsrClient();

        // 初始化AsrRecognizer，AsrRecognizer需要在每个识别会话中单独创建
        this.asrRecognizer = new AsrRecognizer(asrClient, new AsrRecognizerListener() {

            // 一句话的开始事件（如果一句话过短，可能没有开始事件，所有结果都在onSentenceEnd中返回）
            public void onSentenceBegin(AsrRecognizerResult result) {
                // 请不要再此进行耗时操作，进行耗时操作可能引发一些不可预知问题；
                // 如需进行耗时操作，请另外开辟线程执行
                logger.info("SentenceBegin----" + result.toString());

                if (StringUtils.isNoneEmpty(result.getResultText())) {
                    resultViewHandler.handleResult(result.getResultText(), false);
                }
            }

            // 一句话的中间结果
            public void onSentenceBeginChanged(AsrRecognizerResult result) {
                // 请不要再此进行耗时操作，进行耗时操作可能引发一些不可预知问题；
                // 如需进行耗时操作，请另外开辟线程执行
                logger.info("SentenceChanged--" + result.toString());
                if (StringUtils.isNoneEmpty(result.getResultText())) {
                    resultViewHandler.handleResult(result.getResultText(), false);
                }
            }

            // 一句话的结束事件
            public void onSentenceEnd(AsrRecognizerResult result) {
                // 请不要再此进行耗时操作，进行耗时操作可能引发一些不可预知问题；
                // 如需进行耗时操作，请另外开辟线程执行
                logger.info("SentenceEnd-----" + result.toString());
                if (StringUtils.isNoneEmpty(result.getResultText())) {
                    resultViewHandler.handleResult(result.getResultText(), true);
                }
            }
        });

        // 设置参数
        // 热词id
        asrRecognizer.setHotWordId(hotWordId);
        // 是否打标点
        asrRecognizer.setEnablePunctuation(enablePunctuation);
        // 是否返回中间结果
        asrRecognizer.setEnableIntermediateResult(enableIntermediateResult);
        // 自学习模型
        asrRecognizer.setSelfLearningModelId(selfLearningModelId);
        // 自学习模型比率
        asrRecognizer.setSelfLearningRatio(selfLearningRatio);
        // 是否开启逆文本功能
        asrRecognizer.setEnableInverseTextNormalization(enableInverseTextNormalization);

        // 开启asr识别
        asrRecognizer.startAsr();
        return asrRecognizer;
    }
}
