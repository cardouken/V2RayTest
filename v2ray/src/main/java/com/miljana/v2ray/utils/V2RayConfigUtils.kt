package com.miljana.v2ray.utils

import android.content.Context
import android.text.TextUtils
import com.google.gson.Gson
import com.miljana.v2ray.core.V2RayConfigData

object V2RayConfigUtils {

    fun getV2rayNonCustomConfig(context: Context): V2RayConfigData? {
        val assets = Utils.readTextFromAssets(context, "v2ray_config_ng.json")
        if (TextUtils.isEmpty(assets)) {
            return null
        }

        return Gson().fromJson(assets, V2RayConfigData::class.java) ?: null
    }

    fun getV2rayNonCustomConfigJson(context: Context): String? {
        val assets = Utils.readTextFromAssets(context, "v2ray_config_ng.json")
        if (TextUtils.isEmpty(assets)) {
            return null
        }

        val configuration = Gson().fromJson(assets, V2RayConfigData::class.java) ?: null

        return Gson().toJson(configuration)
    }
}