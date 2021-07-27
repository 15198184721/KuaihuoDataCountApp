package com.kuaihuo.data.count.api

import com.kuaihuo.data.count.api.req.UserLoginInfoReq
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

    /**
     * 提交用户登录统计信息
     * @param req 用户请求体
     */
    @POST("appUserCount/setUserLoginInfo")
    fun setUserLoginInfo(@Body req: UserLoginInfoReq): Observable<BaseResp<Int>>
}