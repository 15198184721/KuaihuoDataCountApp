package com.kuaihuo.data.count.api

import com.kuaihuo.data.count.api.req.UserLoginInfoReq
import com.kuaihuo.data.count.api.resp.AppGeneralConfigResp
import com.kuaihuo.data.count.api.resp.UserCountResp
import io.reactivex.Observable
import okhttp3.RequestBody
import okhttp3.ResponseBody
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
    @GET("count/appUserCount/findUserId/{userId}")
    fun findUserId(@Path("userId") userId: String): Observable<BaseResp<UserCountResp>>

    /**
     * 提交用户登录统计信息
     * @param req 用户请求体
     */
    @POST("count/appUserCount/setUserLoginInfo")
    fun setUserLoginInfo(@Body req: UserLoginInfoReq): Observable<BaseResp<Int>>


    /******************** model app的通用配置 API ********************/

    /**
     * 提交用户登录统计信息
     */
    @POST("count/appConfig/getAppConfig")
    fun getAppConfig(): Observable<BaseResp<MutableList<AppGeneralConfigResp>>>


    /******************** model 其他的 API ********************/

    /**
     * 根据自己ip确定大致位置
     */
    @POST("https://pv.sohu.com/cityjson")
    fun getFormIp2Addr(): Observable<ResponseBody>
}