package com.kuaihuo.data.count

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.SPUtils
import com.google.gson.Gson
import com.kuaihuo.data.count.beans.IpQueryAddrss
import com.kuaihuo.data.count.configs.FileConfig
import com.kuaihuo.data.count.enums.CountTagEnum
import com.kuaihuo.data.count.ext.requestMainToIo
import com.kuaihuo.data.count.managers.ActivityJumpCountManager
import com.kuaihuo.data.count.managers.ActivityUserStayCountManager
import com.kuaihuo.data.count.managers.UserLoginCountManager
import com.kuaihuo.data.count.managers.downs.AppGeneralConfigManager
import com.kuaihuo.data.count.managers.test.AppTestManager
import com.kuaihuo.data.count.utils.HttpHelper
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.nio.charset.Charset


/**
 * 快活的统计管理类
 */
@SuppressLint("StaticFieldLeak")
object KuaihuoCountManager {
    val TAG = "count"

    //model的运行helper工具集合。需要初始化的模块集合
    private val modelRunHelperObjs = mutableListOf<AbsModelRunHelper>()

    //添加需要统计的所有model的工具。必须在此添加到初始化列表。否则无法初始化
    init {
        modelRunHelperObjs.add(AppTestManager()) //测试工具(主要用于测试各种管理类的处理逻辑)
        modelRunHelperObjs.add(ActivityJumpCountManager()) //添加页面跳转统计工具
        modelRunHelperObjs.add(UserLoginCountManager()) //添加用户登录统计工具
        modelRunHelperObjs.add(ActivityUserStayCountManager()) //添加用户在每个页面停留时间统计工具
        //只是获取数据的管理类
    }

    //全局上下文
    lateinit var appCcontext: Context

    //路径配置
    val fileConfig: FileConfig = FileConfig()

    //本地缓存地址的SP文件
    private var saveAddrssFile: String = "cache_address_file"

    //本地缓存地址在SP文件中的key
    private var saveAddrssKey: String = "cache_arce_address"

    //本地缓存地址在SP文件中最新保存时间的key
    private var saveAddrssTime: String = "cache_arce_time_key"

    //通过IP查询得到的地址信息(大概地址，省市级)
    private var addressInfo: IpQueryAddrss? = null

    /**
     * 初始化方法。启动统计
     * @param context Context
     */
    fun initCount(context: Application) {
        try {
            appCcontext = context
            initFilePath() //初始化文件路径
            //ActivityJumpCountManager.startCount()  //调用页面的调换路径统计

            //启动构造。为最后一步。其他初始化放在此之前。启动方式为：异步
//            runModelManager(scanClzz(appCcontext, runHelperPackage))
            //请求地址信息
            requestIpQueryAddress()
            //采用直接调用。反射太慢了
            modelRunHelperObjs.forEach {
                it.initFile()
                it.startCount()
            }
            modelRunHelperObjs.clear()
        } catch (e: Exception) {
            print("初始化出错数据统计出错:$e")
        }
    }

    /**
     * 依赖地址信息的相关配置。再次此方法中启动
     * @param add 地址信息
     */
    fun setAddress(add: IpQueryAddrss) {
        this.addressInfo = add
        val cache = SPUtils.getInstance(saveAddrssFile).getString(saveAddrssKey, "")
        val saveData = Gson().toJson(add)
        if (cache.trim().isEmpty() || saveData != cache) {
            //保存到本地(只有两次保存不一致才更新)
            SPUtils.getInstance(saveAddrssFile).clear()
            SPUtils.getInstance(saveAddrssFile).put(saveAddrssKey, saveData)
            SPUtils.getInstance(saveAddrssFile).put(saveAddrssTime, System.currentTimeMillis())
        }
        //依赖地址信息的。再次方法中启动
        AppGeneralConfigManager().startCount()
    }

    /**
     * 获取地址信息
     * @return IpQueryAddrss?
     */
    fun getAddress(): IpQueryAddrss? {
        return addressInfo
    }

    /**
     * 输出统计的日志信息
     * @param msg String
     */
    fun print(msg: String) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, msg)
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
     * 强制将现有临时文件归档并重新生成一个临时文件
     * @param typeTag 类型
     * @param file 文件
     * @return File
     */
    fun resetBuildNewTemFile(typeTag: CountTagEnum, file: File): File? {
        return try {
            val dest =
                File("${fileConfig.getFinalDirPath()}${FileUtils.getFileName(file)}")
            FileUtils.moveFile(file, dest)
            file.delete()
            //生成新文件
            File(buildNewSaveFile(typeTag))
        } catch (e: Exception) {
            null
        } finally {
            //上传已经完成的文件到服务器
            HttpHelper.uploadFinalFiles()
        }
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
        typeTag: CountTagEnum,
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
                if (file.length() >= typeTag.fileMaxLenght) {
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

    //设置是否为正在请求地址中
    private var isQueryIp2AddrLoading = false

    /**
     * 请求当前用户的大概位置信息
     */
    @Synchronized
    internal fun requestIpQueryAddress() {
        if (isQueryIp2AddrLoading) {
            return
        }
        val cache = SPUtils.getInstance(saveAddrssFile).getString(saveAddrssKey, "")
        val saveTime = SPUtils.getInstance(saveAddrssFile).getLong(saveAddrssTime, 0L)
        val timeStep = 24 * 60 * 60 * 1000 //缓存间隔必须小于一天(配置的缓存只在一天内有效)
        if (cache.trim().isNotEmpty() &&
            System.currentTimeMillis() - saveTime < timeStep
        ) {
            try {
                //本地存在缓存。直接使用
                setAddress(Gson().fromJson(cache, IpQueryAddrss::class.java))
                isQueryIp2AddrLoading = true
                //然后使用了本地的。在请求一次网络地址。更新一次本地缓存，保存缓存的有效性
                HttpHelper.getHttpApi().getFormIp2Addr()
                    .requestMainToIo({
                    }, {
                        buildIpQueryAddress(String(it.bytes(), Charset.forName("GBK")), true)
                    })
            } catch (e: Exception) {
                SPUtils.getInstance(saveAddrssFile).clear()
            }
            return
        }
        isQueryIp2AddrLoading = true
        HttpHelper.getHttpApi().getFormIp2Addr()
            .requestMainToIo({
                isQueryIp2AddrLoading = false
            }, {
                buildIpQueryAddress(String(it.bytes(), Charset.forName("GBK")))
            })
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

    //json:得到的数据。justSaveCache：只是保存缓存
    private fun buildIpQueryAddress(json: String, justSaveCache: Boolean = false) {
        try {
            if (justSaveCache) {
                val newJson = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1)
                //无论如何。强制更新
                SPUtils.getInstance(saveAddrssFile).clear()
                SPUtils.getInstance(saveAddrssFile).put(saveAddrssKey, newJson)
                SPUtils.getInstance(saveAddrssFile).put(saveAddrssTime, System.currentTimeMillis())
                return
            }
            if (getAddress() != null) {
                return
            }
            val newJson = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1)
            setAddress(Gson().fromJson(newJson, IpQueryAddrss::class.java))
            isQueryIp2AddrLoading = true //设置为请求中。让此请求失效
        } catch (e: Exception) {
            //出错重置，为可请求状态
            isQueryIp2AddrLoading = false
            e.printStackTrace()
        }
    }
}