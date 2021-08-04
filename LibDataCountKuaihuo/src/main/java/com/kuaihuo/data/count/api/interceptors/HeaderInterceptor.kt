package com.kuaihuo.data.count.api.interceptors

import com.blankj.utilcode.util.AppUtils
import com.kuaihuo.data.count.KuaihuoCountManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * 统一处理http的header
 */
class HeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        /* 添加的header  */
        val versionName = AppUtils.getAppVersionName()
        val isDebug = KuaihuoCountManager.IS_DEBUG //是否为debug模式

        val request: Request = chain.request().newBuilder()
            .addHeader("version_name", versionName) //版本号
            .addHeader("debug", "" + isDebug) //是否为debug模式
            .build()
        return chain.proceed(request)
    }
}