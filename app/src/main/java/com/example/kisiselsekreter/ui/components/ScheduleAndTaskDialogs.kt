package com.example.kisiselsekreter.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.kisiselsekreter.data.DateTimeUtils
import com.example.kisiselsekreter.data.FolderItem
import com.example.kisiselsekreter.data.ReminderConfig
import com.example.kisiselsekreter.data.ReminderType
import com.example.kisiselsekreter.data.RoutineFrequency
import com.example.kisiselsekreter.data.TaskItem
import com.example.kisiselsekreter.data.TimeWindow
import com.example.kisiselsekreter.ui.theme.LocalIsDarkTheme

@Composable
fun ScheduleTaskDialog(
    task: TaskItem,
    onDismiss: () -> Unit,
    onConfirmSchedule: (
        dateMillis: Long,
        hour: Int?,
        minute: Int?,
        reminderConfig: ReminderConfig
    ) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var selectedDate by remember(task) {
        mutableLongStateOf(task.scheduledDateMillis ?: DateTimeUtils.startOfDayMillis(0))
    }
    var hasTime by remember(task) { mutableStateOf(task.scheduledHour != null) }
    var selectedHour by remember(task) { mutableIntStateOf(task.scheduledHour ?: 10) }
    var selectedMinute by remember(task) { mutableIntStateOf(task.scheduledMinute ?: 0) }
    var reminderConfig by remember(task) { mutableStateOf(task.reminderConfig) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Text(
                    text = "Görevi Planla",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 2
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DateAndTimePickerSection(
                    selectedDateMillis = selectedDate,
                    onDateChange = { selectedDate = it },
                    hasTime = hasTime,
                    onHasTimeChange = { hasTime = it },
                    selectedHour = selectedHour,
                    selectedMinute = selectedMinute,
                    onTimeChange = { h, m ->
                        selectedHour = h
                        selectedMinute = m
                    }
                )

                ReminderTypeConfigSection(
                    config = reminderConfig,
                    onConfigChange = { next ->
                        reminderConfig = next
                        if (next.reminderType == ReminderType.FLEXIBLE && next.flexibleTimeWindow != TimeWindow.NONE) {
                            hasTime = false
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmSchedule(
                        selectedDate,
                        if (hasTime) selectedHour else null,
                        if (hasTime) selectedMinute else null,
                        reminderConfig
                    )
                },
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
            ) {
                Text("Planla")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç")
            }
        }
    )
}

@Composable
fun TaskEditorDialog(
    task: TaskItem,
    folders: List<FolderItem>,
    onDismiss: () -> Unit,
    onSave: (TaskItem) -> Unit,
    onDelete: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val colors = MaterialTheme.colorScheme

    var title by remember(task) { mutableStateOf(task.title) }
    var note by remember(task) { mutableStateOf(task.note) }
    var selectedFolderId by remember(task) { mutableStateOf(task.folderId) }
    var selectedSubFolderId by remember(task) { mutableStateOf(task.subFolderId) }
    var isPlanned by remember(task) { mutableStateOf(task.isPlanned) }
    var selectedDate by remember(task) {
        mutableLongStateOf(task.scheduledDateMillis ?: DateTimeUtils.startOfDayMillis(0))
    }
    var hasTime by remember(task) { mutableStateOf(task.scheduledHour != null) }
    var selectedHour by remember(task) { mutableIntStateOf(task.scheduledHour ?: 10) }
    var selectedMinute by remember(task) { mutableIntStateOf(task.scheduledMinute ?: 0) }
    var reminderConfig by remember(task) { mutableStateOf(task.reminderConfig) }

    var hasCounter by remember(task) { mutableStateOf(task.hasCounter) }
    var counterTargetText by remember(task) {
        mutableStateOf(if (task.counterTarget > 0) task.counterTarget.toString() else "10")
    }
    var counterCurrentText by remember(task) {
        mutableStateOf(task.counterCurrent.toString())
    }
    var counterUnit by remember(task) { mutableStateOf(task.counterUnit) }

    val selectedFolder = folders.firstOrNull { it.id == selectedFolderId }
    val parsedTarget = counterTargetText.toIntOrNull() ?: 0
    val canSave = title.trim().isNotEmpty() && (!hasCounter || parsedTarget > 0)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Görevi Düzenle",
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Görev adı") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Kısa not (isteğe bağlı)") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Klasör seçimi
                if (folders.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Klasör",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            folders.forEach { folder ->
                                val selected = folder.id == selectedFolderId
                                Surface(
                                    onClick = {
                                        selectedFolderId = folder.id
                                        selectedSubFolderId = null
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selected) folder.colorTone.containerColor(isDark) else colors.surfaceContainerLow,
                                    border = BorderStroke(
                                        width = if (selected) 1.5.dp else 1.dp,
                                        color = if (selected) folder.colorTone.accentColor(isDark) else colors.outlineVariant
                                    )
                                ) {
                                    Text(
                                        text = folder.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colors.onSurface,
                                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (selectedFolder != null && selectedFolder.subFolders.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Alt klasör",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    onClick = { selectedSubFolderId = null },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selectedSubFolderId == null) colors.primaryContainer else colors.surfaceContainerLow,
                                    border = BorderStroke(1.dp, colors.outlineVariant)
                                ) {
                                    Text(
                                        text = "Genel",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colors.onSurface,
                                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
                                    )
                                }
                                selectedFolder.subFolders.forEach { sub ->
                                    val selected = sub.id == selectedSubFolderId
                                    Surface(
                                        onClick = { selectedSubFolderId = sub.id },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (selected) selectedFolder.colorTone.containerColor(isDark) else colors.surfaceContainerLow,
                                        border = BorderStroke(
                                            width = if (selected) 1.5.dp else 1.dp,
                                            color = if (selected) selectedFolder.colorTone.accentColor(isDark) else colors.outlineVariant
                                        )
                                    ) {
                                        Text(
                                            text = sub.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = colors.onSurface,
                                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Planlı / Planlanmamış seçici
                PlannedStatusSelector(
                    isPlanned = isPlanned,
                    onChange = { isPlanned = it }
                )

                if (isPlanned) {
                    DateAndTimePickerSection(
                        selectedDateMillis = selectedDate,
                        onDateChange = { selectedDate = it },
                        hasTime = hasTime,
                        onHasTimeChange = { hasTime = it },
                        selectedHour = selectedHour,
                        selectedMinute = selectedMinute,
                        onTimeChange = { h, m ->
                            selectedHour = h
                            selectedMinute = m
                        }
                    )

                    ReminderTypeConfigSection(
                        config = reminderConfig,
                        onConfigChange = { next ->
                            reminderConfig = next
                            if (next.reminderType == ReminderType.FLEXIBLE && next.flexibleTimeWindow != TimeWindow.NONE) {
                                hasTime = false
                            }
                        }
                    )
                }

                // Opsiyonel sayaç
                CounterConfigSection(
                    hasCounter = hasCounter,
                    onHasCounterChange = { hasCounter = it },
                    targetText = counterTargetText,
                    onTargetTextChange = { counterTargetText = it },
                    unitText = counterUnit,
                    onUnitTextChange = { counterUnit = it },
                    showCurrentInput = true,
                    currentText = counterCurrentText,
                    onCurrentTextChange = { counterCurrentText = it }
                )

                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Text("Görevi sil", color = colors.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = if (hasCounter) parsedTarget.coerceAtLeast(1) else 0
                    val current = if (hasCounter) {
                        (counterCurrentText.toIntOrNull() ?: 0).coerceIn(0, target)
                    } else {
                        0
                    }
                    onSave(
                        task.copy(
                            title = title.trim(),
                            note = note.trim(),
                            folderId = selectedFolderId,
                            subFolderId = selectedSubFolderId,
                            isPlanned = isPlanned,
                            scheduledDateMillis = if (isPlanned) selectedDate else null,
                            scheduledHour = if (isPlanned && hasTime) selectedHour else null,
                            scheduledMinute = if (isPlanned && hasTime) selectedMinute else null,
                            reminderType = if (isPlanned) reminderConfig.reminderType else ReminderType.NONE,
                            routineFrequency = reminderConfig.routineFrequency,
                            routineWeekDays = reminderConfig.routineWeekDays,
                            flexibleRepeatAfterDays = reminderConfig.flexibleRepeatAfterDays,
                            flexibleTimeWindow = reminderConfig.flexibleTimeWindow,
                            hasCounter = hasCounter,
                            counterTarget = target,
                            counterCurrent = current,
                            counterUnit = if (hasCounter) counterUnit.trim() else "",
                            isCompleted = if (hasCounter) current >= target else task.isCompleted
                        )
                    )
                },
                enabled = canSave,
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReminderTypeConfigSection(
    config: ReminderConfig,
    onConfigChange: (ReminderConfig) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var customRepeatDaysText by remember(config.flexibleRepeatAfterDays) {
        mutableStateOf(config.flexibleRepeatAfterDays?.toString().orEmpty())
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Hatırlatma Tipi",
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ReminderType.entries.forEach { type ->
                    val selected = config.reminderType == type
                    Surface(
                        onClick = { onConfigChange(config.copy(reminderType = type)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) colors.primaryContainer else colors.surface,
                        border = BorderStroke(
                            width = if (selected) 1.5.dp else 1.dp,
                            color = if (selected) colors.primary else colors.outlineVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            when (config.reminderType) {
                ReminderType.NONE -> {
                    Text(
                        text = "Belirlenen tarih ve saatte bir kez hatırlatılır.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }

                ReminderType.ROUTINE -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Tekrar sıklığı",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            RoutineFrequency.entries.forEach { freq ->
                                QuickDateChip(
                                    label = freq.label,
                                    selected = config.routineFrequency == freq,
                                    onClick = {
                                        val defaultDays = if (freq == RoutineFrequency.SELECTED_DAYS && config.routineWeekDays.isEmpty()) {
                                            setOf(1, 3, 5)
                                        } else {
                                            config.routineWeekDays
                                        }
                                        onConfigChange(
                                            config.copy(
                                                routineFrequency = freq,
                                                routineWeekDays = defaultDays
                                            )
                                        )
                                    }
                                )
                            }
                        }

                        if (config.routineFrequency == RoutineFrequency.SELECTED_DAYS) {
                            Text(
                                text = "Günleri seçin",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                (1..7).forEach { isoDay ->
                                    val selected = isoDay in config.routineWeekDays
                                    Surface(
                                        onClick = {
                                            val updatedDays = if (selected) {
                                                config.routineWeekDays - isoDay
                                            } else {
                                                config.routineWeekDays + isoDay
                                            }
                                            onConfigChange(config.copy(routineWeekDays = updatedDays))
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (selected) colors.primaryContainer else colors.surface,
                                        border = BorderStroke(
                                            width = if (selected) 1.5.dp else 1.dp,
                                            color = if (selected) colors.primary else colors.outlineVariant
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 7.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = DateTimeUtils.weekDayShortLabel(isoDay),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                ReminderType.FLEXIBLE -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Zaman aralığı (saat baskısı olmadan)",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                TimeWindow.entries.forEach { window ->
                                    QuickDateChip(
                                        label = window.label,
                                        selected = config.flexibleTimeWindow == window,
                                        onClick = {
                                            onConfigChange(config.copy(flexibleTimeWindow = window))
                                        }
                                    )
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Tamamlandıktan sonra tekrar hatırlat",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                QuickDateChip(
                                    label = "Tekrarsız",
                                    selected = config.flexibleRepeatAfterDays == null,
                                    onClick = {
                                        customRepeatDaysText = ""
                                        onConfigChange(config.copy(flexibleRepeatAfterDays = null))
                                    }
                                )
                                listOf(1, 2, 3, 7, 14).forEach { days ->
                                    QuickDateChip(
                                        label = "+$days gün",
                                        selected = config.flexibleRepeatAfterDays == days,
                                        onClick = {
                                            customRepeatDaysText = days.toString()
                                            onConfigChange(config.copy(flexibleRepeatAfterDays = days))
                                        }
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = customRepeatDaysText,
                                onValueChange = { raw ->
                                    val digits = raw.filter { it.isDigit() }.take(3)
                                    customRepeatDaysText = digits
                                    val parsed = digits.toIntOrNull()?.takeIf { it > 0 }
                                    onConfigChange(config.copy(flexibleRepeatAfterDays = parsed))
                                },
                                label = { Text("Özel gün aralığı (isteğe bağlı)") },
                                placeholder = { Text("Örn. 5") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlannedStatusSelector(
    isPlanned: Boolean,
    onChange: (Boolean) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Plan durumu",
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                onClick = { onChange(false) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = if (!isPlanned) colors.primaryContainer else colors.surfaceContainerLow,
                border = BorderStroke(
                    width = if (!isPlanned) 1.5.dp else 1.dp,
                    color = if (!isPlanned) colors.primary else colors.outlineVariant
                )
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Planlanmamış",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (!isPlanned) colors.onPrimaryContainer else colors.onSurfaceVariant
                    )
                }
            }

            Surface(
                onClick = { onChange(true) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = if (isPlanned) colors.primaryContainer else colors.surfaceContainerLow,
                border = BorderStroke(
                    width = if (isPlanned) 1.5.dp else 1.dp,
                    color = if (isPlanned) colors.primary else colors.outlineVariant
                )
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Planlı",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isPlanned) colors.onPrimaryContainer else colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateAndTimePickerSection(
    selectedDateMillis: Long,
    onDateChange: (Long) -> Unit,
    hasTime: Boolean,
    onHasTimeChange: (Boolean) -> Unit,
    selectedHour: Int,
    selectedMinute: Int,
    onTimeChange: (Int, Int) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val todayMillis = remember { DateTimeUtils.startOfDayMillis(0) }
    val tomorrowMillis = remember { DateTimeUtils.startOfDayMillis(1) }
    val inTwoDaysMillis = remember { DateTimeUtils.startOfDayMillis(2) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Tarih · ${DateTimeUtils.formatDateShort(selectedDateMillis)}",
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurface
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuickDateChip(
                    label = "Bugün",
                    selected = DateTimeUtils.isSameDay(selectedDateMillis, todayMillis),
                    onClick = { onDateChange(todayMillis) }
                )
                QuickDateChip(
                    label = "Yarın",
                    selected = DateTimeUtils.isSameDay(selectedDateMillis, tomorrowMillis),
                    onClick = { onDateChange(tomorrowMillis) }
                )
                QuickDateChip(
                    label = "2 Gün Sonra",
                    selected = DateTimeUtils.isSameDay(selectedDateMillis, inTwoDaysMillis),
                    onClick = { onDateChange(inTwoDaysMillis) }
                )
                Surface(
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = "Tarih seç",
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tarih Seç",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurface
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (hasTime) {
                        "Saat · ${DateTimeUtils.formatTime(selectedHour, selectedMinute)}"
                    } else {
                        "Kesin saat ekle (isteğe bağlı)"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface
                )
                Switch(
                    checked = hasTime,
                    onCheckedChange = onHasTimeChange
                )
            }

            if (hasTime) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(9 to 0, 13 to 0, 18 to 0, 21 to 0)
                    presets.forEach { (h, m) ->
                        QuickDateChip(
                            label = DateTimeUtils.formatTime(h, m),
                            selected = selectedHour == h && selectedMinute == m,
                            onClick = { onTimeChange(h, m) }
                        )
                    }
                    Surface(
                        onClick = { showTimePicker = true },
                        shape = RoundedCornerShape(12.dp),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccessTime,
                                contentDescription = "Saat seç",
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Saat Seç",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurface
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = DateTimeUtils.localDayToUtcDatePickerMillis(selectedDateMillis)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { utcMillis ->
                            onDateChange(DateTimeUtils.normalizeUtcDatePickerMillisToLocalDay(utcMillis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Tamam")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Vazgeç")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = colors.surface,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Saat Seç",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface
                )
            },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeChange(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    }
                ) {
                    Text("Tamam")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Vazgeç")
                }
            }
        )
    }
}

@Composable
private fun QuickDateChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) colors.primaryContainer else colors.surface,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) colors.primary else colors.outlineVariant
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) colors.onPrimaryContainer else colors.onSurface,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
        )
    }
}

@Composable
fun CounterConfigSection(
    hasCounter: Boolean,
    onHasCounterChange: (Boolean) -> Unit,
    targetText: String,
    onTargetTextChange: (String) -> Unit,
    unitText: String,
    onUnitTextChange: (String) -> Unit,
    showCurrentInput: Boolean = false,
    currentText: String = "0",
    onCurrentTextChange: (String) -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sayaç Ekle",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface
                    )
                    Text(
                        text = "Zikir, sayfa veya tekrar takibi için (ör. 3/10)",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
                Switch(
                    checked = hasCounter,
                    onCheckedChange = onHasCounterChange
                )
            }

            if (hasCounter) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showCurrentInput) {
                        OutlinedTextField(
                            value = currentText,
                            onValueChange = { value ->
                                onCurrentTextChange(value.filter { it.isDigit() }.take(4))
                            },
                            label = { Text("Mevcut") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { value ->
                            onTargetTextChange(value.filter { it.isDigit() }.take(4))
                        },
                        label = { Text("Hedef sayı") },
                        placeholder = { Text("10") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = unitText,
                        onValueChange = { onUnitTextChange(it.take(16)) },
                        label = { Text("Birim") },
                        placeholder = { Text("sayfa") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.1f)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("10", "20", "33", "100").forEach { preset ->
                        QuickDateChip(
                            label = "Hedef: $preset",
                            selected = targetText == preset,
                            onClick = { onTargetTextChange(preset) }
                        )
                    }
                }
            }
        }
    }
}
