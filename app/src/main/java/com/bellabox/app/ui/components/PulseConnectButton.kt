package com.bellabox.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bellabox.app.ui.theme.StatusConnected
import com.bellabox.app.ui.theme.StatusConnecting
import com.bellabox.app.ui.theme.StatusFailed
import com.bellabox.app.ui.theme.StatusIdle
import com.bellabox.core.model.ConnectionState

@Composable
fun PulseConnectButton(
    state: ConnectionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseScale = if (state == ConnectionState.CONNECTING || state == ConnectionState.RECONNECTING) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.25f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "connecting_pulse"
        ).value
    } else if (state == ConnectionState.CONNECTED) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "connected_pulse"
        ).value
    } else {
        1.0f
    }

    val baseColor = when (state) {
        ConnectionState.CONNECTED -> StatusConnected
        ConnectionState.CONNECTING, ConnectionState.RECONNECTING, ConnectionState.STARTING -> StatusConnecting
        ConnectionState.FAILED -> StatusFailed
        else -> StatusIdle
    }

    val animatedColor = animateColorAsState(
        targetValue = baseColor,
        animationSpec = tween(durationMillis = 400),
        label = "btn_color"
    )

    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing ring
        Box(
            modifier = Modifier
                .size(190.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(animatedColor.value.copy(alpha = 0.15f))
        )

        // Middle soft glow
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(animatedColor.value.copy(alpha = 0.25f))
        )

        // Inner main button
        Box(
            modifier = Modifier
                .size(136.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = animatedColor.value.copy(alpha = 0.5f)
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            animatedColor.value.copy(alpha = 0.95f),
                            animatedColor.value
                        )
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            val s = com.bellabox.app.ui.i18n.LocalAppStrings.current
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.PowerSettingsNew,
                    contentDescription = "Power",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (state) {
                        ConnectionState.CONNECTED -> s.btnConnected
                        ConnectionState.CONNECTING -> s.btnConnecting
                        ConnectionState.RECONNECTING -> s.btnReconnect
                        ConnectionState.STARTING -> s.btnStarting
                        ConnectionState.STOPPING -> s.btnStopping
                        ConnectionState.FAILED -> s.btnRetry
                        else -> s.btnConnect
                    },
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
