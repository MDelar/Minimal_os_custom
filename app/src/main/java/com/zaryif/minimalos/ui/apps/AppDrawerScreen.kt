package com.zaryif.minimalos.ui.apps

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zaryif.minimalos.domain.model.AppInfo
import com.zaryif.minimalos.ui.theme.MinimalBlack
import com.zaryif.minimalos.ui.theme.MinimalCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkGray
import com.zaryif.minimalos.ui.theme.MinimalGray
import com.zaryif.minimalos.ui.theme.MinimalRed
import com.zaryif.minimalos.ui.theme.MinimalWhite

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawerScreen(
    apps: List<AppInfo>,
    favoriteAppPackages: Set<String> = emptySet(),
    onTogglePin: ((AppInfo) -> Unit)? = null,
    onUninstall: ((AppInfo) -> Unit)? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedAppForOptions by remember { mutableStateOf<AppInfo?>(null) }

    val filteredApps = remember(searchQuery, apps) {
        if (searchQuery.isBlank()) {
            apps
        } else {
            val q = searchQuery.lowercase()
            apps.filter { it.label.lowercase().contains(q) }
        }
    }

    // Handle back button to clear search before exiting
    BackHandler(enabled = searchQuery.isNotEmpty()) {
        searchQuery = ""
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MinimalBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar with Back and Search
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MinimalCardBg)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MinimalWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "search apps…",
                            style = TextStyle(
                                fontSize = 15.sp,
                                color = MinimalGray
                            )
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        cursorBrush = SolidColor(MinimalRed),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (filteredApps.isNotEmpty()) {
                                    val topApp = filteredApps.first()
                                    val launchIntent = context.packageManager.getLaunchIntentForPackage(topApp.packageName)
                                    launchIntent?.let { context.startActivity(it) }
                                }
                            }
                        ),
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            color = MinimalWhite,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { searchQuery = "" },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = MinimalGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MinimalGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App Count & Hint
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredApps.size} apps",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MinimalGray,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "hold to pin or uninstall",
                    fontSize = 10.sp,
                    color = MinimalDarkGray
                )
            }

            // App List
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredApps, key = { it.packageName }) { app ->
                    val isPinned = app.packageName in favoriteAppPackages
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.98f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.75f, stiffness = 600f),
                        label = "appRowScale"
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clip(RoundedCornerShape(8.dp))
                            .combinedClickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = {
                                    val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                    launchIntent?.let { context.startActivity(it) }
                                },
                                onLongClick = {
                                    selectedAppForOptions = app
                                }
                            )
                            .padding(vertical = 14.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = app.label,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Normal,
                            color = MinimalWhite,
                            modifier = Modifier.weight(1f)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isPinned) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MinimalDarkCardBg)
                                        .border(1.dp, MinimalRed.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "PINNED",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MinimalRed
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Text(
                                text = "open",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MinimalDarkGray
                            )
                        }
                    }
                }
            }
        }
    }

    // Long-press options dialog
    selectedAppForOptions?.let { app ->
        val isPinned = app.packageName in favoriteAppPackages
        Dialog(onDismissRequest = { selectedAppForOptions = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MinimalCardBg)
                    .border(1.dp, MinimalDarkGray, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = app.label,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MinimalWhite
                    )
                    Text(
                        text = app.packageName,
                        fontSize = 11.sp,
                        color = MinimalGray,
                        modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                    )

                    // Option 1: Pin / Unpin
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MinimalDarkCardBg)
                            .clickable {
                                onTogglePin?.invoke(app)
                                selectedAppForOptions = null
                            }
                            .padding(vertical = 12.dp, horizontal = 14.dp)
                    ) {
                        Text(
                            text = if (isPinned) "Unpin from Home" else "Pin to Home",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isPinned) MinimalRed else MinimalWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Option 2: Uninstall
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MinimalDarkCardBg)
                            .clickable {
                                selectedAppForOptions = null
                                onUninstall?.invoke(app)
                            }
                            .padding(vertical = 12.dp, horizontal = 14.dp)
                    ) {
                        Text(
                            text = if (app.isSystemApp) "App Info (System App)" else "Uninstall App",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (app.isSystemApp) MinimalWhite else MinimalRed
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Option 3: App Info Settings
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MinimalDarkCardBg)
                            .clickable {
                                selectedAppForOptions = null
                                val settingsIntent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", app.packageName, null)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                try {
                                    context.startActivity(settingsIntent)
                                } catch (_: Exception) {}
                            }
                            .padding(vertical = 12.dp, horizontal = 14.dp)
                    ) {
                        Text(
                            text = "System App Info",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MinimalWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Cancel
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedAppForOptions = null }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 13.sp,
                            color = MinimalGray
                        )
                    }
                }
            }
        }
    }
}
