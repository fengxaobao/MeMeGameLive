package com.game.live;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.HandlerThread;
import android.os.IBinder;
import android.widget.Toast;

public class ScreenRecordService extends Service {
    private ScreenLive screenLive;

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread serviceThread = new HandlerThread("service_thread", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        serviceThread.start();
        screenLive = new ScreenLive();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ScreenRecordBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public boolean startRecord(MainActivity mainActivity) {
        screenLive.startLive(mainActivity, "rtmp://push-bs.juliweilai.cn/live/999999-999999-1616155209011-242b916e0bc62c20eddb5a61939eec69?sign=3e3519391c82b926170ff752ec02ea0f&t=6054ae69");
        return true;
    }
    public boolean stopRecord() {
        screenLive.stoptLive();
        Toast.makeText(ScreenRecordService.this, "录屏完成，已保存。", Toast.LENGTH_SHORT).show();
        return true;
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (null != screenLive) {
            screenLive.onActivityResult(requestCode, resultCode, data);
        }

    }

    public class ScreenRecordBinder extends Binder {
        public ScreenRecordService getScreenRecordService() {
            return ScreenRecordService.this;
        }
    }

}
