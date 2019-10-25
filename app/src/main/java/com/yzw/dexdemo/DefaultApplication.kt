package com.yzw.dexdemo

import android.app.Application
import android.content.Context

/**
 * Create by yinzhengwei on 2019-10-14
 * @Function
 */
class DefaultApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        BugFix.installPatch(base!!, "/sdcard/patch.jar")
    }

}