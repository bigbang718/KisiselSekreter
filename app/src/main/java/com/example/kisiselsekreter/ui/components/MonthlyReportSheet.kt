package com.example.kisiselsekreter.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.kisiselsekreter.data.DateTimeUtils
import com.example.kisiselsekreter.data.DefaultData
import com.example.kisiselsekreter.data.FolderItem
import com.example.kisiselsekreter.data.MonthlyReportData
import com.example.kisiselsekreter.data.MonthlyStatsCalculator
import com.example.kisiselsekreter.data.TaskItem
import com.example.kisiselsekreter.ui.theme.KisiselSekreterTheme
import com.example.kisiselsekreter.ui.theme.LocalIsDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyReportSheet(
    folders: List<FolderItem>,
    tasks: List<TaskItem>,
    initialYear: Int,
    initialMonthZeroBased: Int,
    isFirstOfMonthWelcome: Boolean,
    onDismiss: () -> Unit,
    onReviewTaskClick: (TaskItem) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = MaterialTheme.colorScheme

    var selectedYear by remember(initialYear) { mutableIntStateOf(initialYear) }
    var selectedMonth by remember(initialMonthZeroBased) { mutableIntStateOf(initialMonthZeroBased) }

    val (currentYear, currentMonth) = remember { DateTimeUtils.currentYearMonth() }
    val canGoNext = selectedYear < currentYear || (selectedYear == currentYear && selectedMonth < currentMonth)

    val report = remember(folders, tasks, selectedYear, selectedMonth) {
        MonthlyStatsCalculator.calculate(
            folders = folders,
            tasks = tasks,
            year = selectedYear,
            monthZeroBased = selectedMonth
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        MonthlyReportContent(
            report = report,
            isFirstOfMonthWelcome = isFirstOfMonthWelcome,
            canGoNextMonth = canGoNext,
            onPreviousMonth = {
                val (nextY, nextM) = DateTimeUtils.shiftMonth(selectedYear, selectedMonth, -1)
                selectedYear = nextY
                selectedMonth = nextM
            },
            onNextMonth = {
                if (canGoNext) {
                    val (nextY, nextM) = DateTimeUtils.shiftMonth(selectedYear, selectedMonth, 1)
                    selectedYear = nextY
                    selectedMonth = nextM
                }
            },
            onDismiss = onDismiss,
            onReviewTaskClick = onReviewTaskClick
        )
    }
}

@Composable
internal fun MonthlyReportContent(
    report: MonthlyReportData,
    isFirstOfMonthWelcome: Boolean,
    canGoNextMonth: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDismiss: () -> Unit,
    onReviewTaskClick: (TaskItem) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 18.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Üst başlık
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (isFirstOfMonthWelcome) {
                    Surface(
                        shape = RoundedCornerShape(99.dp),
                        color = colors.primaryContainer,
                        border = BorderStroke(1.dp, colors.outlineVariant)
                    ) {
                        Text(
                            text = "Ayın 1'i · Yeni Ay Karşılaması",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Text(
                    text = "Aylık İstatistik Raporu",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onSurface
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Kapat",
                    tint = colors.onSurfaceVariant
                )
            }
        }

        // Ay değiştirici
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = "Önceki ay",
                        tint = colors.onSurface
                    )
                }
                Text(
                    text = report.periodLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface
                )
                IconButton(
                    onClick = onNextMonth,
                    enabled = canGoNextMonth
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "Sonraki ay",
                        tint = if (canGoNextMonth) colors.onSurface else colors.onSurfaceVariant.copy(alpha = 0.35f)
                    )
                }
            }
        }

        // 1. Genel özet & yargılayıcı olmayan değerlendirme
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ReportMetricPill(
                        label = "Tamamlanan Görev",
                        value = report.completedCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    ReportMetricPill(
                        label = "Tamamlama Oranı",
                        value = "%${report.completionRatePercent}",
                        modifier = Modifier.weight(1f)
                    )
                    ReportMetricPill(
                        label = "Aktif Gün",
                        value = report.activeDaysCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }

                LinearProgressIndicator(
                    progress = { (report.completionRatePercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(99.dp)),
                    color = colors.primary,
                    trackColor = colors.surfaceVariant
                )

                Text(
                    text = report.calmReflectionMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }
        }

        // 2. En İstikrarlı Olduğun Alan vurgusu
        report.mostConsistentArea?.let { area ->
            val tone = area.folder.colorTone
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = tone.containerColor(isDark),
                border = BorderStroke(1.dp, tone.borderColor(isDark)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = tone.accentColor(isDark),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "En İstikrarlı Olduğun Alan",
                            style = MaterialTheme.typography.labelLarge,
                            color = tone.accentColor(isDark)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = colors.surface.copy(alpha = if (isDark) 0.35f else 0.72f),
                            border = BorderStroke(1.dp, tone.borderColor(isDark)),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = area.folder.iconKey.icon,
                                    contentDescription = null,
                                    tint = tone.accentColor(isDark),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = area.folder.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.onSurface
                            )
                            Text(
                                text = "${area.completedCount} görev tamamlandı · ${area.activeDaysCount} aktif gün",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(99.dp),
                            color = colors.surface.copy(alpha = if (isDark) 0.4f else 0.78f),
                            border = BorderStroke(1.dp, tone.borderColor(isDark))
                        ) {
                            Text(
                                text = "%${area.completionRatioPercent}",
                                style = MaterialTheme.typography.labelMedium,
                                color = tone.accentColor(isDark),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = area.calmNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }

        // 3. Klasör bazlı minimal yatay çubuk grafikler
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Klasör Bazlı Dağılım",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface
                )

                val completedDistributions = report.folderDistributions.filter { it.completedCount > 0 }
                if (completedDistributions.isEmpty()) {
                    Text(
                        text = "Bu ay tamamlanan görevler oluştukça klasörlerinizin sakin dağılım grafiği burada belirir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                } else {
                    completedDistributions.forEach { item ->
                        val tone = item.folder.colorTone
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(tone.accentColor(isDark))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.folder.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${item.completedCount} görev · %${item.sharePercent}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.onSurfaceVariant
                                )
                            }

                            // Minimal yatay çubuk grafik
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(99.dp))
                                    .background(tone.containerColor(isDark))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(item.barFraction.coerceAtLeast(0.06f))
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(99.dp))
                                        .background(tone.accentColor(isDark))
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. En Sık Ertelenen Görevler — rutinleri nazikçe gözden geçirme alanı
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "En Sık Ertelenen Görevler",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface
                    )
                }

                Text(
                    text = "Sık ertelenen işler bir eksiklik değil; zamanlamanın veya sıklığın yaşam ritminize göre yeniden ayarlanabileceğine dair nazik bir işarettir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )

                if (report.mostSnoozedTasks.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = colors.surfaceContainerLow,
                        border = BorderStroke(1.dp, colors.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Şu ana kadar sık ertelenen bir görev kaydı yok. Rutinleriniz dengeli görünüyor.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                } else {
                    report.mostSnoozedTasks.forEachIndexed { index, item ->
                        if (index > 0) {
                            HorizontalDivider(thickness = 1.dp, color = colors.outlineVariant)
                        }
                        val tone = item.folder?.colorTone
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = item.task.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    itemVerticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (item.folder != null && tone != null) {
                                        Surface(
                                            shape = RoundedCornerShape(99.dp),
                                            color = tone.containerColor(isDark),
                                            border = BorderStroke(1.dp, tone.borderColor(isDark))
                                        ) {
                                            Text(
                                                text = if (item.subFolderName != null) {
                                                    "${item.folder.name} · ${item.subFolderName}"
                                                } else {
                                                    item.folder.name
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = tone.accentColor(isDark),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(99.dp),
                                        color = colors.surfaceContainerLow,
                                        border = BorderStroke(1.dp, colors.outlineVariant)
                                    ) {
                                        Text(
                                            text = "${item.task.snoozeCount} kez ertelendi",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            TextButton(onClick = { onReviewTaskClick(item.task) }) {
                                Icon(
                                    imageVector = Icons.Outlined.Tune,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Gözden Geçir")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportMetricPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
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

@Preview(showBackground = true, name = "Aylık İstatistik Raporu")
@Composable
private fun MonthlyReportContentPreview() {
    val (year, month) = DateTimeUtils.currentYearMonth()
    val report = MonthlyStatsCalculator.calculate(
        folders = DefaultData.defaultFolders(),
        tasks = DefaultData.defaultSampleTasks(),
        year = year,
        monthZeroBased = month
    )
    KisiselSekreterTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            MonthlyReportContent(
                report = report,
                isFirstOfMonthWelcome = true,
                canGoNextMonth = false,
                onPreviousMonth = {},
                onNextMonth = {},
                onDismiss = {},
                onReviewTaskClick = {}
            )
        }
    }
}
