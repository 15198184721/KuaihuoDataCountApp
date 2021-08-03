package com.kuaihuo.data.count.managers

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import com.blankj.utilcode.util.FileUtils
import com.chat_hook.HookMethodCall
import com.chat_hook.HookMethodCallParams
import com.chat_hook.HookMethodHelper
import com.chat_hook.HookMethodParams
import com.google.gson.Gson
import com.kuaihuo.data.count.AbsModelRunHelper
import com.kuaihuo.data.count.KuaihuoCountManager
import com.kuaihuo.data.count.KuaihuoCountManager.buildNewSaveFile
import com.kuaihuo.data.count.api.IDataCountApi
import com.kuaihuo.data.count.beans.IpQueryAddrss
import com.kuaihuo.data.count.enums.CountTagEnum
import com.kuaihuo.data.count.ext.requestMainToIo
import com.kuaihuo.data.count.utils.HttpHelper
import org.json.JSONObject
import java.io.File
import java.nio.charset.Charset

/**
 * 页面的路径跳转统计的模块管理器
 * 作为管理器需要满足条件：
 *  1、必须有无参构造函数
 *  2、必须继承[AbsModelRunHelper]接口。因为启动方式为扫描
 */
internal class ActivityJumpCountManager : AbsModelRunHelper() {

    override fun startCount() {
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
        saveFile = File(buildNewSaveFile(getCountTag()))
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
            if (param.getThisObject() == null) {
                return null
            }
            val thisActivity: String? = if (param.getThisObject() is Application) {
                val am =
                    (param.getThisObject() as? Application)?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                am.getRunningTasks(1)[0].topActivity?.className
            } else {
                param.getThisObject()?.javaClass?.name
            }
            if (thisActivity == null) {
                return null //无法确定跳转源。放弃记录
            }
            var toActivity: String? = null
            param.getArges()?.forEach {
                if (it != null && Intent::class.java.isAssignableFrom(it.javaClass)) {
                    toActivity = (it as Intent).component?.className
                }
            }
            if (toActivity == null) {
                return null
            }
            val json = JSONObject()
            json.put("thisActivity", thisActivity)
            json.put("toActivity", toActivity)
            return json.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            KuaihuoCountManager.print("记录内容构建失败:$e")
            return null
        }
    }

    //开始统计任务
    private fun startCountHookTask() {
        val startActivityCall = object : HookMethodCall {
            override fun afterHookedMethod(param: HookMethodCallParams?) {
                //没打开一次页面。检查一次地址查询是否成功，不成功再次查询，这个主要是为了容错
                KuaihuoCountManager.requestIpQueryAddress()
            }

            override fun beforeHookedMethod(param: HookMethodCallParams?) {
                //拦截在方法调用之前
                writeLog2File(buildRecordWriteContent(param))
            }
        }
        //hook startActivity
        val startActivityMethodName = "startActivity"
        HookMethodHelper.addHookMethod(
            HookMethodParams(
                ContextWrapper::class.java, startActivityMethodName,
                arrayOf(Intent::class.java), startActivityCall
            )
        )
        HookMethodHelper.addHookMethod(
            HookMethodParams(
                ContextWrapper::class.java, startActivityMethodName,
                arrayOf(Intent::class.java, Bundle::class.java), startActivityCall
            )
        )
        //hook startActivityForResult
        val startActivityForResultMethodName = "startActivityForResult"
        //暂时只记录Activity跳转方法引起的页面跳转
        HookMethodHelper.addHookMethod(
            HookMethodParams(
                Activity::class.java, startActivityForResultMethodName,
                arrayOf(Intent::class.java, Int::class.java, Bundle::class.java), startActivityCall
            )
        )
    }
}