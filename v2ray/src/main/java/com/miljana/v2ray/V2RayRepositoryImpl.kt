package com.miljana.v2ray

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.miljana.v2ray.core.V2RayVpnService2
import com.miljana.v2ray.utils.AppConfigs
import com.miljana.v2ray.utils.Utils
import libv2ray.Libv2ray

class V2RayRepositoryImpl {

    fun init(context: Context) {
        Utils.copyAssets(context)
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(arg0: Context, arg1: Intent) {
                arg1.extras?.let { bundle ->
                    AppConfigs.V2RAY_STATE =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                            bundle.getSerializable(
                                "STATE",
                                AppConfigs.V2RAY_STATES::class.java
                            )!!
                        else
                            (bundle.getSerializable("STATE") as AppConfigs.V2RAY_STATES?)!!
                }

            }
        }, IntentFilter("CONNECTION_INFO"))
    }

    fun startV2Ray(context: Context) {
        val v2rayConfig = Utils.readTextFromAssets(context, "v2ray_config_ng.json")
        AppConfigs.V2RAY_CONFIG = Utils.parseV2rayJsonFile(v2rayConfig)
        if (AppConfigs.V2RAY_CONFIG == null) {
            return
        }

        val startIntent = Intent(context, V2RayVpnService2::class.java)
        startIntent.putExtra("COMMAND", AppConfigs.V2RAY_SERVICE_COMMANDS.START_SERVICE)
        startIntent.putExtra("V2RAY_CONFIG", AppConfigs.V2RAY_CONFIG)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            context.startForegroundService(startIntent)
        } else {
            context.startService(startIntent)
        }
    }

    fun stopV2Ray(context: Context) {
        val stopIntent = Intent(context, V2RayVpnService2::class.java)
        stopIntent.putExtra("COMMAND", AppConfigs.V2RAY_SERVICE_COMMANDS.STOP_SERVICE)
        context.startService(stopIntent)
        AppConfigs.V2RAY_CONFIG = null
    }

    fun getVersion(): String = Libv2ray.checkVersionX()

    fun getV2RayConfig() = AppConfigs.V2RAY_CONFIG?.V2RAY_FULL_JSON_CONFIG ?: ""

    fun getConnectionMode() = AppConfigs.V2RAY_CONNECTION_MODE

    fun getConnectionState() = AppConfigs.V2RAY_STATE
}