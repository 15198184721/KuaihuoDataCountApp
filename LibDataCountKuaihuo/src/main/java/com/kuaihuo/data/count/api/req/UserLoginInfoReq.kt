package com.kuaihuo.data.count.api.req

/**
 * 提交用户登录信息统计
 */
class UserLoginInfoReq(
    /** 用户id */
    val userId:String,
    /** 名称 */
    val name:String?,
    /** 0:女，1：男 */
    val gender:Int? = null
)
