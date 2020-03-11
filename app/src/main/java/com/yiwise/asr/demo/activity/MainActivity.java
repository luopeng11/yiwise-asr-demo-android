package com.yiwise.asr.demo.activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yiwise.asr.AsrClient;
import com.yiwise.asr.AsrClientFactory;
import com.yiwise.asr.AsrRecognizer;
import com.yiwise.asr.AsrRecognizerListener;
import com.yiwise.asr.common.client.protocol.AsrRecognizerResult;
import com.yiwise.asr.demo.utils.PropertiesLoader;
import com.yiwise.asr.demo.utils.ThreadUtils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public class MainActivity extends AppCompatActivity {
    private static Logger logger = LoggerFactory.getLogger(MainActivity.class);

    private TextView textView;

    private String gatewayUrl;
    private String accessKeyId;
    private String accessKeySecret;
    private String audioFileName;
    private Long hotWordId;
    private Float selfLearningRatio;
    private Long selfLearningModelId;
    private boolean enablePunctuation;
    private boolean enableIntermediateResult;
    private boolean enableInverseTextNormalization;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.text);


        try (InputStream stream = getResources().getAssets().open("config.properties")) {
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

            // 启动识别
            ThreadUtils.getInstance().execute(this::process);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void process() {
        try (InputStream fileInputStream = getResources().getAssets().open(audioFileName)) {
            final StringBuilder sb = new StringBuilder();

            AsrClient asrClient = AsrClientFactory.getAsrClient();

            // 丢弃wav的头文件
            if (audioFileName.endsWith(".wav")) {
                byte[] bytes = new byte[44];
                fileInputStream.read(bytes);
            }

            // 初始化AsrRecognizer，AsrRecognizer需要在每个识别会话中单独创建
            AsrRecognizer asrRecognizer = new AsrRecognizer(asrClient, new AsrRecognizerListener() {

                // 一句话的开始事件（如果一句话过短，可能没有开始事件，所有结果都在onSentenceEnd中返回）
                public void onSentenceBegin(AsrRecognizerResult result) {
                    // 请不要再此进行耗时操作，进行耗时操作可能引发一些不可预知问题；
                    // 如需进行耗时操作，请另外开辟线程执行
                    logger.info("SentenceBegin----" + result.toString());
                }

                // 一句话的中间结果
                public void onSentenceBeginChanged(AsrRecognizerResult result) {
                    // 请不要再此进行耗时操作，进行耗时操作可能引发一些不可预知问题；
                    // 如需进行耗时操作，请另外开辟线程执行
                    logger.info("SentenceChanged--" + result.toString());
                }

                // 一句话的结束事件
                public void onSentenceEnd(AsrRecognizerResult result) {
                    // 请不要再此进行耗时操作，进行耗时操作可能引发一些不可预知问题；
                    // 如需进行耗时操作，请另外开辟线程执行
                    logger.info("SentenceEnd-----" + result.toString());
                    sb.append(result.getResultText());

                    // 在UI界面上更新最后结果
                    ThreadUtils.getInstance().runOnUiThread(() -> textView.setText(sb.toString()));
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

            // 发送音频流（模拟真实说话音频速率）
            // demo使用了文件来模拟音频流的发送，真实条件下，根据采样率发送音频数据即可
            // 对于8k pcm 编码数据，建议每发送4800字节 sleep 300 ms
            // 对于16k pcm 编码数据，建议每发送9600字节 sleep 300 ms
            // 在识别的过程中，必须持续发送音频，超过十秒钟没有往服务器发送新的音频数据，服务器会主动断开WebSocket连接，并结束当前识别会话
            asrRecognizer.sendAudio(fileInputStream, 4800, 300);

            // 停止ASR识别（发送停止识别后，最后的识别结果返回可能有一定延迟）
            asrRecognizer.stopAsr();
        } catch (Exception e) {
            logger.error("识别过程出现错误", e);
        }
    }

}
