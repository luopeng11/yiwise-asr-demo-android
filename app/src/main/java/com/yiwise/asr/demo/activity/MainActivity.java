package com.yiwise.asr.demo.activity;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yiwise.asr.AsrClient;
import com.yiwise.asr.AsrClientFactory;
import com.yiwise.asr.AsrRecognizer;
import com.yiwise.asr.AsrRecognizerListener;
import com.yiwise.asr.common.client.protocol.AsrRecognizerResult;
import com.yiwise.asr.demo.ThreadUtils;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {


    private TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.text);

        String gatewayUrl = "https://asr-gateway.yiwise.com";
        String accessKeyId = "DEMO867JHSKHFSK";
        String accessKeySecret = "DlFSDjlsf87SDJFO";

        AsrClientFactory.init(gatewayUrl, accessKeyId, accessKeySecret);

        ThreadUtils.getInstance().execute(() -> {
            process();
        });
    }

    private void process() {
        String audioFileName = "01.wav";
        AssetManager assetManager = this.getAssets();
        try {
            final StringBuilder sb = new StringBuilder();

            AsrClient asrClient = AsrClientFactory.getAsrClient();

            InputStream fileInputStream = assetManager.open("01.wav");

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
                    System.out.println("SentenceBegin----" + result.toString());
                }

                // 一句话的中间结果
                public void onSentenceBeginChanged(AsrRecognizerResult result) {
                    // 请不要再此进行耗时操作，进行耗时操作可能引发一些不可预知问题；
                    // 如需进行耗时操作，请另外开辟线程执行
                    System.out.println("SentenceChanged--" + result.toString());
                }

                // 一句话的结束事件
                public void onSentenceEnd(AsrRecognizerResult result) {
                    // 请不要再此进行耗时操作，进行耗时操作可能引发一些不可预知问题；
                    // 如需进行耗时操作，请另外开辟线程执行
                    System.out.println("SentenceEnd-----" + result.toString());
                    sb.append(result.getResultText());

                    ThreadUtils.getInstance().runOnUiThread(() -> {
                        textView.setText(sb.toString());
                    });
                }
            });


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
            e.printStackTrace();
        }
    }

}
