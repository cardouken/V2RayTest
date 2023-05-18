package com.miljana.v2ray.interfaces

interface V2RayRepository {

    fun startService()

    fun stopService()

    fun isServiceActive()

    fun isConnected()
}