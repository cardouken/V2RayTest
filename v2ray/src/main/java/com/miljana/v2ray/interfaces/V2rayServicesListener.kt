package com.miljana.v2ray.interfaces

import android.app.Service

interface V2rayServicesListener {

    fun onProtect(socket: Int): Boolean

    fun getService(): Service

    fun startService()

    fun stopService()
}