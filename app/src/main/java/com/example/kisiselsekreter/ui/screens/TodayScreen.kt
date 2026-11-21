package com.example.kisiselsekreter.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kisiselsekreter.data.FolderItem
import com.example.kisiselsekreter.data.TaskItem
import com.example.kisiselsekreter.ui.components.TaskRowCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TodayScreen(
    folders: List<FolderItem>,
    tasks: List<TaskItem>,
    onToggleTaskComplete: (String) -> Unit,
    onSnoozeTaskClick: (TaskItem) -> Unit,
    onEditTaskClick: (TaskItem) -> Unit,
    onIncrementCounter: (String) -> Unit,
    onDecrementCounter: (String) -> Unit,
    onDeleteTask: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val folderMap = remember(folders) { folders.associateBy { it.id } }

    val overdueTasks = tasks
        .filter { it.isOverdue }
        .sortedBy { it.scheduledSortKey }

    val todayTasks = tasks
        .filter { it.isScheduledForToday }
        .sortedBy { it.scheduledSortKey }

    val pendingToday = todayTasks.filter { !it.isCompleted }
    val completedToday = todayTasks.filter { it.isCompleted }
    val progress = if (todayTasks.isEmpty()) 0f else completedToday.size.toFloat() / todayTasks.size.toFloat()

    val formattedDate = remember {
        val localeTr = Locale.forLanguageTag("tr-TR")
        val raw = SimpleDateFormat("EEEE, d MMMM", localeTr).format(Date())
        raw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(localeTr) else it.toString() }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Günün özet kartı
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = colors.surfaceContainer,
                border = BorderStroke(1.dp, colors.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when {
                            todayTasks.isEmpty() && overdueTasks.isEmpty() ->
                                "Bugün için sakin ve esnek bir planınız var."
                            pendingToday.isEmpty() && overdueTasks.isEmpty() ->
                                "Bugünkü tüm planlı işlerinizi tamamladınız."
                            else ->
                                "Adım adım ilerleyin; bugün odaklanmanız gereken ${pendingToday.size} planlı iş var."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )

                    if (todayTasks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Günlük ilerleme",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.onSurfaceVariant
                            )
                            Text(
                                text = "${completedToday.size} / ${todayTasks.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(99.dp)),
                            color = colors.primary,
                            trackColor = colors.outlineVariant
                        )
                    }
                }
            }
        }

        if (overdueTasks.isNotEmpty()) {
            item {
                Text(
                    text = "Önceki Günlerden Kalanlar (${overdueTasks.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.error,
                    modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                )
            }

            items(overdueTasks, key = { it.id }) { task ->
                val folder = folderMap[task.folderId]
                val subName = folder?.subFolders?.firstOrNull { it.id == task.subFolderId }?.name
                TaskRowCard(
                    task = task,
                    folder = folder,
                    subFolderName = subName,
                    showFolderBadge = true,
                    onToggleComplete = { onToggleTaskComplete(task.id) },
                    onEditClick = { onEditTaskClick(task) },
                    onDelete = { onDeleteTask(task.id) },
                    onSnoozeClick = { onSnoozeTaskClick(task) },
                    onIncrementCounter = { onIncrementCounter(task.id) },
                    onDecrementCounter = { onDecrementCounter(task.id) }
                )
            }
        }

        if (todayTasks.isEmpty() && overdueTasks.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 38.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Bugün için planlı iş yok",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Sağ alttaki + butonuyla bugüne yeni bir görev planlayabilirsiniz.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            if (pendingToday.isNotEmpty()) {
                item {
                    Text(
                        text = "Bugünkü Planlı İşler",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                    )
                }

                items(pendingToday, key = { it.id }) { task ->
                    val folder = folderMap[task.folderId]
                    val subName = folder?.subFolders?.firstOrNull { it.id == task.subFolderId }?.name
                    TaskRowCard(
                        task = task,
                        folder = folder,
                        subFolderName = subName,
                        showFolderBadge = true,
                        onToggleComplete = { onToggleTaskComplete(task.id) },
                        onEditClick = { onEditTaskClick(task) },
                        onDelete = { onDeleteTask(task.id) },
                        onSnoozeClick = { onSnoozeTaskClick(task) },
                        onIncrementCounter = { onIncrementCounter(task.id) },
                        onDecrementCounter = { onDecrementCounter(task.id) }
                    )
                }
            }

            if (completedToday.isNotEmpty()) {
                item {
                    Text(
                        text = "Bugün Tamamlananlar (${completedToday.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, start = 2.dp)
                    )
                }

                items(completedToday, key = { it.id }) { task ->
                    val folder = folderMap[task.folderId]
                    val subName = folder?.subFolders?.firstOrNull { it.id == task.subFolderId }?.name
                    TaskRowCard(
                        task = task,
                        folder = folder,
                        subFolderName = subName,
                        showFolderBadge = true,
                        onToggleComplete = { onToggleTaskComplete(task.id) },
                        onEditClick = { onEditTaskClick(task) },
                        onDelete = { onDeleteTask(task.id) },
                        onIncrementCounter = { onIncrementCounter(task.id) },
                        onDecrementCounter = { onDecrementCounter(task.id) }
                    )
                }
            }
        }
    }
}
