package com.example.kisiselsekreter.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kisiselsekreter.data.DateTimeUtils
import com.example.kisiselsekreter.data.TaskItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnoozeBottomSheet(
    task: TaskItem,
    folderName: String?,
    onDismiss: () -> Unit,
    onSnoozeSelected: (newDateMillis: Long, newHour: Int?, newMinute: Int?) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val plus2h = remember(task.id) { DateTimeUtils.computeSnoozePlus2Hours() }
    val thisEvening = remember(task.id) { DateTimeUtils.computeSnoozeThisEvening() }
    val tomorrow = remember(task.id) { DateTimeUtils.computeSnoozeTomorrow(task) }
    val nextWeek = remember(task.id) { DateTimeUtils.computeSnoozeNextWeek(task) }

    var showCustomPicker by remember { mutableStateOf(false) }
    var customDateMillis by remember { mutableLongStateOf(DateTimeUtils.startOfDayMillis(1)) }
    var customHasTime by remember { mutableStateOf(true) }
    var customHour by remember { mutableIntStateOf(task.scheduledHour ?: 10) }
    var customMinute by remember { mutableIntStateOf(task.scheduledMinute ?: 0) }

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
            Column {
                Text(
                    text = "Hatırlatmayı Ertele",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onSurface
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (!folderName.isNullOrBlank()) "${task.title} · $folderName" else task.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 4 Hızlı Erteleme Seçeneği (2x2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SnoozeOptionTile(
                    icon = Icons.Outlined.Schedule,
                    title = "+2 Saat",
                    subtitle = DateTimeUtils.formatScheduledLabel(plus2h.first, plus2h.second, plus2h.third),
                    onClick = {
                        onSnoozeSelected(plus2h.first, plus2h.second, plus2h.third)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                )

                SnoozeOptionTile(
                    icon = Icons.Outlined.Bedtime,
                    title = "Bu Akşam",
                    subtitle = DateTimeUtils.formatScheduledLabel(
                        thisEvening.first,
                        thisEvening.second,
                        thisEvening.third
                    ),
                    onClick = {
                        onSnoozeSelected(thisEvening.first, thisEvening.second, thisEvening.third)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SnoozeOptionTile(
                    icon = Icons.Outlined.WbSunny,
                    title = "Yarın",
                    subtitle = DateTimeUtils.formatScheduledLabel(
                        tomorrow.first,
                        tomorrow.second,
                        tomorrow.third,
                        task.flexibleTimeWindow
                    ),
                    onClick = {
                        onSnoozeSelected(tomorrow.first, tomorrow.second, tomorrow.third)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                )

                SnoozeOptionTile(
                    icon = Icons.Outlined.DateRange,
                    title = "Gelecek Hafta",
                    subtitle = DateTimeUtils.formatScheduledLabel(
                        nextWeek.first,
                        nextWeek.second,
                        nextWeek.third,
                        task.flexibleTimeWindow
                    ),
                    onClick = {
                        onSnoozeSelected(nextWeek.first, nextWeek.second, nextWeek.third)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Özel Tarih / Saat Seçimi
            Surface(
                onClick = { showCustomPicker = !showCustomPicker },
                shape = RoundedCornerShape(16.dp),
                color = if (showCustomPicker) colors.primaryContainer else colors.surfaceContainerLow,
                border = BorderStroke(
                    1.dp,
                    if (showCustomPicker) colors.primary.copy(alpha = 0.5f) else colors.outlineVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = colors.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Özel tarih ve saat seç",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.onSurface
                    )
                }
            }

            if (showCustomPicker) {
                DateAndTimePickerSection(
                    selectedDateMillis = customDateMillis,
                    onDateChange = { customDateMillis = it },
                    hasTime = customHasTime,
                    onHasTimeChange = { customHasTime = it },
                    selectedHour = customHour,
                    selectedMinute = customMinute,
                    onTimeChange = { h, m ->
                        customHour = h
                        customMinute = m
                    }
                )

                Button(
                    onClick = {
                        onSnoozeSelected(
                            customDateMillis,
                            if (customHasTime) customHour else null,
                            if (customHasTime) customMinute else null
                        )
                        onDismiss()
                    },
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Seçili Zamana Ertele")
                }
            }
        }
    }
}

@Composable
private fun SnoozeOptionTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = colors.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
