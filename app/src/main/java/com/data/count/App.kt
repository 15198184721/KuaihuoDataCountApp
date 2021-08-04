package com.data.count

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.kuaihuo.data.count.KuaihuoCountManager

class App : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(base)
    }

    override fun onCreate() {
        super.onCreate()
        KuaihuoCountManager.initCount(this, BuildConfig.DEBUG)
    }

}