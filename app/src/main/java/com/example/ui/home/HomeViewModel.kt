package com.example.ui.home

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.IptvRepository
import com.example.data.local.LauncherPreferences
import com.example.data.model.AppItem
import com.example.data.model.AppSection
import com.example.data.model.IptvChannel
import androidx.compose.ui.graphics.toArgb
import com.example.ui.common.AuroraColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.util.LocaleHelper

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

enum class EditingMode {
    NONE, MOVE, DELETE, HIDE, OPTIONS
}

data class ReorderState(
    val selectedPackage: String,
    val items: List<AppItem>
)

data class ChannelReorderState(
    val selectedChannelId: Long,
    val items: List<IptvChannel>
)

data class AppFocusRestoreRequest(
    val primaryPackage: String,
    val fallbackPackage: String? = null,
    val requestId: Long = System.nanoTime()
)

data class ChannelFocusRestoreRequest(
    val primaryChannelId: Long,
    val fallbackChannelId: Long? = null,
    val requestId: Long = System.nanoTime()
)

class HomeViewModel(
    private val appRepository: AppRepository,
    private val iptvRepository: IptvRepository,
    private val preferences: LauncherPreferences
) : ViewModel() {

    // ... (rest of the fields)

    private val _editingMode = MutableStateFlow(EditingMode.NONE)
    val editingMode: StateFlow<EditingMode> = _editingMode.asStateFlow()

    val installedApps: StateFlow<List<AppItem>> = appRepository.installedAppsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, appRepository.getInitialFastApps())

    val favoriteChannels: StateFlow<List<IptvChannel>> = iptvRepository.favoriteChannelsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allChannels: StateFlow<List<IptvChannel>> = iptvRepository.getChannelsByCategory("All")
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val appLockPin: StateFlow<String> = preferences.appLockPinFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "1234")

    val isDynamicBgEnabled: StateFlow<Boolean> = preferences.isDynamicBackgroundEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val wallpaperMode: StateFlow<String> = preferences.wallpaperModeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "dynamic")

    val customWallpaperUrl: StateFlow<String> = preferences.customWallpaperUrlFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val wallpaperVisibility: StateFlow<Int> = preferences.wallpaperVisibilityFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 100)

    val headerControlMode: StateFlow<String> = preferences.headerControlModeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "control_center")

    val isClock24h: StateFlow<Boolean> = preferences.isClock24h
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isClockSecondsEnabled: StateFlow<Boolean> = preferences.isClockSecondsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val clockSize: StateFlow<String> = preferences.clockSizeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "large")

    val dateFormatStyle: StateFlow<String> = preferences.dateFormatStyleFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "full")

    val greetingMode: StateFlow<String> = preferences.greetingModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "poetic")

    val customGreetingText: StateFlow<String> = preferences.customGreetingTextFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val appsPerRow: StateFlow<Int> = preferences.appsPerRowFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    val appsTopOffset: StateFlow<Int> = preferences.appsTopOffsetFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 450)

    val glassOpacity: StateFlow<Int> = preferences.glassOpacityFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 75)

    val screensaverTimeout: StateFlow<String> = preferences.screensaverTimeoutFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "5")

    val screensaverMode: StateFlow<String> = preferences.screensaverModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "clock_date")

    val screensaverWallpaper: StateFlow<String> = preferences.screensaverWallpaperFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "current")

    val screensaverVideo: StateFlow<String> = preferences.screensaverVideoFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "EG_g7BFaL5s")

    val showRecentApps: StateFlow<Boolean> = preferences.showRecentAppsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val recentApps: StateFlow<List<AppItem>> = combine(
        preferences.recentAppsFlow,
        installedApps
    ) { recentPkgs, apps ->
        val byPkg = apps.associateBy { it.packageName }
        recentPkgs.mapNotNull { byPkg[it] }.take(6)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isRtl: StateFlow<Boolean> = preferences.languageFlow
        .map { lang -> LocaleHelper.isRtlLanguage(lang) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _focusedColor = MutableStateFlow<Int?>(null)
    val focusedColor: StateFlow<Int?> = _focusedColor.asStateFlow()

    val defaultAuroraColors = AuroraColors()
    private val _auroraColors = MutableStateFlow(defaultAuroraColors)
    val auroraColors: StateFlow<AuroraColors> = _auroraColors.asStateFlow()

    private var focusColorDebounceJob: kotlinx.coroutines.Job? = null

    private val _selectedAppForMenu = MutableStateFlow<AppItem?>(null)
    val selectedAppForMenu: StateFlow<AppItem?> = _selectedAppForMenu.asStateFlow()

    private val _reorderState = MutableStateFlow<ReorderState?>(null)
    val reorderState: StateFlow<ReorderState?> = _reorderState.asStateFlow()

    private val _channelReorderState = MutableStateFlow<ChannelReorderState?>(null)
    val channelReorderState: StateFlow<ChannelReorderState?> = _channelReorderState.asStateFlow()

    private val _selectedChannelForMenu = MutableStateFlow<IptvChannel?>(null)
    val selectedChannelForMenu: StateFlow<IptvChannel?> = _selectedChannelForMenu.asStateFlow()

    private val _appFocusRestoreRequest = MutableStateFlow<AppFocusRestoreRequest?>(null)
    val appFocusRestoreRequest: StateFlow<AppFocusRestoreRequest?> = _appFocusRestoreRequest.asStateFlow()

    private val _channelFocusRestoreRequest = MutableStateFlow<ChannelFocusRestoreRequest?>(null)
    val channelFocusRestoreRequest: StateFlow<ChannelFocusRestoreRequest?> = _channelFocusRestoreRequest.asStateFlow()

    fun findNeighborAppPackage(targetPkg: String, items: List<AppItem> = installedApps.value): String? {
        val target = items.find { it.packageName == targetPkg } ?: return items.firstOrNull()?.packageName
        val sameSection = items.filter { it.section == target.section }
        val idx = sameSection.indexOfFirst { it.packageName == targetPkg }
        if (idx != -1) {
            sameSection.getOrNull(idx + 1)?.let { return it.packageName }
            sameSection.getOrNull(idx - 1)?.let { return it.packageName }
        }
        return items.firstOrNull { it.packageName != targetPkg }?.packageName
    }

    fun findNeighborChannelId(targetId: Long, items: List<IptvChannel> = favoriteChannels.value): Long? {
        val idx = items.indexOfFirst { it.id == targetId }
        if (idx != -1) {
            items.getOrNull(idx + 1)?.let { return it.id }
            items.getOrNull(idx - 1)?.let { return it.id }
        }
        return items.firstOrNull { it.id != targetId }?.id
    }

    fun requestAppFocusRestore(primaryPackage: String, fallbackPackage: String? = null) {
        _appFocusRestoreRequest.value = AppFocusRestoreRequest(
            primaryPackage = primaryPackage,
            fallbackPackage = fallbackPackage
        )
    }

    fun clearAppFocusRestore(requestId: Long) {
        if (_appFocusRestoreRequest.value?.requestId == requestId) {
            _appFocusRestoreRequest.value = null
        }
    }

    fun requestChannelFocusRestore(primaryChannelId: Long, fallbackChannelId: Long? = null) {
        _channelFocusRestoreRequest.value = ChannelFocusRestoreRequest(
            primaryChannelId = primaryChannelId,
            fallbackChannelId = fallbackChannelId
        )
    }

    fun clearChannelFocusRestore(requestId: Long) {
        if (_channelFocusRestoreRequest.value?.requestId == requestId) {
            _channelFocusRestoreRequest.value = null
        }
    }

    fun onFocusColorChanged(color: Int?) {
        if (!isDynamicBgEnabled.value || wallpaperMode.value != "dynamic") return
        if (_focusedColor.value == color) return
        focusColorDebounceJob?.cancel()
        focusColorDebounceJob = viewModelScope.launch {
            kotlinx.coroutines.delay(85L)
            if (_focusedColor.value != color) {
                _focusedColor.value = color
            }
        }
    }

    fun onAppFocused(app: AppItem) {
        if (!isDynamicBgEnabled.value || wallpaperMode.value != "dynamic") return
        val colors = app.auroraColors ?: defaultAuroraColors
        val glowArgb = colors.glow.toArgb()
        if (_auroraColors.value == colors && _focusedColor.value == glowArgb) return
        focusColorDebounceJob?.cancel()
        focusColorDebounceJob = viewModelScope.launch {
            kotlinx.coroutines.delay(85L)
            if (_auroraColors.value != colors) {
                _auroraColors.value = colors
            }
            if (_focusedColor.value != glowArgb) {
                _focusedColor.value = glowArgb
            }
        }
    }

    fun onNonAppFocused() {
        if (!isDynamicBgEnabled.value || wallpaperMode.value != "dynamic") return
        if (_auroraColors.value == defaultAuroraColors && _focusedColor.value == null) return
        focusColorDebounceJob?.cancel()
        focusColorDebounceJob = viewModelScope.launch {
            kotlinx.coroutines.delay(85L)
            if (_auroraColors.value != defaultAuroraColors) {
                _auroraColors.value = defaultAuroraColors
            }
            if (_focusedColor.value != null) {
                _focusedColor.value = null
            }
        }
    }

    fun setEditingMode(mode: EditingMode) {
        _editingMode.value = mode
    }

    fun recordAppLaunch(app: AppItem) {
        viewModelScope.launch {
            preferences.recordRecentApp(app.packageName)
        }
    }

    fun launchApp(app: AppItem) {
        when (_editingMode.value) {
            EditingMode.MOVE -> startReorder(app)
            EditingMode.HIDE -> hideApp(app)
            EditingMode.OPTIONS -> showAppMenu(app)
            EditingMode.DELETE, EditingMode.NONE -> {
                if (_reorderState.value != null) {
                    confirmReorder()
                    return
                }
                if (_editingMode.value == EditingMode.DELETE) {
                    // Logic for uninstall will be handled by the UI or calling uninstallApp
                }
                if (_editingMode.value == EditingMode.NONE) {
                    recordAppLaunch(app)
                    appRepository.launchApp(app)
                }
            }
        }
    }

    fun showAppMenu(app: AppItem) {
        _selectedAppForMenu.value = app
    }

    fun dismissAppMenu(restoreFocus: Boolean = false) {
        val previousPkg = _selectedAppForMenu.value?.packageName
        if (restoreFocus && previousPkg != null && _reorderState.value == null) {
            requestAppFocusRestore(primaryPackage = previousPkg)
        }
        _selectedAppForMenu.value = null
    }

    fun startReorder(app: AppItem) {
        if (_reorderState.value != null) {
            confirmReorder()
            return
        }
        val current = installedApps.value
        val exists = current.any { it.packageName == app.packageName }
        if (exists) {
            _reorderState.value = ReorderState(selectedPackage = app.packageName, items = current)
            _editingMode.value = EditingMode.MOVE
        }
        dismissAppMenu(restoreFocus = false)
    }

    fun startChannelReorder(channel: IptvChannel) {
        if (_channelReorderState.value != null) {
            confirmChannelReorder()
            return
        }
        val current = favoriteChannels.value
        if (current.any { it.id == channel.id }) {
            _channelReorderState.value = ChannelReorderState(selectedChannelId = channel.id, items = current)
            _editingMode.value = EditingMode.MOVE
        }
        dismissChannelMenu(restoreFocus = false)
    }

    fun showChannelMenu(channel: IptvChannel) {
        _selectedChannelForMenu.value = channel
    }

    fun dismissChannelMenu(restoreFocus: Boolean = false) {
        val previousId = _selectedChannelForMenu.value?.id
        if (restoreFocus && previousId != null && _channelReorderState.value == null) {
            requestChannelFocusRestore(primaryChannelId = previousId)
        }
        _selectedChannelForMenu.value = null
    }

    fun toggleFavorite(channel: IptvChannel) {
        val neighborId = findNeighborChannelId(channel.id)
        viewModelScope.launch {
            if (neighborId != null) {
                requestChannelFocusRestore(primaryChannelId = neighborId)
            } else {
                installedApps.value.firstOrNull()?.let { firstApp ->
                    requestAppFocusRestore(primaryPackage = firstApp.packageName)
                }
            }
            iptvRepository.toggleFavorite(channel)
            dismissChannelMenu(restoreFocus = false)
        }
    }

    private fun AppItem.withSection(section: AppSection): AppItem = when (section) {
        AppSection.APPS -> copy(isGame = false, isWeb = false)
        AppSection.WEB -> copy(isGame = false, isWeb = true)
        AppSection.GAMES -> copy(isGame = true, isWeb = false)
    }

    private fun partitionSections(items: List<AppItem>): Triple<MutableList<AppItem>, MutableList<AppItem>, MutableList<AppItem>> {
        val apps = items.filter { it.section == AppSection.APPS }.toMutableList()
        val web = items.filter { it.section == AppSection.WEB }.toMutableList()
        val games = items.filter { it.section == AppSection.GAMES }.toMutableList()
        return Triple(apps, web, games)
    }

    fun moveAppLeft() {
        val state = _reorderState.value ?: return
        val targetPkg = state.selectedPackage
        val currentItem = state.items.find { it.packageName == targetPkg } ?: return
        val (apps, web, games) = partitionSections(state.items)
        val list = when (currentItem.section) {
            AppSection.APPS -> apps
            AppSection.WEB -> web
            AppSection.GAMES -> games
        }
        val idx = list.indexOfFirst { it.packageName == targetPkg }
        if (idx > 0) {
            val temp = list[idx]
            list[idx] = list[idx - 1]
            list[idx - 1] = temp
            _reorderState.value = ReorderState(selectedPackage = targetPkg, items = apps + web + games)
        }
    }

    fun moveAppRight() {
        val state = _reorderState.value ?: return
        val targetPkg = state.selectedPackage
        val currentItem = state.items.find { it.packageName == targetPkg } ?: return
        val (apps, web, games) = partitionSections(state.items)
        val list = when (currentItem.section) {
            AppSection.APPS -> apps
            AppSection.WEB -> web
            AppSection.GAMES -> games
        }
        val idx = list.indexOfFirst { it.packageName == targetPkg }
        if (idx != -1 && idx < list.lastIndex) {
            val temp = list[idx]
            list[idx] = list[idx + 1]
            list[idx + 1] = temp
            _reorderState.value = ReorderState(selectedPackage = targetPkg, items = apps + web + games)
        }
    }

    fun moveAppUp() {
        val state = _reorderState.value ?: return
        val targetPkg = state.selectedPackage
        val currentItem = state.items.find { it.packageName == targetPkg } ?: return
        val step = appsPerRow.value.coerceIn(4, 7)
        val (apps, web, games) = partitionSections(state.items)

        when (currentItem.section) {
            AppSection.APPS -> {
                val idx = apps.indexOfFirst { it.packageName == targetPkg }
                if (idx >= step) {
                    val item = apps.removeAt(idx)
                    apps.add(idx - step, item)
                    _reorderState.value = ReorderState(selectedPackage = targetPkg, items = apps + web + games)
                }
            }
            AppSection.WEB -> {
                val idx = web.indexOfFirst { it.packageName == targetPkg }
                if (idx >= step) {
                    val item = web.removeAt(idx)
                    web.add(idx - step, item)
                    _reorderState.value = ReorderState(selectedPackage = targetPkg, items = apps + web + games)
                } else if (idx != -1) {
                    // Moving up from top row of Web into Apps section
                    web.removeAt(idx)
                    apps.add(currentItem.withSection(AppSection.APPS))
                    _reorderState.value = ReorderState(selectedPackage = targetPkg, items = apps + web + games)
                }
            }
            AppSection.GAMES -> {
                val idx = games.indexOfFirst { it.packageName == targetPkg }
                if (idx >= step) {
                    val item = games.removeAt(idx)
                    games.add(idx - step, item)
                    _reorderState.value = ReorderState(selectedPackage = targetPkg, items = apps + web + games)
                } else if (idx != -1) {
                    // Moving up from top row of Games into Web section
                    games.removeAt(idx)
                    web.add(currentItem.withSection(AppSection.WEB))
                    _reorderState.value = ReorderState(selectedPackage = targetPkg, items = apps + web + games)
                }
            }
        }
    }

    fun moveAppDown() {
        val state = _reorderState.value ?: return
        val targetPkg = state.selectedPackage
        val currentItem = state.items.find { it.packageName == targetPkg } ?: return
        val step = appsPerRow.value.coerceIn(4, 7)
        val (apps, web, games) = partitionSections(state.items)

        when (currentItem.section) {
            AppSection.APPS -> {
                val idx = apps.indexOfFirst { it.packageName == targetPkg }
                if (idx == -1) return
                val currentRow = idx / step
                val lastRow = (apps.size - 1) / step
                if (currentRow < lastRow) {
                    val targetIdx = (idx + step).coerceAtMost(apps.lastIndex)
                    val item = apps.removeAt(idx)
                    apps.add(targetIdx, item)
                } else {
                    // Moving down from bottom row of Apps into Web section
                    apps.removeAt(idx)
                    web.add(0, currentItem.withSection(AppSection.WEB))
                }
                _reorderState.value = ReorderState(selectedPackage = targetPkg, items = apps + web + games)
            }
            AppSection.WEB -> {
                val idx = web.indexOfFirst { it.packageName == targetPkg }
                if (idx == -1) return
                val currentRow = idx / step
                val lastRow = (web.size - 1) / step
                if (currentRow < lastRow) {
                    val targetIdx = (idx + step).coerceAtMost(web.lastIndex)
                    val item = web.removeAt(idx)
                    web.add(targetIdx, item)
                } else {
                    // Moving down from bottom row of Web into Games section
                    web.removeAt(idx)
                    games.add(0, currentItem.withSection(AppSection.GAMES))
                }
                _reorderState.value = ReorderState(selectedPackage = targetPkg, items = apps + web + games)
            }
            AppSection.GAMES -> {
                val idx = games.indexOfFirst { it.packageName == targetPkg }
                if (idx == -1) return
                val currentRow = idx / step
                val lastRow = (games.size - 1) / step
                if (currentRow < lastRow) {
                    val targetIdx = (idx + step).coerceAtMost(games.lastIndex)
                    val item = games.removeAt(idx)
                    games.add(targetIdx, item)
                    _reorderState.value = ReorderState(selectedPackage = targetPkg, items = apps + web + games)
                }
            }
        }
    }

    fun moveChannelLeft() {
        val state = _channelReorderState.value ?: return
        val currentItems = state.items.toMutableList()
        val idx = currentItems.indexOfFirst { it.id == state.selectedChannelId }
        if (idx > 0) {
            val temp = currentItems[idx]
            currentItems[idx] = currentItems[idx - 1]
            currentItems[idx - 1] = temp
            _channelReorderState.value = state.copy(items = currentItems)
        }
    }

    fun moveChannelRight() {
        val state = _channelReorderState.value ?: return
        val currentItems = state.items.toMutableList()
        val idx = currentItems.indexOfFirst { it.id == state.selectedChannelId }
        if (idx != -1 && idx < currentItems.lastIndex) {
            val temp = currentItems[idx]
            currentItems[idx] = currentItems[idx + 1]
            currentItems[idx + 1] = temp
            _channelReorderState.value = state.copy(items = currentItems)
        }
    }

    fun confirmChannelReorder() {
        val state = _channelReorderState.value ?: return
        val movedId = state.selectedChannelId
        val neighborId = findNeighborChannelId(movedId, state.items)
        val expectedIds = state.items.map { it.id }
        viewModelScope.launch {
            iptvRepository.saveFavoriteOrder(state.items)
            withTimeoutOrNull(350L) {
                favoriteChannels.first { list -> list.map { it.id } == expectedIds }
            }
            requestChannelFocusRestore(primaryChannelId = movedId, fallbackChannelId = neighborId)
            _channelReorderState.value = null
            _editingMode.value = EditingMode.NONE
            dismissChannelMenu(restoreFocus = false)
        }
    }

    fun confirmReorder() {
        val state = _reorderState.value ?: return
        val movedPkg = state.selectedPackage
        val neighborPkg = findNeighborAppPackage(movedPkg, state.items)
        val expectedOrder = state.items.map { it.packageName }
        val expectedSections = state.items.associate { it.packageName to it.section }
        val previousSections = installedApps.value.associate { it.packageName to it.section }
        val changedSections = state.items
            .filter { previousSections[it.packageName] != it.section }
            .associate { it.packageName to it.section }
        viewModelScope.launch {
            // Save complete ordered list of package names
            appRepository.saveNewOrder(state.items)

            // Persist all Apps / Web / Games section changes atomically in one DataStore write
            if (changedSections.isNotEmpty()) {
                preferences.setAppSectionsBatch(changedSections)
            }

            appRepository.triggerReload()
            // Wait until installedApps reflects the new order and sections so clearing reorderState never flickers
            withTimeoutOrNull(450L) {
                installedApps.first { list ->
                    list.map { it.packageName } == expectedOrder &&
                        list.all { cur ->
                            val expected = expectedSections[cur.packageName]
                            expected == null || expected == cur.section
                        }
                }
            }
            requestAppFocusRestore(primaryPackage = movedPkg, fallbackPackage = neighborPkg)
            _reorderState.value = null
            _editingMode.value = EditingMode.NONE
            dismissAppMenu(restoreFocus = false)
        }
    }

    fun moveAppToSection(app: AppItem, targetSection: AppSection) {
        val neighborPkg = findNeighborAppPackage(app.packageName)
        viewModelScope.launch {
            requestAppFocusRestore(
                primaryPackage = neighborPkg ?: app.packageName,
                fallbackPackage = app.packageName
            )
            preferences.setAppSection(app.packageName, targetSection)
            dismissAppMenu(restoreFocus = false)
            appRepository.triggerReload()
            withTimeoutOrNull(350L) {
                installedApps.first { list ->
                    list.find { it.packageName == app.packageName }?.section == targetSection
                }
            }
            requestAppFocusRestore(
                primaryPackage = neighborPkg ?: app.packageName,
                fallbackPackage = app.packageName
            )
        }
    }

    fun toggleAppGameCategory(app: AppItem) {
        moveAppToSection(app, if (app.isGame) AppSection.APPS else AppSection.GAMES)
    }

    fun toggleAppLock(app: AppItem) {
        viewModelScope.launch {
            val newLocked = !app.isLocked
            requestAppFocusRestore(primaryPackage = app.packageName)
            preferences.setAppLocked(app.packageName, newLocked)
            dismissAppMenu(restoreFocus = false)
            appRepository.triggerReload()
            withTimeoutOrNull(350L) {
                installedApps.first { list ->
                    list.find { it.packageName == app.packageName }?.isLocked == newLocked
                }
            }
            requestAppFocusRestore(primaryPackage = app.packageName)
        }
    }

    fun toggleClock24h() {
        viewModelScope.launch {
            preferences.setClock24h(!isClock24h.value)
        }
    }

    fun toggleDynamicBg() {
        viewModelScope.launch {
            val next = !isDynamicBgEnabled.value
            preferences.setDynamicBackgroundEnabled(next)
            preferences.setWallpaperMode(if (next) "dynamic" else "default")
        }
    }

    fun openPlayStoreGames(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/category/GAME".toUri()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, "market://search?q=games".toUri()).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    fun hideApp(app: AppItem) {
        val neighborPkg = findNeighborAppPackage(app.packageName)
        viewModelScope.launch {
            if (neighborPkg != null) {
                requestAppFocusRestore(primaryPackage = neighborPkg)
            }
            appRepository.hideApp(app.packageName)
            dismissAppMenu(restoreFocus = false)
            withTimeoutOrNull(350L) {
                installedApps.first { list -> list.none { it.packageName == app.packageName } }
            }
            if (neighborPkg != null) {
                requestAppFocusRestore(primaryPackage = neighborPkg)
            }
        }
    }

    fun deleteWebBookmark(app: AppItem) {
        val pkg = app.packageName
        if (!pkg.startsWith("com.bookmark.") || !pkg.endsWith(".portal")) return
        val id = pkg.removePrefix("com.bookmark.").removeSuffix(".portal")
        val neighborPkg = findNeighborAppPackage(pkg)
        viewModelScope.launch {
            if (neighborPkg != null) {
                requestAppFocusRestore(primaryPackage = neighborPkg)
            }
            preferences.removeCustomWebBookmark(id)
            dismissAppMenu(restoreFocus = false)
            appRepository.triggerReload()
            withTimeoutOrNull(350L) {
                installedApps.first { list -> list.none { it.packageName == pkg } }
            }
            if (neighborPkg != null) {
                requestAppFocusRestore(primaryPackage = neighborPkg)
            }
        }
    }

    fun openAppInfo(context: Context, app: AppItem) {
        dismissAppMenu()
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = "package:${app.packageName}".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun uninstallApp(context: Context, app: AppItem) {
        dismissAppMenu()
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = "package:${app.packageName}".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
    private val _showDefaultLauncherPrompt = MutableStateFlow(false)
    val showDefaultLauncherPrompt: StateFlow<Boolean> = _showDefaultLauncherPrompt.asStateFlow()

    private val _showStoragePermissionPrompt = MutableStateFlow(false)
    val showStoragePermissionPrompt: StateFlow<Boolean> = _showStoragePermissionPrompt.asStateFlow()

    init {
        checkDefaultLauncherPrompt()
        checkStoragePermissionPrompt()
    }

    private fun checkDefaultLauncherPrompt() {
        viewModelScope.launch {
            preferences.hasPromptedLauncherFlow.collect { hasPrompted ->
                _showDefaultLauncherPrompt.value = !hasPrompted
            }
        }
    }

    private fun checkStoragePermissionPrompt() {
        viewModelScope.launch {
            preferences.hasPromptedStorageFlow.collect { hasPrompted ->
                _showStoragePermissionPrompt.value = !hasPrompted
            }
        }
    }

    fun onDefaultLauncherPromptDismissed() {
        _showDefaultLauncherPrompt.value = false
        viewModelScope.launch {
            preferences.setHasPromptedLauncher(true)
        }
    }

    fun onStoragePermissionPromptDismissed() {
        _showStoragePermissionPrompt.value = false
        viewModelScope.launch {
            preferences.setHasPromptedStorage(true)
        }
    }

    fun isDefaultLauncher(context: Context): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            resolveInfo?.activityInfo?.packageName == context.packageName
        } catch (_: Exception) {
            false
        }
    }
}
