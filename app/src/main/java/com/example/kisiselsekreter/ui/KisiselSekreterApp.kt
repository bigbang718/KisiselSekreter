package com.example.kisiselsekreter.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.kisiselsekreter.data.DateTimeUtils
import com.example.kisiselsekreter.data.DefaultData
import com.example.kisiselsekreter.data.FolderItem
import com.example.kisiselsekreter.data.QuietHoursConfig
import com.example.kisiselsekreter.data.ReminderConfig
import com.example.kisiselsekreter.data.SubFolder
import com.example.kisiselsekreter.data.TaskItem
import com.example.kisiselsekreter.data.ThemePreference
import com.example.kisiselsekreter.ui.components.FolderEditorDialog
import com.example.kisiselsekreter.ui.components.MonthlyReportSheet
import com.example.kisiselsekreter.ui.components.PinnedReminderBanner
import com.example.kisiselsekreter.ui.components.QuickAddBottomSheet
import com.example.kisiselsekreter.ui.components.ScheduleTaskDialog
import com.example.kisiselsekreter.ui.components.SecretaryBottomBar
import com.example.kisiselsekreter.ui.components.SecretaryTopBar
import com.example.kisiselsekreter.ui.components.SnoozeBottomSheet
import com.example.kisiselsekreter.ui.components.SubFolderEditorDialog
import com.example.kisiselsekreter.ui.components.TaskEditorDialog
import com.example.kisiselsekreter.ui.navigation.AppTab
import com.example.kisiselsekreter.ui.screens.FolderDetailScreen
import com.example.kisiselsekreter.ui.screens.FoldersScreen
import com.example.kisiselsekreter.ui.screens.HistoryReportScreen
import com.example.kisiselsekreter.ui.screens.SettingsScreen
import com.example.kisiselsekreter.ui.screens.TodayScreen
import com.example.kisiselsekreter.ui.theme.FolderColorTone
import com.example.kisiselsekreter.ui.theme.FolderIconKey
import com.example.kisiselsekreter.ui.theme.KisiselSekreterTheme
import com.example.kisiselsekreter.ui.viewmodel.SecretaryUiState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun KisiselSekreterApp(
    uiState: SecretaryUiState,
    hasNotificationPermission: Boolean,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onRequestNotificationPermission: () -> Unit,
    onToggleTheme: () -> Unit,
    onSelectThemePreference: (ThemePreference) -> Unit,
    onUpdateQuietHours: (QuietHoursConfig) -> Unit,
    onTriggerTestReminder: () -> Unit,
    onExportBackupJson: () -> Unit,
    onImportBackupJson: () -> Unit,
    onMarkMonthlyReportSeen: (String) -> Unit,
    onOpenFolder: (String) -> Unit,
    onCloseFolder: () -> Unit,
    onSelectSubFolder: (String?) -> Unit,
    onAddFolder: (String, FolderColorTone, FolderIconKey) -> Unit,
    onUpdateFolder: (String, String, FolderColorTone, FolderIconKey) -> Unit,
    onDeleteFolder: (String) -> Unit,
    onAddSubFolder: (String, String) -> Unit,
    onUpdateSubFolder: (String, String, String) -> Unit,
    onDeleteSubFolder: (String, String) -> Unit,
    onRestoreDefaultFolders: () -> Unit,
    onAddTask: (
        title: String,
        note: String,
        folderId: String,
        subFolderId: String?,
        isPlanned: Boolean,
        scheduledDateMillis: Long?,
        scheduledHour: Int?,
        scheduledMinute: Int?,
        reminderConfig: ReminderConfig,
        hasCounter: Boolean,
        counterTarget: Int,
        counterUnit: String
    ) -> Unit,
    onAddQuickUnplannedTask: (folderId: String, subFolderId: String?, title: String) -> Unit,
    onUpdateTask: (TaskItem) -> Unit,
    onScheduleTask: (
        taskId: String,
        dateMillis: Long,
        hour: Int?,
        minute: Int?,
        reminderConfig: ReminderConfig
    ) -> Unit,
    onSnoozeTask: (taskId: String, newDateMillis: Long, newHour: Int?, newMinute: Int?) -> Unit,
    onToggleTaskComplete: (String) -> Unit,
    onIncrementTaskCounter: (String) -> Unit,
    onDecrementTaskCounter: (String) -> Unit,
    onDeleteTask: (String) -> Unit,
    initialTab: AppTab = AppTab.FOLDERS
) {
    val colors = MaterialTheme.colorScheme
    var selectedTab by rememberSaveable { mutableStateOf(initialTab) }
    var showQuickAddSheet by remember { mutableStateOf(false) }

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var editingFolder by remember { mutableStateOf<FolderItem?>(null) }

    var subFolderDialogParent by remember { mutableStateOf<FolderItem?>(null) }
    var editingSubFolder by remember { mutableStateOf<SubFolder?>(null) }

    var schedulingTask by remember { mutableStateOf<TaskItem?>(null) }
    var snoozingTask by remember { mutableStateOf<TaskItem?>(null) }
    var editingTask by remember { mutableStateOf<TaskItem?>(null) }

    val (defaultYear, defaultMonth) = remember { DateTimeUtils.currentYearMonth() }
    var showMonthlyReportSheet by remember { mutableStateOf(false) }
    var monthlyReportYear by remember { mutableIntStateOf(defaultYear) }
    var monthlyReportMonth by remember { mutableIntStateOf(defaultMonth) }
    var monthlyReportFirstDayWelcome by remember { mutableStateOf(false) }

    // Her ayın 1'inde kullanıcıyı önceki ayın sakin istatistik raporuyla karşıla
    LaunchedEffect(uiState.isLoading, uiState.lastMonthlyReportSeenKey) {
        if (!uiState.isLoading) {
            val todayMonthKey = DateTimeUtils.currentYearMonthKey()
            if (DateTimeUtils.isFirstDayOfMonth() && uiState.lastMonthlyReportSeenKey != todayMonthKey) {
                val (curYear, curMonth) = DateTimeUtils.currentYearMonth()
                val (prevYear, prevMonth) = DateTimeUtils.shiftMonth(curYear, curMonth, -1)
                monthlyReportYear = prevYear
                monthlyReportMonth = prevMonth
                monthlyReportFirstDayWelcome = true
                showMonthlyReportSheet = true
            }
        }
    }

    // Zamanı gelen hatırlatmaları gerçek zamanlı takip etmek için 30 saniyelik hafif saat
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30.seconds)
            nowMillis = System.currentTimeMillis()
        }
    }

    val folderMap = remember(uiState.folders) { uiState.folders.associateBy { it.id } }
    val dueTasks = remember(uiState.tasks, nowMillis) {
        uiState.tasks
            .filter { it.isDueNow(nowMillis) }
            .sortedBy { it.scheduledSortKey }
    }

    val openedFolder = if (selectedTab == AppTab.FOLDERS) uiState.openedFolder else null

    BackHandler(enabled = openedFolder != null) {
        onCloseFolder()
    }

    val topBarTitle = when {
        openedFolder != null -> openedFolder.name
        selectedTab == AppTab.FOLDERS -> "Kişisel Sekreter"
        else -> selectedTab.label
    }
    val topBarSubtitle = when {
        openedFolder != null -> "Klasör Detayı"
        selectedTab == AppTab.FOLDERS -> "Klasörler"
        else -> null
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = RoundedCornerShape(16.dp),
                    containerColor = colors.onSurface,
                    contentColor = colors.surface,
                    actionColor = colors.primaryContainer
                )
            }
        },
        topBar = {
            SecretaryTopBar(
                title = topBarTitle,
                subtitle = topBarSubtitle,
                showBackButton = openedFolder != null,
                onBackClick = onCloseFolder,
                onToggleTheme = onToggleTheme
            )
        },
        bottomBar = {
            SecretaryBottomBar(
                selectedTab = selectedTab,
                onSelectTab = { nextTab ->
                    if (nextTab == AppTab.FOLDERS && selectedTab == AppTab.FOLDERS && openedFolder != null) {
                        onCloseFolder()
                    }
                    selectedTab = nextTab
                }
            )
        },
        floatingActionButton = {
            Surface(
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.4f)),
                color = colors.primary
            ) {
                FloatingActionButton(
                    onClick = { showQuickAddSheet = true },
                    shape = RoundedCornerShape(18.dp),
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        focusedElevation = 0.dp,
                        hoveredElevation = 0.dp
                    ),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Hızlı Ekle",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Zamanı gelmiş hatırlatmalar için üst alanda sabit, sakin kart
            if (dueTasks.isNotEmpty() && selectedTab != AppTab.SETTINGS) {
                PinnedReminderBanner(
                    dueTasks = dueTasks,
                    folderMap = folderMap,
                    onMarkDone = { task -> onToggleTaskComplete(task.id) },
                    onSnoozeClick = { task -> snoozingTask = task }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    AppTab.FOLDERS -> {
                        if (openedFolder != null) {
                            FolderDetailScreen(
                                folder = openedFolder,
                                selectedSubFolderId = uiState.selectedSubFolderId,
                                tasks = uiState.tasks,
                                onSelectSubFolder = onSelectSubFolder,
                                onEditFolder = { editingFolder = openedFolder },
                                onAddSubFolder = {
                                    editingSubFolder = null
                                    subFolderDialogParent = openedFolder
                                },
                                onEditSubFolder = { sub ->
                                    editingSubFolder = sub
                                    subFolderDialogParent = openedFolder
                                },
                                onAddQuickUnplannedTask = { title ->
                                    onAddQuickUnplannedTask(openedFolder.id, uiState.selectedSubFolderId, title)
                                },
                                onPlanTaskClick = { task -> schedulingTask = task },
                                onSnoozeTaskClick = { task -> snoozingTask = task },
                                onEditTaskClick = { task -> editingTask = task },
                                onToggleTaskComplete = onToggleTaskComplete,
                                onIncrementCounter = onIncrementTaskCounter,
                                onDecrementCounter = onDecrementTaskCounter,
                                onDeleteTask = onDeleteTask
                            )
                        } else {
                            FoldersScreen(
                                folders = uiState.folders,
                                tasks = uiState.tasks,
                                onFolderClick = { onOpenFolder(it.id) },
                                onEditFolderClick = { editingFolder = it },
                                onAddFolderClick = { showCreateFolderDialog = true }
                            )
                        }
                    }

                    AppTab.TODAY -> {
                        TodayScreen(
                            folders = uiState.folders,
                            tasks = uiState.tasks,
                            onToggleTaskComplete = onToggleTaskComplete,
                            onSnoozeTaskClick = { task -> snoozingTask = task },
                            onEditTaskClick = { task -> editingTask = task },
                            onIncrementCounter = onIncrementTaskCounter,
                            onDecrementCounter = onDecrementTaskCounter,
                            onDeleteTask = onDeleteTask
                        )
                    }

                    AppTab.HISTORY_REPORT -> {
                        HistoryReportScreen(
                            folders = uiState.folders,
                            tasks = uiState.tasks,
                            onUndoComplete = onToggleTaskComplete,
                            onOpenMonthlyReport = {
                                val (curYear, curMonth) = DateTimeUtils.currentYearMonth()
                                monthlyReportYear = curYear
                                monthlyReportMonth = curMonth
                                monthlyReportFirstDayWelcome = DateTimeUtils.isFirstDayOfMonth()
                                showMonthlyReportSheet = true
                            }
                        )
                    }

                    AppTab.SETTINGS -> {
                        SettingsScreen(
                            themePreference = uiState.themePreference,
                            quietHours = uiState.quietHours,
                            folders = uiState.folders,
                            taskCount = uiState.tasks.size,
                            hasNotificationPermission = hasNotificationPermission,
                            onRequestNotificationPermission = onRequestNotificationPermission,
                            onSelectTheme = onSelectThemePreference,
                            onUpdateQuietHours = onUpdateQuietHours,
                            onTriggerTestReminder = onTriggerTestReminder,
                            onExportBackupJson = onExportBackupJson,
                            onImportBackupJson = onImportBackupJson,
                            onAddFolderClick = { showCreateFolderDialog = true },
                            onRestoreDefaultFolders = onRestoreDefaultFolders
                        )
                    }
                }
            }
        }
    }

    if (showMonthlyReportSheet) {
        MonthlyReportSheet(
            folders = uiState.folders,
            tasks = uiState.tasks,
            initialYear = monthlyReportYear,
            initialMonthZeroBased = monthlyReportMonth,
            isFirstOfMonthWelcome = monthlyReportFirstDayWelcome,
            onDismiss = {
                if (monthlyReportFirstDayWelcome || DateTimeUtils.isFirstDayOfMonth()) {
                    onMarkMonthlyReportSeen(DateTimeUtils.currentYearMonthKey())
                }
                showMonthlyReportSheet = false
                monthlyReportFirstDayWelcome = false
            },
            onReviewTaskClick = { task ->
                if (monthlyReportFirstDayWelcome || DateTimeUtils.isFirstDayOfMonth()) {
                    onMarkMonthlyReportSeen(DateTimeUtils.currentYearMonthKey())
                }
                showMonthlyReportSheet = false
                monthlyReportFirstDayWelcome = false
                editingTask = task
            }
        )
    }

    if (showQuickAddSheet) {
        QuickAddBottomSheet(
            folders = uiState.folders,
            initialFolderId = openedFolder?.id ?: uiState.folders.firstOrNull()?.id,
            initialSubFolderId = if (openedFolder != null) uiState.selectedSubFolderId else null,
            defaultPlanned = selectedTab == AppTab.TODAY,
            onDismiss = { showQuickAddSheet = false },
            onAddTask = { title, note, folderId, subFolderId, isPlanned, dateMillis, hour, minute, reminderConfig, hasCounter, counterTarget, counterUnit ->
                if (isPlanned && !hasNotificationPermission) {
                    onRequestNotificationPermission()
                }
                onAddTask(
                    title,
                    note,
                    folderId,
                    subFolderId,
                    isPlanned,
                    dateMillis,
                    hour,
                    minute,
                    reminderConfig,
                    hasCounter,
                    counterTarget,
                    counterUnit
                )
            },
            onAddFolder = onAddFolder,
            onAddSubFolder = onAddSubFolder
        )
    }

    schedulingTask?.let { task ->
        ScheduleTaskDialog(
            task = task,
            onDismiss = { schedulingTask = null },
            onConfirmSchedule = { dateMillis, hour, minute, reminderConfig ->
                if (!hasNotificationPermission) {
                    onRequestNotificationPermission()
                }
                onScheduleTask(task.id, dateMillis, hour, minute, reminderConfig)
                schedulingTask = null
            }
        )
    }

    snoozingTask?.let { task ->
        SnoozeBottomSheet(
            task = task,
            folderName = folderMap[task.folderId]?.name,
            onDismiss = { snoozingTask = null },
            onSnoozeSelected = { newDateMillis, newHour, newMinute ->
                onSnoozeTask(task.id, newDateMillis, newHour, newMinute)
                snoozingTask = null
            }
        )
    }

    editingTask?.let { task ->
        TaskEditorDialog(
            task = task,
            folders = uiState.folders,
            onDismiss = { editingTask = null },
            onSave = { updated ->
                if (updated.isPlanned && !hasNotificationPermission) {
                    onRequestNotificationPermission()
                }
                onUpdateTask(updated)
                editingTask = null
            },
            onDelete = {
                onDeleteTask(task.id)
                editingTask = null
            }
        )
    }

    if (showCreateFolderDialog) {
        FolderEditorDialog(
            existingFolder = null,
            onDismiss = { showCreateFolderDialog = false },
            onSave = { name, tone, icon ->
                onAddFolder(name, tone, icon)
                showCreateFolderDialog = false
            }
        )
    }

    editingFolder?.let { folder ->
        FolderEditorDialog(
            existingFolder = folder,
            onDismiss = { editingFolder = null },
            onSave = { name, tone, icon ->
                onUpdateFolder(folder.id, name, tone, icon)
                editingFolder = null
            },
            onDelete = {
                onDeleteFolder(folder.id)
                editingFolder = null
            }
        )
    }

    subFolderDialogParent?.let { parent ->
        SubFolderEditorDialog(
            parentFolderName = parent.name,
            existingSubFolder = editingSubFolder,
            onDismiss = {
                subFolderDialogParent = null
                editingSubFolder = null
            },
            onSave = { subName ->
                val currentSub = editingSubFolder
                if (currentSub == null) {
                    onAddSubFolder(parent.id, subName)
                } else {
                    onUpdateSubFolder(parent.id, currentSub.id, subName)
                }
                subFolderDialogParent = null
                editingSubFolder = null
            },
            onDelete = editingSubFolder?.let { currentSub ->
                {
                    onDeleteSubFolder(parent.id, currentSub.id)
                    subFolderDialogParent = null
                    editingSubFolder = null
                }
            }
        )
    }
}

@Preview(showBackground = true, name = "Kişisel Sekreter - Geçmiş & Rapor")
@Composable
private fun KisiselSekreterHistoryPreview() {
    KisiselSekreterTheme(darkTheme = false) {
        KisiselSekreterApp(
            uiState = SecretaryUiState(
                folders = DefaultData.defaultFolders(),
                tasks = DefaultData.defaultSampleTasks(),
                isLoading = false
            ),
            hasNotificationPermission = true,
            onRequestNotificationPermission = {},
            onToggleTheme = {},
            onSelectThemePreference = {},
            onUpdateQuietHours = {},
            onTriggerTestReminder = {},
            onExportBackupJson = {},
            onImportBackupJson = {},
            onMarkMonthlyReportSeen = {},
            onOpenFolder = {},
            onCloseFolder = {},
            onSelectSubFolder = {},
            onAddFolder = { _, _, _ -> },
            onUpdateFolder = { _, _, _, _ -> },
            onDeleteFolder = {},
            onAddSubFolder = { _, _ -> },
            onUpdateSubFolder = { _, _, _ -> },
            onDeleteSubFolder = { _, _ -> },
            onRestoreDefaultFolders = {},
            onAddTask = { _, _, _, _, _, _, _, _, _, _, _, _ -> },
            onAddQuickUnplannedTask = { _, _, _ -> },
            onUpdateTask = {},
            onScheduleTask = { _, _, _, _, _ -> },
            onSnoozeTask = { _, _, _, _ -> },
            onToggleTaskComplete = {},
            onIncrementTaskCounter = {},
            onDecrementTaskCounter = {},
            onDeleteTask = {},
            initialTab = AppTab.HISTORY_REPORT
        )
    }
}
