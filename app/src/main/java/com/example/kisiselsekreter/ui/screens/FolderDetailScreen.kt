package com.example.kisiselsekreter.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kisiselsekreter.data.FolderItem
import com.example.kisiselsekreter.data.SubFolder
import com.example.kisiselsekreter.data.TaskItem
import com.example.kisiselsekreter.ui.components.TaskRowCard
import com.example.kisiselsekreter.ui.theme.LocalIsDarkTheme

private enum class FolderTaskSection(val label: String) {
    PLANNED("Planlı İşler"),
    UNPLANNED("Planlanmamış Yapılacaklar")
}

@Composable
fun FolderDetailScreen(
    folder: FolderItem,
    selectedSubFolderId: String?,
    tasks: List<TaskItem>,
    onSelectSubFolder: (String?) -> Unit,
    onEditFolder: () -> Unit,
    onAddSubFolder: () -> Unit,
    onEditSubFolder: (SubFolder) -> Unit,
    onAddQuickUnplannedTask: (String) -> Unit,
    onPlanTaskClick: (TaskItem) -> Unit,
    onSnoozeTaskClick: (TaskItem) -> Unit,
    onEditTaskClick: (TaskItem) -> Unit,
    onToggleTaskComplete: (String) -> Unit,
    onIncrementCounter: (String) -> Unit,
    onDecrementCounter: (String) -> Unit,
    onDeleteTask: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val colors = MaterialTheme.colorScheme
    val containerColor = folder.colorTone.containerColor(isDark)
    val borderColor = folder.colorTone.borderColor(isDark)
    val accentColor = folder.colorTone.accentColor(isDark)

    var activeSection by rememberSaveable(folder.id) { mutableStateOf(FolderTaskSection.PLANNED) }
    var quickUnplannedInput by remember(folder.id, selectedSubFolderId) { mutableStateOf("") }

    val scopedTasks = tasks.filter { task ->
        task.folderId == folder.id &&
            (selectedSubFolderId == null || task.subFolderId == selectedSubFolderId)
    }

    val plannedTasks = scopedTasks
        .filter { it.isPlanned }
        .sortedWith(compareBy<TaskItem> { it.isCompleted }.thenBy { it.scheduledSortKey })
    val plannedActive = plannedTasks.filter { !it.isCompleted }
    val plannedCompleted = plannedTasks.filter { it.isCompleted }

    val unplannedTasks = scopedTasks
        .filter { !it.isPlanned }
        .sortedWith(compareBy<TaskItem> { it.isCompleted }.thenByDescending { it.createdAtMillis })
    val unplannedActive = unplannedTasks.filter { !it.isCompleted }
    val unplannedCompleted = unplannedTasks.filter { it.isCompleted }

    val activeTasks = if (activeSection == FolderTaskSection.PLANNED) plannedActive else unplannedActive
    val completedTasks = if (activeSection == FolderTaskSection.PLANNED) plannedCompleted else unplannedCompleted

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Klasör başlık kartı
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = containerColor,
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = colors.surface.copy(alpha = if (isDark) 0.25f else 0.78f),
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = folder.iconKey.icon,
                                contentDescription = folder.name,
                                tint = accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = folder.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${plannedActive.size} planlı · ${unplannedActive.size} planlanmamış",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onAddSubFolder) {
                        Icon(
                            imageVector = Icons.Outlined.CreateNewFolder,
                            contentDescription = "Alt klasör ekle",
                            tint = colors.onSurface
                        )
                    }
                    IconButton(onClick = onEditFolder) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Klasörü düzenle",
                            tint = colors.onSurface
                        )
                    }
                }
            }
        }

        // Alt klasörler şeridi
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = { onSelectSubFolder(null) },
                    shape = RoundedCornerShape(14.dp),
                    color = if (selectedSubFolderId == null) containerColor else colors.surface,
                    border = BorderStroke(
                        width = if (selectedSubFolderId == null) 1.5.dp else 1.dp,
                        color = if (selectedSubFolderId == null) accentColor else colors.outlineVariant
                    )
                ) {
                    Text(
                        text = "Tümü",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurface,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                    )
                }

                folder.subFolders.forEach { sub ->
                    val isSelected = sub.id == selectedSubFolderId
                    Surface(
                        onClick = { onSelectSubFolder(sub.id) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) containerColor else colors.surface,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) accentColor else colors.outlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                start = 14.dp,
                                end = if (isSelected) 6.dp else 14.dp,
                                top = 7.dp,
                                bottom = 7.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sub.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.onSurface
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { onEditSubFolder(sub) },
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = "${sub.name} alt klasörünü düzenle",
                                        tint = accentColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Surface(
                    onClick = onAddSubFolder,
                    shape = RoundedCornerShape(14.dp),
                    color = colors.surfaceContainerLow,
                    border = BorderStroke(1.dp, colors.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Alt klasör ekle",
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Alt Klasör",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // İki sekme: Planlı İşler & Planlanmamış Yapılacaklar
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colors.surfaceContainer,
                border = BorderStroke(1.dp, colors.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FolderTaskSection.entries.forEach { section ->
                        val isSelected = activeSection == section
                        val count = if (section == FolderTaskSection.PLANNED) {
                            plannedActive.size
                        } else {
                            unplannedActive.size
                        }
                        Surface(
                            onClick = { activeSection = section },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) colors.surface else colors.surfaceContainer,
                            border = if (isSelected) {
                                BorderStroke(1.dp, borderColor)
                            } else {
                                null
                            }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = section.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) colors.onSurface else colors.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$count açık",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) accentColor else colors.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Planlanmamış sekmesinde tarih seçmeden hızlı not / görev ekleme çubuğu
        if (activeSection == FolderTaskSection.UNPLANNED) {
            item {
                val submitQuickUnplanned = {
                    if (quickUnplannedInput.isNotBlank()) {
                        onAddQuickUnplannedTask(quickUnplannedInput)
                        quickUnplannedInput = ""
                    }
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = quickUnplannedInput,
                            onValueChange = { quickUnplannedInput = it },
                            placeholder = {
                                Text(
                                    text = "Tarih seçmeden hızlı görev veya not ekle...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.onSurfaceVariant.copy(alpha = 0.75f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { submitQuickUnplanned() }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                                focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Surface(
                            onClick = submitQuickUnplanned,
                            enabled = quickUnplannedInput.trim().isNotEmpty(),
                            shape = RoundedCornerShape(12.dp),
                            color = if (quickUnplannedInput.trim().isNotEmpty()) containerColor else colors.surfaceContainerLow,
                            border = BorderStroke(
                                1.dp,
                                if (quickUnplannedInput.trim().isNotEmpty()) borderColor else colors.outlineVariant
                            ),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Hızlı ekle",
                                    tint = if (quickUnplannedInput.trim().isNotEmpty()) accentColor else colors.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (activeTasks.isEmpty() && completedTasks.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (activeSection == FolderTaskSection.PLANNED) {
                                "Planlı iş bulunmuyor"
                            } else {
                                "Planlanmamış yapılacak yok"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (activeSection == FolderTaskSection.PLANNED) {
                                "Sağ alttaki + butonuyla tarihli bir görev ekleyebilir veya planlanmamış bir maddeyi tek tıkla planlayabilirsiniz."
                            } else {
                                "Yukarıdaki alandan tarih seçmeden hemen not veya görev bırakabilirsiniz."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            if (activeTasks.isNotEmpty()) {
                items(activeTasks, key = { it.id }) { task ->
                    val subName = folder.subFolders.firstOrNull { it.id == task.subFolderId }?.name
                    TaskRowCard(
                        task = task,
                        folder = folder,
                        subFolderName = subName,
                        showFolderBadge = false,
                        onToggleComplete = { onToggleTaskComplete(task.id) },
                        onEditClick = { onEditTaskClick(task) },
                        onDelete = { onDeleteTask(task.id) },
                        onPlanClick = if (!task.isPlanned) {
                            { onPlanTaskClick(task) }
                        } else {
                            null
                        },
                        onSnoozeClick = if (task.isPlanned) {
                            { onSnoozeTaskClick(task) }
                        } else {
                            null
                        },
                        onIncrementCounter = { onIncrementCounter(task.id) },
                        onDecrementCounter = { onDecrementCounter(task.id) }
                    )
                }
            }

            if (completedTasks.isNotEmpty()) {
                item {
                    Text(
                        text = "Tamamlananlar (${completedTasks.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, start = 2.dp)
                    )
                }

                items(completedTasks, key = { it.id }) { task ->
                    val subName = folder.subFolders.firstOrNull { it.id == task.subFolderId }?.name
                    TaskRowCard(
                        task = task,
                        folder = folder,
                        subFolderName = subName,
                        showFolderBadge = false,
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
