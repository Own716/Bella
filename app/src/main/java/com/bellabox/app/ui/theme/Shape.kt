package com.bellabox.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val BellaShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),       // Badges, chips, small buttons
    medium = RoundedCornerShape(18.dp),      // List items, secondary cards
    large = RoundedCornerShape(24.dp),       // Primary cards, control panels
    extraLarge = RoundedCornerShape(28.dp)   // Floating bottom bar, dialogs, modals
)

val PillShape = RoundedCornerShape(999.dp)
