package com.hnkj.curtainview

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

/**
 * @author: zhuw
 * Created by zhuwang 2021-06-05-星期六-下午5:57
 * Email zhuwang999@foxmail.com
 */
private const val TAG = "zhuw"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<CurtainView>(R.id.curtain).setOnProgressChangeListener(object :
            CurtainView.OnProgressChangeListener {
            override fun onProgressChanged(seekBar: CurtainView?, progress: Int, isUser: Boolean) {
                Log.d(TAG, "progress = $progress")
            }

            override fun onStartTrackingTouch(seekBar: CurtainView?) {
                Log.d(TAG, "onStartTrackingTouch...")
            }

            override fun onStopTrackingTouch(seekBar: CurtainView?) {
                Log.d(TAG, "onStopTrackingTouch...")
            }
        })
    }

}