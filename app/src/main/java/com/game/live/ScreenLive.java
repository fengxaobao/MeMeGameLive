package com.game.live;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Lance
 * @date 2019/5/9
 */
public class ScreenLive implements Runnable {

    private String url;
    private boolean isLiving;
    private LinkedBlockingQueue<RTMPPackage> queue = new LinkedBlockingQueue<>();
    private MediaProjection mediaProjection;

    static {
        System.loadLibrary("native-lib");
    }

    public void startLive(String url) {
        this.url = url;

    }

    public void stoptLive() {
        addPackage(RTMPPackage.EMPTY_PACKAGE);
        isLiving = false;
    }

    public void onActivityResult(int resultCode, Intent data, MediaProjection mediaProjection) {
        this.mediaProjection = mediaProjection;
        // 用户授权
        if (resultCode == Activity.RESULT_OK && null != data) {
            Log.d("TAG", "ScreenLive onActivityResult: onActivityResult");
            LiveTaskManager.getInstance().execute(this);
        }
    }

    public void addPackage(RTMPPackage rtmpPackage) {
        if (!isLiving) {
            return;
        }
        queue.add(rtmpPackage);
    }


    @Override
    public void run() {
        Log.d("TAG", "ScreenLive run: ");

        //1、连接服务器  斗鱼rtmp服务器
        if (!connect(url)) {
            Log.d("TAG", "ScreenLive connect 失败: ");
            return;
        }
        Log.d("TAG", "ScreenLive connect 成功: ");

        isLiving = true;
        VideoCodec videoCodec = new VideoCodec(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            videoCodec.startLive(mediaProjection);
        }
//        AudioCodec audioCodec = new AudioCodec(this);
//        audioCodec.startLive();
        boolean isSend = true;
        while (isLiving && isSend) {
            RTMPPackage rtmpPackage = null;
            try {
                rtmpPackage = queue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (null == rtmpPackage) {
                break;
            }
            if (rtmpPackage.getBuffer() != null && rtmpPackage.getBuffer().length != 0) {
                isSend = sendData(rtmpPackage.getBuffer(), rtmpPackage.getBuffer()
                        .length, rtmpPackage
                        .getType(), rtmpPackage.getTms());
            }
        }
        isLiving = false;
        videoCodec.stopLive();
//        audioCodec.stopLive();
        queue.clear();
        disConnect();
    }

    private native boolean connect(String url);

    private native void disConnect();

    private native boolean sendData(byte[] data, int len, int type, long tms);

}
