package com.game.live

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {
    private var screenRecordService: ScreenRecordService? = null
    private val RECORD_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkMyPermission()
    }

    private fun checkMyPermission() {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) !== PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.RECORD_AUDIO
            ) !== PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO
                ),
                1101
            )
        } else {
            connectService()
            Toast.makeText(this@MainActivity, "开始录屏", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        if (requestCode == 1101) {
            if (grantResults.size != 0 && (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED)
            ) {
                Toast.makeText(this@MainActivity, "请设置必须的应用权限，否则将会导致运行异常！", Toast.LENGTH_SHORT)
                    .show()
            } else if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                if (null == screenRecordService) {
                    connectService()
                } else {


                }
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RECORD_REQUEST_CODE && resultCode == RESULT_OK) {
            //设置mediaProjection

            Log.d("TAG", "onActivityResult: onActivityResult")
            val service = Intent(this@MainActivity, ScreenRecordService::class.java)
            service.putExtra("code", resultCode)
            service.putExtra("data", data)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this@MainActivity.startForegroundService(service)
                Toast.makeText(this@MainActivity, "开始录屏", Toast.LENGTH_SHORT).show()
                setToBackground()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun startLive(view: View?) {
        //权限检查
        var mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        // 创建截屏请求intent
        val captureIntent: Intent = mediaProjectionManager.createScreenCaptureIntent()
        // 投屏管理器
        // 投屏管理器
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.startActivityForResult(captureIntent, 100)
        }
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder: ScreenRecordService.ScreenRecordBinder =
                service as ScreenRecordService.ScreenRecordBinder
            screenRecordService = binder.getScreenRecordService()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Toast.makeText(this@MainActivity, "录屏服务断开！", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setToBackground() {
        val home = Intent(Intent.ACTION_MAIN)
        home.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        home.addCategory(Intent.CATEGORY_HOME)
        startActivity(home)
    }


    private fun connectService() {
        val intent = Intent(this, ScreenRecordService::class.java)
        bindService(intent, serviceConnection, BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
        screenRecordService!!.stopRecord()
        //关闭服务
        //关闭服务
        val service = Intent(this, ScreenRecordService::class.java)
        stopService(service)
    }
    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }
}