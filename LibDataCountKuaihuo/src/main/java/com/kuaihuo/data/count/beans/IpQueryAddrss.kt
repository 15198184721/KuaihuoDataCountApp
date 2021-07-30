package com.kuaihuo.data.count.beans

/**
 * 根据IP查询得到的大概地址信息
 */
class IpQueryAddrss(
    //自己的ip
    val cip:String,
    //自己的邮编
    val cid:String,
    //城市名称(省市级)
    val cname:String,
)