package com.example.kisiselsekreter.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kisiselsekreter.data.BackupPayload
import com.example.kisiselsekreter.data.DateTimeUtils
import com.example.kisiselsekreter.data.DefaultData
import com.example.kisiselsekreter.data.FolderItem
import com.example.kisiselsekreter.data.LocalStorageRepository
import com.example.kisiselsekreter.data.QuietHoursConfig
import com.example.kisiselsekreter.data.ReminderConfig
import com.example.kisiselsekreter.data.ReminderType
import com.example.kisiselsekreter.data.SubFolder
import com.example.kisiselsekreter.data.TaskItem
import com.example.kisiselsekreter.data.ThemePreference
import com.example.kisiselsekreter.notifications.ReminderNotificationHelper
import com.example.kisiselsekreter.notifications.ReminderScheduler
import com.example.kisiselsekreter.notifications.TestReminderResult
import com.example.kisiselsekreter.ui.theme.FolderColorTone
import com.example.kisiselsekreter.ui.theme.FolderIconKey
import com.example.kisiselsekreter.widget.SecretaryWidgetUpdater
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class SecretaryUiState(
    val folders: List<FolderItem> = emptyList(),
    val tasks: List<TaskItem> = emptyList(),
    val themePreference: ThemePreference = ThemePreference.LIGHT,
    val quietHours: QuietHoursConfig = QuietHoursConfig(),
    val lastMonthlyReportSeenKey: String = "",
    val openedFolderId: String? = null,
    val selectedSubFolderId: String? = null,
    val isLoading: Boolean = true
) {
    val openedFolder: FolderItem?
        get() = folders.firstOrNull { it.id == openedFolderId }
}

class SecretaryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LocalStorageRepository(application.applicationContext)
    private val reminderScheduler = ReminderScheduler(application.applicationContext)

    private val _uiState = MutableStateFlow(SecretaryUiState())
    val uiState: StateFlow<SecretaryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
        }
        viewModelScope.launch {
            repository.dataFlow.collect { data ->
                if (data.isInitialized) {
                    _uiState.update { state ->
                        state.copy(
                            folders = data.folders,
                            tasks = data.tasks,
                            themePreference = data.themePreference,
                            quietHours = data.quietHours,
                            lastMonthlyReportSeenKey = data.lastMonthlyReportSeenKey,
                            isLoading = false
                        )
                    }
                    reminderScheduler.syncAllReminders(data.tasks, data.folders)
                }
            }
        }
    }

    private fun folderNameOf(folderId: String): String =
        _uiState.value.folders.firstOrNull { it.id == folderId }?.name.orEmpty()

    private fun persistTasks(updated: List<TaskItem>) {
        viewModelScope.launch {
            repository.saveTasks(updated)
            SecretaryWidgetUpdater.refreshAll(getApplication<Application>().applicationContext)
        }
    }

    private fun persistFolders(updated: List<FolderItem>) {
        viewModelScope.launch {
            repository.saveFolders(updated)
            SecretaryWidgetUpdater.refreshAll(getApplication<Application>().applicationContext)
        }
    }

    private fun persistFoldersAndTasks(folders: List<FolderItem>, tasks: List<TaskItem>) {
        viewModelScope.launch {
            repository.saveFoldersAndTasks(folders, tasks)
            SecretaryWidgetUpdater.refreshAll(getApplication<Application>().applicationContext)
        }
    }

    fun setThemePreference(preference: ThemePreference) {
        _uiState.update { it.copy(themePreference = preference) }
        viewModelScope.launch {
            repository.saveThemePreference(preference)
            SecretaryWidgetUpdater.refreshAll(getApplication<Application>().applicationContext)
        }
    }

    fun toggleLightDark(isCurrentlyDark: Boolean) {
        val next = if (isCurrentlyDark) ThemePreference.LIGHT else ThemePreference.DARK
        setThemePreference(next)
    }

    fun updateQuietHours(config: QuietHoursConfig) {
        _uiState.update { it.copy(quietHours = config) }
        viewModelScope.launch {
            repository.saveQuietHours(config)
        }
    }

    fun markMonthlyReportSeen(yearMonthKey: String) {
        _uiState.update { it.copy(lastMonthlyReportSeenKey = yearMonthKey) }
        viewModelScope.launch {
            repository.saveLastMonthlyReportSeenKey(yearMonthKey)
        }
    }

    fun triggerTestReminder(): TestReminderResult {
        return ReminderNotificationHelper.triggerTestReminder(
            context = getApplication<Application>().applicationContext,
            quietHours = _uiState.value.quietHours
        )
    }

    fun buildBackupJson(): String {
        val state = _uiState.value
        return repository.buildBackupJson(
            themePreference = state.themePreference,
            quietHours = state.quietHours,
            folders = state.folders,
            tasks = state.tasks
        )
    }

    fun parseBackupJson(rawJson: String): Result<BackupPayload> {
        return repository.parseBackupJson(rawJson)
    }

    fun restoreFromBackup(payload: BackupPayload) {
        val previousTasks = _uiState.value.tasks
        previousTasks.forEach { reminderScheduler.cancelTaskReminder(it.id) }
        _uiState.update { state ->
            state.copy(
                folders = payload.folders,
                tasks = payload.tasks,
                themePreference = payload.themePreference,
                quietHours = payload.quietHours,
                openedFolderId = null,
                selectedSubFolderId = null,
                isLoading = false
            )
        }
        reminderScheduler.syncAllReminders(payload.tasks, payload.folders)
        viewModelScope.launch {
            repository.restoreFromBackup(payload)
            SecretaryWidgetUpdater.refreshAll(getApplication<Application>().applicationContext)
        }
    }

    fun openFolder(folderId: String) {
        _uiState.update { it.copy(openedFolderId = folderId, selectedSubFolderId = null) }
    }

    fun closeFolder() {
        _uiState.update { it.copy(openedFolderId = null, selectedSubFolderId = null) }
    }

    fun selectSubFolder(subFolderId: String?) {
        _uiState.update { it.copy(selectedSubFolderId = subFolderId) }
    }

    fun addFolder(name: String, colorTone: FolderColorTone, iconKey: FolderIconKey) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val newFolder = FolderItem(
            id = UUID.randomUUID().toString(),
            name = trimmed,
            colorTone = colorTone,
            iconKey = iconKey,
            isDefault = false
        )
        val updated = _uiState.value.folders + newFolder
        _uiState.update { it.copy(folders = updated) }
        persistFolders(updated)
    }

    fun updateFolder(folderId: String, name: String, colorTone: FolderColorTone, iconKey: FolderIconKey) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val updated = _uiState.value.folders.map { folder ->
            if (folder.id == folderId) {
                folder.copy(name = trimmed, colorTone = colorTone, iconKey = iconKey)
            } else {
                folder
            }
        }
        _uiState.update { it.copy(folders = updated) }
        persistFolders(updated)
    }

    fun deleteFolder(folderId: String) {
        val deletedTasks = _uiState.value.tasks.filter { it.folderId == folderId }
        deletedTasks.forEach { reminderScheduler.cancelTaskReminder(it.id) }
        val updatedFolders = _uiState.value.folders.filterNot { it.id == folderId }
        val updatedTasks = _uiState.value.tasks.filterNot { it.folderId == folderId }
        _uiState.update { state ->
            state.copy(
                folders = updatedFolders,
                tasks = updatedTasks,
                openedFolderId = if (state.openedFolderId == folderId) null else state.openedFolderId,
                selectedSubFolderId = if (state.openedFolderId == folderId) null else state.selectedSubFolderId
            )
        }
        persistFoldersAndTasks(updatedFolders, updatedTasks)
    }

    fun addSubFolder(folderId: String, subFolderName: String) {
        val trimmed = subFolderName.trim()
        if (trimmed.isEmpty()) return
        val newSub = SubFolder(name = trimmed)
        val updated = _uiState.value.folders.map { folder ->
            if (folder.id == folderId) {
                folder.copy(subFolders = folder.subFolders + newSub)
            } else {
                folder
            }
        }
        _uiState.update { it.copy(folders = updated) }
        persistFolders(updated)
    }

    fun updateSubFolder(folderId: String, subFolderId: String, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        val updated = _uiState.value.folders.map { folder ->
            if (folder.id == folderId) {
                folder.copy(
                    subFolders = folder.subFolders.map { sub ->
                        if (sub.id == subFolderId) sub.copy(name = trimmed) else sub
                    }
                )
            } else {
                folder
            }
        }
        _uiState.update { it.copy(folders = updated) }
        persistFolders(updated)
    }

    fun deleteSubFolder(folderId: String, subFolderId: String) {
        val updatedFolders = _uiState.value.folders.map { folder ->
            if (folder.id == folderId) {
                folder.copy(subFolders = folder.subFolders.filterNot { it.id == subFolderId })
            } else {
                folder
            }
        }
        val updatedTasks = _uiState.value.tasks.map { task ->
            if (task.folderId == folderId && task.subFolderId == subFolderId) {
                task.copy(subFolderId = null)
            } else {
                task
            }
        }
        _uiState.update { state ->
            state.copy(
                folders = updatedFolders,
                tasks = updatedTasks,
                selectedSubFolderId = if (state.selectedSubFolderId == subFolderId) null else state.selectedSubFolderId
            )
        }
        persistFoldersAndTasks(updatedFolders, updatedTasks)
    }

    fun restoreMissingDefaultFolders() {
        val current = _uiState.value.folders
        val existingIds = current.map { it.id }.toSet()
        val missing = DefaultData.defaultFolders().filterNot { it.id in existingIds }
        if (missing.isEmpty()) return
        val merged = current + missing
        _uiState.update { it.copy(folders = merged) }
        persistFolders(merged)
    }

    fun addTask(
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
    ) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) return
        val effectivePlannedDate = if (isPlanned) {
            scheduledDateMillis ?: DateTimeUtils.startOfDayMillis(0)
        } else {
            null
        }
        val safeTarget = if (hasCounter) counterTarget.coerceAtLeast(1) else 0
        val newTask = TaskItem(
            title = trimmedTitle,
            note = note.trim(),
            folderId = folderId,
            subFolderId = subFolderId,
            isPlanned = isPlanned,
            scheduledDateMillis = effectivePlannedDate,
            scheduledHour = if (isPlanned) scheduledHour else null,
            scheduledMinute = if (isPlanned) scheduledMinute else null,
            reminderType = if (isPlanned) reminderConfig.reminderType else ReminderType.NONE,
            routineFrequency = reminderConfig.routineFrequency,
            routineWeekDays = reminderConfig.routineWeekDays,
            flexibleRepeatAfterDays = reminderConfig.flexibleRepeatAfterDays,
            flexibleTimeWindow = reminderConfig.flexibleTimeWindow,
            hasCounter = hasCounter && safeTarget > 0,
            counterCurrent = 0,
            counterTarget = safeTarget,
            counterUnit = if (hasCounter) counterUnit.trim() else ""
        )
        val updated = listOf(newTask) + _uiState.value.tasks
        _uiState.update { it.copy(tasks = updated) }
        reminderScheduler.scheduleTaskReminder(newTask, folderNameOf(folderId))
        persistTasks(updated)
    }

    fun addQuickUnplannedTask(folderId: String, subFolderId: String?, title: String) {
        addTask(
            title = title,
            note = "",
            folderId = folderId,
            subFolderId = subFolderId,
            isPlanned = false,
            scheduledDateMillis = null,
            scheduledHour = null,
            scheduledMinute = null,
            reminderConfig = ReminderConfig(),
            hasCounter = false,
            counterTarget = 0,
            counterUnit = ""
        )
    }

    fun updateTask(updatedTask: TaskItem) {
        val trimmedTitle = updatedTask.title.trim()
        if (trimmedTitle.isEmpty()) return
        val safeTarget = if (updatedTask.hasCounter) updatedTask.counterTarget.coerceAtLeast(1) else 0
        val safeCurrent = if (updatedTask.hasCounter) {
            updatedTask.counterCurrent.coerceIn(0, safeTarget)
        } else {
            0
        }
        val normalized = updatedTask.copy(
            title = trimmedTitle,
            note = updatedTask.note.trim(),
            scheduledDateMillis = if (updatedTask.isPlanned) {
                updatedTask.scheduledDateMillis ?: DateTimeUtils.startOfDayMillis(0)
            } else {
                null
            },
            scheduledHour = if (updatedTask.isPlanned) updatedTask.scheduledHour else null,
            scheduledMinute = if (updatedTask.isPlanned) updatedTask.scheduledMinute else null,
            reminderType = if (updatedTask.isPlanned) updatedTask.reminderType else ReminderType.NONE,
            hasCounter = updatedTask.hasCounter && safeTarget > 0,
            counterCurrent = safeCurrent,
            counterTarget = safeTarget,
            counterUnit = if (updatedTask.hasCounter) updatedTask.counterUnit.trim() else ""
        )
        val updated = _uiState.value.tasks.map { if (it.id == normalized.id) normalized else it }
        _uiState.update { it.copy(tasks = updated) }
        reminderScheduler.scheduleTaskReminder(normalized, folderNameOf(normalized.folderId))
        persistTasks(updated)
    }

    fun scheduleTask(
        taskId: String,
        dateMillis: Long,
        hour: Int?,
        minute: Int?,
        reminderConfig: ReminderConfig
    ) {
        var rescheduled: TaskItem? = null
        val updated = _uiState.value.tasks.map { task ->
            if (task.id == taskId) {
                val next = task.copy(
                    isPlanned = true,
                    scheduledDateMillis = dateMillis,
                    scheduledHour = hour,
                    scheduledMinute = minute,
                    reminderType = reminderConfig.reminderType,
                    routineFrequency = reminderConfig.routineFrequency,
                    routineWeekDays = reminderConfig.routineWeekDays,
                    flexibleRepeatAfterDays = reminderConfig.flexibleRepeatAfterDays,
                    flexibleTimeWindow = reminderConfig.flexibleTimeWindow
                )
                rescheduled = next
                next
            } else {
                task
            }
        }
        _uiState.update { it.copy(tasks = updated) }
        rescheduled?.let { reminderScheduler.scheduleTaskReminder(it, folderNameOf(it.folderId)) }
        persistTasks(updated)
    }

    fun snoozeTask(taskId: String, newDateMillis: Long, newHour: Int?, newMinute: Int?) {
        var snoozed: TaskItem? = null
        val now = System.currentTimeMillis()
        val updated = _uiState.value.tasks.map { task ->
            if (task.id == taskId) {
                val next = task.copy(
                    isPlanned = true,
                    isCompleted = false,
                    completedAtMillis = null,
                    scheduledDateMillis = newDateMillis,
                    scheduledHour = newHour,
                    scheduledMinute = newMinute,
                    snoozeCount = task.snoozeCount + 1,
                    lastSnoozedAtMillis = now
                )
                snoozed = next
                next
            } else {
                task
            }
        }
        _uiState.update { it.copy(tasks = updated) }
        snoozed?.let { reminderScheduler.scheduleTaskReminder(it, folderNameOf(it.folderId)) }
        persistTasks(updated)
    }

    fun incrementTaskCounter(taskId: String) {
        val targetTask = _uiState.value.tasks.firstOrNull { it.id == taskId } ?: return
        if (!targetTask.hasCounter || targetTask.counterTarget <= 0) return

        val nextCount = (targetTask.counterCurrent + 1).coerceAtMost(targetTask.counterTarget)
        if (nextCount >= targetTask.counterTarget && !targetTask.isCompleted) {
            completeTaskInternal(taskId, finalCounter = nextCount)
            return
        }

        val updated = _uiState.value.tasks.map { task ->
            if (task.id == taskId) {
                task.copy(counterCurrent = nextCount)
            } else {
                task
            }
        }
        _uiState.update { it.copy(tasks = updated) }
        persistTasks(updated)
    }

    fun decrementTaskCounter(taskId: String) {
        val updated = _uiState.value.tasks.map { task ->
            if (task.id == taskId && task.hasCounter && task.counterTarget > 0) {
                val nextCount = (task.counterCurrent - 1).coerceAtLeast(0)
                val stillDone = nextCount >= task.counterTarget
                task.copy(
                    counterCurrent = nextCount,
                    isCompleted = stillDone,
                    completedAtMillis = if (stillDone) task.completedAtMillis else null
                )
            } else {
                task
            }
        }
        _uiState.update { it.copy(tasks = updated) }
        persistTasks(updated)
    }

    fun toggleTaskCompletion(taskId: String) {
        val target = _uiState.value.tasks.firstOrNull { it.id == taskId } ?: return
        if (!target.isCompleted) {
            completeTaskInternal(
                taskId = taskId,
                finalCounter = if (target.hasCounter) target.counterTarget else target.counterCurrent
            )
        } else {
            var reopened: TaskItem? = null
            val updated = _uiState.value.tasks.map { task ->
                if (task.id == taskId) {
                    val nextCounter = if (task.hasCounter && task.counterCurrent >= task.counterTarget) {
                        (task.counterTarget - 1).coerceAtLeast(0)
                    } else {
                        task.counterCurrent
                    }
                    val next = task.copy(
                        isCompleted = false,
                        completedAtMillis = null,
                        counterCurrent = nextCounter
                    )
                    reopened = next
                    next
                } else {
                    task
                }
            }
            _uiState.update { it.copy(tasks = updated) }
            reopened?.let { reminderScheduler.scheduleTaskReminder(it, folderNameOf(it.folderId)) }
            persistTasks(updated)
        }
    }

    private fun completeTaskInternal(taskId: String, finalCounter: Int) {
        val now = System.currentTimeMillis()
        val currentTasks = _uiState.value.tasks
        val target = currentTasks.firstOrNull { it.id == taskId } ?: return

        val shouldRepeatRoutine = target.isPlanned && target.reminderType == ReminderType.ROUTINE
        val repeatAfterDays = target.flexibleRepeatAfterDays
        val shouldRepeatFlexible = target.isPlanned &&
            target.reminderType == ReminderType.FLEXIBLE &&
            repeatAfterDays != null &&
            repeatAfterDays > 0

        if (shouldRepeatRoutine || shouldRepeatFlexible) {
            val nextDate = if (shouldRepeatRoutine) {
                DateTimeUtils.nextRoutineDateMillis(
                    fromDateMillis = target.scheduledDateMillis ?: DateTimeUtils.startOfDayMillis(0),
                    frequency = target.routineFrequency,
                    selectedWeekDays = target.routineWeekDays
                )
            } else {
                DateTimeUtils.startOfDayMillis(repeatAfterDays ?: 1)
            }

            // Tamamlanan kaydı geçmiş & bugün raporu için arşivle
            val completedRecord = target.copy(
                id = UUID.randomUUID().toString(),
                reminderType = ReminderType.NONE,
                scheduledDateMillis = DateTimeUtils.startOfDayMillis(0),
                isCompleted = true,
                completedAtMillis = now,
                counterCurrent = finalCounter
            )

            // Asıl tekrarlayan görevi bir sonraki döngüye taşı
            val nextOccurrence = target.copy(
                scheduledDateMillis = nextDate,
                isCompleted = false,
                completedAtMillis = null,
                counterCurrent = if (target.hasCounter) 0 else target.counterCurrent
            )

            val updated = currentTasks.map { if (it.id == taskId) nextOccurrence else it } + completedRecord
            _uiState.update { it.copy(tasks = updated) }
            reminderScheduler.scheduleTaskReminder(nextOccurrence, folderNameOf(nextOccurrence.folderId))
            persistTasks(updated)
        } else {
            val updated = currentTasks.map { task ->
                if (task.id == taskId) {
                    task.copy(
                        isCompleted = true,
                        completedAtMillis = now,
                        counterCurrent = finalCounter
                    )
                } else {
                    task
                }
            }
            _uiState.update { it.copy(tasks = updated) }
            reminderScheduler.cancelTaskReminder(taskId)
            persistTasks(updated)
        }
    }

    fun deleteTask(taskId: String) {
        reminderScheduler.cancelTaskReminder(taskId)
        val updated = _uiState.value.tasks.filterNot { it.id == taskId }
        _uiState.update { it.copy(tasks = updated) }
        persistTasks(updated)
    }
}
