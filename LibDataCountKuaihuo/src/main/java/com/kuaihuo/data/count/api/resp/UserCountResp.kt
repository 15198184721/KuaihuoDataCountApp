package com.kuaihuo.data.count.api.resp

/**
 * 用户查询实体。用户信息实体
 * @constructor
 */
class UserCountResp(
    //用户id
    val userId: String,
    //名称
    val name: String?,
    //性别 0:女  1:男
    val gender: Int = 0,
    //创建时间
    val createTime: String?,
    //创建时间
    val updateTime: String?,
    //登录次数
    val loginCount: Int = 1
)