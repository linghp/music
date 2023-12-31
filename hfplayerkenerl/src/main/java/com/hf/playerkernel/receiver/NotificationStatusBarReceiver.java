package com.hf.playerkernel.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.hf.playerkernel.config.MusicPlayAction;
import com.hf.playerkernel.service.PlayService;
import com.hf.playerkernel.manager.HFPlayerApi;
import com.hf.playerkernel.utils.MusicLogUtils;


/**
 * 操作通知栏的按钮时发送广播，自定义NotificationReceiver,并静态注册，控制音频播放
 */
public class NotificationStatusBarReceiver extends BroadcastReceiver {

    public static final String ACTION_STATUS_BAR = "ACTION_STATUS_BAR";
    public static final String EXTRA = "extra";


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || TextUtils.isEmpty(intent.getAction())) {
            return;
        }
        String extra = intent.getStringExtra(EXTRA);
        if (TextUtils.equals(extra, MusicPlayAction.TYPE_NEXT)) {
            PlayService.startCommand(context, MusicPlayAction.TYPE_NEXT);
            MusicLogUtils.e("NotifiyStatusBarReceiver"+"下一首");
        } else if (TextUtils.equals(extra, MusicPlayAction.TYPE_START_PAUSE)) {
            if(HFPlayerApi.with()!=null){
                boolean playing = HFPlayerApi.with().isPlaying();
                if(playing){
                    MusicLogUtils.e("NotifiyStatusBarReceiver"+"暂停");
                }else {
                    MusicLogUtils.e("NotifiyStatusBarReceiver"+"播放");
                }
                PlayService.startCommand(context, MusicPlayAction.TYPE_START_PAUSE);
            }

        }else if(TextUtils.equals(extra, MusicPlayAction.TYPE_PRE)){
            PlayService.startCommand(context, MusicPlayAction.TYPE_PRE);
            MusicLogUtils.e("NotifiyStatusBarReceiver"+"上一首");
        }
    }
}
