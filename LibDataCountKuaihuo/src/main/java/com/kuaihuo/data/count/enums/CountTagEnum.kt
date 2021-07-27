package com.kuaihuo.data.count.enums

import java.util.*

/**
 * 统计的标志,就是统计的类型,和后台支持的类型一致
 */
enum class CountTagEnum {
    /** 页面跳转的类型 */
    ACTIVITY_JUMP(
        0,
        "ACTIVITY_JUP",
        (0.005 * 1024 * 1024).toInt() //0.005 = 默认5k(大概跳转50次页面提交一次统计)
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