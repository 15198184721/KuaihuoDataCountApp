package com.kuaihuo.data.count.api.resp

import java.time.LocalDateTime

/**
 * app的通用配置返回实体
 */
class AppGeneralConfigResp(
    /**
     * 当前的时间(服务器的时间)
     */
    val curenntTime:String? = null,

    /**
     * id
     */
    val id: Long? = null,

    /**
     * 配置类型
     */
    val type: String? = null,

    /**
     * 配置的值,通过这个值判断是否打开了此项的配置
     */
    val value:Int? = null,

    /**
     * 配置生效截止，就是这个配置有效期到什么时候为止
     * null:表示长期有效  生效区
     */
    val validPeriod: String? = null,

    /**
     * 生效区域(省级)，null表示全国范围
     */
    val effectiveArea: String? = null,

    /**
     * 描述信息，就是对这个类型的描述以及值说明
     */
    val msg: String? = null
)