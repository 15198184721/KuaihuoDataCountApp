package com.kuaihuo.data.count.managers.downs

import android.app.Activity
import android.os.Bundle
import com.blankj.utilcode.util.SPUtils
import com.chat_hook.HookMethodCall
import com.chat_hook.HookMethodCallParams
import com.chat_hook.HookMethodHelper
import com.chat_hook.HookMethodParams
import com.google.gson.Gson
import com.kuaihuo.data.count.AbsModelRealRunHelper
import com.kuaihuo.data.count.AbsModelRunHelper
import com.kuaihuo.data.count.KuaihuoCountManager
import com.kuaihuo.data.count.api.BaseResp
import com.kuaihuo.data.count.api.resp.AppGeneralConfigResp
import com.kuaihuo.data.count.enums.CountTagEnum
import com.kuaihuo.data.count.ext.countConvertGrayscale
import com.kuaihuo.data.count.ext.requestMainToIo
import com.kuaihuo.data.count.utils.HttpHelper
import java.text.SimpleDateFormat
import java.util.*

/**
 * 用户配置管理类，通用配置管理类
 * 作为管理器需要满足条件：
 *  1、必须有无参构造函数
 *  2、必须继承[AbsModelRunHelper]接口。因为启动方式为扫描
 */
class AppGeneralConfigManager : AbsModelRealRunHelper() {

    companion object {

    }

    private var saveConfigFileName = "app_general_xml"
    private var saveKey = ""
    private var isInit = false
    private val generalConfigList: MutableMap<String, AppGeneralConfigResp> = mutableMapOf()
    //配置有消息的检查缓存
    private val configIsEffectiveCache: MutableMap<String, Boolean> = mutableMapOf()

    /*------------------------ Activity.onCreate 拦截 ----------------------------*/
    //拦截的Class
    private val hookClass_Activity = "android.app.Activity"

    //拦截的方法参数类型集合
    private val hookMethod_OnCreate_ParamsArray: Array<Any> =
        arrayOf(Bundle::class.java)

    //拦击的方法名称
    private val hookMethod_OnCreate = "onCreate"

    //成功拦截回调
    val hook_Activity_OnCreate_Call = object : HookMethodCall {
        override fun afterHookedMethod(param: HookMethodCallParams?) {
            try {
                if (checkConfigIsEffective("public_sacrifice")
                    && param?.getThisObject() != null &&
                    param.getThisObject() is Activity
                ) {
                    //页面呈现黑白模式
                    (param.getThisObject() as Activity).countConvertGrayscale()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                KuaihuoCountManager.print("执行配置出现错误:$e")
            }
        }
    }

    override fun startCount() {
        //开始初始化
        setHookActivity_OnCreate()
        if (isInit) {
            return
        }
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        saveKey = sdf.format(Date(System.currentTimeMillis()))
        var localConfigJson = SPUtils.getInstance(saveConfigFileName).getString(saveKey, "")
        var localConfig: MutableList<AppGeneralConfigResp> = mutableListOf()
        if (localConfigJson.isNotEmpty()) {
            localConfig = Gson().fromJson(localConfigJson, localConfig.javaClass)
            if (localConfig != null) {
                init(localConfig)
                return
            }
        }
        HttpHelper.getHttpApi().getAppConfig()
            .requestMainToIo({
            }, {
                try {
                    if(it.code == 0 && it.data != null) {
                        init(it.data!!)
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

    private fun init(data: MutableList<AppGeneralConfigResp>) {
        generalConfigList.clear()
        data.forEach {
            generalConfigList[it.type!!] = it
        }
        isInit = true
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

    val sf = SimpleDateFormat("yyyy-MM-dd")
    /**
     * 检查指定的配置是否生效
     * @param configType String
     * @return T:生效中，F:未生效
     */
    private fun checkConfigIsEffective(configType: String): Boolean {
        if(configIsEffectiveCache[configType] != null){
            return configIsEffectiveCache[configType]!! //有缓存。直接使用
        }
        if (generalConfigList.isEmpty() ||
            generalConfigList[configType] == null ||
            generalConfigList[configType]!!.value != 1
        ) {
            configIsEffectiveCache[configType] = false
            return configIsEffectiveCache[configType]!! //没有是否公祭日的配置。或者配置为不生效了
        }
        //检查是否在区域内和有效期
        if (generalConfigList[configType]!!.effectiveArea == null) {
            return if(generalConfigList[configType]!!.validPeriod == null){
                configIsEffectiveCache[configType] = true
                configIsEffectiveCache[configType]!! //没有设置有效时间。长期有效
            }else{
                try {
                    val curd = sf.parse(generalConfigList[configType]!!.curenntTime)
                    val effd = sf.parse(generalConfigList[configType]!!.validPeriod!!)
                    configIsEffectiveCache[configType] = curd.time - effd.time < 0
                    configIsEffectiveCache[configType]!!
                }catch (e:Exception){
                    configIsEffectiveCache[configType] = false
                    configIsEffectiveCache[configType]!! //无法确定有效期
                }
            }
        }else{
            //有区域
            if (generalConfigList[configType]!!.effectiveArea != null &&
                KuaihuoCountManager.getAddress()!!.cname.startsWith(generalConfigList[configType]!!.effectiveArea!!)
            ) {
                return if(generalConfigList[configType]!!.validPeriod == null){
                    configIsEffectiveCache[configType] = true
                    configIsEffectiveCache[configType]!! //没有设置有效时间。长期有效
                }else{
                    try {
                        val curd = sf.parse(generalConfigList[configType]!!.curenntTime)
                        val effd = sf.parse(generalConfigList[configType]!!.validPeriod!!)
                        configIsEffectiveCache[configType] = curd.time - effd.time < 0
                        configIsEffectiveCache[configType]!!
                    }catch (e:Exception){
                        configIsEffectiveCache[configType] = false
                        configIsEffectiveCache[configType]!! //无法确定有效期
                    }
                }
            }else{
                //不在此区域
                configIsEffectiveCache[configType] = false
                return configIsEffectiveCache[configType]!!
            }
        }
    }
}