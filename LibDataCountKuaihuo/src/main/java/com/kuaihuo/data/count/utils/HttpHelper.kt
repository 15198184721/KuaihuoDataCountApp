package com.kuaihuo.data.count.utils

import com.google.gson.GsonBuilder
import com.kuaihuo.data.count.KuaihuoCountManager
import com.kuaihuo.data.count.api.BaseUrlInfo
import com.kuaihuo.data.count.api.IDataCountApi
import com.kuaihuo.data.count.api.factory.NullStringToEmptyAdapterFactory
import com.kuaihuo.data.count.api.interceptors.HttpLogger
import com.kuaihuo.data.count.ext.getHttpApi
import com.kuaihuo.data.count.ext.requestMainToIo
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 统计类的http工具类
 */
object HttpHelper {

    //retrofit的请求对象
    private val mRetrofit: Retrofit by lazy {
        initRetrofit()
    }

    //数据统计访问接口
    private val dataCountUrlInteface: IDataCountApi by lazy {
        mRetrofit.create(IDataCountApi::class.java)
    }

    /**
     * 获取网络请求的API。网络请求接口
     * @return IDataCountApi
     */
    fun getHttpApi(): IDataCountApi {
        return dataCountUrlInteface
    }

    /**
     * 上传已经完成的文件
     */
    fun uploadFinalFiles(){
        val list = KuaihuoCountManager.buildUploadRecordFiles()
        if (list.isEmpty()) {
            KuaihuoCountManager.print("没有可上传的文件")
            return
        }
        getHttpApi()
            .uploadRecordFile(list)
            .requestMainToIo {
                KuaihuoCountManager.print("日志信息已上传完成")
            }
    }

    //初始化
    private fun initRetrofit(): Retrofit {
        val logInterceptor = HttpLoggingInterceptor(HttpLogger())
        logInterceptor.level = HttpLoggingInterceptor.Level.BODY
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .registerTypeAdapterFactory(NullStringToEmptyAdapterFactory())
            .create()
        return Retrofit.Builder()
            .baseUrl(BaseUrlInfo.BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .addNetworkInterceptor(logInterceptor)
                    .build()
            )
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}