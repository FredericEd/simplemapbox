package com.fveloz.mapdirections

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class MapApplication: Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var mContext: Context? = null
        fun getAppContext(): Context {
            return mContext!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        mContext = applicationContext
    }
}