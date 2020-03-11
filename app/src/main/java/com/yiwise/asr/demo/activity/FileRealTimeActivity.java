package com.yiwise.asr.demo.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yiwise.asr.AsrRecognizer;
import com.yiwise.asr.demo.R;
import com.yiwise.asr.demo.handler.AsrHandler;
import com.yiwise.asr.demo.handler.ResultViewHandler;
import com.yiwise.asr.demo.utils.ThreadUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * 使用文件模拟实时语音流DEMO
 */
public class FileRealTimeActivity extends AppCompatActivity {
    private static Logger logger = LoggerFactory.getLogger(FileRealTimeActivity.class);

    private AsrHandler asrHandler;
    private ResultViewHandler resultViewHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filerealtime);

        asrHandler = new AsrHandler(this);
        resultViewHandler = new ResultViewHandler(this);

        ThreadUtils.getInstance().execute(this::process);
    }

    private void process() {
        String audioFileName = asrHandler.audioFileName;
        try (InputStream fileInputStream = getResources().getAssets().open(audioFileName)) {

            // 丢弃wav的头文件
            if (audioFileName.endsWith(".wav")) {
                byte[] bytes = new byte[44];
                fileInputStream.read(bytes);
            }

            AsrRecognizer asrRecognizer = asrHandler.start(resultViewHandler);

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
