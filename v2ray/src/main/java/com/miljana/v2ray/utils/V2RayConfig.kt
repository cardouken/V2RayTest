package com.miljana.v2ray.utils

import com.miljana.v2ray.core.V2RayConfigData

class V2RayConfig : java.io.Serializable {

    var CONNECTED_V2RAY_SERVER_ADDRESS = ""

    var CONNECTED_V2RAY_SERVER_PORT = ""

    var LOCAL_SOCKS5_PORT = 10808

    var LOCAL_HTTP_PORT = 10809

    var V2RAY_FULL_JSON_CONFIG: String? = null

    //var V2RAY_DATA: V2RayConfigData? = null

    var ENABLE_TRAFFIC_STATICS = false
}