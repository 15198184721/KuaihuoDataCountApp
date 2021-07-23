package com.kuaihuo.data.count.configs

import java.io.File

/**
 * 路径的配置
 */
class FileConfig {
    companion object{
        /** 记录完成的日志。待上传日志的整体允许的最大限制:默认 2MB */
        const val RECORD_WAIT_UPLOAD_FILE_MAX_SIZE = (2 * 1024 * 1024)
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