package com.kuaihuo.data.count.managers

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.chat_hook.HookMethodCall
import com.chat_hook.HookMethodCallParams
import com.chat_hook.HookMethodHelper
import com.chat_hook.HookMethodParams
import com.google.gson.Gson
import com.kuaihuo.data.count.AbsModelRealRunHelper
import com.kuaihuo.data.count.AbsModelRunHelper
import com.kuaihuo.data.count.KuaihuoCountManager
import com.kuaihuo.data.count.api.req.UserLoginInfoReq
import com.kuaihuo.data.count.enums.CountTagEnum
import com.kuaihuo.data.count.ext.requestMainToIo
import com.kuaihuo.data.count.utils.HttpHelper
import org.json.JSONObject

/**
 * 用户登录统计管理器,统计用户的登录次数
 * 作为管理器需要满足条件：
 *  1、必须有无参构造函数
 *  2、必须继承[AbsModelRunHelper]接口。因为启动方式为扫描
 */
class UserLoginCountManager : AbsModelRealRunHelper() {
    //登录成功的拦截class
    private val hookUserLoginClass = "com.uni.kuaihuo.di.mvvm.view.MainActivity"
//    private val hookUserLoginClass = "com.data.count.MainActivity"

    //登录成功的方法参数拦截
//    private val hookUserLoginMethodParamsArray =
//        arrayOf("com.data.count.bean.TestABean")
    private val hookUserLoginMethodParamsArray =
        arrayOf("com.uni.kuaihuo.lib.repository.data.user.model.UserInfo")

    //登录成功的拦截方法名称
    private val hookUserLoginMethod = "loginSuccessCount"

    //登录成功的拦截回调
    val loginSuccess = object : HookMethodCall {
        override fun afterHookedMethod(param: HookMethodCallParams?) {
            requestJson(buildRecordWriteContent(param))
        }
    }

    override fun startCount() {
        startLoginSuccessHookTask()
    }

    override fun getCountTag(): CountTagEnum {
        return CountTagEnum.USER_LOGIN
    }

    override fun buildRecordWriteContent(param: HookMethodCallParams?): String? {
        try {
            if (param == null) {
                return null
            }
            return Gson().toJson(param.getArges()?.get(0))
        } catch (e: Exception) {
            e.printStackTrace()
            KuaihuoCountManager.print("构建用户登录统计内容失败:$e")
            return null
        }
    }

    //提交请求
    private fun requestJson(json: String?) {
        try {
            if (json?.isNotEmpty() == true) {
                val req = ""
                val jsonObj = JSONObject(json)
                val name: String? = if (jsonObj.has("nickName")) {
                    jsonObj.getString("nickName")
                } else {
                    null
                }
                val gender: Int? = if (jsonObj.has("userSex")) {
                    jsonObj.getInt("userSex")
                } else {
                    null
                }
                val user = UserLoginInfoReq(
                    jsonObj.getString("id"),
                    name,
                    gender
                )
                HttpHelper.getHttpApi().setUserLoginInfo(user)
                    .requestMainToIo()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //登录成功的任务
    private fun startLoginSuccessHookTask() {
        try {
            //hook loginSuccessCount方法来拦截登录成功
            val czz = Class.forName(hookUserLoginClass)
            val paramsClassArray: Array<Any> = hookUserLoginMethodParamsArray.map {
                Class.forName(it)
            }.toTypedArray()
            HookMethodHelper.addHookMethod(
                HookMethodParams(
                    czz, hookUserLoginMethod,
                    paramsClassArray, loginSuccess
                )
            )
        } catch (e: Exception) {
            KuaihuoCountManager.print("执行用户登录统计失败:$e")
        }
    }
}