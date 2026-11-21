package com.example.kisiselsekreter.data

import com.example.kisiselsekreter.ui.theme.FolderColorTone
import com.example.kisiselsekreter.ui.theme.FolderIconKey
import java.util.UUID

enum class ThemePreference(val label: String) {
    SYSTEM("Sistem"),
    LIGHT("Aydınlık"),
    DARK("Karanlık");

    companion object {
        fun fromNameOrDefault(name: String?): ThemePreference =
            entries.firstOrNull { it.name == name } ?: LIGHT
    }
}

enum class ReminderType(val label: String) {
    NONE("Tek Seferlik"),
    ROUTINE("Rutin"),
    FLEXIBLE("Esnek");

    companion object {
        fun fromNameOrDefault(name: String?): ReminderType =
            entries.firstOrNull { it.name == name } ?: NONE
    }
}

enum class RoutineFrequency(val label: String) {
    DAILY("Günlük"),
    WEEKLY("Haftalık"),
    MONTHLY("Aylık"),
    SELECTED_DAYS("Seçili Günler");

    companion object {
        fun fromNameOrDefault(name: String?): RoutineFrequency =
            entries.firstOrNull { it.name == name } ?: DAILY
    }
}

enum class TimeWindow(
    val label: String,
    val defaultHour: Int,
    val defaultMinute: Int
) {
    NONE("Belirtilmedi", 9, 0),
    MORNING("Sabah", 8, 30),
    AFTERNOON("Öğleden Sonra", 13, 30),
    EVENING("Akşam", 19, 30);

    companion object {
        fun fromNameOrDefault(name: String?): TimeWindow =
            entries.firstOrNull { it.name == name } ?: NONE
    }
}

data class ReminderConfig(
    val reminderType: ReminderType = ReminderType.NONE,
    val routineFrequency: RoutineFrequency = RoutineFrequency.DAILY,
    val routineWeekDays: Set<Int> = emptySet(),
    val flexibleRepeatAfterDays: Int? = null,
    val flexibleTimeWindow: TimeWindow = TimeWindow.NONE
)

data class QuietHoursConfig(
    val enabled: Boolean = false,
    val startHour: Int = 22,
    val startMinute: Int = 0,
    val endHour: Int = 8,
    val endMinute: Int = 0
) {
    val formattedRange: String
        get() = "${DateTimeUtils.formatTime(startHour, startMinute)} - ${DateTimeUtils.formatTime(endHour, endMinute)}"
}

data class BackupPayload(
    val schemaVersion: Int,
    val exportedAtMillis: Long,
    val themePreference: ThemePreference,
    val quietHours: QuietHoursConfig,
    val folders: List<FolderItem>,
    val tasks: List<TaskItem>
)

data class SubFolder(
    val id: String = UUID.randomUUID().toString(),
    val name: String
)

data class FolderItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val colorTone: FolderColorTone,
    val iconKey: FolderIconKey = FolderIconKey.FOLDER,
    val subFolders: List<SubFolder> = emptyList(),
    val isDefault: Boolean = false
)

data class TaskItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val note: String = "",
    val folderId: String,
    val subFolderId: String? = null,
    val isPlanned: Boolean = false,
    val scheduledDateMillis: Long? = null,
    val scheduledHour: Int? = null,
    val scheduledMinute: Int? = null,
    val reminderType: ReminderType = ReminderType.NONE,
    val routineFrequency: RoutineFrequency = RoutineFrequency.DAILY,
    val routineWeekDays: Set<Int> = emptySet(),
    val flexibleRepeatAfterDays: Int? = null,
    val flexibleTimeWindow: TimeWindow = TimeWindow.NONE,
    val hasCounter: Boolean = false,
    val counterCurrent: Int = 0,
    val counterTarget: Int = 0,
    val counterUnit: String = "",
    val isCompleted: Boolean = false,
    val snoozeCount: Int = 0,
    val lastSnoozedAtMillis: Long? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val completedAtMillis: Long? = null
) {
    val isScheduledForToday: Boolean
        get() = isPlanned && DateTimeUtils.isToday(scheduledDateMillis)

    val isOverdue: Boolean
        get() = isPlanned && !isCompleted && DateTimeUtils.isBeforeToday(scheduledDateMillis)

    val triggerAtMillis: Long?
        get() = if (!isPlanned) {
            null
        } else {
            DateTimeUtils.computeTriggerAtMillis(
                dateMillis = scheduledDateMillis,
                hour = scheduledHour,
                minute = scheduledMinute,
                reminderType = reminderType,
                timeWindow = flexibleTimeWindow
            )
        }

    fun isDueNow(nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (!isPlanned || isCompleted) return false
        val trigger = triggerAtMillis ?: return false
        return trigger <= nowMillis
    }

    val scheduledSortKey: Long
        get() = triggerAtMillis ?: Long.MAX_VALUE

    val counterDisplayText: String
        get() {
            val ratio = "$counterCurrent/$counterTarget"
            return if (counterUnit.isBlank()) ratio else "$ratio $counterUnit"
        }

    val reminderConfig: ReminderConfig
        get() = ReminderConfig(
            reminderType = reminderType,
            routineFrequency = routineFrequency,
            routineWeekDays = routineWeekDays,
            flexibleRepeatAfterDays = flexibleRepeatAfterDays,
            flexibleTimeWindow = flexibleTimeWindow
        )

    val reminderSummaryLabel: String?
        get() = when (reminderType) {
            ReminderType.NONE -> null
            ReminderType.ROUTINE -> {
                if (routineFrequency == RoutineFrequency.SELECTED_DAYS && routineWeekDays.isNotEmpty()) {
                    val days = routineWeekDays.sorted().joinToString(", ") {
                        DateTimeUtils.weekDayShortLabel(it)
                    }
                    "Rutin · $days"
                } else {
                    "Rutin · ${routineFrequency.label}"
                }
            }
            ReminderType.FLEXIBLE -> {
                val parts = buildList {
                    if (flexibleTimeWindow != TimeWindow.NONE) {
                        add(flexibleTimeWindow.label)
                    }
                    val repeatDays = flexibleRepeatAfterDays
                    if (repeatDays != null && repeatDays > 0) {
                        add("+$repeatDays gün")
                    }
                }
                if (parts.isEmpty()) "Esnek" else "Esnek · ${parts.joinToString(" · ")}"
            }
        }
}
