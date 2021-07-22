package com.kuaihuo.data.count

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.kuaihuo.data.count.configs.FileConfig
import com.kuaihuo.data.count.configs.FileConfig.Companion.RECORD_FILE_MAX_SIZE
import com.kuaihuo.data.count.enums.CountTagEnum
import com.kuaihuo.data.count.managers.ActivityJumpCountManager
import com.kuaihuo.data.count.utils.HttpHelper
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


/**
 * 快活的统计管理类
 */
@SuppressLint("StaticFieldLeak")
object KuaihuoCountManager {
    private val TAG = "count"

    //model的运行helper工具集合。需要初始化的模块集合
    private val modelRunHelperObjs = mutableListOf<AbsModelRunHelper>()

    //添加需要统计的所有model的工具。必须在此添加到初始化列表。否则无法初始化
    init {
        modelRunHelperObjs.add(ActivityJumpCountManager()) //添加页面跳转统计工具
    }

    //全局上下文
    lateinit var appCcontext: Context

    //路径配置
    val fileConfig: FileConfig = FileConfig()

    /**
     * 初始化方法。启动统计
     * @param context Context
     */
    fun initCount(context: Context) {
        try {
            appCcontext = context.applicationContext
            initFilePath() //初始化文件路径
            //ActivityJumpCountManager.startCount()  //调用页面的调换路径统计

            //启动构造。为最后一步。其他初始化放在此之前。启动方式为：异步
//            runModelManager(scanClzz(appCcontext, runHelperPackage))
            //采用直接调用。反射太慢了
            modelRunHelperObjs.forEach {
                it.startCount()
            }
            modelRunHelperObjs.clear()
        } catch (e: Exception) {
            print("初始化出错数据统计出错:$e")
        }
    }

    /**
     * 生成新的存储文件,如果存在返回原文件。不存在生成新的当前类型的临时存储文件
     * @return String
     */
    fun buildNewSaveFile(typeTag: CountTagEnum): String {
        val dirList = FileUtils.listFilesInDir(fileConfig.cacheFileDirPathTemp)
        for (file in dirList) {
            if (file.absolutePath.startsWith(typeTag.typeName)) {
                //应存在文件
                return file.absolutePath
            }
        }
        val dir = "${fileConfig.cacheFileDirPathTemp}${File.separator}"
        val newFilePaht = "${dir}${typeTag.generateFileName()}"
        if (!FileUtils.isFileExists(dir)) {
            FileUtils.createOrExistsDir(newFilePaht)
        }
        if (!FileUtils.isFileExists(newFilePaht)) {
            FileUtils.createOrExistsFile(newFilePaht)
        }
        return newFilePaht
    }

    /**
     * 输出统计的日志信息
     * @param msg String
     */
    fun print(msg: String) {
        Log.e(TAG, msg)
    }

    /**
     * 构建上传的记录文件参数，将所有文件上传给服务器,会自动去完成的目录查找文件
     * @return MutableList<MultipartBody.Part>
     */
    fun buildUploadRecordFiles(): MutableList<MultipartBody.Part> {
        val fileList = FileUtils.listFilesInDir(fileConfig.getFinalDirPath())
        val partList = mutableListOf<MultipartBody.Part>()
        fileList.forEach {
            val requestBody: RequestBody = RequestBody.create(MediaType.parse("text/log"), it)
            val part = MultipartBody.Part.createFormData("file", it.name, requestBody)
            partList.add(part)
        }
        return partList
    }

    /**
     * 删除记录成功的所有文件
     * @return T：成功，F:失败
     */
    fun deleteFinishFiles(): Boolean {
        return FileUtils.deleteAllInDir(fileConfig.getFinalDirPath())
    }

    /**
     * 将数据写入指定文件
     * @param file 写入的文件
     * @param appendStr 追加的内容
     * @param finishCreateNewTemFileListener
     *        完成的记录，此文件达到完成状态。已被移动到带上传目录。需要一个新的临时文件来记录的回调
     *        作用就是上传一个新的文件（需要创建一个新的临时文件的动作通知）
     * @return T:成功，F:失败
     */
    @Synchronized
    fun writeLog2File(
        file: File,
        appendStr: String,
        finishCreateNewTemFileListener: () -> Unit
    ): Boolean {
        var isInvokFinishListener = false //是否回调
        try {
            if (FileUtils.isDir(file)) {
                return false //是目录
            }
            if (!FileUtils.isFileExists(file)) {
//                file.createNewFile()
                return false //不创建是因为存在可能是上一次失败的文件
            }
            if (appendStr.isEmpty()) {
                return false
            }
            //默认会在内容后面加上换行
            FileIOUtils.writeFileFromString(
                file,
                "${appendStr}${System.getProperty("line.separator")}",
                true
            )
            try {
                if (file.length() >= RECORD_FILE_MAX_SIZE) {
                    val dest =
                        File("${fileConfig.getFinalDirPath()}${FileUtils.getFileName(file)}")
                    FileUtils.moveFile(file, dest)
                    file.delete()
                    isInvokFinishListener = true
                }
            } catch (e: Exception) {
                print("判断文件完成和上传时候出现错误：$e")
            }
            return true
        } catch (e: Exception) {
            return false
        } finally {
            try {
                if (isInvokFinishListener) {
                    //上传已经完成的文件到服务器
                    HttpHelper.uploadFinalFiles()
                    //通知上层重新创建一个临时文件
                    finishCreateNewTemFileListener.invoke()
                }
            } catch (e: Exception) {
                print("执行新建临时文件创建错误：$e")
            }
        }
    }

    /**
     * 文件路径初始化
     */
    private fun initFilePath() {
        //临时统计的缓存目录
        fileConfig.cacheFileDirPathTemp =
            appCcontext.getDir("count_temp", Context.MODE_PRIVATE).absolutePath
        //统计完成的数据保存目录
        fileConfig.cacheFileDirPathFinish =
            appCcontext.getDir("count_finish", Context.MODE_PRIVATE).absolutePath
    }

}