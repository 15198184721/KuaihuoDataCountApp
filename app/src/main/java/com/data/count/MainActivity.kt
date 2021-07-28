package com.data.count

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.data.count.bean.TestABean
import com.data.count.utisl.CountInfoUtils
import com.kuaihuo.data.count.KuaihuoCountManager
import com.kuaihuo.data.count.enums.CountTagEnum
import com.kuaihuo.data.count.ext.getHttpApi
import com.kuaihuo.data.count.ext.requestMainToIo
import java.io.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        uiUpdate()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        uiUpdate()
    }

    private fun uiUpdate() {
        findViewById<View>(R.id.test_user_login).setOnClickListener {
            CountInfoUtils.loginSuccessCount(TestABean())
        }
        findViewById<View>(R.id.test_user_login_out).setOnClickListener {
            CountInfoUtils.loginOutCount()
        }
        findViewById<View>(R.id.test_user_login).setOnClickListener {
            CountInfoUtils.loginSuccessCount(TestABean())
        }
        findViewById<View>(R.id.connect).setOnClickListener {
            val list = KuaihuoCountManager.buildUploadRecordFiles()
            if (list.isEmpty()) {
                KuaihuoCountManager.print("没有可上传的文件")
                return@setOnClickListener
            }
            getHttpApi()
                .uploadRecordFile(list)
                .requestMainToIo {
//                    KuaihuoCountManager.deleteFinishFiles()
                    KuaihuoCountManager.print("文件上传成功")
                }
        }
        findViewById<Button>(R.id.testAddFile).text =
            "新增文件(${KuaihuoCountManager.buildUploadRecordFiles().size})"
        findViewById<View>(R.id.testAddFile).setOnClickListener {
        }
        findViewById<View>(R.id.testDelFile).setOnClickListener {
            KuaihuoCountManager.deleteFinishFiles()
            uiUpdate()
        }

        findViewById<View>(R.id.to_test1).setOnClickListener {
            startActivity(Intent(this, NewTestActivity::class.java))
        }
        findViewById<View>(R.id.to_test2).setOnClickListener {
            startActivity(Intent(this, NewTestActivity::class.java), Bundle())
        }
        findViewById<View>(R.id.to_test3).setOnClickListener {
            startActivityForResult(Intent(this, NewTestActivity::class.java), 2)
        }
        findViewById<View>(R.id.to_test4).setOnClickListener {
            startActivityForResult(Intent(this, NewTestActivity::class.java), 2, Bundle())
        }
    }

}