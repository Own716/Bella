package com.bellabox.engine

import com.bellabox.core.model.ConnectionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ConnectionStateMachineTest {

    @Test
    fun testTransitions() {
        ConnectionStateMachine.transitionTo(ConnectionState.IDLE)
        assertEquals(ConnectionState.IDLE, ConnectionStateMachine.currentState)

        ConnectionStateMachine.markStarting()
        assertEquals(ConnectionState.STARTING, ConnectionStateMachine.currentState)

        ConnectionStateMachine.markConnecting()
        assertEquals(ConnectionState.CONNECTING, ConnectionStateMachine.currentState)

        ConnectionStateMachine.markConnected()
        assertEquals(ConnectionState.CONNECTED, ConnectionStateMachine.currentState)

        ConnectionStateMachine.markReconnecting("Wi-Fi network lost")
        assertEquals(ConnectionState.RECONNECTING, ConnectionStateMachine.currentState)
        assertEquals("Wi-Fi network lost", ConnectionStateMachine.lastErrorMessage.value)

        ConnectionStateMachine.markStopping()
        assertEquals(ConnectionState.STOPPING, ConnectionStateMachine.currentState)

        ConnectionStateMachine.markStopped()
        assertEquals(ConnectionState.STOPPED, ConnectionStateMachine.currentState)

        ConnectionStateMachine.markFailed("Handshake timeout")
        assertEquals(ConnectionState.FAILED, ConnectionStateMachine.currentState)
        assertEquals("Handshake timeout", ConnectionStateMachine.lastErrorMessage.value)
    }
}
