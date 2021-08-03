package com.kuaihuo.data.count.managers.test

import android.os.Bundle
import android.view.View
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
import okhttp3.Connection
import okhttp3.OkHttpClient
import retrofit2.http.Url
import java.net.SocketAddress
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.*

/**
 * 测试的管理器。用于测试
 * 作为管理器需要满足条件：
 *  1、必须有无参构造函数
 *  2、必须继承[AbsModelRunHelper]接口。因为启动方式为扫描
 */
class AppTestManager : AbsModelRealRunHelper() {
    /*------------------------ Activity.onCreate 拦截 ----------------------------*/
    //拦截的Class
    private val hookClass = "android.view.View"

    //拦击的方法名称
    private val hookMethod = "setOnClickListener"

    //拦截的方法参数类型集合
    private val hookMethod_ParamsArray: Array<Any> =
        arrayOf(View.OnClickListener::class.java)

    //成功拦截回调
    val hook_Activity_Call = object : HookMethodCall {

        override fun afterHookedMethod(param: HookMethodCallParams?) {
            try {
                KuaihuoCountManager.print("执行了方法：$hookClass.$hookMethod")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun startCount() {
        //开始初始化
//        setHookURL_openConnect()
    }

    override fun getCountTag(): CountTagEnum {
        return CountTagEnum.NONE
    }

    override fun buildRecordWriteContent(param: HookMethodCallParams?): String? {
        return null
    }

    //URL的openConnect()拦截
    private fun setHookURL_openConnect() {
        try {
            val czz = Class.forName(hookClass)
            HookMethodHelper.addHookMethod(
                HookMethodParams(
                    czz, hookMethod,
                    hookMethod_ParamsArray, hook_Activity_Call
                )
            )
        } catch (e: Exception) {
            KuaihuoCountManager.print("执行hook test 方法出错:$e")
        }
    }
}