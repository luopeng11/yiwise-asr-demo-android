package com.yiwise.asr.demo;

import android.app.Application;
import android.content.Context;

import com.richie.easylog.LoggerConfig;
import com.richie.easylog.LoggerFactory;


public class App extends Application {
    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;

        // Debug 模式才开启日志
        LoggerFactory.init(new LoggerConfig.Builder().context(this)
                .logcatEnabled(true)
                .logFileEnabled(true)
                .build());
    }
}
