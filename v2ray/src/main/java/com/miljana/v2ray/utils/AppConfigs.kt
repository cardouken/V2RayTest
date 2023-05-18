package com.miljana.v2ray.utils

import com.miljana.v2ray.core.V2RayConfigData

object AppConfigs {

    val BYTE: Long = 1
    val KILO_BYTE = BYTE * 1024
    val MEGA_BYTE = KILO_BYTE * 1024
    val GIGA_BYTE = MEGA_BYTE * 1024

    var V2RAY_CONNECTION_MODE = V2RAY_CONNECTION_MODES.VPN_TUN

    var V2RAY_CONFIG: V2RayConfig? = null

    var V2RAY_STATE = V2RAY_STATES.V2RAY_DISCONNECTED

    var ENABLE_TRAFFIC_AND_SPEED_STATICS = false

    enum class V2RAY_SERVICE_COMMANDS {
        START_SERVICE, STOP_SERVICE, MEASURE_DELAY
    }

    enum class V2RAY_STATES {
        V2RAY_CONNECTED, V2RAY_DISCONNECTED, V2RAY_CONNECTING
    }

    enum class V2RAY_CONNECTION_MODES {
        VPN_TUN, PROXY_ONLY
    }
}