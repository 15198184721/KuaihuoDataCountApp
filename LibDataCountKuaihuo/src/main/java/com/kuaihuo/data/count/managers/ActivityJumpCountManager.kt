package com.kuaihuo.data.count.managers

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.blankj.utilcode.util.FileUtils
import com.chat_hook.HookMethodCall
import com.chat_hook.HookMethodCallParams
import com.chat_hook.HookMethodHelper
import com.chat_hook.HookMethodParams
import com.kuaihuo.data.count.AbsModelRunHelper
import com.kuaihuo.data.count.KuaihuoCountManager
import com.kuaihuo.data.count.KuaihuoCountManager.buildNewSaveFile
import com.kuaihuo.data.count.enums.CountTagEnum
import org.json.JSONObject
import java.io.File

/**
 * 页面的路径跳转统计的模块管理器
 * 作为管理器需要满足条件：
 *  1、必须有无参构造函数
 *  2、必须继承[AbsModelRunHelper]接口。因为启动方式为扫描
 */
class ActivityJumpCountManager : AbsModelRunHelper() {

    override fun startCount() {
        addActivityListener()
        startCountHookTask()
        KuaihuoCountManager.print(
            "文件初始化成功：${saveFile.absolutePath},文件是否存在：${
                FileUtils.isFileExists(
                    saveFile.absolutePath
                )
            }"
        )
    }

    override fun getCountTag(): CountTagEnum {
        return CountTagEnum.ACTIVITY_JUMP
    }

    override fun getTemSaveFile(): File? {
        return saveFile
    }

    override fun resetTemSaveFile() {
        if (saveFile.exists()) {
            //文件存在。删除此文件
            saveFile.delete()
        }
        saveFile = File(buildNewSaveFile(CountTagEnum.ACTIVITY_JUMP))
        KuaihuoCountManager.print(
            "文件已重置：${saveFile.absolutePath},文件是否存在：${
                FileUtils.isFileExists(
                    saveFile.absolutePath
                )
            }"
        )
    }

    override fun buildRecordWriteContent(param: HookMethodCallParams?): String? {
        try {
            if (param == null) {
                return null
            }
            var thisActivity: String? = param.getThisObject()?.javaClass?.name
            var toActivity: String? = null
            param.getArges()?.forEach {
                if (it != null && Intent::class.java.isAssignableFrom(it.javaClass)) {
                    toActivity = (it as Intent).component?.className
                }
            }
            if (thisActivity == null || toActivity == null) {
                return null
            }
            val json = JSONObject()
            json.put("thisActivity", thisActivity)
            json.put("toActivity", toActivity)
            return json.toString()
        }catch (e:Exception){
            e.printStackTrace()
            KuaihuoCountManager.print("记录内容构建失败:$e")
            return null
        }
    }

    //开始统计任务
    private fun startCountHookTask() {
        val startActivity = object : HookMethodCall {
            override fun afterHookedMethod(param: HookMethodCallParams?) {
                writeLog2File(buildRecordWriteContent(param))
            }
        }
        //hook startActivity
//        val startActivity = "startActivity"
//        HookMethodHelper.addHookMethod(
//            HookMethodParams(
//                ContextWrapper::class.java, startActivity,
//                arrayOf(Intent::class.java), call
//            )
//        )
//        HookMethodHelper.addHookMethod(
//            HookMethodParams(
//                ContextWrapper::class.java, startActivity,
//                arrayOf(Intent::class.java, Bundle::class.java), call
//            )
//        )
        //hook startActivityForResult
        val startActivityForResult = "startActivityForResult"
        //暂时只记录Activity跳转方法引起的页面跳转
        HookMethodHelper.addHookMethod(
            HookMethodParams(
                Activity::class.java, startActivityForResult,
                arrayOf(Intent::class.java, Int::class.java, Bundle::class.java), startActivity
            )
        )
//        HookMethodHelper.addHookMethod(
//            HookMethodParams(
//                ContextWrapper::class.java, startActivityForResult,
//                arrayOf(Intent::class.java, Int::class.java), call
//            )
//        )
//        HookMethodHelper.addHookMethod(
//            HookMethodParams(
//                ContextWrapper::class.java, startActivityForResult,
//                arrayOf(Intent::class.java, Int::class.java, Bundle::class.java), call
//            )
//        )
    }

    //添加页面生命周期监听
    private fun addActivityListener() {
        if (KuaihuoCountManager.appCcontext is Application) {
            (KuaihuoCountManager.appCcontext as Application).registerActivityLifecycleCallbacks(
                object :
                    Application.ActivityLifecycleCallbacks {
                    override fun onActivityCreated(
                        activity: Activity,
                        savedInstanceState: Bundle?
                    ) {
                    }

                    override fun onActivityStarted(activity: Activity) {
                    }

                    override fun onActivityResumed(activity: Activity) {
                    }

                    override fun onActivityPaused(activity: Activity) {
                    }

                    override fun onActivityStopped(activity: Activity) {
                    }

                    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                    }

                    override fun onActivityDestroyed(activity: Activity) {
                    }
                })
        }
    }
}