package com.anzen.android.examenandroid

import android.app.Application
import com.orhanobut.hawk.Hawk

@Suppress("unused")
class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Hawk.init(this).build()
    }
}