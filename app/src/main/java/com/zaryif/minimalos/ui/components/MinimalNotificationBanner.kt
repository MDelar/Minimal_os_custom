package com.zaryif.minimalos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaryif.minimalos.ui.theme.AppleSanFranciscoFontFamily
import com.zaryif.minimalos.ui.theme.MinimalCardBg
import com.zaryif.minimalos.ui.theme.MinimalRed
import com.zaryif.minimalos.ui.theme.MinimalWhite

@Composable
fun MinimalNotificationBanner(
    message: String?,
    modifier: Modifier = Modifier
) {
    // Retain the last valid message to prevent flicker/glitch during exit animation
    var lastValidMessage by remember { mutableStateOf("") }
    if (!message.isNullOrBlank()) {
        lastValidMessage = message
    }

    AnimatedVisibility(
        visible = !message.isNullOrBlank(),
        enter = slideInVertically(spring(dampingRatio = 0.78f, stiffness = 420f)) { -it } + fadeIn(tween(160)),
        exit = slideOutVertically(tween(140, easing = FastOutLinearInEasing)) { -it } + fadeOut(tween(140)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MinimalCardBg)
                .border(1.2.dp, MinimalRed.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val isError = lastValidMessage.startsWith("Error", ignoreCase = true) || 
                              lastValidMessage.startsWith("Could not", ignoreCase = true)
                Icon(
                    imageVector = if (isError) Icons.Default.Info else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MinimalRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = lastValidMessage,
                    fontSize = 14.sp,
                    fontFamily = AppleSanFranciscoFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = MinimalWhite
                )
            }
        }
    }
}
