package com.game.live

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var mScreenLive: ScreenLive? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mScreenLive!!.onActivityResult(requestCode, resultCode, data)
    }

    fun startLive(view: View?) {
        mScreenLive = ScreenLive()
        mScreenLive!!.startLive(
            this,
            "rtmp://192.168.0.128/myapp/mystream"
        )
    }

    fun stopLive(view: View?) {
        mScreenLive!!.stoptLive()
    }

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}