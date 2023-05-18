package com.miljana.v2ray.core

import android.app.Service
import android.content.Intent
import android.net.LocalSocket
import android.net.LocalSocketAddress
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import com.miljana.v2ray.interfaces.V2rayServicesListener
import com.miljana.v2ray.utils.AppConfigs
import com.miljana.v2ray.utils.V2RayConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class V2RayVpnService2 : VpnService(), V2rayServicesListener {

    companion object {
        private const val VPN_MTU = 1500
        private const val PRIVATE_VLAN4_CLIENT = "26.26.26.1"
        private const val PRIVATE_VLAN4_ROUTER = "26.26.26.2"
        private const val PRIVATE_VLAN6_CLIENT = "da26:2626::1"
        private const val PRIVATE_VLAN6_ROUTER = "da26:2626::2"
        private const val PRIVATE_VPN_ROUTE = "0.0.0.0"
        private const val TUN2SOCKS = "tun2socks.aar"
    }

    private var mInterface: ParcelFileDescriptor? = null
    private var process: Process? = null
    private var v2RayConfig: V2RayConfig? = null
    private var isRunning = true

    override fun onCreate() {
        super.onCreate()
        V2RayCoreManager.instance.setUpServicesListener(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val startCommand = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getSerializableExtra("COMMAND", AppConfigs.V2RAY_SERVICE_COMMANDS::class.java)
        else
            (intent.getSerializableExtra("COMMAND") as AppConfigs.V2RAY_SERVICE_COMMANDS?)

        Timber.e("onStartCommand success => $startCommand.")
        when (startCommand) {
            AppConfigs.V2RAY_SERVICE_COMMANDS.START_SERVICE -> {
                v2RayConfig = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    intent.getSerializableExtra("V2RAY_CONFIG", V2RayConfig::class.java)
                else intent.getSerializableExtra("V2RAY_CONFIG") as V2RayConfig

                v2RayConfig?.let { config ->

                    if (V2RayCoreManager.instance.isV2RayCoreRunning()) {
                        V2RayCoreManager.instance.stopCore()
                    }

                    if (V2RayCoreManager.instance.startCore(config)) {
                        Timber.e("onStartCommand success => v2ray core started.")
                    } else {
                        Timber.d("onDestroy invoked 1")
                        this.onDestroy()
                    }
                }

                if (v2RayConfig == null) {
                    Timber.d("onDestroy invoked 2")
                }


            }
            AppConfigs.V2RAY_SERVICE_COMMANDS.STOP_SERVICE -> {
                V2RayCoreManager.instance.stopCore()
            }
            AppConfigs.V2RAY_SERVICE_COMMANDS.MEASURE_DELAY -> {
                // ignore for now
            }
            else -> {
                this.onDestroy()
            }
        }

        return START_STICKY
    }

    private fun stopAllProcess() {
        Timber.e("stopAllProcess => invoked")
        // stop foreground service
        isRunning = false
        process?.destroy()
        V2RayCoreManager.instance.stopCore()
        try {
            //stopSelf has to be called ahead of mInterface.close(). otherwise v2ray core cannot be stooped
            //It's strange but true.
            //This can be verified by putting stopself() behind and call stopLoop and startLoop
            //in a row for several times. You will find that later created v2ray core report port in use
            //which means the first v2ray core somehow failed to stop and release the port.

            stopSelf()
        } catch (e: Exception) {
            //ignore
            Timber.e("CANT_STOP", "SELF")
        }

        try {
            mInterface?.close()
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun setup() {
        Timber.e("setup => invoked")
        val prepareIntent = prepare(this)
        if (prepareIntent != null) {
            return
        }

        val builder = Builder()
        // TODO add session string
        builder.setSession("")
        builder.setMtu(VPN_MTU)
        builder.addAddress(PRIVATE_VLAN4_CLIENT, 24)
        builder.addRoute(PRIVATE_VPN_ROUTE, 0)

        try {
            mInterface?.close()
        } catch (e: Exception) {
            // ignore
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false)
        }

        try {
            mInterface = builder.establish()
            isRunning = true
            runTun2socks()
        } catch (e: java.lang.Exception) {
            stopAllProcess()
        }
    }

    private fun runTun2socks() {
        Timber.e("runTun2socks => invoked")
        val socksPort = v2RayConfig?.LOCAL_SOCKS5_PORT
        val cmd = arrayListOf(
            File(applicationInfo.nativeLibraryDir, TUN2SOCKS).absolutePath,
            "--netif-ipaddr", PRIVATE_VLAN4_ROUTER,
            "--netif-netmask", "255.255.255.252",
            "--socks-server-addr", "127.0.0.1:${socksPort}",
            "--tunmtu", VPN_MTU.toString(),
            "--sock-path", "sock_path",//File(applicationContext.filesDir, "sock_path").absolutePath,
            "--enable-udprelay",
            "--loglevel", "error")

        /*if (settingsStorage?.decodeBool(AppConfig.PREF_PREFER_IPV6) == true) {
            cmd.add("--netif-ip6addr")
            cmd.add(PRIVATE_VLAN6_ROUTER)
        }
        if (settingsStorage?.decodeBool(AppConfig.PREF_LOCAL_DNS_ENABLED) == true) {
            val localDnsPort = Utils.parseInt(settingsStorage?.decodeString(AppConfig.PREF_LOCAL_DNS_PORT), AppConfig.PORT_LOCAL_DNS.toInt())
            cmd.add("--dnsgw")
            cmd.add("127.0.0.1:${localDnsPort}")
        }
        Log.d(packageName, cmd.toString())*/
        Timber.d("runTun2socks cmd")
        try {
            val proBuilder = ProcessBuilder(cmd)
            proBuilder.redirectErrorStream(true)
            Timber.d("runTun2socks proBuilder")
            process = proBuilder
                .directory(applicationContext.filesDir)
                .start()
            Timber.d("runTun2socks process start")
            process?.let {
                Thread(Runnable {
                    try {
                        Timber.d(packageName,"$TUN2SOCKS check")
                        it.waitFor()
                        Timber.d(packageName,"$TUN2SOCKS exited")
                        if (isRunning) {
                            Timber.d(packageName,"$TUN2SOCKS restart")
                            runTun2socks()
                        }
                    } catch (e: InterruptedException) {
                        // ignore
                        Timber.d("runTun2socks failed => $e")
                    }

                }, "Tun2socks_Thread").start()

                Timber.d(packageName, process.toString())
            }
            sendFd()
        } catch (e: Exception) {
            Timber.d(" tun2socks failure => $e")
            Timber.d("onDestroy invoked 3")
            this.onDestroy()
        }
    }

    private fun sendFd() {
        val tunFd = mInterface?.fileDescriptor
        val localSocksFile = File(applicationContext.filesDir, "sock_path").absolutePath
        Timber.d(packageName, localSocksFile)

        GlobalScope.launch(Dispatchers.IO) {
            var tries = 0
            while (true) try {
                Thread.sleep(50L * tries)
                Timber.d(packageName, "sendFd tries: $tries")
                LocalSocket().use { localSocket ->
                    localSocket.connect(LocalSocketAddress(localSocksFile, LocalSocketAddress.Namespace.FILESYSTEM))

                    if (!localSocket.isConnected) {
                        Timber.e("SOCK_FILE", "Unable to connect to localSocksFile [$localSocksFile]")
                    } else {
                        Timber.e("SOCK_FILE", "connected to sock file [$localSocksFile]")
                    }

                    localSocket.setFileDescriptorsForSend(arrayOf(tunFd))
                    localSocket.outputStream.write(42)

                    // TODO should we use it?
                    //localSocket.setFileDescriptorsForSend(null)
                    //localSocket.shutdownOutput()
                    //localSocket.close()
                }
                break
            } catch (e: Exception) {
                Timber.d("sendFd failed => $e")
                if (tries > 5) break
                tries += 1
            }
        }
    }

    override fun onDestroy() {
        Timber.d("onDestroy invoked")
        super.onDestroy()
    }

    override fun onRevoke() {
        stopAllProcess()
    }

    override fun onProtect(socket: Int): Boolean {
        return protect(socket)
    }

    override fun getService(): Service {
        return this
    }

    override fun startService() {
        setup()
    }

    override fun stopService() {
        stopAllProcess()
    }

}