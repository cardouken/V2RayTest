package com.miljana.v2ray.utils

import android.content.Context
import android.util.Log
import com.miljana.v2ray.utils.V2RayConfigConstants.DIR_ASSETS
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

object Utils {

    /**
     * readTextFromAssets
     */
    fun readTextFromAssets(context: Context, fileName: String): String {
        val content = context.assets.open(fileName).bufferedReader().use {
            it.readText()
        }
        return content
    }

    fun userAssetPath(context: Context?): String {
        if (context == null)
            return ""
        val extDir = context.getExternalFilesDir(DIR_ASSETS)
            ?: return context.getDir(DIR_ASSETS, 0).absolutePath
        return extDir.absolutePath
    }

    fun copyAssets(context: Context) {
        val extFolder: String = userAssetPath(context)
        try {
            val geo = arrayOf("geosite.dat", "geoip.dat")
            context.resources.assets.list("")
                ?.filter { geo.contains(it) }
                ?.filter { !File(extFolder, it).exists() }
                ?.forEach {
                    val target = File(extFolder, it)
                    context.resources.assets.open(it).use { input ->
                        FileOutputStream(target).use { output ->
                            input.copyTo(output)
                        }
                    }
                    Timber.i("Copied from apk assets folder to ${target.absolutePath}")
                }
        } catch (e: Exception) {
            Timber.e("asset copy failed", e)
        }
    }

    fun parseV2rayJsonFile(config: String): V2RayConfig? {
        val v2rayConfig = V2RayConfig()
        try {
            var config = config
            val config_json = JSONObject(config)
            try {
                val inbounds = config_json.getJSONArray("inbounds")
                for (i in 0 until inbounds.length()) {
                    try {
                        if (inbounds.getJSONObject(i).getString("protocol") == "socks") {
                            v2rayConfig.LOCAL_SOCKS5_PORT = inbounds.getJSONObject(i).getInt("port")
                        }
                    } catch (e: Exception) {
                        //ignore
                    }
                    try {
                        if (inbounds.getJSONObject(i).getString("protocol") == "http") {
                            v2rayConfig.LOCAL_HTTP_PORT = inbounds.getJSONObject(i).getInt("port")
                        }
                    } catch (e: Exception) {
                        //ignore
                    }
                }
            } catch (e: Exception) {
                Timber.w("startCore warn => can`t find inbound port of socks5 or http.")
                return null
            }
            try {
                v2rayConfig.CONNECTED_V2RAY_SERVER_ADDRESS = config_json.getJSONArray("outbounds")
                    .getJSONObject(0).getJSONObject("settings")
                    .getJSONArray("vnext").getJSONObject(0)
                    .getString("address")
                v2rayConfig.CONNECTED_V2RAY_SERVER_PORT = config_json.getJSONArray("outbounds")
                    .getJSONObject(0).getJSONObject("settings")
                    .getJSONArray("vnext").getJSONObject(0)
                    .getString("port")
            } catch (e: Exception) {
                v2rayConfig.CONNECTED_V2RAY_SERVER_ADDRESS = config_json.getJSONArray("outbounds")
                    .getJSONObject(0).getJSONObject("settings")
                    .getJSONArray("servers").getJSONObject(0)
                    .getString("address")
                v2rayConfig.CONNECTED_V2RAY_SERVER_PORT = config_json.getJSONArray("outbounds")
                    .getJSONObject(0).getJSONObject("settings")
                    .getJSONArray("servers").getJSONObject(0)
                    .getString("port")
            }
            /*try {
                if (config_json.has("policy")) {
                    config_json.remove("policy")
                }
                if (config_json.has("stats")) {
                    config_json.remove("stats")
                }
            } catch (ignore_error: Exception) {
                //ignore
            }
            if (AppConfigs.ENABLE_TRAFFIC_AND_SPEED_STATICS) {
                try {
                    val policy = JSONObject()
                    val levels = JSONObject()
                    levels.put(
                        "8", JSONObject()
                            .put("connIdle", 300)
                            .put("downlinkOnly", 1)
                            .put("handshake", 4)
                            .put("uplinkOnly", 1)
                    )
                    val system = JSONObject()
                        .put("statsOutboundUplink", true)
                        .put("statsOutboundDownlink", true)
                    policy.put("levels", levels)
                    policy.put("system", system)
                    config_json.put("policy", policy)
                    config_json.put("stats", JSONObject())
                    config = config_json.toString()
                    v2rayConfig.ENABLE_TRAFFIC_STATICS = true
                } catch (e: Exception) {
                    //ignore
                }
            }*/
        } catch (e: Exception) {
            Timber.e(
                "parseV2rayJsonFile failed => $e"
            )
            //ignore
            return null
        }
        v2rayConfig.V2RAY_FULL_JSON_CONFIG = config
        //v2rayConfig.V2RAY_DATA = Gson().fromJson(config, V2RayConfigData::class.java)
        return v2rayConfig
    }
}