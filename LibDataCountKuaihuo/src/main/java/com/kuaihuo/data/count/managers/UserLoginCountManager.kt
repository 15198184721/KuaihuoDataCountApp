package com.kuaihuo.data.count.managers

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
 * 用户登录统计管理器,统计用户的登录
 * 作为管理器需要满足条件：
 *  1、必须有无参构造函数
 *  2、必须继承[AbsModelRunHelper]接口。因为启动方式为扫描
 */
internal class UserLoginCountManager : AbsModelRealRunHelper() {

    companion object {
        /** 记录登录的用户id，如果为空或者空串表示未登录状态 */
        private var loginUserId: String? = null

        /**
         * 获取登录用户的ID
         * @return null 或者 "" 表示未登录，否则表示已登录
         */
        fun getLoginUserId(): String? {
            return loginUserId
        }
    }

    /*------------------------ 登录成功的拦截 ----------------------------*/
    //登录成功的拦截class
    private val hookUserLoginClass = "com.uni.kuaihuo.lib.repository.data.count.CountInfoUtils"
//    private val hookUserLoginClass = "com.data.count.utisl.CountInfoUtils"

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

    /*------------------------ 退出登录的拦截----------------------------*/
    //登录成功的拦截class
    private val hookUserLoginOutClass = "com.uni.kuaihuo.lib.repository.data.count.CountInfoUtils"
//    private val hookUserLoginOutClass = "com.data.count.utisl.CountInfoUtils"

    //退出登录的拦截方法名称
    private val hookUserLoginOutMethod = "loginOutCount"

    //退出登录的拦截回调
    val loginOut = object : HookMethodCall {
        override fun afterHookedMethod(param: HookMethodCallParams?) {
            exitLoginHookRun()
        }
    }

    override fun startCount() {
        startLoginSuccessHookTask()
        startLoginOutHookTask()
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

    //提交登录成功的请求
    @Synchronized
    private fun requestJson(json: String?) {
        try {
            if (json?.isNotEmpty() == true) {
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
                    gender,
                    loginAddrss = KuaihuoCountManager.getAddress()?.cname
                )
                loginUserId = user.userId
                HttpHelper.getHttpApi().setUserLoginInfo(user)
                    .requestMainToIo()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //退出登录的事件发生了，需要做响应的操作
    @Synchronized
    private fun exitLoginHookRun() {
        loginUserId = null
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

    //退出登录的任务
    private fun startLoginOutHookTask() {
        try {
            //hook loginOutCount方法来拦截登录成功
            val czz = Class.forName(hookUserLoginOutClass)
            HookMethodHelper.addHookMethod(
                HookMethodParams(
                    czz, hookUserLoginOutMethod,
                    null, loginOut
                )
            )
        } catch (e: Exception) {
            KuaihuoCountManager.print("执行用户登录统计失败:$e")
        }
    }
}