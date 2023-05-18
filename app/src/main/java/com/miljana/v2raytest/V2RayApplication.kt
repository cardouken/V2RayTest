package com.miljana.v2raytest

import android.app.Application
import timber.log.Timber

class V2RayApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
    }
}