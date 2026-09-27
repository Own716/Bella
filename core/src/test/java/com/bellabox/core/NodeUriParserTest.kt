package com.bellabox.core

import com.bellabox.core.model.ProtocolType
import com.bellabox.core.network.NodeUriParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NodeUriParserTest {

    @Test
    fun testParseVlessReality() {
        val uri = "vless://b831381d-6324-4d53-ad4f-8cda48b30811@example.com:443?security=reality&sni=test.com&fp=chrome&pbk=123456&sid=7890#HK-Node-01"
        val node = NodeUriParser.parse(uri)
        assertNotNull(node)
        assertEquals("HK-Node-01", node!!.name)
        assertEquals("example.com", node.server)
        assertEquals(443, node.port)
        assertEquals(ProtocolType.VLESS, node.protocol)
        assertEquals("b831381d-6324-4d53-ad4f-8cda48b30811", node.uuid)
        assertEquals("reality", node.security)
        assertEquals("test.com", node.sni)
        assertEquals("123456", node.publicKey)
        assertEquals("7890", node.shortId)

        val fp = node.computeFingerprint()
        assertTrue(fp.contains("example.com"))
        assertTrue(fp.contains("443"))
    }

    @Test
    fun testParseTrojan() {
        val uri = "trojan://password123@trojan.example.com:443?sni=trojan.example.com#Trojan-US"
        val node = NodeUriParser.parse(uri)
        assertNotNull(node)
        assertEquals("Trojan-US", node!!.name)
        assertEquals("trojan.example.com", node.server)
        assertEquals(443, node.port)
        assertEquals(ProtocolType.TROJAN, node.protocol)
        assertEquals("password123", node.password)
        assertEquals("tls", node.security)
    }

    @Test
    fun testParseHysteria2() {
        val uri = "hysteria2://secretpass@hy2.example.com:8443?sni=hy2.example.com#Hy2-Fast"
        val node = NodeUriParser.parse(uri)
        assertNotNull(node)
        assertEquals("Hy2-Fast", node!!.name)
        assertEquals("hy2.example.com", node.server)
        assertEquals(8443, node.port)
        assertEquals(ProtocolType.HYSTERIA2, node.protocol)
        assertEquals("secretpass", node.password)
    }

    @Test
    fun testDeduplicationFingerprint() {
        val uri1 = "trojan://pass@host.com:443#NameA"
        val uri2 = "trojan://pass@host.com:443#NameB"
        val node1 = NodeUriParser.parse(uri1)!!
        val node2 = NodeUriParser.parse(uri2)!!

        assertEquals(node1.computeFingerprint(), node2.computeFingerprint())
    }
}
