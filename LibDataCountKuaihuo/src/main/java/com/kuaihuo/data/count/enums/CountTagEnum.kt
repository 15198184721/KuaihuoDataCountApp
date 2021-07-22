package com.kuaihuo.data.count.enums

import java.util.*

/**
 * 统计的标志,就是统计的类型,和后台支持的类型一致
 */
enum class CountTagEnum {
    ACTIVITY_JUMP(0, "ACTIVITY_JUP") //页面跳转的类型
    ;

    val typeName: String
    val code: Int

    constructor(code: Int, name: String) {
        this.typeName = name
        this.code = code
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