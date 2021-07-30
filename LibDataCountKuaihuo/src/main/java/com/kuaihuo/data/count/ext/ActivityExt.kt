package com.kuaihuo.data.count.ext

import android.app.Activity
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.view.View
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

/**
 * 将界面设置为灰色，即应用为黑白模式
 * @receiver Activity
 */
fun Activity.countConvertGrayscale() {
    val paint = Paint()
    val cm = ColorMatrix()
    cm.setSaturation(0f)
    paint.colorFilter = ColorMatrixColorFilter(cm)
    window.decorView.setLayerType(View.LAYER_TYPE_HARDWARE, paint)
}