package com.yiwise.asr.demo.common;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;

import com.yiwise.asr.AsrRecognizer;
import com.yiwise.asr.demo.utils.TimeUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.media.AudioRecord.STATE_UNINITIALIZED;

// 录音并发送给识别的代码，客户可以直接使用
public class RecordTask extends AsyncTask<Void, Integer, Void> {
    private static final Logger logger = LoggerFactory.getLogger(RecordTask.class);

    public final static int SAMPLE_RATE = 8000;
    private final static int SAMPLES_PER_FRAME = 3400;

    private AtomicBoolean sending = new AtomicBoolean();

    private AsrRecognizer asrRecognizer;

    public RecordTask(AsrRecognizer asrRecognizer) {
        this.asrRecognizer = asrRecognizer;
    }

    public void stop() {
        logger.info("停止录音");
        sending.set(false);
        TimeUtils.sleepMilliseconds(200);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        int bufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        AudioRecord mAudioRecorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes * 2);

        if (mAudioRecorder.getState() == STATE_UNINITIALIZED) {
            throw new IllegalStateException("Failed to initialize AudioRecord!");
        }
        mAudioRecorder.startRecording();

        ByteBuffer buf = ByteBuffer.allocateDirect(SAMPLES_PER_FRAME);
        sending.set(true);

        while (sending.get()) {
            try {
                buf.clear();

                // 采集语音并发送
                int readBytes = mAudioRecorder.read(buf, SAMPLES_PER_FRAME);
                if (readBytes > 0 && sending.get()) {
                    byte[] bytes = new byte[SAMPLES_PER_FRAME];
                    buf.get(bytes, 0, SAMPLES_PER_FRAME);

                    // 发送语音数据到识别服务
                    asrRecognizer.sendAudio(bytes);
                }
            } catch (Exception e) {
                logger.error("执行发送音频流程出错", e);
            }
        }

        mAudioRecorder.stop();
        return null;
    }

}