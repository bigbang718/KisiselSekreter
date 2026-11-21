package com.example.kisiselsekreter.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.kisiselsekreter.data.DateTimeUtils
import com.example.kisiselsekreter.data.DefaultData
import com.example.kisiselsekreter.data.FolderItem
import com.example.kisiselsekreter.data.TaskItem
import com.example.kisiselsekreter.ui.theme.KisiselSekreterTheme
import com.example.kisiselsekreter.ui.theme.LocalIsDarkTheme
import kotlin.math.roundToInt

@Composable
fun HistoryReportScreen(
    folders: List<FolderItem>,
    tasks: List<TaskItem>,
    onUndoComplete: (String) -> Unit,
    onOpenMonthlyReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val colors = MaterialTheme.colorScheme
    val folderMap = remember(folders) { folders.associateBy { it.id } }

    var selectedFolderFilterId by rememberSaveable { mutableStateOf<String?>(null) }

    // Seçili klasör silindiyse filtreyi sıfırla
    val activeFilterId = if (selectedFolderFilterId != null && folderMap.containsKey(selectedFolderFilterId)) {
        selectedFolderFilterId
    } else {
        null
    }

    val allCompletedTasks = remember(tasks) {
        tasks
            .filter { it.isCompleted }
            .sortedByDescending { it.completedAtMillis ?: it.createdAtMillis }
    }

    val filteredCompletedTasks = remember(allCompletedTasks, activeFilterId) {
        if (activeFilterId == null) {
            allCompletedTasks
        } else {
            allCompletedTasks.filter { it.folderId == activeFilterId }
        }
    }

    val groupedByDay = remember(filteredCompletedTasks) {
        filteredCompletedTasks
            .groupBy { DateTimeUtils.dayBucketKey(it.completedAtMillis ?: it.createdAtMillis) }
            .toList()
            .sortedByDescending { it.first }
    }

    val completedTodayCount = remember(allCompletedTasks) {
        allCompletedTasks.count { DateTimeUtils.isToday(it.completedAtMillis) }
    }

    val overallRatePercent = remember(tasks) {
        if (tasks.isEmpty()) 0
        else ((tasks.count { it.isCompleted }.toFloat() / tasks.size.toFloat()) * 100f).roundToInt()
    }

    val completedCountByFolder = remember(allCompletedTasks) {
        allCompletedTasks.groupingBy { it.folderId }.eachCount()
    }

    val currentMonthLabel = remember {
        val (year, month) = DateTimeUtils.currentYearMonth()
        DateTimeUtils.formatMonthYear(year, month)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Aylık İstatistik Raporu çağrı kartı
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = colors.surfaceContainer,
                border = BorderStroke(1.dp, colors.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = colors.primaryContainer,
                                border = BorderStroke(1.dp, colors.outlineVariant),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.BarChart,
                                        contentDescription = null,
                                        tint = colors.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Aylık İstatistik Raporu",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.onSurface
                                )
                                Text(
                                    text = "$currentMonthLabel · Klasör dengesi ve rutin özeti",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Button(
                        onClick = onOpenMonthlyReport,
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Aylık Raporu Görüntüle")
                    }
                }
            }
        }

        // Minimal özet sayılar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricTile(
                    label = "Tamamlanan",
                    value = allCompletedTasks.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "Bugün Biten",
                    value = completedTodayCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "Genel Oran",
                    value = "%$overallRatePercent",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Klasöre göre filtreleme çubuğu
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Geçmiş Paneli · Klasöre Göre Filtrele",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(start = 2.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    item(key = "filter_all") {
                        val selected = activeFilterId == null
                        Surface(
                            onClick = { selectedFolderFilterId = null },
                            shape = RoundedCornerShape(99.dp),
                            color = if (selected) colors.primaryContainer else colors.surface,
                            border = BorderStroke(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) colors.primary else colors.outlineVariant
                            )
                        ) {
                            Text(
                                text = "Tümü (${allCompletedTasks.size})",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }

                    items(folders, key = { "filter_${it.id}" }) { folder ->
                        val selected = activeFilterId == folder.id
                        val count = completedCountByFolder[folder.id] ?: 0
                        val tone = folder.colorTone
                        Surface(
                            onClick = {
                                selectedFolderFilterId = if (selected) null else folder.id
                            },
                            shape = RoundedCornerShape(99.dp),
                            color = if (selected) tone.containerColor(isDark) else colors.surface,
                            border = BorderStroke(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) tone.accentColor(isDark) else colors.outlineVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(tone.accentColor(isDark))
                                )
                                Text(
                                    text = if (count > 0) "${folder.name} ($count)" else folder.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) tone.accentColor(isDark) else colors.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Tarih sırasıyla tamamlanan görev listesi
        if (groupedByDay.isEmpty()) {
            item {
                val filterFolderName = activeFilterId?.let { folderMap[it]?.name }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (filterFolderName != null) {
                                "$filterFolderName klasöründe tamamlanan kayıt yok"
                            } else {
                                "Henüz tamamlanan kayıt yok"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tamamladığınız görevler tarih sırasıyla ve klasör renk etiketleriyle burada sakin bir arşiv oluşturur.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            groupedByDay.forEach { (dayKey, dayTasks) ->
                item(key = "header_$dayKey") {
                    Text(
                        text = DateTimeUtils.formatDateGroupHeader(dayKey),
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                    )
                }

                items(dayTasks, key = { it.id }) { task ->
                    val folder = folderMap[task.folderId]
                    val subName = folder?.subFolders?.firstOrNull { it.id == task.subFolderId }?.name
                    val completedTimeText = DateTimeUtils.formatTimeFromMillis(
                        task.completedAtMillis ?: task.createdAtMillis
                    )

                    CompletedHistoryCard(
                        task = task,
                        folder = folder,
                        subFolderName = subName,
                        completedTimeText = completedTimeText,
                        isDark = isDark,
                        onUndoComplete = { onUndoComplete(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletedHistoryCard(
    task: TaskItem,
    folder: FolderItem?,
    subFolderName: String?,
    completedTimeText: String,
    isDark: Boolean,
    onUndoComplete: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val tone = folder?.colorTone

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = tone?.accentColor(isDark) ?: colors.primary,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurface
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    itemVerticalAlignment = Alignment.CenterVertically
                ) {
                    if (folder != null && tone != null) {
                        // Klasörün kendi nude renk etiketi
                        Surface(
                            shape = RoundedCornerShape(99.dp),
                            color = tone.containerColor(isDark),
                            border = BorderStroke(1.dp, tone.borderColor(isDark))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(tone.accentColor(isDark))
                                )
                                Text(
                                    text = if (subFolderName != null) {
                                        "${folder.name} · $subFolderName"
                                    } else {
                                        folder.name
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tone.accentColor(isDark),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    if (task.hasCounter && task.counterTarget > 0) {
                        Surface(
                            shape = RoundedCornerShape(99.dp),
                            color = colors.surfaceContainerLow,
                            border = BorderStroke(1.dp, colors.outlineVariant)
                        ) {
                            Text(
                                text = task.counterDisplayText,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = completedTimeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onUndoComplete) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Undo,
                    contentDescription = "Tekrar aç",
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = colors.surfaceContainer,
        border = BorderStroke(1.dp, colors.outlineVariant),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = colors.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true, name = "Geçmiş & Rapor Ekranı")
@Composable
private fun HistoryReportScreenPreview() {
    KisiselSekreterTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            HistoryReportScreen(
                folders = DefaultData.defaultFolders(),
                tasks = DefaultData.defaultSampleTasks(),
                onUndoComplete = {},
                onOpenMonthlyReport = {}
            )
        }
    }
}
