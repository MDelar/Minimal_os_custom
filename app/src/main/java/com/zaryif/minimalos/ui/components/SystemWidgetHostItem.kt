package com.zaryif.minimalos.ui.components

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.zaryif.minimalos.ui.theme.MinimalCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkGray
import com.zaryif.minimalos.ui.theme.MinimalGray

@Composable
fun MinimalSystemWidgetHostItem(
    widgetId: Int,
    appWidgetManager: AppWidgetManager,
    appWidgetHost: AppWidgetHost,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MinimalCardBg)
            .border(1.dp, MinimalDarkGray, RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val label = remember(widgetId) {
                    appWidgetManager.getAppWidgetInfo(widgetId)?.loadLabel(context.packageManager)
                }
                Text(
                    text = label ?: "App Widget",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MinimalGray
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(22.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Widget",
                        tint = MinimalGray,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 280.dp),
                factory = { ctx ->
                    val info = appWidgetManager.getAppWidgetInfo(widgetId)
                    if (info != null) {
                        try {
                            appWidgetHost.createView(ctx, widgetId, info).apply {
                                setAppWidget(widgetId, info)
                                setOnTouchListener { v, event ->
                                    when (event.actionMasked) {
                                        android.view.MotionEvent.ACTION_DOWN,
                                        android.view.MotionEvent.ACTION_MOVE -> {
                                            v.parent?.requestDisallowInterceptTouchEvent(true)
                                        }
                                        android.view.MotionEvent.ACTION_UP,
                                        android.view.MotionEvent.ACTION_CANCEL -> {
                                            v.parent?.requestDisallowInterceptTouchEvent(false)
                                        }
                                    }
                                    false
                                }
                            }
                        } catch (_: Exception) {
                            android.view.View(ctx)
                        }
                    } else {
                        android.view.View(ctx)
                    }
                }
            )
        }
    }
}
