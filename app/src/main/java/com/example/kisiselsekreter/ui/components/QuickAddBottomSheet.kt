package com.example.kisiselsekreter.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.kisiselsekreter.data.DateTimeUtils
import com.example.kisiselsekreter.data.FolderItem
import com.example.kisiselsekreter.data.ReminderConfig
import com.example.kisiselsekreter.data.ReminderType
import com.example.kisiselsekreter.data.TimeWindow
import com.example.kisiselsekreter.ui.theme.FolderColorTone
import com.example.kisiselsekreter.ui.theme.FolderIconKey
import com.example.kisiselsekreter.ui.theme.LocalIsDarkTheme

private enum class QuickAddMode(val label: String) {
    TASK("Görev / Not"),
    FOLDER("Klasör / Alt Klasör")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddBottomSheet(
    folders: List<FolderItem>,
    initialFolderId: String?,
    initialSubFolderId: String?,
    defaultPlanned: Boolean,
    onDismiss: () -> Unit,
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
    onAddFolder: (name: String, tone: FolderColorTone, icon: FolderIconKey) -> Unit,
    onAddSubFolder: (parentFolderId: String, name: String) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val colors = MaterialTheme.colorScheme
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var mode by remember { mutableStateOf(QuickAddMode.TASK) }

    // Görev / Not durumu
    var taskTitle by remember { mutableStateOf("") }
    var taskNote by remember { mutableStateOf("") }
    var selectedFolderId by remember {
        mutableStateOf(initialFolderId ?: folders.firstOrNull()?.id.orEmpty())
    }
    var selectedSubFolderId by remember { mutableStateOf(initialSubFolderId) }
    var isPlanned by remember { mutableStateOf(defaultPlanned) }
    var selectedDateMillis by remember { mutableLongStateOf(DateTimeUtils.startOfDayMillis(0)) }
    var hasTime by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableIntStateOf(10) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var reminderConfig by remember { mutableStateOf(ReminderConfig()) }

    // Opsiyonel sayaç (varsayılan kapalı)
    var hasCounter by remember { mutableStateOf(false) }
    var counterTargetText by remember { mutableStateOf("10") }
    var counterUnit by remember { mutableStateOf("") }

    // Klasör / Alt Klasör durumu
    var isSubFolderMode by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var selectedTone by remember { mutableStateOf(FolderColorTone.LINEN_SAND) }
    var selectedIcon by remember { mutableStateOf(FolderIconKey.FOLDER) }
    var parentFolderForSub by remember {
        mutableStateOf(initialFolderId ?: folders.firstOrNull()?.id.orEmpty())
    }

    val selectedFolder = folders.firstOrNull { it.id == selectedFolderId }
    val parsedTarget = counterTargetText.toIntOrNull() ?: 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Hızlı Ekle",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickAddMode.entries.forEach { item ->
                    val selected = mode == item
                    Surface(
                        onClick = { mode = item },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) colors.primaryContainer else colors.surfaceContainerLow,
                        border = BorderStroke(
                            1.dp,
                            if (selected) colors.primary.copy(alpha = 0.45f) else colors.outlineVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (mode == QuickAddMode.TASK) {
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    label = { Text("Görev adı") },
                    placeholder = { Text("Ne planlıyorsunuz?") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = taskNote,
                    onValueChange = { taskNote = it },
                    label = { Text("Kısa not (isteğe bağlı)") },
                    placeholder = { Text("Detay veya hatırlatıcı not...") },
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (folders.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Hedef klasör",
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
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (selected) {
                                        folder.colorTone.containerColor(isDark)
                                    } else {
                                        colors.surfaceContainerLow
                                    },
                                    border = BorderStroke(
                                        width = if (selected) 1.5.dp else 1.dp,
                                        color = if (selected) {
                                            folder.colorTone.accentColor(isDark)
                                        } else {
                                            colors.outlineVariant
                                        }
                                    )
                                ) {
                                    Text(
                                        text = folder.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colors.onSurface,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (selectedFolder != null && selectedFolder.subFolders.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                    color = if (selectedSubFolderId == null) {
                                        colors.primaryContainer
                                    } else {
                                        colors.surfaceContainerLow
                                    },
                                    border = BorderStroke(1.dp, colors.outlineVariant)
                                ) {
                                    Text(
                                        text = "Genel",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colors.onSurface,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                    )
                                }
                                selectedFolder.subFolders.forEach { sub ->
                                    val selected = sub.id == selectedSubFolderId
                                    Surface(
                                        onClick = { selectedSubFolderId = sub.id },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (selected) {
                                            selectedFolder.colorTone.containerColor(isDark)
                                        } else {
                                            colors.surfaceContainerLow
                                        },
                                        border = BorderStroke(
                                            width = if (selected) 1.5.dp else 1.dp,
                                            color = if (selected) {
                                                selectedFolder.colorTone.accentColor(isDark)
                                            } else {
                                                colors.outlineVariant
                                            }
                                        )
                                    ) {
                                        Text(
                                            text = sub.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = colors.onSurface,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Planlı / Planlanmamış seçimi
                PlannedStatusSelector(
                    isPlanned = isPlanned,
                    onChange = { isPlanned = it }
                )

                if (isPlanned) {
                    DateAndTimePickerSection(
                        selectedDateMillis = selectedDateMillis,
                        onDateChange = { selectedDateMillis = it },
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

                // Opsiyonel Sayaç Ekle (varsayılan kapalı)
                CounterConfigSection(
                    hasCounter = hasCounter,
                    onHasCounterChange = { hasCounter = it },
                    targetText = counterTargetText,
                    onTargetTextChange = { counterTargetText = it },
                    unitText = counterUnit,
                    onUnitTextChange = { counterUnit = it }
                )

                Button(
                    onClick = {
                        val targetFolderId = selectedFolderId.ifBlank { folders.firstOrNull()?.id.orEmpty() }
                        if (taskTitle.isNotBlank() && targetFolderId.isNotBlank()) {
                            onAddTask(
                                taskTitle,
                                taskNote,
                                targetFolderId,
                                selectedSubFolderId,
                                isPlanned,
                                if (isPlanned) selectedDateMillis else null,
                                if (isPlanned && hasTime) selectedHour else null,
                                if (isPlanned && hasTime) selectedMinute else null,
                                if (isPlanned) reminderConfig else ReminderConfig(),
                                hasCounter,
                                if (hasCounter) parsedTarget.coerceAtLeast(1) else 0,
                                if (hasCounter) counterUnit else ""
                            )
                            onDismiss()
                        }
                    },
                    enabled = taskTitle.trim().isNotEmpty() && folders.isNotEmpty() && (!hasCounter || parsedTarget > 0),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(if (isPlanned) "Planlı Görev Ekle" else "Planlanmamış Olarak Ekle")
                }
            } else {
                // Klasör / Alt Klasör ekleme
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = { isSubFolderMode = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = if (!isSubFolderMode) colors.secondaryContainer else colors.surfaceContainerLow,
                        border = BorderStroke(1.dp, colors.outlineVariant)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Yeni Ana Klasör",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.onSurface
                            )
                        }
                    }
                    Surface(
                        onClick = { isSubFolderMode = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSubFolderMode) colors.secondaryContainer else colors.surfaceContainerLow,
                        border = BorderStroke(1.dp, colors.outlineVariant)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Alt Klasör Ekle",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.onSurface
                            )
                        }
                    }
                }

                if (isSubFolderMode && folders.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Üst klasör",
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
                                val selected = folder.id == parentFolderForSub
                                Surface(
                                    onClick = { parentFolderForSub = folder.id },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (selected) {
                                        folder.colorTone.containerColor(isDark)
                                    } else {
                                        colors.surfaceContainerLow
                                    },
                                    border = BorderStroke(
                                        width = if (selected) 1.5.dp else 1.dp,
                                        color = if (selected) {
                                            folder.colorTone.accentColor(isDark)
                                        } else {
                                            colors.outlineVariant
                                        }
                                    )
                                ) {
                                    Text(
                                        text = folder.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colors.onSurface,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text(if (isSubFolderMode) "Alt klasör adı" else "Klasör adı") },
                    placeholder = { Text(if (isSubFolderMode) "Örn. Tarih" else "Örn. Seyahat Planları") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (!isSubFolderMode) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Renk tonu · ${selectedTone.displayName}",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FolderColorTone.entries.forEach { tone ->
                                val selected = tone == selectedTone
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(tone.containerColor(isDark))
                                        .border(
                                            width = if (selected) 2.dp else 1.dp,
                                            color = if (selected) colors.onSurface else tone.borderColor(isDark),
                                            shape = CircleShape
                                        )
                                        .clickable { selectedTone = tone },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selected) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = tone.displayName,
                                            tint = colors.onSurface,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "İkon",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FolderIconKey.entries.forEach { iconKey ->
                                val selected = iconKey == selectedIcon
                                Surface(
                                    onClick = { selectedIcon = iconKey },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selected) selectedTone.containerColor(isDark) else colors.surfaceContainerLow,
                                    border = BorderStroke(
                                        width = if (selected) 1.5.dp else 1.dp,
                                        color = if (selected) selectedTone.accentColor(isDark) else colors.outlineVariant
                                    )
                                ) {
                                    Icon(
                                        imageVector = iconKey.icon,
                                        contentDescription = iconKey.displayName,
                                        tint = if (selected) colors.onSurface else colors.onSurfaceVariant,
                                        modifier = Modifier
                                            .padding(9.dp)
                                            .size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Button(
                    onClick = {
                        val trimmed = newFolderName.trim()
                        if (trimmed.isNotEmpty()) {
                            if (isSubFolderMode && parentFolderForSub.isNotBlank()) {
                                onAddSubFolder(parentFolderForSub, trimmed)
                            } else {
                                onAddFolder(trimmed, selectedTone, selectedIcon)
                            }
                            onDismiss()
                        }
                    },
                    enabled = newFolderName.trim().isNotEmpty() && (!isSubFolderMode || folders.isNotEmpty()),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(if (isSubFolderMode) "Alt Klasör Ekle" else "Klasör Oluştur")
                }
            }
        }
    }
}
