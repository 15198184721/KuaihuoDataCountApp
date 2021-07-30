package com.kuaihuo.data.count.enums

import java.util.*

/**
 * 统计的标志,就是统计的类型,和后台支持的类型一致
 */
enum class CountTagEnum {
    /** 无统计类型。表示是一个非统计的类型。可能是一个配置或者其他不具备上传功能的hook模块，只有获取功能的模块 */
    NONE(
        -1,
        "NONE"
    ),
    /** 页面跳转的类型 */
    ACTIVITY_JUMP(
        0,
        "ACTIVITY_JUP",
        (0.005 * 1024 * 1024).toInt() //0.005 = 默认5k(大概跳转50次页面提交一次统计)
    ),
    /** 用户页面停留的统计,统计每个用户在页面的停留时间,小于最小停留时间(2秒)不统计 */
    ACTIVITY_USER_STAY(
        1,
        "ACTIVITY_USER_STAY",
        (0.003 * 1024 * 1024).toInt() //0.005 = 默认3k(大约30-50条记录提交一次)
    ),
    /** 用户登录统计(无文件长度,实时上报) */
    USER_LOGIN(
        0,
        "USER_LOGIN"
    )
    ;

    val typeName: String
    val code: Int

    /** 此分类的文件最大允许的长度,如果为0，表示实时上报 */
    val fileMaxLenght: Int

    constructor(code: Int, name: String, fileMaxLenght: Int = 0) {
        this.typeName = name
        this.code = code
        this.fileMaxLenght = fileMaxLenght
    }

    /**
     * 生成当前类型新的文件名
     * 规则：类型 + id + 当前时间 + uuid
     * @return String
     */
    fun generateFileName(): String {
        return "${name}_${code}_${System.currentTimeMillis()}_${UUID.randomUUID()}.log"
    }
}