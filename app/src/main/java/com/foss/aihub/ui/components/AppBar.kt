package com.foss.aihub.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.VideocamOff
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.VerticalSplit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.foss.aihub.R
import com.foss.aihub.models.AiService
import com.foss.aihub.ui.screens.dialogs.ActiveServicesDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TooltipIconButton(
    tooltipText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val tooltipState = rememberTooltipState()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(tooltipText)
            }
        },
        state = tooltipState
    ) {
        IconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            content = content
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiHubAppBar(
    selectedService: AiService,
    onMenuClick: () -> Unit,
    onHomeClick: () -> Unit = {},
    isHomeActive: Boolean = false,
    onFreezeBackgroundTabs: () -> Unit = {},
    isAllBackgroundFrozen: Boolean = false,
    frozenServices: Set<String> = emptySet(),
    onPromptVaultClick: () -> Unit = {},
    onBroadcastClick: () -> Unit = {},
    isBroadcastActive: Boolean = false,
    onSplitScreenToggle: () -> Unit = {},
    isSplitScreenActive: Boolean = false,
    onToggleDesktopMode: () -> Unit = {},
    isDesktopMode: Boolean = false,
    onToggleAudio: () -> Unit = {},
    isAudioAllowed: Boolean = false,
    onToggleVideo: () -> Unit = {},
    isVideoAllowed: Boolean = false,
    onToggleImmersiveMode: () -> Unit = {},
    isImmersiveMode: Boolean = false,
    onNextService: () -> Unit = {},
    onPreviousService: () -> Unit = {},
    loadedServiceNames: Set<String>,
    allServices: List<AiService>,
    onReload: (AiService) -> Unit,
    onServiceSelected: (AiService) -> Unit,
    onKillService: (AiService) -> Unit,
    onToggleFreeze: (AiService) -> Unit = {},
    onFreezeAllBackground: () -> Unit = {},
    onFreezeAndExit: () -> Unit = {},
    onClearSiteData: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    isLoading: Boolean = false,
    loadingProgress: Int = 0,
    loadingColor: Color = colorScheme.primary
) {
    var showServicesDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isCompactScreen = configuration.screenWidthDp < 600

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "icon_rotation"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        TopAppBar(
            title = {
                AnimatedContent(
                    targetState = selectedService,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220, delayMillis = 90)) togetherWith fadeOut(
                            animationSpec = tween(90)
                        )
                    },
                    label = "service_title"
                ) { service ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .pointerInput(service.name) {
                                var totalDrag = 0f
                                detectHorizontalDragGestures(
                                    onDragStart = { totalDrag = 0f },
                                    onDragEnd = {
                                        if (totalDrag > 50f) {
                                            onPreviousService()
                                        } else if (totalDrag < -50f) {
                                            onNextService()
                                        }
                                    },
                                    onHorizontalDrag = { _, dragAmount ->
                                        totalDrag += dragAmount
                                    }
                                )
                            }
                    ) {
                        Text(
                            text = if (isHomeActive) "AI Hub Home" else service.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (!isHomeActive) {
                            Spacer(modifier = Modifier.width(6.dp))
                            val isFrozen = service.name in frozenServices
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isFrozen) Color(0xFF162D4A) else Color(0xFF133618),
                                contentColor = if (isFrozen) Color(0xFF9DD2FF) else Color(0xFF75F98A)
                            ) {
                                Text(
                                    text = if (isFrozen) "❄️ Frozen" else "●",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            },
            navigationIcon = {
                TooltipIconButton(
                    tooltipText = stringResource(R.string.label_menu),
                    onClick = onMenuClick,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        Icons.Rounded.Menu,
                        contentDescription = stringResource(R.string.label_menu),
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            actions = {
                // 1. Home Dashboard button
                TooltipIconButton(
                    tooltipText = if (isHomeActive) "Close Home Dashboard" else "Bento Home Dashboard",
                    onClick = onHomeClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Home,
                        contentDescription = "Home Dashboard",
                        tint = if (isHomeActive) colorScheme.primary else colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // 2. Freeze Tabs button
                val isCurrentFrozen = selectedService.name in frozenServices
                TooltipIconButton(
                    tooltipText = if (isCurrentFrozen) "Tab Frozen (0% CPU) - Tap to Wake" else "Deep Freeze Tab & Background (Save RAM & Data)",
                    onClick = onFreezeBackgroundTabs,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AcUnit,
                        contentDescription = "Freeze Tabs",
                        tint = if (isCurrentFrozen || isAllBackgroundFrozen) Color(0xFF9DD2FF) else colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // 3. Prompt Vault button
                TooltipIconButton(
                    tooltipText = "Quick Prompt Vault",
                    onClick = onPromptVaultClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ElectricBolt,
                        contentDescription = "Quick Prompt Vault",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // 4. Video button
                TooltipIconButton(
                    tooltipText = if (isVideoAllowed) "Block Video" else "Unblock Video",
                    onClick = onToggleVideo,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = if (isVideoAllowed) Icons.Outlined.Videocam else Icons.Outlined.VideocamOff,
                        contentDescription = if (isVideoAllowed) "Block Video" else "Unblock Video",
                        tint = if (isVideoAllowed) colorScheme.primary else colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // 5. Audio button (next to Video button)
                TooltipIconButton(
                    tooltipText = if (isAudioAllowed) "Block Audio" else "Unblock Audio",
                    onClick = onToggleAudio,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = if (isAudioAllowed) Icons.Outlined.VolumeUp else Icons.Outlined.VolumeOff,
                        contentDescription = if (isAudioAllowed) "Block Audio" else "Unblock Audio",
                        tint = if (isAudioAllowed) colorScheme.primary else colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                if (!isCompactScreen) {
                    // 4. Broadcast Bar button
                    TooltipIconButton(
                        tooltipText = "Broadcast Prompt",
                        onClick = onBroadcastClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Campaign,
                            contentDescription = "Broadcast Prompt",
                            tint = if (isBroadcastActive) colorScheme.primary else colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(23.dp)
                        )
                    }

                    // 5. Split-Screen Compare Mode button
                    TooltipIconButton(
                        tooltipText = "Dual Split-Screen Mode",
                        onClick = onSplitScreenToggle,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.VerticalSplit,
                            contentDescription = "Dual Split-Screen Mode",
                            tint = if (isSplitScreenActive) colorScheme.primary else colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }

                // Active Services Badged Box
                BadgedBox(
                    badge = {
                        if (loadedServiceNames.isNotEmpty()) {
                            Badge(
                                containerColor = colorScheme.primary,
                                contentColor = colorScheme.onPrimary
                            ) {
                                Text(
                                    text = loadedServiceNames.size.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    },
                ) {
                    TooltipIconButton(
                        tooltipText = "Active AI Tabs (${loadedServiceNames.size})",
                        onClick = { showServicesDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Apps,
                            contentDescription = stringResource(R.string.title_active_ai_services),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Overflow Menu
                Box {
                    TooltipIconButton(
                        tooltipText = stringResource(R.string.action_more_options),
                        onClick = { expanded = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .rotate(rotation)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = stringResource(R.string.action_more_options),
                            tint = colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.width(230.dp),
                        shape = RoundedCornerShape(18.dp),
                        containerColor = colorScheme.surfaceContainer,
                        tonalElevation = 4.dp
                    ) {
                        if (isCompactScreen) {
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        Icons.Rounded.Campaign,
                                        contentDescription = null,
                                        tint = if (isBroadcastActive) colorScheme.primary else colorScheme.onSurfaceVariant
                                    )
                                },
                                text = { Text("Broadcast Bar") },
                                onClick = {
                                    expanded = false
                                    onBroadcastClick()
                                }
                            )

                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        Icons.Rounded.VerticalSplit,
                                        contentDescription = null,
                                        tint = if (isSplitScreenActive) colorScheme.primary else colorScheme.onSurfaceVariant
                                    )
                                },
                                text = { Text(if (isSplitScreenActive) "Exit Split Screen" else "Dual Split Screen") },
                                onClick = {
                                    expanded = false
                                    onSplitScreenToggle()
                                }
                            )
                        }

                        // Immersive Mode Toggle
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    if (isImmersiveMode) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen,
                                    contentDescription = null,
                                    tint = if (isImmersiveMode) colorScheme.primary else colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text(if (isImmersiveMode) "Exit Fullscreen" else "Full-Screen Immersive") },
                            onClick = {
                                expanded = false
                                onToggleImmersiveMode()
                            }
                        )

                        // 1280px Desktop Mode Toggle
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Computer,
                                    contentDescription = null,
                                    tint = if (isDesktopMode) colorScheme.primary else colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text(if (isDesktopMode) "Mobile Mode" else "1280px Desktop Mode") },
                            onClick = {
                                expanded = false
                                onToggleDesktopMode()
                            }
                        )

                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Replay,
                                    contentDescription = null,
                                    tint = colorScheme.onSurfaceVariant
                                )
                            },
                            text = {
                                Text(
                                    text = stringResource(R.string.action_reload),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            onClick = {
                                expanded = false
                                onReload(selectedService)
                            },
                        )

                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = colorScheme.onSurfaceVariant
                                )
                            },
                            text = {
                                Text(
                                    text = stringResource(R.string.action_clear_site_data),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            onClick = {
                                expanded = false
                                showClearDataDialog = true
                            },
                        )

                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = colorScheme.onSurfaceVariant
                                )
                            },
                            text = {
                                Text(
                                    text = stringResource(R.string.title_settings),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            onClick = {
                                expanded = false
                                onSettingsClick()
                            },
                        )

                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Info,
                                    contentDescription = null,
                                    tint = colorScheme.onSurfaceVariant
                                )
                            },
                            text = {
                                Text(
                                    text = stringResource(R.string.section_about),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            onClick = {
                                expanded = false
                                onAboutClick()
                            },
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorScheme.surface,
                scrolledContainerColor = colorScheme.surfaceContainer,
                titleContentColor = colorScheme.onSurface,
                navigationIconContentColor = colorScheme.onSurfaceVariant,
                actionIconContentColor = colorScheme.onSurfaceVariant
            ),
            windowInsets = TopAppBarDefaults.windowInsets,
        )

        LoadingLine(
            isVisible = isLoading,
            accentColor = loadingColor,
            progress = loadingProgress,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showServicesDialog) {
        ActiveServicesDialog(
            activeServices = loadedServiceNames.mapNotNull { name -> allServices.find { it.name == name } },
            selectedServiceId = selectedService.name,
            frozenServices = frozenServices,
            onServiceSelected = { service ->
                onServiceSelected(service)
                showServicesDialog = false
            },
            onKillService = onKillService,
            onToggleFreeze = onToggleFreeze,
            onFreezeAllBackground = onFreezeAllBackground,
            onFreezeAndExit = onFreezeAndExit,
            onDismiss = { showServicesDialog = false },
        )
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.action_clear_site_data),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.msg_clear_site_data_warning, selectedService.name
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showClearDataDialog = false
                    onClearSiteData()
                }) {
                    Text(
                        stringResource(R.string.action_clear),
                        color = colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
            containerColor = colorScheme.surfaceContainer,
            tonalElevation = 6.dp,
            shape = RoundedCornerShape(28.dp)
        )
    }
}

@Composable
private fun LoadingLine(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    accentColor: Color,
    progress: Int = 0
) {
    val animatedFraction by animateFloatAsState(
        targetValue = progress / 100f,
        animationSpec = tween(durationMillis = 600),
        label = "progress"
    )

    androidx.compose.animation.AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(accentColor.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .height(4.dp)
                    .background(accentColor)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Md3TopAppBar(
    title: String,
    onBack: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.action_back)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorScheme.surface,
            scrolledContainerColor = colorScheme.surfaceColorAtElevation(3.dp),
            titleContentColor = colorScheme.onSurface,
            navigationIconContentColor = colorScheme.onSurface,
            actionIconContentColor = colorScheme.onSurface,
        ),
        scrollBehavior = scrollBehavior,
        actions = actions,
    )
}
