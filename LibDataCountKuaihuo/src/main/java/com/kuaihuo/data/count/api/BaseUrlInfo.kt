package com.kuaihuo.data.count.api

import com.kuaihuo.data.count.BuildConfig

/**
 * 基础的一些网络相关的属性信息
 */
object BaseUrlInfo {

    /** 基础地质 */
    val BASE_URL = if(BuildConfig.DEBUG){
        //调试环境
        "http://192.168.0.32:8080/"
    }else{
        //线上服务器
        "http://123.57.255.137:8080/kuaihuoext/"
    }
}