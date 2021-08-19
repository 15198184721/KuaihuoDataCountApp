package com.kuaihuo.data.count.managers

import android.app.Activity
import android.app.Application
import android.app.Fragment
import android.app.Service
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
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
import java.util.*

/**
 * Fragment的停留统计，就是每个用户在每个Fragment的停留的时间统计
 * 作为管理器需要满足条件：
 *  1、必须有无参构造函数
 *  2、必须继承[AbsModelRunHelper]接口。因为启动方式为扫描
 */
internal class FragmentUserStayCountManager : AbsModelRunHelper() {

    //记录临每个页面的起止时间的标记
    private val temRecordMap: MutableMap<String, Long?> =
        Collections.synchronizedMap(mutableMapOf())

    //Fragment停留统计的最少时长。低于此时长的不统计。至少需要停留大于此时间(秒)
    private val fragmentStayMinTime = 2

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
        return CountTagEnum.FRAGMENT_USER_STAY
    }

    override fun getTemSaveFile(): File {
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
        if (UserLoginCountManager.getLoginUserId()?.trim()?.isNotEmpty() != true) {
            //检查是否有未上传上一个用户的记录。如果有直接上传
            if (saveFile.length() > 10) {
                //存在数据，那么强制归档上传。重置记录文件
                KuaihuoCountManager.print("已经退出登录,上传历史遗留的上一个用户数据.....")
                val file = KuaihuoCountManager.resetBuildNewTemFile(getCountTag(), saveFile)
                if (file != null) {
                    //使用新文件作为临时记录文件。将历史遗留数据上传给服务器
                    saveFile = file
                }
            }
            return null//没有登录。不进行记录
        }
        return null
    }

    //开始统计任务
    private fun startCountHookTask() {
        val clzzName = "androidx.fragment.app.Fragment"
        val clzzNameV4 = "android.support.v4.app.Fragment"
        val methodName_setUserVisibleHint = "setUserVisibleHint"
        val methodName_setUserVisibleHint_params:Array<Any> = arrayOf(Boolean::class.java)
        val setUserVisibleHintCall = object : HookMethodCall {
            override fun afterHookedMethod(param: HookMethodCallParams?) {
                writeLog2File(buildRecordWriteContent(param))
            }
        }
        try {
            HookMethodHelper.addHookMethod(
                HookMethodParams(
                    Class.forName(clzzName), methodName_setUserVisibleHint,
                    methodName_setUserVisibleHint_params, setUserVisibleHintCall
                )
            )
        }catch (e:Exception){
            e.printStackTrace()
            KuaihuoCountManager.print("注册统计${getCountTag()}->$clzzName 出现错误:$e")
        }
        try {
            HookMethodHelper.addHookMethod(
                HookMethodParams(
                    Class.forName(clzzName), methodName_setUserVisibleHint,
                    methodName_setUserVisibleHint_params, setUserVisibleHintCall
                )
            )
        }catch (e:Exception){
            e.printStackTrace()
            KuaihuoCountManager.print("注册统计${getCountTag()}->$clzzNameV4 出现错误:$e")
        }
    }

    /**
     * 对记录的数据文件进行处理。有的是处理为内存数据有的处理为页面数据
     * @param act 页面
     * @param isShow 是否正在显示在前台
     */
    private fun buildRecordData(act: Activity, isShow: Boolean) {

        try {
            val userId = UserLoginCountManager.getLoginUserId()
            val curTime = System.currentTimeMillis()
//            when (type) {
//                1 -> { // onStart()
//                    if (temRecordMap[act.toString()] == null) {
//                        //只记录第一次的时间
//                        temRecordMap[act.toString()] = curTime
//                    }
//                }
//                4 -> { //onStop()
//                    val stepTime =
//                        ((curTime - (temRecordMap[act.toString()] ?: curTime)) / 1000).toInt()
//                    //界面停留超过3秒的才做统计。快速切换的不做统计
//                    if (stepTime >= fragmentStayMinTime) {
//                        //只有存在时间间隔才具有统计意义,开始写入文件
//                        val json = JSONObject()
//                        json.put("userId", userId) //用户id
//                        json.put("stayActivity", act.javaClass.name) //停留的页面
//                        json.put("stayTime", stepTime) //停留的时间(单位秒)
//                        writeLog2File(json.toString())
//                    }
//                    //清空数据
//                    temRecordMap[act.toString()] = null
//                    temRecordMap.keys.remove(act.toString())
//                }
//            }
        } catch (e: Exception) {
            KuaihuoCountManager.print("处理界面停留统计异常:$e")
        }
    }
}