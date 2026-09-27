package com.bellabox.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.bellabox.app.ui.theme.DownloadSpeedColor
import com.bellabox.app.ui.theme.UploadSpeedColor

@Composable
fun SpeedChart(
    downloadHistory: List<Long>,
    uploadHistory: List<Long>,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        val width = size.width
        val height = size.height

        if (downloadHistory.size < 2) return@Canvas

        val maxVal = maxOf(
            downloadHistory.maxOrNull() ?: 1L,
            uploadHistory.maxOrNull() ?: 1L,
            1024L * 100L // minimum scale (100KB/s)
        ).toFloat()

        // Draw Download curve
        drawSmoothCurve(
            data = downloadHistory,
            maxVal = maxVal,
            width = width,
            height = height,
            lineColor = DownloadSpeedColor,
            fillColor = DownloadSpeedColor.copy(alpha = 0.15f)
        )

        // Draw Upload curve
        drawSmoothCurve(
            data = uploadHistory,
            maxVal = maxVal,
            width = width,
            height = height,
            lineColor = UploadSpeedColor,
            fillColor = UploadSpeedColor.copy(alpha = 0.08f)
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSmoothCurve(
    data: List<Long>,
    maxVal: Float,
    width: Float,
    height: Float,
    lineColor: Color,
    fillColor: Color
) {
    val stepX = width / (data.size - 1).coerceAtLeast(1)
    val points = data.mapIndexed { index, value ->
        val x = index * stepX
        val y = height - (value.toFloat() / maxVal * (height * 0.85f)).coerceAtMost(height)
        androidx.compose.ui.geometry.Offset(x, y)
    }

    val path = Path()
    val fillPath = Path()

    path.moveTo(points.first().x, points.first().y)
    fillPath.moveTo(points.first().x, height)
    fillPath.lineTo(points.first().x, points.first().y)

    for (i in 0 until points.size - 1) {
        val p0 = points[i]
        val p1 = points[i + 1]
        val midX = (p0.x + p1.x) / 2f
        path.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
        fillPath.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
    }

    fillPath.lineTo(points.last().x, height)
    fillPath.close()

    drawPath(
        path = fillPath,
        brush = Brush.verticalGradient(
            colors = listOf(fillColor, Color.Transparent),
            startY = 0f,
            endY = height
        )
    )

    drawPath(
        path = path,
        color = lineColor,
        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
    )
}
