package com.yiwise.asr.demo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yiwise.asr.AsrRecognizer;
import com.yiwise.asr.demo.R;
import com.yiwise.asr.demo.common.RecordTask;
import com.yiwise.asr.demo.handler.AsrHandler;
import com.yiwise.asr.demo.handler.ResultViewHandler;
import com.yiwise.asr.demo.utils.ThreadUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 使用录音方式的实时语音流DEMO
 */
public class RecordRealTimeActivity extends AppCompatActivity {
    private static Logger logger = LoggerFactory.getLogger(RecordRealTimeActivity.class);

    private AsrHandler asrHandler;
    private ResultViewHandler resultViewHandler;
    private volatile int asrRecognizerStatus = 0;            // 0:未开始 1:进行中 2:关闭中
    private AsrRecognizer asrRecognizer;
    private RecordTask recordTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordrealtime);

        asrHandler = new AsrHandler(this);
        resultViewHandler = new ResultViewHandler(this);
    }

    public void onStartAndStop(View view) {
        Button button = (Button) view;

        button.setText(asrRecognizerStatus == 0 ? "开始" : "结束");

        ThreadUtils.getInstance().execute(() -> process(button));
    }

    private void process(Button button) {
        try {
            if (asrRecognizerStatus == 1) {
                changeStatus(button, 2);
                recordTask.stop();
                asrRecognizer.stopAsr();
                changeStatus(button, 0);
            } else {
                // 清除之前的内容
                resultViewHandler.clearContext();

                changeStatus(button, 1);
                asrRecognizer = asrHandler.start(resultViewHandler);

                recordTask = new RecordTask(asrRecognizer);
                recordTask.execute();
            }
        } catch (Exception e) {
            logger.error("识别过程出现错误", e);
        }
    }

    private void changeStatus(Button button, int status) {
        asrRecognizerStatus = status;

        StringBuilder sb = new StringBuilder();
        if (status == 0) {
            sb.append("开始");
        } else if (status == 1) {
            sb.append("结束");
        } else if (status == 2) {
            sb.append("结束中");
        }
        ThreadUtils.getInstance().runOnUiThread(() -> button.setText(sb.toString()));
    }
}
