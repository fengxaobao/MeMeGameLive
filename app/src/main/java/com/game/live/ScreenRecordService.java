package com.game.live;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.Build;
import android.os.HandlerThread;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

public class ScreenRecordService extends Service {
    private ScreenLive screenLive;
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread serviceThread = new HandlerThread("service_thread", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        serviceThread.start();
        this.mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        screenLive = new ScreenLive();
//        screenLive.startLive("rtmp://push-bs.juliweilai.cn/live/999999-999999-1616378931629-5747addd9b2cf687ccd5f3423cabc65e?sign=d410da26246c301c96fcd9374df9dc90&t=60581853");
        screenLive.startLive("rtmp://push-bs.juliweilai.cn/live/999999-999999-1616552461630-02e701d03fc4b49ae87671dd6bab942b?sign=1d1cd2de4f8c2c211956b13221feef61&t=605abe2d");

    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ScreenRecordBinder();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        if(null!=intent){
            int resultCode = intent.getIntExtra("code", -1);
            Intent intentData = intent.getParcelableExtra("data");
            onActivityResult(resultCode, intentData);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private void createNotificationChannel() {
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext()); //????????????Notification?????????
        Intent nfIntent = new Intent(this, MainActivity.class); //???????????????????????????????????????????????????

        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // ??????PendingIntent
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher)) // ??????????????????????????????(?????????)
                //.setContentTitle("SMI InstantView") // ??????????????????????????????
                .setSmallIcon(R.mipmap.ic_launcher) // ??????????????????????????????
                .setContentText("is running......") // ?????????????????????
                .setWhen(System.currentTimeMillis()); // ??????????????????????????????

        /*????????????Android 8.0?????????*/
        //??????notification??????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("notification_id");
        }
        //????????????notification??????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("notification_id", "notification_name", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = builder.build(); // ??????????????????Notification
        notification.defaults = Notification.DEFAULT_SOUND; //????????????????????????
        startForeground(11110, notification);

    }

    public void stopRecord() {
        screenLive.stoptLive();
        Toast.makeText(ScreenRecordService.this, "???????????????????????????", Toast.LENGTH_SHORT).show();
    }


    public void onActivityResult(int resultCode, Intent data) {
        if (null != screenLive && null != data) {
            // ???????????????
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mediaProjection = mediaProjectionManager.getMediaProjection
                        (resultCode, data);
                screenLive.onActivityResult(resultCode, data, mediaProjection);
            }
        }
    }

    public class ScreenRecordBinder extends Binder {
        public ScreenRecordService getScreenRecordService() {
            return ScreenRecordService.this;
        }
    }

}
