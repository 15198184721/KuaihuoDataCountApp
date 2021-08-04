package com.kuaihuo.data.count.managers.downs

import android.os.Bundle
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.SPUtils
import com.chat_hook.HookMethodCall
import com.chat_hook.HookMethodCallParams
import com.chat_hook.HookMethodHelper
import com.chat_hook.HookMethodParams
import com.google.gson.Gson
import com.kuaihuo.data.count.AbsModelRealRunHelper
import com.kuaihuo.data.count.AbsModelRunHelper
import com.kuaihuo.data.count.KuaihuoCountManager
import com.kuaihuo.data.count.api.resp.AppGeneralConfigResp
import com.kuaihuo.data.count.enums.CountTagEnum
import com.kuaihuo.data.count.ext.requestMainToIo
import com.kuaihuo.data.count.managers.downs.configs.ConfigDetailsTask
import com.kuaihuo.data.count.managers.downs.configs.PublicSacrificeConfigDetails
import com.kuaihuo.data.count.utils.HttpHelper
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.*

/**
 * 用户配置管理类，通用配置管理类。用于管理所有的配置信息。注：
 *  此配置类的所有配置在Activity的OnCreate方法中进行统一检查也就是粒度可以控制到页面级
 * 作为管理器需要满足条件：
 *  1、必须有无参构造函数
 *  2、必须继承[AbsModelRunHelper]接口。因为启动方式为扫描
 */
class AppGeneralConfigManager : AbsModelRealRunHelper() {

    private var saveConfigFileName = "app_general_xml"
    private var saveKey = ""
    private var isInit = false
    private val generalConfigList: MutableMap<String, AppGeneralConfigResp> = mutableMapOf()

    //配置有效信息的检查缓存
    private val configIsEffectiveCache: MutableMap<String, Boolean> = mutableMapOf()

    /*------------------------ Activity.onCreate 拦截 ----------------------------*/
    //拦截的Class
    private val hookClass_Activity = "android.app.Activity"

    //拦截的方法参数类型集合
    private val hookMethod_OnCreate_ParamsArray: Array<Any> =
        arrayOf(Bundle::class.java)

    //拦击的方法名称
    private val hookMethod_OnCreate = "onCreate"

    //类型详情的任务集合。各种支持的配置详情处理情况(所有的配置处理模块)
    private val configDiteilsTasks = mutableListOf<ConfigDetailsTask>(
        PublicSacrificeConfigDetails(generalConfigList, configIsEffectiveCache) //公祭日的配置处理任务
    )

    //成功拦截回调
    val hook_Activity_OnCreate_Call = object : HookMethodCall {
        override fun beforeHookedMethod(param: HookMethodCallParams?) {
            //检查是否为启动页。如果是更新一次配置信息(无论成功与否)
            if (param?.getThisObject()?.javaClass?.name == ActivityUtils.getLauncherActivity()) {
                updateAppConfig()
            }
            super.beforeHookedMethod(param)
        }

        override fun afterHookedMethod(param: HookMethodCallParams?) {
            try {
                configDiteilsTasks.forEach {
                    if (it.checkConfigIsEffective()) {
                        it.runConfigTask(param)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                KuaihuoCountManager.print("执行配置出现错误:$e")
            }
        }
    }

    override fun checkInitStartCount() {
        //此类是无论是否为debug都会启用。所以覆盖掉父类的方法判断逻辑
        startCount()
    }

    override fun startCount() {
        //开始初始化
        setHookActivity_OnCreate()
        if (isInit) {
            return
        }
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        saveKey = sdf.format(Date(System.currentTimeMillis()))
        try {
            var localConfigJson = SPUtils.getInstance(saveConfigFileName).getString(saveKey, "")
            var localConfig: MutableList<AppGeneralConfigResp> = mutableListOf()
            if (localConfigJson.isNotEmpty()) {
                localConfig =
                    Gson().fromJson(localConfigJson, Array<AppGeneralConfigResp>::class.java)
                        .toMutableList()
                if (localConfig.isNotEmpty()) {
                    init(localConfig, true)
                    return
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        HttpHelper.getHttpApi().getAppConfig()
            .requestMainToIo({
            }, {
                val s:OkHttpClient
                try {
                    if (it.code == 0 && it.data != null) {
                        init(it.data)
                        //保存到本地
                        SPUtils.getInstance(saveConfigFileName).clear()
                        SPUtils.getInstance(saveConfigFileName).put(saveKey, Gson().toJson(it.data))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            })
    }

    override fun getCountTag(): CountTagEnum {
        return CountTagEnum.NONE
    }

    override fun buildRecordWriteContent(param: HookMethodCallParams?): String? {
        return null
    }

    //data:配置数据,isLocalCache:是否来自于本地缓存
    private fun init(data: MutableList<AppGeneralConfigResp>, isLocalCache: Boolean = false) {
        generalConfigList.clear()
        data.forEach {
            generalConfigList[it.type!!] = it
        }
        //情况缓存的配置信息。让所有配置重新检查一次
        configIsEffectiveCache.clear()
        isInit = true
        if (isLocalCache) {
            updateAppConfig()
        }
    }

    //强制更新app的配置信息
    private fun updateAppConfig() {
        //本地的。强制重新更新一次配置信息
        HttpHelper.getHttpApi().getAppConfig()
            .requestMainToIo({
            }, {
                try {
                    if (it.code == 0 && it.data != null) {
                        //重新配置一次最新数据
                        init(it.data)
                        SPUtils.getInstance(saveConfigFileName).clear()
                        SPUtils.getInstance(saveConfigFileName).put(saveKey, Gson().toJson(it.data))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            })
    }

    //onCreate拦截
    private fun setHookActivity_OnCreate() {
        try {
            //hook loginSuccessCount方法来拦截登录成功
            val czz = Class.forName(hookClass_Activity)
            HookMethodHelper.addHookMethod(
                HookMethodParams(
                    czz, hookMethod_OnCreate,
                    hookMethod_OnCreate_ParamsArray, hook_Activity_OnCreate_Call
                )
            )
        } catch (e: Exception) {
            KuaihuoCountManager.print("执行用户登录统计失败:$e")
        }
    }
}