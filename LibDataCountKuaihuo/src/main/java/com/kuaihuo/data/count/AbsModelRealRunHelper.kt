package com.kuaihuo.data.count

import java.io.File

/**
 * 模块统计管理的运行启动辅助类
 * 注：
 * 此类是用作需要写缓存文件上传方式的类。如果是实时的请继承:
 */
abstract class AbsModelRealRunHelper : AbsModelRunHelper() {

    /**
     * 获取临时保存日志的文件路径
     * @return
     */
    override fun getTemSaveFile(): File?{
         return null
     }

    /**
     * 重置临时保存的文件，现有文件已经被移动到待上传区。需要新建一个临时文件来记录存储日志
     */
    override fun resetTemSaveFile(){}

    override fun initFile(){}

}