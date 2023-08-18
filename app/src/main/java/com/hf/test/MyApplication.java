package com.hf.test;

import android.app.Application;
import android.util.Log;

import androidx.multidex.MultiDex;

import com.hf.openplayer.HFOpenMusicPlayer;
import com.hf.playerkernel.manager.HFPlayerApi;
import com.hfopen.sdk.common.HFOpenCallback;
import com.hfopen.sdk.manager.HFOpenApi;
import com.hfopen.sdk.rx.BaseException;
import com.hfopenmusic.sdk.HFOpenMusic;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化MultiDex
        MultiDex.install(this);
    }
}
