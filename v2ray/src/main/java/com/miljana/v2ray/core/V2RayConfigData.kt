package com.miljana.v2ray.core

import com.google.gson.annotations.SerializedName

/*data class V2RayConfig(
    val log: LogData,
    val stats: Any? = null,
    val policy: PolicyData,
    val inbounds: ArrayList<InboundData>,
    val outbounds: ArrayList<OutboundData>,
    val transport: TransportData
) {
    data class LogData(
        val loglevel: String
    )

    data class PolicyData(
        var levels: Map<String, LevelData>,
        var system: SystemData
    ) {
        data class LevelData(
            var uplinkOnly: Int,
            var downlinkOnly: Int,
        )

        data class SystemData(
            val statsOutboundDownlink: Boolean,
            val statsOutboundUplink: Boolean
        )
    }

    data class InboundData(
        var tag: String,
        var protocol: String,
        var listen: String? = null,
        var port: Int,
        val settings: InSettingsData?,
        val sniffing: SniffingData?,
    ) {

        data class InSettingsData(
            val udp: Boolean? = null,
            val ip: String? = null,
        )

        data class SniffingData(
            var enabled: Boolean,
            val destOverride: ArrayList<String>,
        )
    }

    data class OutboundData(
        val tag: String,
        var protocol: String,
        var settings: OutSettingsData? = null,
        var streamSettings: StreamSettingsData? = null,
    ) {

        data class OutSettingsData(var vnext: List<VnextData>? = null) {

            data class VnextData(
                var address: String = "",
                var port: Int,
                var users: List<UsersData>
            ) {

                data class UsersData(
                    var id: String = "",
                    var alterId: Int? = null,
                )
            }
        }

        data class StreamSettingsData(var network: String)
    }

    data class TransportData(
        val dsSettings: Any? = null,
        val grpcSettings: Any? = null,
        val gunSettings: Any? = null,
        val httpSettings: Any? = null,
        val kcpSettings: Any? = null,
        val quicSettings: QuicSettingsData? = null,
        val tcpSettings: Any? = null,
        val wsSettings: Any? = null,
    ) {

        data class QuicSettingsData(
            val security: String
        )
    }
}*/


data class V2RayConfigData(
    val log: LogData,
    val stats: Any? = null,
    val policy: PolicyData,
    val inbounds: ArrayList<InboundData>,
    val outbounds: ArrayList<OutboundData>,
): java.io.Serializable {
    data class LogData(
        val access: String,
        val error: String,
        var loglevel: String?
    )

    data class PolicyData(
        var levels: Map<String, LevelData>,
        var system: Any? = null
    ) {
        data class LevelData(
            var handshake: Int? = null,
            var connIdle: Int? = null,
            var uplinkOnly: Int? = null,
            var downlinkOnly: Int? = null,
            val statsUserUplink: Boolean? = null,
            val statsUserDownlink: Boolean? = null,
            var bufferSize: Int? = null
        )
    }

    data class InboundData(
        var tag: String,
        var protocol: String,
        var listen: String? = null,
        var port: Int,
        val settings: InSettingsData?,
        val sniffing: SniffingData?,
        val streamSettings: Any? = null,
        val allocate: Any? = null
    ) {

        data class InSettingsData(
            val auth: String? = null,
            val udp: Boolean? = null,
            val userLevel: Int? = null,
            val address: String? = null,
            val port: Int? = null,
            val network: String? = null
        )

        data class SniffingData(
            var enabled: Boolean,
            val destOverride: ArrayList<String>,
            val metadataOnly: Boolean? = null
        )
    }

    data class OutboundData(
        val tag: String,
        var protocol: String,
        var settings: OutSettingsData? = null,
        var streamSettings: StreamSettingsData? = null,
        val proxySettings: Any? = null,
        val sendThrough: String? = null,
        val mux: MuxData? = MuxData(false)
    ) {

        data class OutSettingsData(var vnext: List<VnextData>? = null) {

            data class VnextData(
                var address: String = "",
                var port: Int,
                var users: List<UsersData>
            ) {

                data class UsersData(
                    var id: String = "",
                    var alterId: Int? = null,
                    var security: String?,
                    var level: Int?,
                    var encryption: String = "",
                    var flow: String = ""
                )
            }

        }

        data class StreamSettingsData(var network: String?,
                                      var security: String = "",
                                      var tcpSettings: TcpSettingsData? = null,
                                      var kcpSettings: KcpSettingsData? = null,
                                      var wsSettings: WsSettingsData? = null,
                                      var httpSettings: HttpSettingsData? = null,
                                      var tlsSettings: TlsSettingsData? = null,
                                      var quicSettings: QuicSettingData? = null,
                                      var realitySettings: TlsSettingsData? = null,
                                      var grpcSettings: GrpcSettingsData? = null,
                                      val dsSettings: Any? = null,
                                      val sockopt: Any? = null
        ) {
            data class TcpSettingsData(var header: HeaderData = HeaderData(),
                                       val acceptProxyProtocol: Boolean? = null) {
                data class HeaderData(var type: String = "none",
                                      var request: RequestData? = null,
                                      var response: Any? = null) {
                    data class RequestData(var path: List<String> = ArrayList(),
                                           var headers: HeadersData = HeadersData(),
                                           val version: String? = null,
                                           val method: String? = null) {
                        data class HeadersData(var Host: List<String> = ArrayList(),
                                               @SerializedName("User-Agent")
                                               val userAgent: List<String>? = null,
                                               @SerializedName("Accept-Encoding")
                                               val acceptEncoding: List<String>? = null,
                                               val Connection: List<String>? = null,
                                               val Pragma: String? = null)
                    }
                }
            }

            data class KcpSettingsData(var mtu: Int = 1350,
                                       var tti: Int = 50,
                                       var uplinkCapacity: Int = 12,
                                       var downlinkCapacity: Int = 100,
                                       var congestion: Boolean = false,
                                       var readBufferSize: Int = 1,
                                       var writeBufferSize: Int = 1,
                                       var header: HeaderData = HeaderData(),
                                       var seed: String? = null) {
                data class HeaderData(var type: String = "none")
            }

            data class WsSettingsData(var path: String = "",
                                      var headers: HeadersData = HeadersData(),
                                      val maxEarlyData: Int? = null,
                                      val useBrowserForwarding: Boolean? = null,
                                      val acceptProxyProtocol: Boolean? = null) {
                data class HeadersData(var Host: String = "")
            }

            data class HttpSettingsData(var host: List<String> = ArrayList(),
                                        var path: String = "")

            data class TlsSettingsData(var allowInsecure: Boolean = false,
                                       var serverName: String = "",
                                       val alpn: List<String>? = null,
                                       val minVersion: String? = null,
                                       val maxVersion: String? = null,
                                       val preferServerCipherSuites: Boolean? = null,
                                       val cipherSuites: String? = null,
                                       val fingerprint: String? = null,
                                       val certificates: List<Any>? = null,
                                       val disableSystemRoot: Boolean? = null,
                                       val enableSessionResumption: Boolean? = null,
                // REALITY settings
                                       val show: Boolean = false,
                                       var publicKey: String? = null,
                                       var shortId: String? = null,
                                       var spiderX: String? = null)

            data class QuicSettingData(var security: String = "none",
                                       var key: String = "",
                                       var header: HeaderData = HeaderData()) {
                data class HeaderData(var type: String = "none")
            }

            data class GrpcSettingsData(var serviceName: String = "",
                                        var multiMode: Boolean? = null)
        }

        data class MuxData(var enabled: Boolean, var concurrency: Int = 8)
    }
}
