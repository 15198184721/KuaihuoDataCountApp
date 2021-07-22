package com.kuaihuo.data.count.api

import com.kuaihuo.data.count.api.resp.UserCountResp
import io.reactivex.Observable
import retrofit2.http.*

/**
 * 数据接口
 */
interface IDataCountApi : IBaseDataCountApi {

    /******************** model 用户相关的模块 API ********************/

    /**
     * 查询指定id的用户
     * @param userId 用户id
     */
    @GET("appUserCount/findUserId/{userId}")
    fun findUserId(@Path("userId") userId: String): Observable<BaseResp<UserCountResp>>
}