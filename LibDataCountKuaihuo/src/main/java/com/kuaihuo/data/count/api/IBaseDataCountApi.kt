package com.kuaihuo.data.count.api

import io.reactivex.Observable
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Api的一些此处接口，通用的接口
 */
interface IBaseDataCountApi {
    /**
     * 上传本地记录日志到服务器。提交统计日志到服务器处理
     */
    @Multipart
    @POST("count/baseRecordFileController/uploadRecordFile")
    fun uploadRecordFile(@Part files: MutableList<MultipartBody.Part>): Observable<BaseResp<String>>
}