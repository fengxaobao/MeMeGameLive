package com.game.live

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.projection.MediaProjection
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
    private var mediaProjectionManager: MediaProjectionManager? = null
    private var mediaProjection: MediaProjection? = null
    private val RECORD_REQUEST_CODE = 101

    private var mScreenLive: ScreenLive? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //权限检查
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
            } else if (grantResults.size != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                connectService()
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RECORD_REQUEST_CODE && resultCode == RESULT_OK) {
            //设置mediaProjection
            if (screenRecordService != null) {
                Log.d("TAG", "onActivityResult: onActivityResult")
                screenRecordService!!.onActivityResult(requestCode, resultCode, data)

            }
        }
    }

    fun startLive(view: View?) {
        if (screenRecordService != null) {
            screenRecordService!!.startRecord(this)

            Toast.makeText(this@MainActivity, "开始录屏", Toast.LENGTH_SHORT).show()
            setToBackground()
        } else if (screenRecordService != null) {
            screenRecordService!!.stopRecord()
        } else if (screenRecordService == null) {
            connectService()
        }
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder: ScreenRecordService.ScreenRecordBinder =
                service as ScreenRecordService.ScreenRecordBinder
            screenRecordService = binder.getScreenRecordService()
            mediaProjectionManager =
                getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            //开启录屏请求intent
            val captureIntent: Intent = mediaProjectionManager!!.createScreenCaptureIntent()
            startActivityForResult(captureIntent, RECORD_REQUEST_CODE)
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

    fun stopLive(view: View?) {
        mScreenLive!!.stoptLive()
    }

    private fun connectService() {
        val intent = Intent(this, ScreenRecordService::class.java)
        bindService(intent, serviceConnection, BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}