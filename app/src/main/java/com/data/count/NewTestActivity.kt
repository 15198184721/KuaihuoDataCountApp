package com.data.count

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.data.count.databinding.ActivityMainBinding
import com.kuaihuo.data.count.KuaihuoCountManager
import com.kuaihuo.data.count.enums.CountTagEnum
import com.kuaihuo.data.count.ext.getHttpApi
import com.kuaihuo.data.count.ext.requestMainToIo
import io.reactivex.Single
import java.io.*

class NewTestActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_test)
        uiUpdate()
    }

    private fun uiUpdate(){
        findViewById<View>(R.id.to_main1).setOnClickListener {
            startActivity(Intent(this,MainActivity::class.java))
        }
        findViewById<View>(R.id.to_main2).setOnClickListener {
            startActivity(Intent(this,MainActivity::class.java), Bundle())
        }
        findViewById<View>(R.id.to_main3).setOnClickListener {
            startActivityForResult(Intent(this,MainActivity::class.java),2)
        }
        findViewById<View>(R.id.to_main4).setOnClickListener {
            startActivityForResult(Intent(this,MainActivity::class.java),2,Bundle())
        }
    }

}