package com.kuaihuo.data.count.managers.downs.configs

import com.chat_hook.HookMethodCallParams
import com.kuaihuo.data.count.KuaihuoCountManager
import com.kuaihuo.data.count.api.resp.AppGeneralConfigResp
import java.text.SimpleDateFormat

/**
 * 任务的所有接口。即每个配置的通用接口
 */
abstract class ConfigDetailsTask(
    /**
     * 通用的配置列表。就是服务器的原始配置数据
     */
    val generalConfigList: MutableMap<String, AppGeneralConfigResp>,
    /**
     * 通用的缓存列表，就是处理之后的所有配置缓存在此列表中。方便高效率使用
     */
    val configIsEffectiveCache: MutableMap<String, Boolean>,
) {

    private val sf = SimpleDateFormat("yyyy-MM-dd")

    /**
     * 获取当前配置的类型标识
     * @return 当前类型配置的唯一标识
     */
    abstract fun getConfigTypeKey(): String

    /**
     * 检查昂起类型的配置是否打开了。也就是服务器配置开启。检查是否开启了此项配置
     * @param config 检查当前的配置是否已经打开了
     * @return T:服务器已开启，F:未开启此项配置(服务器设置为不开启状态、没有此项配置等)
     */
    abstract fun checkConfigIsOpen(config: AppGeneralConfigResp): Boolean

    /**
     * 执行配置任务
     * @param hookParams HookMethodCallParams?
     */
    abstract fun runConfigTask(hookParams: HookMethodCallParams?)

    /**
     * 执行配置任务
     */
    open fun runConfigTask(){
        runConfigTask(null)
    }

    /**
     * 检查当前配置的有效性。是否在有效期内。或者这个配置是否生效
     * @return T:生效中，F:不生效中(超期、禁用、未配置等)
     */
    fun checkConfigIsEffective(): Boolean {
        val configType = getConfigTypeKey()
        if (configIsEffectiveCache[configType] != null) {
            return configIsEffectiveCache[configType]!! //有缓存。直接使用
        }
        if (generalConfigList.isEmpty() ||
            generalConfigList[configType] == null ||
            !checkConfigIsOpen(generalConfigList[configType]!!)
        ) {
            //未开启此配置。那么直接设置为未生效
            configIsEffectiveCache[configType] = false
            return configIsEffectiveCache[configType]!! //没有是否公祭日的配置。或者配置为不生效了
        }
        //检查是否在区域内和有效期
        if (generalConfigList[configType]!!.effectiveArea == null ||
            generalConfigList[configType]!!.effectiveArea!!.trim().isEmpty()
        ) {
            return if (generalConfigList[configType]!!.validPeriod?.trim()?.isEmpty() == true) {
                configIsEffectiveCache[configType] = true
                configIsEffectiveCache[configType]!! //没有设置有效时间。长期有效
            } else {
                try {
                    val curd = sf.parse(generalConfigList[configType]!!.curenntTime)
                    val effd = sf.parse(generalConfigList[configType]!!.validPeriod!!)
                    configIsEffectiveCache[configType] = curd.time - effd.time <= 0
                    configIsEffectiveCache[configType]!!
                } catch (e: Exception) {
                    configIsEffectiveCache[configType] = false
                    configIsEffectiveCache[configType]!! //无法确定有效期
                }
            }
        } else {
            //有区域
            if (generalConfigList[configType]!!.effectiveArea?.isNotEmpty() == true &&
                KuaihuoCountManager.getAddress()!!.cname.startsWith(generalConfigList[configType]!!.effectiveArea!!)
            ) {
                return if (generalConfigList[configType]!!.validPeriod?.trim()?.isEmpty() == true) {
                    configIsEffectiveCache[configType] = true
                    configIsEffectiveCache[configType]!! //没有设置有效时间。长期有效
                } else {
                    try {
                        val curd = sf.parse(generalConfigList[configType]!!.curenntTime)
                        val effd = sf.parse(generalConfigList[configType]!!.validPeriod!!)
                        configIsEffectiveCache[configType] = curd.time - effd.time <= 0
                        configIsEffectiveCache[configType]!!
                    } catch (e: Exception) {
                        configIsEffectiveCache[configType] = false
                        configIsEffectiveCache[configType]!! //无法确定有效期
                    }
                }
            } else {
                //不在此区域
                configIsEffectiveCache[configType] = false
                return configIsEffectiveCache[configType]!!
            }
        }
    }
}