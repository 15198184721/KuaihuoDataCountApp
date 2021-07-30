package com.kuaihuo.data.count.api

/**
 * 返回数据的基础实体
 */
class BaseResp<T>(val code: Int, val msg: String?, val data: T?)