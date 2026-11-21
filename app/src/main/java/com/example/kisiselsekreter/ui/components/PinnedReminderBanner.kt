package com.example.kisiselsekreter.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kisiselsekreter.data.DateTimeUtils
import com.example.kisiselsekreter.data.FolderItem
import com.example.kisiselsekreter.data.TaskItem
import com.example.kisiselsekreter.ui.theme.LocalIsDarkTheme

@Composable
fun PinnedReminderBanner(
    dueTasks: List<TaskItem>,
    folderMap: Map<String, FolderItem>,
    onMarkDone: (TaskItem) -> Unit,
    onSnoozeClick: (TaskItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (dueTasks.isEmpty()) return

    val colors = MaterialTheme.colorScheme
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(20.dp),
        color = colors.primaryContainer,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        if (dueTasks.size == 1) {
            val task = dueTasks.first()
            SingleReminderContent(
                task = task,
                folder = folderMap[task.folderId],
                onMarkDone = { onMarkDone(task) },
                onSnoozeClick = { onSnoozeClick(task) }
            )
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = colors.surface.copy(alpha = 0.72f),
                        border = BorderStroke(1.dp, colors.outlineVariant),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.NotificationsActive,
                                contentDescription = "Bekleyen hatırlatmalar",
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${dueTasks.size} bekleyen hatırlatma",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onPrimaryContainer
                        )
                        Text(
                            text = if (isExpanded) {
                                "Tamamlayın veya uygun bir zamana erteleyin"
                            } else {
                                "${dueTasks.first().title} ve ${dueTasks.size - 1} diğer kayıt"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        imageVector = if (isExpanded) {
                            Icons.Rounded.KeyboardArrowUp
                        } else {
                            Icons.Rounded.KeyboardArrowDown
                        },
                        contentDescription = if (isExpanded) "Daralt" else "Genişlet",
                        tint = colors.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = colors.outline.copy(alpha = 0.5f)
                        )
                        dueTasks.forEach { task ->
                            Surface(
                                shape = RoundedCornerShape(15.dp),
                                color = colors.surface,
                                border = BorderStroke(1.dp, colors.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ReminderActionRow(
                                    task = task,
                                    folder = folderMap[task.folderId],
                                    onMarkDone = { onMarkDone(task) },
                                    onSnoozeClick = { onSnoozeClick(task) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SingleReminderContent(
    task: TaskItem,
    folder: FolderItem?,
    onMarkDone: () -> Unit,
    onSnoozeClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = colors.surface.copy(alpha = 0.72f),
                border = BorderStroke(1.dp, colors.outlineVariant),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsActive,
                        contentDescription = "Hatırlatma",
                        tint = colors.primary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onPrimaryContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                ReminderMetaLine(task = task, folder = folder)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                onClick = onMarkDone,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = colors.primary
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 9.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = colors.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Yapıldı",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onPrimary
                    )
                }
            }

            Surface(
                onClick = onSnoozeClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.outline)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 9.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = colors.onSurface,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Ertele",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun ReminderActionRow(
    task: TaskItem,
    folder: FolderItem?,
    onMarkDone: () -> Unit,
    onSnoozeClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            ReminderMetaLine(task = task, folder = folder)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                onClick = onMarkDone,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(11.dp),
                color = colors.primary
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 7.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = colors.onPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Yapıldı",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onPrimary
                    )
                }
            }

            Surface(
                onClick = onSnoozeClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(11.dp),
                color = colors.surfaceContainerLow,
                border = BorderStroke(1.dp, colors.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 7.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = colors.onSurface,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Ertele",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun ReminderMetaLine(
    task: TaskItem,
    folder: FolderItem?
) {
    val isDark = LocalIsDarkTheme.current
    val colors = MaterialTheme.colorScheme
    val scheduleText = DateTimeUtils.formatScheduledLabel(
        dateMillis = task.scheduledDateMillis,
        hour = task.scheduledHour,
        minute = task.scheduledMinute,
        timeWindow = task.flexibleTimeWindow
    )
    val meta = buildString {
        if (folder != null) append(folder.name)
        if (scheduleText.isNotBlank()) {
            if (isNotEmpty()) append(" · ")
            append(scheduleText)
        }
        task.reminderSummaryLabel?.let { summary ->
            append(" · ")
            append(summary)
        }
    }

    Text(
        text = meta,
        style = MaterialTheme.typography.labelSmall,
        color = folder?.colorTone?.accentColor(isDark) ?: colors.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
