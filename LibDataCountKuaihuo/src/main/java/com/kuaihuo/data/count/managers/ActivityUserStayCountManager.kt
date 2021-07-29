package com.kuaihuo.data.count.managers

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.blankj.utilcode.util.FileUtils
import com.chat_hook.HookMethodCallParams
import com.kuaihuo.data.count.AbsModelRunHelper
import com.kuaihuo.data.count.KuaihuoCountManager
import com.kuaihuo.data.count.KuaihuoCountManager.buildNewSaveFile
import com.kuaihuo.data.count.enums.CountTagEnum
import org.json.JSONObject
import java.io.File
import java.util.*

/**
 * 页面的停留统计，就是每个用户在每个页面停留的时间统计
 * 作为管理器需要满足条件：
 *  1、必须有无参构造函数
 *  2、必须继承[AbsModelRunHelper]接口。因为启动方式为扫描
 */
internal class ActivityUserStayCountManager : AbsModelRunHelper() {

    //记录临每个页面的起止时间的标记
    private val temRecordMap: MutableMap<String, Long?> =
        Collections.synchronizedMap(mutableMapOf())

    //界面停留统计的最少时长。低于此时长的不统计。至少需要停留大于此时间(秒)
    private val activityStayMinTime = 2

    override fun startCount() {
        addActivityListener()
        KuaihuoCountManager.print(
            "文件初始化成功：${saveFile.absolutePath},文件是否存在：${
                FileUtils.isFileExists(
                    saveFile.absolutePath
                )
            }"
        )
    }

    override fun getCountTag(): CountTagEnum {
        return CountTagEnum.ACTIVITY_USER_STAY
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
        return null;
    }

    /**
     * 对记录的数据文件进行处理。有的是处理为内存数据有的处理为页面数据
     * @param act 页面
     * @param type 操作类型
     *  0：onActivityCreated
     *  1：onActivityStarted
     *  2：onActivityResumed
     *  3：onActivityPaused
     *  4：onActivityStopped
     *  5：onActivityDestroyed
     */
    private fun buildRecordData(act: Activity, type: Int) {
        if (UserLoginCountManager.getLoginUserId()?.trim()?.isNotEmpty() != true) {
            //检查是否有未上传上一个用户的记录。如果有直接上传
            if(saveFile.length() > 10){
                //存在数据，那么强制归档上传。重置记录文件
                KuaihuoCountManager.print("已经退出登录,上传历史遗留的上一个用户数据.....")
                val file = KuaihuoCountManager.resetBuildNewTemFile(getCountTag(),saveFile)
                if(file != null){
                    //使用新文件作为临时记录文件。将历史遗留数据上传给服务器
                    saveFile = file
                }
            }
            return //没有登录。不进行记录
        }
        try {
            val userId = UserLoginCountManager.getLoginUserId()
            val curTime = System.currentTimeMillis()
            when (type) {
                1 -> { // onStart()
                    if (temRecordMap[act.toString()] == null) {
                        //只记录第一次的时间
                        temRecordMap[act.toString()] = curTime
                    }
                }
                4 -> { //onStop()
                    val stepTime =
                        ((curTime - (temRecordMap[act.toString()] ?: curTime)) / 1000).toInt()
                    //界面停留超过3秒的才做统计。快速切换的不做统计
                    if (stepTime >= activityStayMinTime) {
                        //只有存在时间间隔才具有统计意义,开始写入文件
                        val json = JSONObject()
                        json.put("userId", userId) //用户id
                        json.put("stayActivity", act.javaClass.name) //停留的页面
                        json.put("stayTime", stepTime) //停留的时间(单位秒)
                        writeLog2File(json.toString())
                    }
                    //清空数据
                    temRecordMap[act.toString()] = null
                    temRecordMap.keys.remove(act.toString())
                }
            }
        } catch (e: Exception) {
            KuaihuoCountManager.print("处理界面停留统计异常:$e")
        }
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
                        buildRecordData(activity, 1)
                    }

                    override fun onActivityResumed(activity: Activity) {
                    }

                    override fun onActivityPaused(activity: Activity) {
                    }

                    override fun onActivityStopped(activity: Activity) {
                        buildRecordData(activity, 4)
                    }

                    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                    }

                    override fun onActivityDestroyed(activity: Activity) {
                    }
                })
        }
    }
}