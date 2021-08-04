package com.kuaihuo.data.count

import com.chat_hook.HookMethodCallParams
import com.kuaihuo.data.count.KuaihuoCountManager.IS_COLLECT_DEBUG_INFO
import com.kuaihuo.data.count.enums.CountTagEnum
import com.kuaihuo.data.count.ext.requestMainToIo
import io.reactivex.Observable
import java.io.File

/**
 * 模块统计管理的运行启动辅助类
 * 注：
 * 此类是用作需要写缓存文件上传方式的类。如果是实时的请继承:[AbsModelRealRunHelper]
 */
abstract class AbsModelRunHelper {

    /** 本次保存的数据保存的恩建路径 */
    protected lateinit var saveFile: File;

    /**
     * 开始启动方法
     *
     * 所有的模块启动管理类。都必须继承此方法
     */
    abstract fun startCount()

    /**
     * 获取当前模块的统计类型
     * @return 统计类型(统计分类)
     */
    abstract fun getCountTag(): CountTagEnum

    /**
     * 获取临时保存日志的文件路径
     * @return
     */
    abstract fun getTemSaveFile(): File?

    /**
     * 重置临时保存的文件，现有文件已经被移动到待上传区。需要新建一个临时文件来记录存储日志
     */
    abstract fun resetTemSaveFile()

    /**
     * 构建需要写入文件的数据
     *
     *  @param param 拦截到的参数信息
     * @return 需要吸入的数据
     */
    abstract fun buildRecordWriteContent(param: HookMethodCallParams?): String?

    /**
     * 检查初始化或者开始初始化，检查初始化条件。满足就初始化不满足就放弃初始化
     * (不满足表示当前不初始化此模块，相当于禁用此功能)
     */
    open fun checkInitStartCount(){
        //检查是否允许收集debug模式信息
        if(IS_COLLECT_DEBUG_INFO){
            startCount()
            return
        }
        if(KuaihuoCountManager.IS_DEBUG){
            return //debug模式不初始化，表示不手机debug模式下的内容
        }
        //满足手机日志的条件
        startCount()
    }

    /**
     * 初始化保存的文件
     */
    open fun initFile() {
        if (!this::saveFile.isInitialized) {
            saveFile = File(
                KuaihuoCountManager.buildNewSaveFile(
                    this.getCountTag()
                )
            )
        }
    }

    /**
     * 写入记录内容到文件中
     * @param content String
     */
    fun writeLog2File(content: String?) {
        if (content == null || content.isEmpty()) {
            return
        }
        Observable.just(getTemSaveFile())
            .doOnNext {
                if (it != null) {
                    KuaihuoCountManager.writeLog2File(getCountTag(),it, content) {
                        resetTemSaveFile()
                    }
                } else {
                    resetTemSaveFile()
                }
            }
            .requestMainToIo(err = {
                KuaihuoCountManager.print("写入文件[${saveFile.absolutePath}]出现错误:$it")
            })
    }
}