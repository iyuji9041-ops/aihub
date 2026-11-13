package com.foss.aihub.ui.screens

import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.VerticalSplit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue.Closed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.foss.aihub.MainActivity
import com.foss.aihub.R
import com.foss.aihub.models.AiService
import com.foss.aihub.models.JsDialog
import com.foss.aihub.models.LinkData
import com.foss.aihub.models.ServiceUiState
import com.foss.aihub.models.UpdateResult
import com.foss.aihub.models.WebViewState
import com.foss.aihub.models.loadServices
import com.foss.aihub.ui.components.AiHubAppBar
import com.foss.aihub.ui.components.BroadcastBar
import com.foss.aihub.ui.components.DrawerContent
import com.foss.aihub.ui.components.ErrorOverlay
import com.foss.aihub.ui.components.ErrorType
import com.foss.aihub.ui.screens.dialogs.AppUpdateDialog
import com.foss.aihub.ui.screens.dialogs.JsDialogHandler
import com.foss.aihub.ui.screens.dialogs.LinkOptionsDialog
import com.foss.aihub.ui.screens.dialogs.PromptVaultSheet
import com.foss.aihub.ui.screens.dialogs.UpdateResultDialog
import com.foss.aihub.ui.webview.createWebViewForService
import com.foss.aihub.ui.webview.updateWebViewSettings
import com.foss.aihub.ui.components.frostedBorder
import com.foss.aihub.utils.AppUpdateInfo
import com.foss.aihub.utils.CloudDataHandler
import com.foss.aihub.utils.SUPPORT_EMAIL
import com.foss.aihub.utils.buildPromptInjectionJs
import com.foss.aihub.utils.checkForUpdate
import com.foss.aihub.utils.copyLinkToClipboard
import com.foss.aihub.utils.openInExternalBrowser
import com.foss.aihub.utils.performServiceUpdate
import com.foss.aihub.utils.shareLink
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun FrostedFreezeBanner(
    serviceName: String,
    onWake: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(0.9f)
                .frostedBorder(RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF162D4A),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.AcUnit,
                            contentDescription = "Frozen",
                            tint = Color(0xFF9DD2FF),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Text(
                    text = "❄️ $serviceName is Deep-Frozen (0% CPU • 0 Data Usage)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "JavaScript execution, background media, and network timers are stopped to save battery & mobile data.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = onWake,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "⚡ Wake Up Tab",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiHubApp(
    context: MainActivity,
    aiServices: List<AiService>,
    onServicesUpdated: (List<AiService>) -> Unit,
    initialSharedText: String? = null,
    onClearSharedText: () -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current

    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = Closed)
    val scope = rememberCoroutineScope()

    val settingsManager = remember { context.settingsManager }
    val settings by settingsManager.settingsFlow.collectAsState()

    val initialServiceName = if (settings.loadLastOpenedAI) {
        settingsManager.getLastOpenedService() ?: settings.defaultServiceName
        ?: aiServices.first().name
    } else {
        settings.defaultServiceName ?: aiServices.first().name
    }

    val initialServiceNames = remember(settings) {
        if (!settings.loadLastOpenedAI && settings.multipleDefaultAi) {
            val ordered =
                settings.serviceOrder.filter { it in settings.defaultServiceNames && it in settings.enabledServices }
            ordered.take(settings.maxKeepAlive).toSet()
        } else {
            setOf(initialServiceName)
        }
    }

    var selectedService by remember {
        val firstName = initialServiceNames.firstOrNull() ?: initialServiceName
        mutableStateOf(aiServices.find { it.name == firstName } ?: aiServices.first())
    }

    // Feature 5: Dual Split-Screen Compare Mode
    var isSplitScreenMode by remember { mutableStateOf(false) }
    var secondaryService by remember {
        val second = aiServices.firstOrNull { it.name != selectedService.name }
            ?: aiServices.getOrNull(1)
            ?: aiServices.first()
        mutableStateOf(second)
    }

    // Feature 3: Quick Prompt Vault
    var showPromptVault by remember { mutableStateOf(false) }
    val promptVaultSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Feature 4: Broadcast Bar
    var showBroadcastBar by remember { mutableStateOf(false) }

    // Bento Home Dashboard
    var showHomeDashboard by remember { mutableStateOf(true) }

    // Full-Screen Immersive Mode
    var isImmersiveMode by remember { mutableStateOf(false) }

    // Throttled progress update state
    var lastReportedProgress by remember { mutableIntStateOf(0) }

    var backPressedTime by remember { mutableLongStateOf(0L) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var selectedLink by remember { mutableStateOf<LinkData?>(null) }
    var showSettingsScreen by remember { mutableStateOf(false) }
    var showManageServices by remember { mutableStateOf(false) }
    var showCustomInjectionScreen by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showRequestNewAiScreen by remember { mutableStateOf(false) }

    var previousEnabledServices by remember { mutableStateOf(settings.enabledServices) }
    var previousDesktopView by remember { mutableStateOf(settings.desktopView) }
    var previousThirdPartyCookies by remember { mutableStateOf(settings.thirdPartyCookies) }
    var previousConnectionBlocking by remember { mutableStateOf(settings.blockAdsAndTrackers) }

    val serviceStates = remember { mutableStateMapOf<String, ServiceUiState>() }
    val webViews = remember { mutableStateMapOf<String, WebView>() }
    val loadedServices = remember { mutableStateSetOf<String>() }
    val serviceAccessOrder = remember { mutableListOf<String>() }

    // Feature 1: Smart RAM Freezer & Instant Telemetry
    val frozenServices = remember { mutableStateSetOf<String>() }
    val pauseJobs = remember { mutableStateMapOf<String, Job>() }

    val enabledServiceList = remember(settings.enabledServices, aiServices) {
        val enabledSet = settings.enabledServices
        val uniqueServices = aiServices.distinctBy { it.name.trim().lowercase() }
        val orderMap = settings.serviceOrder.withIndex().associate { it.value to it.index }
        uniqueServices.filter { it.name in enabledSet }.sortedBy { orderMap[it.name] ?: Int.MAX_VALUE }
    }

    fun cycleService(forward: Boolean) {
        if (enabledServiceList.isEmpty()) return
        val currentIndex = enabledServiceList.indexOfFirst { it.name == selectedService.name }
        if (currentIndex == -1) return
        val nextIndex = if (forward) {
            (currentIndex + 1) % enabledServiceList.size
        } else {
            (currentIndex - 1 + enabledServiceList.size) % enabledServiceList.size
        }
        selectedService = enabledServiceList[nextIndex]
    }

    fun toggleImmersiveMode() {
        isImmersiveMode = !isImmersiveMode
        val window = context.window
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        if (isImmersiveMode) {
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    fun freezeServiceNow(serviceName: String) {
        if (serviceName.isBlank()) return
        pauseJobs.remove(serviceName)?.cancel()
        webViews[serviceName]?.let { wv ->
            wv.evaluateJavascript("window.stop(); document.querySelectorAll('video, audio').forEach(m => m.pause());", null)
            wv.stopLoading()
            wv.onPause()
            wv.pauseTimers()
        }
        frozenServices.add(serviceName)
        Toast.makeText(context, "❄️ $serviceName Frozen (0% CPU & 0 Data)", Toast.LENGTH_SHORT).show()
    }

    fun freezeAllBackgroundServices() {
        var count = 0
        webViews.forEach { (name, wv) ->
            if (name != selectedService.name && (!isSplitScreenMode || name != secondaryService.name)) {
                pauseJobs.remove(name)?.cancel()
                wv.evaluateJavascript("window.stop(); document.querySelectorAll('video, audio').forEach(m => m.pause());", null)
                wv.stopLoading()
                wv.onPause()
                wv.pauseTimers()
                frozenServices.add(name)
                count++
            }
        }
        if (count > 0) {
            Toast.makeText(context, "❄️ Frozen $count background tab(s) (0% CPU)", Toast.LENGTH_SHORT).show()
        }
    }

    fun wakeService(serviceName: String) {
        if (serviceName.isBlank()) return
        pauseJobs.remove(serviceName)?.cancel()
        webViews[serviceName]?.let { wv ->
            wv.onResume()
            wv.resumeTimers()
        }
        frozenServices.remove(serviceName)
        Toast.makeText(context, "⚡ Woke $serviceName", Toast.LENGTH_SHORT).show()
    }

    fun resumeService(serviceName: String) {
        pauseJobs.remove(serviceName)?.cancel()
        webViews[serviceName]?.let { wv ->
            wv.onResume()
            wv.resumeTimers()
        }
        frozenServices.remove(serviceName)
    }

    fun schedulePauseService(serviceName: String) {
        if (serviceName.isBlank()) return
        pauseJobs[serviceName]?.cancel()
        pauseJobs[serviceName] = scope.launch {
            delay(20_000L) // 20-second grace period before freezing RAM/timers
            webViews[serviceName]?.onPause()
            frozenServices.add(serviceName)
        }
    }

    var previousSettings by remember { mutableStateOf(settings) }

    val applySettingsToAllWebViews: (Boolean) -> Unit = { reload ->
        webViews.forEach { (_, webView) ->
            updateWebViewSettings(webView, settings, reload)
        }
        previousSettings = settings
    }

    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateResult by remember { mutableStateOf<UpdateResult?>(null) }

    var jsDialog by remember { mutableStateOf<JsDialog?>(null) }
    var showAppUpdateDialog by remember { mutableStateOf(false) }
    var appUpdateInfo by remember { mutableStateOf<AppUpdateInfo?>(null) }

    val currentState by remember {
        derivedStateOf {
            serviceStates[selectedService.name] ?: ServiceUiState()
        }
    }

    val hasCurrentError by remember {
        derivedStateOf {
            currentState.error?.let { ErrorType.shouldShowOverlay(it.first) } == true
        }
    }

    fun updateServiceState(serviceId: String, update: (ServiceUiState) -> ServiceUiState) {
        val newState = update(serviceStates[serviceId] ?: ServiceUiState())
        serviceStates[serviceId] = newState
    }

    fun reloadAllActiveTabs() {
        webViews.forEach { (serviceId, webView) ->
            webView.reload()
            updateServiceState(serviceId) { state ->
                state.copy(
                    webViewState = WebViewState.LOADING,
                    isLoading = true,
                    error = null,
                    progress = 0
                )
            }
        }
    }

    fun enforceWebViewLimit() {
        val limit = settings.maxKeepAlive
        if (limit == Int.MAX_VALUE) {
            return
        }

        val servicesToKeep = serviceAccessOrder.take(limit).toSet()
        val servicesToDestroy = webViews.keys.filterNot { it in servicesToKeep }.toMutableList()

        if (selectedService.name !in servicesToKeep && webViews.containsKey(selectedService.name)) {
            servicesToDestroy.remove(selectedService.name)
        }

        servicesToDestroy.forEach { serviceId ->
            webViews[serviceId]?.let { webView ->
                (webView.parent as? ViewGroup)?.removeView(webView)
                webView.destroy()
                webViews.remove(serviceId)
                serviceStates.remove(serviceId)
                loadedServices.remove(serviceId)
                serviceAccessOrder.remove(serviceId)
            }
        }
    }

    fun getOrCreateWebView(service: AiService): WebView {
        val existing = webViews[service.name]
        if (existing != null) return existing

        val webView = createWebViewForService(
            context = context,
            service = service,
            activity = context,
            settings = settings,
            onProgressUpdate = { progress ->
                if (progress == 100 || kotlin.math.abs(progress - lastReportedProgress) >= 10) {
                    lastReportedProgress = progress
                    updateServiceState(service.name) { it.copy(progress = progress) }
                }
            },
            onLoadingStateChange = { isLoading ->
                updateServiceState(service.name) {
                    it.copy(
                        isLoading = isLoading,
                        webViewState = if (isLoading) WebViewState.LOADING else WebViewState.SUCCESS,
                        progress = if (isLoading) 0 else it.progress
                    )
                }
                if (isLoading) {
                    updateServiceState(service.name) { it.copy(error = null) }
                }
            },
            onLinkLongPress = { url, title, type ->
                selectedLink = LinkData(url, title, type)
                showLinkDialog = true
            },
            onError = { errorCode, description ->
                updateServiceState(service.name) {
                    it.copy(
                        error = errorCode to description,
                        webViewState = WebViewState.ERROR,
                        isLoading = false
                    )
                }
            },
            onJsAlertRequest = { message, result ->
                message?.let { jsDialog = JsDialog.Alert(it, result!!) }
            },
            onJsConfirmRequest = { message, result ->
                message?.let { jsDialog = JsDialog.Confirm(it, result!!) }
            },
            onJsPromptRequest = { message, result ->
                message?.let { jsDialog = JsDialog.Prompt(it, result!!) }
            },
            onJsBeforeUnloadRequest = { message, result ->
                message?.let { jsDialog = JsDialog.BeforeUnload(it, result!!) }
            },
        )
        updateWebViewSettings(webView, settings, false)
        webViews[service.name] = webView
        if (service.name !in serviceAccessOrder) {
            serviceAccessOrder.add(0, service.name)
        }
        loadedServices.add(service.name)
        return webView
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    webViews.values.forEach { it.onPause() }
                    try {
                        WebView(context).pauseTimers()
                    } catch (_: Exception) {}
                }

                Lifecycle.Event.ON_RESUME -> {
                    try {
                        WebView(context).resumeTimers()
                    } catch (_: Exception) {}
                    resumeService(selectedService.name)
                    if (isSplitScreenMode) {
                        resumeService(secondaryService.name)
                    }
                }

                Lifecycle.Event.ON_DESTROY -> {
                    pauseJobs.values.forEach { it.cancel() }
                    pauseJobs.clear()
                    webViews.forEach { (_, webView) ->
                        webView.destroy()
                    }
                    webViews.clear()
                    serviceAccessOrder.clear()
                    serviceStates.clear()
                    frozenServices.clear()
                }

                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            try {
                val window = context.window
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            } catch (_: Exception) {}
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Feature 1: Smart RAM Freezer
    LaunchedEffect(selectedService.name, secondaryService.name, isSplitScreenMode) {
        val currentActive = if (isSplitScreenMode) {
            setOf(selectedService.name, secondaryService.name)
        } else {
            setOf(selectedService.name)
        }

        currentActive.forEach { activeName ->
            resumeService(activeName)
        }

        webViews.keys.forEach { name ->
            if (name !in currentActive) {
                schedulePauseService(name)
            }
        }
    }

    // Feature 6: System-Wide "Ask AI Hub"
    LaunchedEffect(initialSharedText, selectedService.name) {
        if (!initialSharedText.isNullOrBlank()) {
            val wv = getOrCreateWebView(selectedService)
            delay(500L)
            wv.evaluateJavascript(
                buildPromptInjectionJs(initialSharedText, autoSubmit = false),
                null
            )
            Toast.makeText(
                context,
                "Shared text pasted into ${selectedService.name}",
                Toast.LENGTH_SHORT
            ).show()
            onClearSharedText()
        }
    }

    LaunchedEffect(initialServiceNames, showHomeDashboard) {
        if (!showHomeDashboard) {
            initialServiceNames.forEach { serviceName ->
                val service = aiServices.find { it.name == serviceName } ?: return@forEach
                getOrCreateWebView(service)
            }
        }
    }

    LaunchedEffect(true) {
        try {
            val lastDate = settingsManager.getDomainsLastUpdatedDate()
            val daysBetween = ChronoUnit.DAYS.between(lastDate, LocalDate.now())
            if (daysBetween >= 1) {
                scope.launch {
                    try {
                        CloudDataHandler.updateDomains(context)
                        settingsManager.saveDomainsLastUpdatedDate()
                    } catch (_: Exception) {
                        // ignore
                    }
                }
            }
        } catch (_: Exception) {
            // ignore
        }

        try {
            val updateFrequency = settings.updateFrequencyDays
            if (updateFrequency != -1) {
                val lastDate = settingsManager.getAiServicesLastUpdatedDate()
                val daysBetween = ChronoUnit.DAYS.between(lastDate, LocalDate.now())

                if (daysBetween >= updateFrequency) {
                    scope.launch {
                        try {
                            val result = performServiceUpdate(context, settingsManager)
                            if (result != null) {
                                updateResult = result
                                showUpdateDialog = true
                                onServicesUpdated(loadServices(context))
                                settingsManager.saveAiServicesLastUpdatedDate()
                            }
                        } catch (_: Exception) {
                            // ignore
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // ignore
        }

        try {
            val checkForUpdate = settings.checkForUpdate
            if (checkForUpdate) {
                val lastDate = settingsManager.getLastUpdateCheckDate()
                val daysBetween = ChronoUnit.DAYS.between(lastDate, LocalDate.now())
                if (daysBetween >= 3) {
                    scope.launch {
                        try {
                            val updateAvailable = checkForUpdate(context)
                            if (updateAvailable != null) {
                                appUpdateInfo = updateAvailable
                                showAppUpdateDialog = true
                            }
                            settingsManager.saveLastUpdateCheckDate()
                        } catch (_: Exception) {
                            // ignore
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // ignore
        }
    }

    LaunchedEffect(configuration.orientation) {
        if (drawerState.isOpen) {
            drawerState.close()
        }
    }

    LaunchedEffect(selectedService) {
        if (settings.loadLastOpenedAI) {
            settingsManager.saveLastOpenedService(selectedService.name)
        }
    }

    LaunchedEffect(selectedService.name) {
        serviceAccessOrder.remove(selectedService.name)
        serviceAccessOrder.add(0, selectedService.name)

        val wv = webViews[selectedService.name]
        if (wv != null) {
            val isActuallyLoading = wv.progress < 100
            val currentError = serviceStates[selectedService.name]?.error
            updateServiceState(selectedService.name) { state ->
                state.copy(
                    isLoading = isActuallyLoading, webViewState = when {
                        currentError != null -> WebViewState.ERROR
                        isActuallyLoading -> WebViewState.LOADING
                        else -> WebViewState.SUCCESS
                    }, progress = wv.progress
                )
            }
            wv.bringToFront()
        } else {
            updateServiceState(selectedService.name) { state ->
                state.copy(
                    webViewState = WebViewState.LOADING,
                    error = null,
                    isLoading = true,
                    progress = 0
                )
            }
        }

        loadedServices.add(selectedService.name)
        enforceWebViewLimit()
    }

    ModalNavigationDrawer(
        drawerState = drawerState, gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            DrawerContent(
                aiServices = aiServices,
                onServicesUpdated = onServicesUpdated,
                selectedService = selectedService,
                onServiceSelected = { service ->
                    selectedService = service
                    scope.launch { drawerState.close() }
                },
                onServiceReload = { service ->
                    scope.launch { drawerState.close() }
                    webViews[service.name]?.reload()

                    updateServiceState(service.name) { state ->
                        state.copy(
                            webViewState = WebViewState.LOADING,
                            isLoading = true,
                            error = null,
                            progress = 0
                        )
                    }
                },
                webViewStates = serviceStates.mapValues { it.value.webViewState },
                enabledServices = settings.enabledServices,
                serviceOrder = settings.serviceOrder,
                favoriteServices = settings.favoriteServices,
                onToggleFavorite = { serviceId ->
                    settingsManager.updateSettings { current ->
                        current.favoriteServices = if (serviceId in current.favoriteServices) {
                            current.favoriteServices - serviceId
                        } else {
                            current.favoriteServices + serviceId
                        }
                    }
                },
                settingsManager = settingsManager
            )
        },
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            topBar = {
                AiHubAppBar(
                    selectedService = selectedService,
                    onMenuClick = {
                        scope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    },
                    onHomeClick = { showHomeDashboard = !showHomeDashboard },
                    isHomeActive = showHomeDashboard,
                    onFreezeBackgroundTabs = {
                        if (selectedService.name in frozenServices) {
                            wakeService(selectedService.name)
                        } else {
                            freezeServiceNow(selectedService.name)
                            freezeAllBackgroundServices()
                        }
                    },
                    isAllBackgroundFrozen = webViews.size <= 1 || frozenServices.size >= (webViews.size - (if (isSplitScreenMode) 2 else 1)),
                    frozenServices = frozenServices,
                    onPromptVaultClick = { showPromptVault = true },
                    onBroadcastClick = { showBroadcastBar = !showBroadcastBar },
                    isBroadcastActive = showBroadcastBar,
                    onSplitScreenToggle = {
                        isSplitScreenMode = !isSplitScreenMode
                        if (isSplitScreenMode && secondaryService.name == selectedService.name) {
                            secondaryService = aiServices.firstOrNull { it.name != selectedService.name }
                                ?: aiServices.first()
                        }
                    },
                    isSplitScreenActive = isSplitScreenMode,
                    onToggleDesktopMode = {
                        settingsManager.updateSettings { it.desktopView = !it.desktopView }
                        applySettingsToAllWebViews(true)
                    },
                    isDesktopMode = settings.desktopView,
                    onToggleImmersiveMode = { toggleImmersiveMode() },
                    isImmersiveMode = isImmersiveMode,
                    onNextService = { cycleService(forward = true) },
                    onPreviousService = { cycleService(forward = false) },
                    loadedServiceNames = webViews.keys,
                    allServices = aiServices,
                    onReload = { service ->
                        webViews[service.name]?.reload()

                        updateServiceState(service.name) { state ->
                            state.copy(
                                webViewState = WebViewState.LOADING,
                                isLoading = true,
                                error = null,
                                progress = 0
                            )
                        }
                    },
                    onServiceSelected = { service ->
                        selectedService = service
                        showHomeDashboard = false
                    },
                    onKillService = { service ->
                        val serviceId = service.name
                        if (serviceId == selectedService.name) return@AiHubAppBar

                        webViews[serviceId]?.let { webView ->
                            (webView.parent as? ViewGroup)?.removeView(webView)
                            webView.destroy()
                            webViews.remove(serviceId)
                        }

                        serviceStates.remove(serviceId)
                        loadedServices.remove(serviceId)
                        serviceAccessOrder.remove(serviceId)
                        frozenServices.remove(serviceId)

                        Toast.makeText(
                            context,
                            context.getString(R.string.msg_service_killed, service.name),
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onToggleFreeze = { service ->
                        if (service.name in frozenServices) {
                            wakeService(service.name)
                        } else {
                            freezeServiceNow(service.name)
                        }
                    },
                    onFreezeAllBackground = { freezeAllBackgroundServices() },
                    onFreezeAndExit = {
                        freezeAllBackgroundServices()
                        context.finishAndRemoveTask()
                    },
                    onClearSiteData = {
                        val serviceId = selectedService.name
                        val webView = webViews[serviceId]
                        if (webView != null) {
                            val currentUrl = webView.url ?: selectedService.url
                            val domain = try {
                                currentUrl.toUri().host ?: ""
                            } catch (_: Exception) {
                                ""
                            }

                            val cookieManager = CookieManager.getInstance()
                            val cookies = cookieManager.getCookie(currentUrl)
                            if (cookies != null) {
                                val cookieNames = cookies.split(";").mapNotNull { cookie ->
                                    cookie.trim().split("=").firstOrNull()?.trim()
                                }
                                for (name in cookieNames) {
                                    cookieManager.setCookie(
                                        currentUrl,
                                        "$name=; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Path=/"
                                    )
                                    cookieManager.setCookie(
                                        currentUrl,
                                        "$name=; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Path=/; Domain=$domain"
                                    )
                                }
                                cookieManager.flush()
                            }

                            webView.evaluateJavascript(
                                "try { localStorage.clear(); sessionStorage.clear(); } catch(e) {}",
                                null
                            )

                            webView.clearCache(true)
                            webView.clearFormData()
                            webView.clearHistory()

                            updateServiceState(serviceId) { state ->
                                state.copy(
                                    webViewState = WebViewState.LOADING,
                                    isLoading = true,
                                    error = null,
                                    progress = 0
                                )
                            }
                            webView.loadUrl(selectedService.url)

                            Toast.makeText(
                                context, context.getString(
                                    R.string.msg_site_data_cleared, selectedService.name
                                ), Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onSettingsClick = { showSettingsScreen = true },
                    onAboutClick = { showAbout = true },
                    isLoading = currentState.isLoading && !hasCurrentError,
                    loadingProgress = currentState.progress,
                    loadingColor = selectedService.accentColor
                )
            },
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 8.dp),
                )
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (showHomeDashboard) {
                    BentoHomeScreen(
                        aiServices = aiServices,
                        enabledServices = settings.enabledServices,
                        favoriteServices = settings.favoriteServices,
                        loadedServiceNames = webViews.keys,
                        frozenServices = frozenServices,
                        onServiceSelected = { service ->
                            selectedService = service
                            showHomeDashboard = false
                        },
                        onToggleFavorite = { serviceId ->
                            settingsManager.updateSettings { current ->
                                current.favoriteServices = if (serviceId in current.favoriteServices) {
                                    current.favoriteServices - serviceId
                                } else {
                                    current.favoriteServices + serviceId
                                }
                            }
                        },
                        onFreezeAllBackground = { freezeAllBackgroundServices() },
                        onRequestNewAi = { showRequestNewAiScreen = true },
                        onPromptSubmit = { promptText ->
                            val wv = getOrCreateWebView(selectedService)
                            wv.evaluateJavascript(
                                buildPromptInjectionJs(promptText, autoSubmit = true),
                                null
                            )
                            showHomeDashboard = false
                            Toast.makeText(context, "Prompt sent to ${selectedService.name}!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (!isSplitScreenMode) {
                    // Normal Single Pane View
                    Box(modifier = Modifier.fillMaxSize()) {
                        AndroidView(
                            factory = { ctx ->
                                FrameLayout(ctx).apply {
                                    val currentWv = getOrCreateWebView(selectedService)
                                    (currentWv.parent as? ViewGroup)?.removeView(currentWv)
                                    addView(currentWv)
                                    currentWv.visibility = View.VISIBLE
                                    currentWv.bringToFront()
                                }
                            },
                            update = { root ->
                                val currentWv = getOrCreateWebView(selectedService)
                                for (i in root.childCount - 1 downTo 0) {
                                    val child = root.getChildAt(i)
                                    if (child != currentWv) {
                                        root.removeViewAt(i)
                                    }
                                }
                                if (currentWv.parent != root) {
                                    (currentWv.parent as? ViewGroup)?.removeView(currentWv)
                                    root.addView(currentWv)
                                }
                                val shouldBeVisible = serviceStates[selectedService.name]?.error == null
                                currentWv.visibility = if (shouldBeVisible) View.VISIBLE else View.GONE
                                currentWv.bringToFront()
                            },
                            modifier = Modifier.fillMaxSize(),
                        )

                        if (hasCurrentError && currentState.error != null) {
                            val (errorCode, errorMessage) = currentState.error!!
                            ErrorOverlay(
                                errorType = ErrorType.fromErrorCode(errorCode),
                                errorCode = errorCode,
                                errorMessage = errorMessage,
                                serviceName = selectedService.name,
                                accentColor = selectedService.accentColor,
                                onRetry = {
                                    updateServiceState(selectedService.name) { state ->
                                        state.copy(
                                            error = null,
                                            webViewState = WebViewState.LOADING,
                                            isLoading = true,
                                            progress = 0
                                        )
                                    }
                                    webViews[selectedService.name]?.reload()
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (selectedService.name in frozenServices) {
                            FrostedFreezeBanner(
                                serviceName = selectedService.name,
                                onWake = { wakeService(selectedService.name) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                } else {
                    // Dual Split-Screen Compare Mode View
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Top Primary Pane
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    FrameLayout(ctx).apply {
                                        val primaryWv = getOrCreateWebView(selectedService)
                                        (primaryWv.parent as? ViewGroup)?.removeView(primaryWv)
                                        addView(primaryWv)
                                        primaryWv.visibility = View.VISIBLE
                                    }
                                },
                                update = { root ->
                                    val primaryWv = getOrCreateWebView(selectedService)
                                    for (i in root.childCount - 1 downTo 0) {
                                        val child = root.getChildAt(i)
                                        if (child != primaryWv) {
                                            root.removeViewAt(i)
                                        }
                                    }
                                    if (primaryWv.parent != root) {
                                        (primaryWv.parent as? ViewGroup)?.removeView(primaryWv)
                                        root.addView(primaryWv)
                                    }
                                    primaryWv.visibility = View.VISIBLE
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            if (selectedService.name in frozenServices) {
                                FrostedFreezeBanner(
                                    serviceName = selectedService.name,
                                    onWake = { wakeService(selectedService.name) },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // Split Divider with Model Switcher
                        SplitCompareDivider(
                            primaryName = selectedService.name,
                            secondaryService = secondaryService,
                            availableServices = aiServices,
                            onSecondarySelected = { secondaryService = it },
                            onClose = { isSplitScreenMode = false }
                        )

                        // Bottom Secondary Pane
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    FrameLayout(ctx).apply {
                                        val secWv = getOrCreateWebView(secondaryService)
                                        (secWv.parent as? ViewGroup)?.removeView(secWv)
                                        addView(secWv)
                                        secWv.visibility = View.VISIBLE
                                    }
                                },
                                update = { root ->
                                    val secWv = getOrCreateWebView(secondaryService)
                                    for (i in root.childCount - 1 downTo 0) {
                                        val child = root.getChildAt(i)
                                        if (child != secWv) {
                                            root.removeViewAt(i)
                                        }
                                    }
                                    if (secWv.parent != root) {
                                        (secWv.parent as? ViewGroup)?.removeView(secWv)
                                        root.addView(secWv)
                                    }
                                    secWv.visibility = View.VISIBLE
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            if (secondaryService.name in frozenServices) {
                                FrostedFreezeBanner(
                                    serviceName = secondaryService.name,
                                    onWake = { wakeService(secondaryService.name) },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                // Feature 4: Broadcast Bar overlay
                if (showBroadcastBar) {
                    BroadcastBar(
                        targetServiceNames = webViews.keys.toList().ifEmpty { listOf(selectedService.name) },
                        onBroadcast = { promptText ->
                            val count = webViews.size.coerceAtLeast(1)
                            webViews.values.forEach { wv ->
                                wv.evaluateJavascript(
                                    buildPromptInjectionJs(promptText, autoSubmit = true),
                                    null
                                )
                            }
                            Toast.makeText(
                                context,
                                "Broadcasted prompt to $count AI tab(s)!",
                                Toast.LENGTH_SHORT
                            ).show()
                            showBroadcastBar = false
                        },
                        onClose = { showBroadcastBar = false },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                    )
                }
            }
        }
    }

    BackHandler(enabled = !showSettingsScreen && !showManageServices && !showCustomInjectionScreen && !showRequestNewAiScreen && !showAbout) {
        when {
            drawerState.isOpen -> {
                scope.launch { drawerState.close() }
            }

            showBroadcastBar -> {
                showBroadcastBar = false
            }

            showHomeDashboard -> {
                showHomeDashboard = false
            }

            isSplitScreenMode -> {
                isSplitScreenMode = false
            }

            hasCurrentError -> {
                updateServiceState(selectedService.name) { state ->
                    state.copy(error = null)
                }
                if (webViews[selectedService.name]?.canGoBack() == true) {
                    webViews[selectedService.name]?.goBack()
                } else {
                    webViews[selectedService.name]?.visibility = View.VISIBLE
                    updateServiceState(selectedService.name) { state ->
                        state.copy(webViewState = WebViewState.SUCCESS)
                    }
                }
            }

            webViews[selectedService.name]?.canGoBack() == true -> {
                webViews[selectedService.name]?.goBack()
            }

            else -> {
                val currentTime = System.currentTimeMillis()
                if (currentTime - backPressedTime < 2000L) {
                    context.finish()
                } else {
                    backPressedTime = currentTime
                    Toast.makeText(
                        context, context.getString(R.string.msg_press_again), Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    if (showLinkDialog) {
        selectedLink?.let { linkData ->
            LinkOptionsDialog(
                linkData = linkData,
                onDismiss = { showLinkDialog = false },
                onOpenLinkInExternalBrowser = { url ->
                    openInExternalBrowser(context, url)
                    showLinkDialog = false
                },
                onCopyLink = {
                    copyLinkToClipboard(context, linkData.url)
                    showLinkDialog = false
                },
                onShareLink = {
                    shareLink(context, linkData.url, linkData.title)
                    showLinkDialog = false
                },
            )
        }
    }

    var previousMaxKeepAlive by remember { mutableIntStateOf(settings.maxKeepAlive) }

    LaunchedEffect(
        settings, settings.blockAdsAndTrackers
    ) {
        val connectionBlockingChanged = settings.blockAdsAndTrackers != previousConnectionBlocking
        val limitChanged = settings.maxKeepAlive != previousMaxKeepAlive

        if (settings != previousSettings || connectionBlockingChanged || limitChanged) {
            val relevantChanges = listOf(
                settings.enableZoom != previousSettings.enableZoom,
                settings.fontSizePercentage != previousSettings.fontSizePercentage,
            ).any { it }

            if ((relevantChanges || connectionBlockingChanged) && webViews.isNotEmpty()) {
                webViews.forEach { (_, webView) ->
                    updateWebViewSettings(webView, settings, connectionBlockingChanged)
                }
            }

            if (limitChanged) {
                enforceWebViewLimit()
                previousMaxKeepAlive = settings.maxKeepAlive
            }

            previousSettings = settings
            previousConnectionBlocking = settings.blockAdsAndTrackers
        }
    }

    LaunchedEffect(showSettingsScreen) {
        if (!showSettingsScreen) {
            val desktopViewChanged = settings.desktopView != previousDesktopView
            val thirdPartyCookiesChanged = settings.thirdPartyCookies != previousThirdPartyCookies

            val reloadRequired = desktopViewChanged || thirdPartyCookiesChanged

            if (reloadRequired) {
                applySettingsToAllWebViews(true)
            }

            previousDesktopView = settings.desktopView
            previousThirdPartyCookies = settings.thirdPartyCookies

            enforceWebViewLimit()

            val currentEnabled = settings.enabledServices

            val disabledServices = previousEnabledServices.filter { it !in currentEnabled }
            val enabledServices = currentEnabled.filter { it !in previousEnabledServices }

            if (disabledServices.isNotEmpty() || enabledServices.isNotEmpty()) {
                if (selectedService.name !in currentEnabled) {
                    val firstEnabled = aiServices.firstOrNull { it.name in currentEnabled }
                        ?: aiServices.firstOrNull { it.name == settings.defaultServiceName }
                    if (firstEnabled != null) {
                        selectedService = firstEnabled
                    }
                }

                val toRemove = mutableListOf<String>()
                webViews.forEach { (id, webView) ->
                    if (id !in currentEnabled) {
                        (webView.parent as? ViewGroup)?.removeView(webView)
                        webView.destroy()
                        toRemove.add(id)

                        serviceAccessOrder.remove(id)
                    }
                }

                toRemove.forEach { id ->
                    webViews.remove(id)
                    serviceStates.remove(id)
                    loadedServices.remove(id)
                }

                previousEnabledServices = currentEnabled
            }
        } else {
            previousEnabledServices = settings.enabledServices
        }
    }

    if (jsDialog != null) {
        BackHandler {
            when (val dialog = jsDialog) {
                is JsDialog.Alert -> dialog.result.cancel()
                is JsDialog.Confirm -> dialog.result.cancel()
                is JsDialog.Prompt -> dialog.result.cancel()
                is JsDialog.BeforeUnload -> dialog.result.cancel()
                null -> {}
            }
            jsDialog = null
        }

        JsDialogHandler(
            dialog = jsDialog, context = context, onDismiss = { jsDialog = null })
    }

    if (showUpdateDialog && updateResult != null) {
        UpdateResultDialog(
            added = updateResult!!.added,
            removed = updateResult!!.removed,
            modified = updateResult!!.modified,
            newCategories = updateResult!!.newCategories,
            onDismiss = {
                showUpdateDialog = false
                updateResult = null
            },
        )
    }

    if (showSettingsScreen) {
        BackHandler {
            showSettingsScreen = false
            applySettingsToAllWebViews(false)
        }

        SettingsScreen(
            context = context,
            aiServices = aiServices,
            onBack = {
                showSettingsScreen = false
                applySettingsToAllWebViews(false)
                enforceWebViewLimit()
            },
            settingsManager = settingsManager,
            onManageServices = { showManageServices = true },
            onCustomInjection = { showCustomInjectionScreen = true },
            onClearCache = {
                try {
                    context.cacheDir?.deleteRecursively()

                    val webViewCacheDir = File(context.cacheDir, "webviewCache")
                    webViewCacheDir.deleteRecursively()

                    val webViewDatabaseDir = File(context.filesDir, "webview")
                    webViewDatabaseDir.deleteRecursively()

                    WebView(context).apply {
                        clearCache(true)
                        clearHistory()
                        destroy()
                    }

                    scope.launch {
                        Toast.makeText(
                            context,
                            context.getString(R.string.msg_cache_cleared),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (_: Exception) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.msg_error_clearing_cache),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onClearData = {
                webViews.forEach { (serviceId, webView) ->
                    webView.clearCache(true)
                    webView.clearHistory()
                    webView.clearFormData()

                    updateServiceState(serviceId) { state ->
                        state.copy(
                            error = null, isLoading = false, progress = 0
                        )
                    }
                }

                val cookieManager = CookieManager.getInstance()

                cookieManager.removeAllCookies { _ ->
                    cookieManager.flush()

                    scope.launch {
                        reloadAllActiveTabs()
                        Toast.makeText(
                            context,
                            context.getString(R.string.msg_all_data_cleared),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
        )
    }

    if (showCustomInjectionScreen) {
        BackHandler { showCustomInjectionScreen = false }
        CustomInjectionScreen(
            context = context,
            onBack = { showCustomInjectionScreen = false },
            settingsManager = settingsManager
        )
    }

    if (showManageServices) {
        BackHandler { showManageServices = false }
        ManageAiServicesScreen(
            onBack = {
                showManageServices = false
                enforceWebViewLimit()
            },
            aiServices = aiServices,
            enabledServices = settings.enabledServices,
            onEnabledServicesChange = { newSet ->
                settingsManager.updateSettings { it.enabledServices = newSet }
            },
            defaultServiceId = settings.defaultServiceName ?: aiServices.first().name,
            loadLastAiEnabled = settings.loadLastOpenedAI,
            settingsManager = settingsManager,
            onRequestNewAi = {
                showRequestNewAiScreen = true
            },
        )
    }

    if (showAbout) {
        BackHandler { showAbout = false }
        AboutScreen(context = context, onBack = { showAbout = false })
    }

    if (showAppUpdateDialog && appUpdateInfo != null) {
        AppUpdateDialog(
            updateInfo = appUpdateInfo!!,
            onDismiss = { showAppUpdateDialog = false },
            onOpenDownload = { url ->
                openInExternalBrowser(context, url)
                showAppUpdateDialog = false
            },
        )
    }

    if (showPromptVault) {
        PromptVaultSheet(
            sheetState = promptVaultSheetState,
            settingsManager = settingsManager,
            onDismiss = { showPromptVault = false },
            onInjectPrompt = { promptText, autoSubmit ->
                val targets = mutableListOf<WebView>()
                webViews[selectedService.name]?.let { targets.add(it) }
                if (isSplitScreenMode) {
                    webViews[secondaryService.name]?.let { targets.add(it) }
                }
                targets.forEach { wv ->
                    wv.evaluateJavascript(
                        buildPromptInjectionJs(promptText, autoSubmit = autoSubmit),
                        null
                    )
                }
                val actionName = if (autoSubmit) "injected & sent" else "injected"
                Toast.makeText(
                    context,
                    "Prompt $actionName into ${selectedService.name}" + if (isSplitScreenMode) " & ${secondaryService.name}" else "",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    if (showRequestNewAiScreen) {
        BackHandler { showRequestNewAiScreen = false }
        RequestNewAiScreen(
            onBack = { showRequestNewAiScreen = false },
            allServices = aiServices,
            settingsManager = settingsManager,
            onCustomServicesChanged = {
                scope.launch {
                    val updated = loadServices(context)
                    onServicesUpdated(updated)
                }
            },
            onSubmit = { content, method ->
                val title = Uri.encode("Request for new AI").replace("+", "%20")
                val body = Uri.encode(content).replace("+", "%20")

                if (method == "github") {
                    openInExternalBrowser(context, content)
                } else {
                    val mailtoUri = "mailto:$SUPPORT_EMAIL?subject=$title&body=$body".toUri()

                    val emailIntent = Intent(Intent.ACTION_VIEW, mailtoUri)
                    context.startActivity(Intent.createChooser(emailIntent, "Request new AI"))
                }
            },
        )
    }
}

@Composable
private fun SplitCompareDivider(
    primaryName: String,
    secondaryService: AiService,
    availableServices: List<AiService>,
    onSecondarySelected: (AiService) -> Unit,
    onClose: () -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 4.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.VerticalSplit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "$primaryName vs",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box {
                    AssistChip(
                        onClick = { dropdownExpanded = true },
                        label = {
                            Text(
                                secondaryService.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Rounded.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        availableServices.forEach { service ->
                            DropdownMenuItem(
                                text = { Text(service.name) },
                                onClick = {
                                    onSecondarySelected(service)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "Exit Split Screen",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}