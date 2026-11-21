package com.example.kisiselsekreter.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kisiselsekreter.data.DateTimeUtils
import com.example.kisiselsekreter.data.FolderItem
import com.example.kisiselsekreter.data.ReminderType
import com.example.kisiselsekreter.data.TaskItem
import com.example.kisiselsekreter.ui.theme.LocalIsDarkTheme

@Composable
fun TaskRowCard(
    task: TaskItem,
    folder: FolderItem?,
    subFolderName: String?,
    showFolderBadge: Boolean,
    onToggleComplete: () -> Unit,
    onEditClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    onPlanClick: (() -> Unit)? = null,
    onSnoozeClick: (() -> Unit)? = null,
    onIncrementCounter: (() -> Unit)? = null,
    onDecrementCounter: (() -> Unit)? = null
) {
    val isDark = LocalIsDarkTheme.current
    val colors = MaterialTheme.colorScheme
    val accentColor = folder?.colorTone?.accentColor(isDark) ?: colors.primary
    val toneContainer = folder?.colorTone?.containerColor(isDark) ?: colors.surfaceContainer
    val toneBorder = folder?.colorTone?.borderColor(isDark) ?: colors.outlineVariant

    Surface(
        onClick = onEditClick,
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (task.isCompleted) 0.74f else 1f),
        shape = RoundedCornerShape(18.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Sakin dairesel onay kutusu
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (task.isCompleted) accentColor else colors.surface)
                        .border(
                            width = 1.5.dp,
                            color = if (task.isCompleted) accentColor else colors.outline,
                            shape = CircleShape
                        )
                        .clickable(onClick = onToggleComplete),
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isCompleted) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Tamamlandı",
                            tint = colors.background,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurface,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )

                    if (task.note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = task.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    val reminderLabel = task.reminderSummaryLabel
                    val hasMeta = (showFolderBadge && folder != null) ||
                        !subFolderName.isNullOrBlank() ||
                        task.isPlanned ||
                        reminderLabel != null

                    if (hasMeta) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (task.isPlanned) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (task.isOverdue) {
                                        colors.error.copy(alpha = 0.12f)
                                    } else {
                                        colors.surfaceContainer
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        if (task.isOverdue) colors.error.copy(alpha = 0.4f) else colors.outlineVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Schedule,
                                            contentDescription = null,
                                            tint = if (task.isOverdue) colors.error else colors.onSurfaceVariant,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = DateTimeUtils.formatScheduledLabel(
                                                dateMillis = task.scheduledDateMillis,
                                                hour = task.scheduledHour,
                                                minute = task.scheduledMinute,
                                                timeWindow = task.flexibleTimeWindow
                                            ),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (task.isOverdue) colors.error else colors.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            if (reminderLabel != null && task.isPlanned && !task.isCompleted) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = colors.surfaceContainerLow,
                                    border = BorderStroke(1.dp, colors.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (task.reminderType == ReminderType.ROUTINE) {
                                                Icons.Outlined.Repeat
                                            } else {
                                                Icons.Outlined.WbTwilight
                                            },
                                            contentDescription = null,
                                            tint = accentColor,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = reminderLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            if (showFolderBadge && folder != null) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = toneContainer,
                                    border = BorderStroke(1.dp, toneBorder)
                                ) {
                                    Text(
                                        text = folder.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            if (!subFolderName.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = colors.surfaceContainerLow,
                                    border = BorderStroke(1.dp, colors.outlineVariant)
                                ) {
                                    Text(
                                        text = subFolderName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Sağ aksiyonlar: Planlanmamışsa "Planla", planlıysa ertele/düzenle + sil
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!task.isPlanned && !task.isCompleted && onPlanClick != null) {
                        Surface(
                            onClick = onPlanClick,
                            shape = RoundedCornerShape(12.dp),
                            color = toneContainer,
                            border = BorderStroke(1.dp, toneBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarMonth,
                                    contentDescription = "Planla",
                                    tint = accentColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Planla",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.onSurface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(2.dp))
                    } else if (task.isPlanned && !task.isCompleted && onSnoozeClick != null) {
                        IconButton(
                            onClick = onSnoozeClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Snooze,
                                contentDescription = "Ertele",
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Görevi düzenle",
                                tint = colors.onSurfaceVariant.copy(alpha = 0.72f),
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Kaydı sil",
                            tint = colors.onSurfaceVariant.copy(alpha = 0.65f),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            // Opsiyonel sayaç / ilerleme alanı (örn. 3/10 veya 14/25 sayfa)
            if (task.hasCounter && task.counterTarget > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                val progress = (task.counterCurrent.toFloat() / task.counterTarget.toFloat()).coerceIn(0f, 1f)

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.surfaceContainerLow,
                    border = BorderStroke(1.dp, colors.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = task.counterDisplayText,
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.onSurface
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    onClick = { onDecrementCounter?.invoke() },
                                    enabled = task.counterCurrent > 0 && onDecrementCounter != null,
                                    shape = CircleShape,
                                    color = colors.surface,
                                    border = BorderStroke(1.dp, colors.outlineVariant),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Remove,
                                            contentDescription = "Sayacı azalt",
                                            tint = if (task.counterCurrent > 0) colors.onSurface else colors.onSurfaceVariant.copy(alpha = 0.4f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Surface(
                                    onClick = { onIncrementCounter?.invoke() },
                                    enabled = task.counterCurrent < task.counterTarget && onIncrementCounter != null,
                                    shape = CircleShape,
                                    color = toneContainer,
                                    border = BorderStroke(1.dp, toneBorder),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = "Sayacı artır",
                                            tint = if (task.counterCurrent < task.counterTarget) accentColor else colors.onSurfaceVariant.copy(alpha = 0.4f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(99.dp)),
                            color = accentColor,
                            trackColor = toneContainer
                        )
                    }
                }
            }
        }
    }
}
