package com.bellabox.engine

import com.bellabox.core.model.ProtocolType
import com.bellabox.core.model.ProxyNode
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SingboxConfigBuilderTest {

    @Test
    fun testBuildConfigForVlessReality() {
        val node = ProxyNode(
            name = "Test-Reality",
            server = "1.2.3.4",
            port = 443,
            protocol = ProtocolType.VLESS,
            uuid = "a1b2c3d4-e5f6-7890-1234-567890abcdef",
            flow = "xtls-rprx-vision",
            security = "reality",
            sni = "gateway.example.com",
            publicKey = "pubkey123",
            shortId = "shortid456"
        )

        val builder = SingboxConfigBuilder()
        val jsonStr = builder.build(node)
        assertNotNull(jsonStr)

        SingboxConfigValidator.validateJsonConfig(jsonStr)

        val root = JSONObject(jsonStr)
        assertTrue(root.has("dns"))
        assertTrue(root.has("inbounds"))
        assertTrue(root.has("outbounds"))
        assertTrue(root.has("route"))

        val outbounds = root.getJSONArray("outbounds")
        val proxy = outbounds.getJSONObject(0)
        assertEquals("proxy", proxy.getString("tag"))
        assertEquals("vless", proxy.getString("type"))
        assertEquals("1.2.3.4", proxy.getString("server"))
        assertEquals(443, proxy.getInt("port"))
        assertEquals("a1b2c3d4-e5f6-7890-1234-567890abcdef", proxy.getString("uuid"))
        assertEquals("xtls-rprx-vision", proxy.getString("flow"))

        val tls = proxy.getJSONObject("tls")
        assertTrue(tls.getBoolean("enabled"))
        assertEquals("gateway.example.com", tls.getString("server_name"))
        val reality = tls.getJSONObject("reality")
        assertTrue(reality.getBoolean("enabled"))
        assertEquals("pubkey123", reality.getString("public_key"))
    }

    @Test(expected = InvalidConfigurationException::class)
    fun testValidationFailsOnBlankServer() {
        val node = ProxyNode(
            name = "Invalid",
            server = "",
            port = 443,
            protocol = ProtocolType.VLESS,
            uuid = "some-uuid"
        )
        SingboxConfigValidator.validateNode(node)
    }

    @Test(expected = InvalidConfigurationException::class)
    fun testValidationFailsOnInvalidPort() {
        val node = ProxyNode(
            name = "Invalid",
            server = "example.com",
            port = 99999,
            protocol = ProtocolType.VLESS,
            uuid = "some-uuid"
        )
        SingboxConfigValidator.validateNode(node)
    }
}
