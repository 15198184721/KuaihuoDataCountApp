package com.kuaihuo.data.count.managers.downs.configs

import android.app.Activity
import com.chat_hook.HookMethodCallParams
import com.kuaihuo.data.count.KuaihuoCountManager
import com.kuaihuo.data.count.api.resp.AppGeneralConfigResp
import com.kuaihuo.data.count.ext.countConvertGrayscale
import java.text.SimpleDateFormat

/**
 * 公祭日的配置详情处理,即可专门处理公祭日的配置相关
 */
class PublicSacrificeConfigDetails(
    generalConfigList: MutableMap<String, AppGeneralConfigResp>,
    configIsEffectiveCache: MutableMap<String, Boolean>,
) : ConfigDetailsTask(generalConfigList, configIsEffectiveCache) {

    //是否为第一次允许的标志
    private var isInitRun = false

    override fun getConfigTypeKey(): String {
        return "public_sacrifice";
    }

    /**
     * 检查是否开启了公祭日的配置
     * @param config
     *  value: 0:未开启，1：已开启
     * @return T:已开启
     */
    override fun checkConfigIsOpen(config:AppGeneralConfigResp): Boolean {
            return config.value == 1
    }

    override fun runConfigTask(hookParams: HookMethodCallParams?) {
        if (hookParams?.getThisObject() != null &&
            hookParams.getThisObject() is Activity
        ) {
            //页面呈现黑白模式
            (hookParams.getThisObject() as Activity).countConvertGrayscale()
        }
    }
}