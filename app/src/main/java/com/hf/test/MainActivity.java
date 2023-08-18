package com.hf.test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import com.hf.openplayer.HFOpenMusicPlayer;
import com.hf.player.view.HFPlayer;
import com.hf.player.view.HFPlayerViewListener;
import com.hf.player.view.HifivePlayerView;
import com.hf.playerkernel.manager.HFPlayerApi;
import com.hfopen.sdk.common.HFOpenCallback;
import com.hfopen.sdk.entity.MusicList;
import com.hfopen.sdk.entity.MusicRecord;
import com.hfopen.sdk.hInterface.DataResponse;
import com.hfopen.sdk.manager.HFOpenApi;
import com.hfopen.sdk.rx.BaseException;
import com.hfopenmusic.sdk.HFOpenMusic;
import com.hfopenmusic.sdk.listener.HFPlayMusicListener;
import com.tbruyelle.rxpermissions2.RxPermissions;


public class MainActivity extends AppCompatActivity {
    private boolean flag;

    /**
     * 权限组
     */
    private static final String[] permissionsGroup = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET};
    private HifivePlayerView mPlayerView;

    private EditText et_search;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();
        initView();
    }

    @SuppressLint("CheckResult")
    private void requestPermission() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(permissionsGroup)
                .subscribe(aBoolean -> {

                });
    }

    private void initView() {
        et_search = findViewById(R.id.et_search);
        et_search.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String content = et_search.getText().toString().trim();
                searchMusic(content);
            }
            return false;
        });

        int type = getIntent().getIntExtra("type", 3);
        if (type == 1) {
            //            HFPlayer.getInstance().removePlayer();
            HFOpenMusic.getInstance().closeOpenMusic();
            HFPlayer.getInstance().showPlayer(MainActivity.this)
                    .setListener(new HFPlayerViewListener() {
                        @Override
                        public void onFold() {

                        }

                        @Override
                        public void onExpanded() {

                        }

                        @Override
                        public void onClick() {
                            play();
                        }

                        @Override
                        public void onPre() {
                            Toast.makeText(MainActivity.this, "上一首", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPlayPause(boolean isPlaying) {

                            Toast.makeText(MainActivity.this, isPlaying ? "暂停" : "播放", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onNext() {
                            Toast.makeText(MainActivity.this, "下一首", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onComplete() {
                            play();
                            Toast.makeText(MainActivity.this, "播放完成", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError() {
                            Toast.makeText(MainActivity.this, "播放错误", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else if (type == 2) {
            HFOpenMusic.getInstance().closeOpenMusic();
            View iv_music = findViewById(R.id.iv_music);
            iv_music.setVisibility(View.VISIBLE);
            iv_music.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (flag) {
                        HFOpenMusic.getInstance().closeOpenMusic();
                        flag = false;
                    } else {
                        showMusic();
                    }
                }
            });
        } else if (type == 3) {
            //Log.d(ConsData.TAG, "type 3");

            HFOpenApi.registerApp(getApplication(), "300a44d050c942eebeae8765a878b0ee", "0e31fe11b31247fca8", "hifivetest");
            HFOpenApi.configCallBack(new HFOpenCallback() {
                @Override
                public void onError(BaseException exception) {
                    Log.e("MainActivity", "HFOpenApi初始化失败：" + exception.getMsg());
                }

                @Override
                public void onSuccess() {
                    Log.e("MainActivity", "HFOpenApi初始化成功");
                    initOpenPlayer();
                    new Handler(Looper.myLooper()).postDelayed(() -> {
                        HFOpenMusic.getInstance().closeOpenMusic();
                        mPlayerView = HFOpenMusicPlayer.getInstance().showPlayer(MainActivity.this, ConsData.marginTop, ConsData.marginBottom).mPlayerView;
                        registerBroadcast();
                    }, 2000);
                }
            });
        }
    }

    private void initOpenPlayer() {
        Log.d(ConsData.TAG, "musicType:" + ConsData.musicType.toString());

        HFOpenMusicPlayer.getInstance()
                .registerApp(getApplication(), "300a44d050c942eebeae8765a878b0ee", "0e31fe11b31247fca8", "hifivetest")
                .setDebug(true)
                .setMaxBufferSize(ConsData.MaxBufferSize)
                .setUseCache(ConsData.UseCache)
                .setReconnect(ConsData.Reconnect)
                .setNotificationSwitch(true)
                .setListenType(ConsData.musicType.toString())
                .apply();
    }

    private void searchMusic(String content) {
        HFOpenApi.getInstance().searchMusic(null, null, null, null, null, null, null,
                content, null, 1, null, 1, 20, new DataResponse<MusicList>() {
                    @Override
                    public void onError(@NonNull BaseException e) {
                        Toast.makeText(MainActivity.this, "请求失败", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(MusicList musicList, @NonNull String s) {
                        if (musicList.getRecord().size() > 0)
                            HFOpenMusic.getInstance().addCurrentSingle(musicList.getRecord().get(0));
                        else
                            Toast.makeText(MainActivity.this, "没找到相应的歌曲", Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private CustomBroadcastReceiver receiver;

    private void registerBroadcast() {
        // 动态注册广播接收器
        receiver = new CustomBroadcastReceiver();
        IntentFilter filter = new IntentFilter("com.hifiveai.openplayer.MUSIC_CONTROL_BROADCAST");
        registerReceiver(receiver, filter);
    }

    private class CustomBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                if (intent.getAction().equals("com.hifiveai.openplayer.MUSIC_CONTROL_BROADCAST")) {
                    String type = intent.getStringExtra("type");
                    String message = intent.getStringExtra("message");
                    if (type != null) {
                        // 在这里处理接收到的广播消息
                        Toast.makeText(context, "接收到广播: " + type + ": " + message, Toast.LENGTH_SHORT).show();
                        switch (type) {
                            case "start":
                                mPlayerView.startPlay();
                                break;
                            case "pause":
                                new Handler(Looper.myLooper()).postDelayed(() -> mPlayerView.pausePlay(), 1000);
                                new Handler(Looper.myLooper()).postDelayed(() -> mPlayerView.pausePlay(), 3000);
                                break;
                            case "next":
                                mPlayerView.getIvNext().performClick();
                                break;
                            case "prev":
                                mPlayerView.getIvLast().performClick();
                                break;
                            case "content":
                                searchMusic(message);
                                break;
                        }
                    }
                }
            }
        }
    }

    private void play() {
        String cover = "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimage.biaobaiju.com%2Fuploads%2F20190521%2F17%2F1558430156-SBswiePxFE.jpg&refer=http%3A%2F%2Fimage.biaobaiju.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1619162218&t=409c6be07cf495ccc4dcf3bc23f94028";
        String url = "http://img.zhugexuetang.com/lleXB2SNF5UFp1LfNpPI0hsyQjNs";

        //初始化播放器UI
        if (HFPlayerApi.with().isPlaying()) return;
        HFPlayer.getInstance()
                .setMajorVersion(false)
                .playMusic("测试测试测试测试测试测试测试测试", url, cover);

    }

    private void showMusic() {
        flag = true;
        HFOpenMusic.getInstance()
                .setPlayListen(new HFPlayMusicListener() {
                    @Override
                    public void onPlayMusic(MusicRecord musicDetail, String url) {
                        Toast.makeText(MainActivity.this, "歌曲:" + musicDetail.getMusicName(), Toast.LENGTH_SHORT).show();
                        Log.e("HFPlayMusicListener", url);
                    }

                    @Override
                    public void onStop() {
                        HFPlayer.getInstance().stopPlay();
                    }

                    @Override
                    public void onCloseOpenMusic() {
                        HFPlayer.getInstance().setMarginBottom(0);
                        flag = false;
                    }
                })
                .showOpenMusic(MainActivity.this);
    }


//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        HFPlayer.getInstance().destory();
//        HFOpenMusic.getInstance().closeOpenMusic();
//    }

}