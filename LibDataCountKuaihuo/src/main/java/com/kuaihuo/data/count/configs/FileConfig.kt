package com.kuaihuo.data.count.configs

import java.io.File

/**
 * 路径的配置
 */
class FileConfig {
    companion object{
        /** 记录文件单个文件的最大长度，默认为 0.8MB */
        const val RECORD_FILE_MAX_SIZE = (0.0003 * 1024 * 1024).toInt()
    }

    /** 缓存的临时文件存储路径。就是还在记录过程中。还没有完成记录的文件保存的路径 */
    lateinit var cacheFileDirPathTemp:String
    /** 已经记录完成的文件。是可以传给服务器处理的文件目录 */
    lateinit var cacheFileDirPathFinish:String

    /**
     * 获取临时目录路径
     * @return String
     */
    fun getTemDirPath():String{
        return "$cacheFileDirPathTemp${File.separator}"
    }

    /**
     * 获取最终目录路径
     * @return String
     */
    fun getFinalDirPath():String{
        return "$cacheFileDirPathFinish${File.separator}"
    }
}