package com.miljana.v2ray.core

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.miljana.v2ray.R
import com.miljana.v2ray.interfaces.V2rayServicesListener
import com.miljana.v2ray.utils.AppConfigs
import com.miljana.v2ray.utils.Utils
import com.miljana.v2ray.utils.V2RayConfig
import libv2ray.Libv2ray
import libv2ray.V2RayPoint
import libv2ray.V2RayVPNServiceSupportsSet
import timber.log.Timber
import java.util.*

class V2RayCoreManager {

    companion object {
        val instance = V2RayCoreManager()
        var v2rayServicesListener: V2rayServicesListener? = null
    }

    private var V2RAY_STATE: AppConfigs.V2RAY_STATES = AppConfigs.V2RAY_STATES.V2RAY_DISCONNECTED

    private var isLibV2rayCoreInitialized = false

    private var mNotificationManager: NotificationManager? = null

    val v2rayPoint: V2RayPoint = Libv2ray.newV2RayPoint(
        V2RayVPNServiceCallback(),
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1
    )

    private class V2RayVPNServiceCallback : V2RayVPNServiceSupportsSet {
        override fun onEmitStatus(p0: Long, p1: String?): Long {
            return 0
        }

        override fun prepare(): Long {
            return 0
        }

        override fun protect(p0: Long): Boolean {
            v2rayServicesListener?.let {
                return it.onProtect(p0.toInt())
            } ?: return true
        }

        override fun setup(p0: String?): Long {
            v2rayServicesListener?.let {
                try {
                    it.startService()
                } catch (e: Exception) {
                    Timber.e("setup failed => $e")
                    return -1
                }
            }

            return 0
        }

        override fun shutdown(): Long {
            if (v2rayServicesListener == null) {
                Timber.e("shutdown failed => can`t find initial service.")
                return -1
            }

            return try {
                v2rayServicesListener?.stopService()
                v2rayServicesListener = null
                0
            } catch (e: Exception) {
                Timber.e("shutdown failed => $e")
                -1
            }
        }

    }

    fun setUpServicesListener(targetService: Service) {
        try {
            v2rayServicesListener = targetService as V2rayServicesListener
            Libv2ray.initV2Env(Utils.userAssetPath(targetService.applicationContext))
            isLibV2rayCoreInitialized = true
            Timber.e(
                "setUpListener => new initialize from " + v2rayServicesListener?.getService()?.javaClass?.simpleName
            )

        } catch (e: Exception) {
            Timber.e("setUpListener failed => $e")
            isLibV2rayCoreInitialized = false
        }
    }

    fun startCore(v2rayConfig: V2RayConfig): Boolean {
        V2RAY_STATE = AppConfigs.V2RAY_STATES.V2RAY_CONNECTING
        if (!isLibV2rayCoreInitialized) {
            Timber.e("startCore failed => LibV2rayCore should be initialize before start.")
            return false
        }

        if (isV2RayCoreRunning()) {
            stopCore()
        }

        try {
            Libv2ray.testConfig(v2rayConfig.V2RAY_FULL_JSON_CONFIG)
        } catch (e: Exception) {
            // send broadcast disconnect event
            sendDisconnectedBroadCast()
            Timber.e("startCore failed => v2ray json config not valid.")
            return false
        }

        try {
            v2rayPoint.configureFileContent = v2rayConfig.V2RAY_FULL_JSON_CONFIG
            v2rayPoint.domainName = v2rayConfig.CONNECTED_V2RAY_SERVER_ADDRESS + ":" + v2rayConfig.CONNECTED_V2RAY_SERVER_PORT

            Timber.e("startCore => ${v2rayPoint.configureFileContent}")
            Timber.e("startCore domainName => ${v2rayPoint.domainName}")

            v2rayPoint.runLoop(false)
            V2RAY_STATE = AppConfigs.V2RAY_STATES.V2RAY_CONNECTED

            if (isV2RayCoreRunning()) {
                // show notification
                showNotification(v2rayConfig)
            }
        } catch (e: Exception) {
            Timber.e("startCore failed => $e")
            V2RAY_STATE = AppConfigs.V2RAY_STATES.V2RAY_DISCONNECTED
            return false
        }

        return true
    }

    fun stopCore() {
        try {
            if (isV2RayCoreRunning()) {
                v2rayPoint.stopLoop()
                // stop service
                v2rayServicesListener?.stopService()
                Timber.e("stopCore success => v2ray core stopped.")
            } else {
                Timber.e("stopCore failed => v2ray core not running.")
            }

            // send broadcast disconnect event
            sendDisconnectedBroadCast()
        } catch (e: Exception) {
            Timber.e("stopCore failed => $e")
        }
    }

    private fun sendDisconnectedBroadCast() {
        V2RAY_STATE = AppConfigs.V2RAY_STATES.V2RAY_DISCONNECTED

        val connection_info_intent = Intent("CONNECTION_INFO")
        connection_info_intent.putExtra(
            "STATE",
            V2RAY_STATE
        )
        try {
            v2rayServicesListener?.getService()?.applicationContext?.sendBroadcast(
                connection_info_intent
            )
        } catch (e: Exception) {
            //ignore
            Timber.e("sendDisconnectedBroadCast failed => $e")
        }
    }

    fun isV2RayCoreRunning(): Boolean = v2rayPoint.isRunning


    //
    //    private fun getNotificationManager(): NotificationManager? {
    //        if (mNotificationManager == null) {
    //            val service = serviceControl?.get()?.getService() ?: return null
    //            mNotificationManager =
    //                    service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    //        }
    //        return mNotificationManager
    //    }
    private fun getNotificationManager(): NotificationManager? {
        if (mNotificationManager == null) {
            try {
                mNotificationManager = v2rayServicesListener!!.getService()
                    .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            } catch (e: java.lang.Exception) {
                return null
            }
        }
        return mNotificationManager
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createNotificationChannelID(applicationName: String): String {
        val notification_channel_id = "DEV7_DEV_V_E_CH_ID"
        val notificationChannel = NotificationChannel(
            notification_channel_id,
            "$applicationName Background Service",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationChannel.lightColor = Color.BLUE
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        notificationChannel.importance = NotificationManager.IMPORTANCE_NONE
        getNotificationManager()?.createNotificationChannel(notificationChannel)
        return notification_channel_id
    }

    private fun judgeForNotificationFlag(): Int {
        return PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    }

    private fun showNotification(v2rayConfig: V2RayConfig) {
        if (v2rayServicesListener == null) {
            return
        }
        val launchIntent =
            v2rayServicesListener!!.getService().packageManager.getLaunchIntentForPackage(
                v2rayServicesListener!!.getService().applicationInfo.packageName
            )
        launchIntent!!.action = "FROM_DISCONNECT_BTN"
        launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val notificationContentPendingIntent = PendingIntent.getActivity(
            v2rayServicesListener!!.getService(), 0, launchIntent, judgeForNotificationFlag()
        )
        var notificationChannelID: String? = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannelID = createNotificationChannelID("V2RayTest")
        }
        val mBuilder = NotificationCompat.Builder(
            v2rayServicesListener!!.getService(),
            notificationChannelID!!
        )
        mBuilder.setSmallIcon(androidx.core.R.drawable.notification_template_icon_bg)
            .setContentTitle("Connected To V2RayTest")
            .setContentText("tap to open application")
            .setOngoing(true)
            .setShowWhen(false)
            .setOnlyAlertOnce(true)
            .setContentIntent(notificationContentPendingIntent)
            .setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
        v2rayServicesListener!!.getService().startForeground(1, mBuilder.build())
    }
}