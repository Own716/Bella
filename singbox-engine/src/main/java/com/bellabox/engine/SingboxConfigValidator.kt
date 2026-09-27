package com.bellabox.engine

import com.bellabox.core.model.ProtocolType
import com.bellabox.core.model.ProxyNode
import org.json.JSONObject

class InvalidConfigurationException(message: String) : Exception(message)

object SingboxConfigValidator {

    fun validateNode(node: ProxyNode) {
        if (node.server.isBlank()) {
            throw InvalidConfigurationException("Server address cannot be empty.")
        }
        if (node.port !in 1..65535) {
            throw InvalidConfigurationException("Server port ${node.port} is out of valid range (1-65535).")
        }
        when (node.protocol) {
            ProtocolType.VLESS, ProtocolType.VMESS -> {
                if (node.uuid.isBlank()) {
                    throw InvalidConfigurationException("${node.protocol.name} requires a valid UUID.")
                }
            }
            ProtocolType.TROJAN, ProtocolType.SHADOWSOCKS, ProtocolType.HYSTERIA2 -> {
                if (node.password.isBlank()) {
                    throw InvalidConfigurationException("${node.protocol.name} requires a password.")
                }
            }
            else -> {}
        }
    }

    fun validateJsonConfig(configJson: String) {
        if (configJson.isBlank()) {
            throw InvalidConfigurationException("Configuration content is empty.")
        }
        try {
            val root = JSONObject(configJson)
            if (!root.has("outbounds") || root.getJSONArray("outbounds").length() == 0) {
                throw InvalidConfigurationException("Configuration must contain at least one outbound.")
            }
            if (!root.has("inbounds")) {
                throw InvalidConfigurationException("Configuration must contain inbounds configuration.")
            }
        } catch (e: Exception) {
            throw InvalidConfigurationException("Malformed JSON configuration: ${e.message}")
        }
    }
}
