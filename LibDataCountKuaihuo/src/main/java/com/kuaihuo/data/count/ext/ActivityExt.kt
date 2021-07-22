package com.kuaihuo.data.count.ext

import android.app.Activity
import com.kuaihuo.data.count.api.IDataCountApi
import com.kuaihuo.data.count.utils.HttpHelper

/**
 * Activity页面获取网络请求接口
 * @receiver Activity
 * @return IDataCountApi
 */
fun Activity.getHttpApi(): IDataCountApi {
    return HttpHelper.getHttpApi()
}