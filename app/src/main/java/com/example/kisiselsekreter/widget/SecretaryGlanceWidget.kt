package com.example.kisiselsekreter.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.kisiselsekreter.MainActivity
import com.example.kisiselsekreter.R
import com.example.kisiselsekreter.data.DateTimeUtils
import com.example.kisiselsekreter.data.DefaultData
import com.example.kisiselsekreter.data.FolderItem
import com.example.kisiselsekreter.data.LocalStorageRepository
import com.example.kisiselsekreter.data.TaskItem
import com.example.kisiselsekreter.data.ThemePreference
import com.example.kisiselsekreter.notifications.ReminderScheduler
import com.example.kisiselsekreter.ui.theme.FolderColorTone

data class WidgetTaskRowModel(
    val task: TaskItem,
    val folderName: String,
    val folderTone: FolderColorTone,
    val scheduleText: String,
    val badgeText: String?,
    val isOverdue: Boolean
)

class SecretaryGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SecretaryGlanceWidget()
}

class SecretaryGlanceWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    // Canonical Layouts kılavuzuna uygun olarak tüm ekran boyutlarına net uyum sağlayan Exact modu
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = LocalStorageRepository(context)
        val initialSnapshot = repository.getSnapshotOnce()

        provideContent {
            // Revizyon anahtarı okunarak updateAppWidgetState sonrası anında yeniden çizim garantilenir
            val revision = currentState<Preferences>()[KEY_REVISION] ?: 0
            val data by repository.dataFlow.collectAsState(initial = initialSnapshot)
            val rows = buildWidgetTaskRows(data.folders, data.tasks)
            val todayDoneCount = data.tasks.count { it.isCompleted && DateTimeUtils.isToday(it.completedAtMillis) }
            val todayOpenCount = data.tasks.count { it.isPlanned && !it.isCompleted && (it.isOverdue || it.isScheduledForToday) }

            GlanceTheme(colors = SecretaryWidgetColors.colorProvidersFor(data.themePreference)) {
                SecretaryWidgetContent(
                    rows = rows,
                    todayOpenCount = todayOpenCount,
                    todayDoneCount = todayDoneCount,
                    themePreference = data.themePreference,
                    revision = revision
                )
            }
        }
    }

    override fun onCompositionError(
        context: Context,
        glanceId: GlanceId,
        appWidgetId: Int,
        throwable: Throwable
    ) {
        // Sıfır bitmap kullanan güvenli metin yedek görünümü
        val fallback = RemoteViews(context.packageName, android.R.layout.simple_list_item_1).apply {
            setTextViewText(android.R.id.text1, "Kişisel Sekreter görevleri şu an yüklenemedi")
        }
        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, fallback)
    }

    companion object {
        val KEY_REVISION = intPreferencesKey("secretary_widget_revision")

        fun buildWidgetTaskRows(
            folders: List<FolderItem>,
            tasks: List<TaskItem>
        ): List<WidgetTaskRowModel> {
            val folderMap = folders.associateBy { it.id }
            val activePlanned = tasks
                .filter { it.isPlanned && !it.isCompleted }
                .sortedWith(
                    compareByDescending<TaskItem> { it.isOverdue || it.isScheduledForToday }
                        .thenBy { it.scheduledSortKey }
                )
                .take(12)

            return activePlanned.map { task ->
                val folder = folderMap[task.folderId]
                val tone = folder?.colorTone ?: FolderColorTone.WARM_STONE_GREY
                val scheduleLabel = DateTimeUtils.formatScheduledLabel(
                    dateMillis = task.scheduledDateMillis,
                    hour = task.scheduledHour,
                    minute = task.scheduledMinute,
                    timeWindow = task.flexibleTimeWindow
                )
                val counterSuffix = if (task.hasCounter && task.counterTarget > 0) {
                    " · ${task.counterDisplayText}"
                } else {
                    ""
                }
                val badge = when {
                    task.isOverdue -> "Bekliyor"
                    task.isScheduledForToday -> "Bugün"
                    DateTimeUtils.isTomorrow(task.scheduledDateMillis) -> "Yarın"
                    else -> task.scheduledDateMillis?.let { DateTimeUtils.formatDateShort(it) }
                }

                WidgetTaskRowModel(
                    task = task,
                    folderName = folder?.name ?: "Klasör",
                    folderTone = tone,
                    scheduleText = "$scheduleLabel$counterSuffix",
                    badgeText = badge,
                    isOverdue = task.isOverdue
                )
            }
        }
    }
}

@Composable
internal fun SecretaryWidgetContent(
    rows: List<WidgetTaskRowModel>,
    todayOpenCount: Int,
    todayDoneCount: Int,
    themePreference: ThemePreference,
    revision: Int = 0
) {
    val size = LocalSize.current
    val isNarrow = size.width < 220.dp
    val isShort = size.height < 140.dp
    val showTrailingBadge = size.width >= 260.dp

    val subtitleText = when {
        rows.isEmpty() && todayDoneCount > 0 -> "Bugün $todayDoneCount görev tamamlandı"
        rows.isEmpty() -> "Bekleyen planlı görev yok"
        todayOpenCount > 0 -> "Bugün $todayOpenCount · Toplam ${rows.size} yaklaşan"
        else -> "${rows.size} yaklaşan görev"
    }

    Scaffold(
        backgroundColor = SecretaryWidgetColors.widgetBackground(themePreference),
        horizontalPadding = 12.dp,
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(top = if (isShort) 8.dp else 12.dp, bottom = 10.dp)
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            // Sakin üst başlık — tıklandığında uygulamayı açar
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(bottom = if (isShort) 6.dp else 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .clickable(actionStartActivity<MainActivity>()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = GlanceModifier
                            .size(30.dp)
                            .cornerRadius(10.dp)
                            .background(SecretaryWidgetColors.softPillSurface(themePreference)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_widget_calendar),
                            contentDescription = "Kişisel Sekreter",
                            colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
                            modifier = GlanceModifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = GlanceModifier.width(9.dp))
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = if (isNarrow) "Görevler" else "Kişisel Sekreter",
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurface,
                                fontSize = if (isNarrow) 13.sp else 15.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                        if (!isShort) {
                            Text(
                                text = subtitleText,
                                style = TextStyle(
                                    color = GlanceTheme.colors.onSurfaceVariant,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }

                // Hafif yenileme butonu (revizyon bilgisini görünür akışta tutar)
                CircleIconButton(
                    imageProvider = ImageProvider(R.drawable.ic_widget_refresh),
                    contentDescription = "Yenile ($revision)",
                    contentColor = GlanceTheme.colors.secondary,
                    backgroundColor = null,
                    onClick = actionRunCallback<RefreshWidgetAction>(),
                    modifier = GlanceModifier.size(34.dp)
                )
            }

            if (rows.isEmpty()) {
                // Sakin boş durum
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .cornerRadius(16.dp)
                        .background(SecretaryWidgetColors.taskCardSurface(themePreference))
                        .clickable(actionStartActivity<MainActivity>())
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_widget_check_checked),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
                            modifier = GlanceModifier.size(24.dp)
                        )
                        Spacer(modifier = GlanceModifier.height(6.dp))
                        Text(
                            text = "Zihniniz ferah",
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = "Bugün ve yaklaşan için açık görev yok.",
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurfaceVariant,
                                fontSize = 11.sp
                            ),
                            maxLines = 2
                        )
                    }
                }
            } else {
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(
                        items = rows,
                        itemId = { item -> item.task.id.hashCode().toLong() }
                    ) { row ->
                        Column(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .padding(bottom = 7.dp)
                        ) {
                            WidgetTaskCard(
                                row = row,
                                themePreference = themePreference,
                                isNarrow = isNarrow,
                                showTrailingBadge = showTrailingBadge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetTaskCard(
    row: WidgetTaskRowModel,
    themePreference: ThemePreference,
    isNarrow: Boolean,
    showTrailingBadge: Boolean
) {
    val folderAccent = SecretaryWidgetColors.folderAccent(row.folderTone, themePreference)
    val folderContainer = SecretaryWidgetColors.folderContainer(row.folderTone, themePreference)

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .cornerRadius(14.dp)
            .background(SecretaryWidgetColors.taskCardSurface(themePreference))
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Doğrudan widget üzerinden tek tıkla tamamlama butonu
        Box(
            modifier = GlanceModifier
                .size(34.dp)
                .cornerRadius(17.dp)
                .background(folderContainer)
                .clickable(
                    actionRunCallback<CompleteTaskWidgetAction>(
                        actionParametersOf(CompleteTaskWidgetAction.KEY_TASK_ID to row.task.id)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_widget_check_unchecked),
                contentDescription = "${row.task.title} görevini tamamla",
                colorFilter = ColorFilter.tint(folderAccent),
                modifier = GlanceModifier.size(20.dp)
            )
        }

        Spacer(modifier = GlanceModifier.width(10.dp))

        // Görev başlığı ve klasör/zaman özeti — tıklandığında uygulamayı açar
        Column(
            modifier = GlanceModifier
                .defaultWeight()
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Text(
                text = row.task.title,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = if (isNarrow) 12.sp else 13.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = if (isNarrow) 1 else 2
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = "${row.folderName} · ${row.scheduleText}",
                style = TextStyle(
                    color = folderAccent,
                    fontSize = 11.sp
                ),
                maxLines = 1
            )
        }

        if (showTrailingBadge && row.badgeText != null) {
            Spacer(modifier = GlanceModifier.width(8.dp))
            Box(
                modifier = GlanceModifier
                    .cornerRadius(99.dp)
                    .background(folderContainer)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = row.badgeText,
                    style = TextStyle(
                        color = if (row.isOverdue) GlanceTheme.colors.error else folderAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

class CompleteTaskWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val taskId = parameters[KEY_TASK_ID] ?: return
        val repository = LocalStorageRepository(context)
        val (nextOccurrence, folders) = repository.completeTaskDirectly(taskId)
        val scheduler = ReminderScheduler(context)
        if (nextOccurrence != null) {
            val folderName = folders.firstOrNull { it.id == nextOccurrence.folderId }?.name.orEmpty()
            scheduler.scheduleTaskReminder(nextOccurrence, folderName)
        } else {
            scheduler.cancelTaskReminder(taskId)
        }
        SecretaryWidgetUpdater.refreshAll(context)
    }

    companion object {
        val KEY_TASK_ID = ActionParameters.Key<String>("task_id")
    }
}

class RefreshWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        SecretaryWidgetUpdater.refreshAll(context)
    }
}

object SecretaryWidgetUpdater {
    suspend fun refreshAll(context: Context) {
        val appContext = context.applicationContext
        val widget = SecretaryGlanceWidget()
        val glanceIds = runCatching {
            GlanceAppWidgetManager(appContext).getGlanceIds(SecretaryGlanceWidget::class.java)
        }.getOrDefault(emptyList())

        for (glanceId in glanceIds) {
            runCatching {
                updateAppWidgetState(appContext, glanceId) { prefs ->
                    prefs[SecretaryGlanceWidget.KEY_REVISION] =
                        (prefs[SecretaryGlanceWidget.KEY_REVISION] ?: 0) + 1
                }
                widget.update(appContext, glanceId)
            }
        }
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 310, heightDp = 260)
@Composable
private fun SecretaryWidgetLightPreview() {
    val rows = SecretaryGlanceWidget.buildWidgetTaskRows(
        folders = DefaultData.defaultFolders(),
        tasks = DefaultData.defaultSampleTasks()
    )
    GlanceTheme(colors = SecretaryWidgetColors.colorProvidersFor(ThemePreference.LIGHT)) {
        SecretaryWidgetContent(
            rows = rows,
            todayOpenCount = 5,
            todayDoneCount = 2,
            themePreference = ThemePreference.LIGHT
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 310, heightDp = 260)
@Composable
private fun SecretaryWidgetDarkPreview() {
    val rows = SecretaryGlanceWidget.buildWidgetTaskRows(
        folders = DefaultData.defaultFolders(),
        tasks = DefaultData.defaultSampleTasks()
    )
    GlanceTheme(colors = SecretaryWidgetColors.colorProvidersFor(ThemePreference.DARK)) {
        SecretaryWidgetContent(
            rows = rows,
            todayOpenCount = 5,
            todayDoneCount = 2,
            themePreference = ThemePreference.DARK
        )
    }
}
