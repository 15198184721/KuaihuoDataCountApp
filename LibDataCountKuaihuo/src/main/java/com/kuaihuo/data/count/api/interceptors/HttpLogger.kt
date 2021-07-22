package com.kuaihuo.data.count.api.interceptors

import android.util.Log
import okhttp3.logging.HttpLoggingInterceptor

/**
 * http 日志打印
 * @property Tag String
 */
class HttpLogger : HttpLoggingInterceptor.Logger {
    val Tag = "http"

    override fun log(message: String) {
        Log.v(Tag, message)
    }
}