package com.example.kisiselsekreter.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.kisiselsekreter.MainActivity
import com.example.kisiselsekreter.R
import com.example.kisiselsekreter.data.DateTimeUtils
import com.example.kisiselsekreter.data.FolderItem
import com.example.kisiselsekreter.data.LocalStorageRepository
import com.example.kisiselsekreter.data.QuietHoursConfig
import com.example.kisiselsekreter.data.TaskItem
import java.util.concurrent.TimeUnit

enum class TestReminderResult {
    SENT,
    SILENCED_BY_QUIET_HOURS,
    MISSING_PERMISSION
}

object ReminderNotificationHelper {
    const val CHANNEL_ID = "kisisel_sekreter_reminders"
    private const val CHANNEL_NAME = "Görev Hatırlatmaları"
    private const val CHANNEL_DESCRIPTION = "Planlı, rutin ve esnek görevleriniz için sakin hatırlatmalar"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val existing = manager.getNotificationChannel(CHANNEL_ID)
            if (existing == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = CHANNEL_DESCRIPTION
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun triggerTestReminder(
        context: Context,
        quietHours: QuietHoursConfig,
        nowMillis: Long = System.currentTimeMillis()
    ): TestReminderResult {
        if (DateTimeUtils.isWithinQuietHours(nowMillis, quietHours)) {
            return TestReminderResult.SILENCED_BY_QUIET_HOURS
        }
        if (!hasNotificationPermission(context)) {
            return TestReminderResult.MISSING_PERMISSION
        }
        showReminderNotification(
            context = context,
            taskId = "test_reminder_notification",
            title = "Sakin Hatırlatma Testi",
            folderName = "Kişisel Sekreter",
            scheduleText = "Bildirimleriniz ve sessiz saat ayarınız hazır"
        )
        return TestReminderResult.SENT
    }

    fun showReminderNotification(
        context: Context,
        taskId: String,
        title: String,
        folderName: String,
        scheduleText: String
    ) {
        if (!hasNotificationPermission(context)) return
        ensureChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = buildString {
            if (folderName.isNotBlank()) append(folderName)
            if (scheduleText.isNotBlank()) {
                if (isNotEmpty()) append(" · ")
                append(scheduleText)
            }
            if (isEmpty()) append("Zamanı gelen hatırlatma")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(taskId.hashCode(), notification)
        } catch (_: SecurityException) {
            // Bildirim izni kullanıcı tarafından reddedildiyse sessizce geç
        }
    }
}

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getString(KEY_TASK_ID) ?: return Result.success()
        val title = inputData.getString(KEY_TITLE) ?: return Result.success()
        val folderName = inputData.getString(KEY_FOLDER_NAME).orEmpty()
        val scheduleText = inputData.getString(KEY_SCHEDULE_TEXT).orEmpty()

        val quietHours = LocalStorageRepository(applicationContext).getQuietHoursOnce()
        val now = System.currentTimeMillis()
        if (DateTimeUtils.isWithinQuietHours(now, quietHours)) {
            val nextAllowed = DateTimeUtils.nextAllowedTimeAfterQuietHours(now, quietHours)
            val delayMillis = (nextAllowed - now).coerceAtLeast(60_000L)
            ReminderScheduler(applicationContext).enqueueReminderWork(
                taskId = taskId,
                title = title,
                folderName = folderName,
                scheduleText = scheduleText,
                delayMillis = delayMillis
            )
            return Result.success()
        }

        ReminderNotificationHelper.showReminderNotification(
            context = applicationContext,
            taskId = taskId,
            title = title,
            folderName = folderName,
            scheduleText = scheduleText
        )
        return Result.success()
    }

    companion object {
        const val KEY_TASK_ID = "task_id"
        const val KEY_TITLE = "task_title"
        const val KEY_FOLDER_NAME = "folder_name"
        const val KEY_SCHEDULE_TEXT = "schedule_text"
    }
}

class ReminderScheduler(context: Context) {

    private val appContext = context.applicationContext
    private val workManager = WorkManager.getInstance(appContext)

    init {
        ReminderNotificationHelper.ensureChannel(appContext)
    }

    fun syncAllReminders(tasks: List<TaskItem>, folders: List<FolderItem>) {
        val folderMap = folders.associateBy { it.id }
        val now = System.currentTimeMillis()
        tasks.forEach { task ->
            val triggerAt = task.triggerAtMillis
            if (task.isPlanned && !task.isCompleted && triggerAt != null && triggerAt > now) {
                scheduleTaskReminder(task, folderMap[task.folderId]?.name.orEmpty(), triggerAt - now)
            } else {
                cancelTaskReminder(task.id)
            }
        }
    }

    fun scheduleTaskReminder(task: TaskItem, folderName: String) {
        val triggerAt = task.triggerAtMillis
        val now = System.currentTimeMillis()
        if (!task.isPlanned || task.isCompleted || triggerAt == null || triggerAt <= now) {
            cancelTaskReminder(task.id)
            return
        }
        scheduleTaskReminder(task, folderName, triggerAt - now)
    }

    private fun scheduleTaskReminder(task: TaskItem, folderName: String, delayMillis: Long) {
        val scheduleText = DateTimeUtils.formatScheduledLabel(
            dateMillis = task.scheduledDateMillis,
            hour = task.scheduledHour,
            minute = task.scheduledMinute,
            timeWindow = task.flexibleTimeWindow
        )
        enqueueReminderWork(
            taskId = task.id,
            title = task.title,
            folderName = folderName,
            scheduleText = scheduleText,
            delayMillis = delayMillis
        )
    }

    fun enqueueReminderWork(
        taskId: String,
        title: String,
        folderName: String,
        scheduleText: String,
        delayMillis: Long
    ) {
        val input = Data.Builder()
            .putString(ReminderWorker.KEY_TASK_ID, taskId)
            .putString(ReminderWorker.KEY_TITLE, title)
            .putString(ReminderWorker.KEY_FOLDER_NAME, folderName)
            .putString(ReminderWorker.KEY_SCHEDULE_TEXT, scheduleText)
            .build()

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMillis.coerceAtLeast(1_000L), TimeUnit.MILLISECONDS)
            .setInputData(input)
            .addTag(WORK_TAG)
            .build()

        workManager.enqueueUniqueWork(
            uniqueWorkName(taskId),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelTaskReminder(taskId: String) {
        workManager.cancelUniqueWork(uniqueWorkName(taskId))
        runCatching {
            NotificationManagerCompat.from(appContext).cancel(taskId.hashCode())
        }
    }

    private fun uniqueWorkName(taskId: String): String = "reminder_$taskId"

    companion object {
        private const val WORK_TAG = "kisisel_sekreter_reminder"
    }
}
