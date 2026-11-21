package com.example.kisiselsekreter.data

import kotlin.math.roundToInt

data class FolderDistributionItem(
    val folder: FolderItem,
    val completedCount: Int,
    val totalCount: Int,
    val sharePercent: Int,
    val barFraction: Float
)

data class ConsistentAreaHighlight(
    val folder: FolderItem,
    val completedCount: Int,
    val activeDaysCount: Int,
    val completionRatioPercent: Int,
    val calmNote: String
)

data class SnoozedTaskItem(
    val task: TaskItem,
    val folder: FolderItem?,
    val subFolderName: String?
)

data class MonthlyReportData(
    val year: Int,
    val monthZeroBased: Int,
    val periodLabel: String,
    val completedCount: Int,
    val totalRelevantCount: Int,
    val completionRatePercent: Int,
    val activeDaysCount: Int,
    val folderDistributions: List<FolderDistributionItem>,
    val mostConsistentArea: ConsistentAreaHighlight?,
    val mostSnoozedTasks: List<SnoozedTaskItem>,
    val calmReflectionMessage: String
)

object MonthlyStatsCalculator {

    fun calculate(
        folders: List<FolderItem>,
        tasks: List<TaskItem>,
        year: Int,
        monthZeroBased: Int
    ): MonthlyReportData {
        val (monthStart, monthEnd) = DateTimeUtils.monthRangeMillis(year, monthZeroBased)
        val periodLabel = DateTimeUtils.formatMonthYear(year, monthZeroBased)
        val folderMap = folders.associateBy { it.id }

        val completedInMonth = tasks.filter { task ->
            val doneAt = task.completedAtMillis ?: task.createdAtMillis
            task.isCompleted && doneAt in monthStart until monthEnd
        }

        // Bu ay tamamlananlar + bu ay içinde oluşturulan veya bu aya planlanan açık görevler
        val openRelevantInMonth = tasks.filter { task ->
            if (task.isCompleted) return@filter false
            val scheduled = task.scheduledDateMillis
            val inScheduledMonth = scheduled != null && scheduled in monthStart until monthEnd
            val createdInMonth = task.createdAtMillis in monthStart until monthEnd
            inScheduledMonth || createdInMonth
        }

        val completedCount = completedInMonth.size
        val totalRelevantCount = completedCount + openRelevantInMonth.size
        val completionRatePercent = if (totalRelevantCount > 0) {
            ((completedCount.toFloat() / totalRelevantCount.toFloat()) * 100f).roundToInt().coerceIn(0, 100)
        } else {
            0
        }

        val activeDaysCount = completedInMonth
            .map { DateTimeUtils.dayBucketKey(it.completedAtMillis ?: it.createdAtMillis) }
            .distinct()
            .size

        // Klasör bazlı dağılım (yatay çubuk grafikler için)
        val folderRawStats = folders.map { folder ->
            val folderCompleted = completedInMonth.filter { it.folderId == folder.id }
            val folderOpen = openRelevantInMonth.filter { it.folderId == folder.id }
            val doneCount = folderCompleted.size
            val totalCount = doneCount + folderOpen.size
            val distinctDays = folderCompleted
                .map { DateTimeUtils.dayBucketKey(it.completedAtMillis ?: it.createdAtMillis) }
                .distinct()
                .size
            QuadFolderStat(folder, doneCount, totalCount, distinctDays)
        }.filter { it.completedCount > 0 || it.totalCount > 0 }

        val maxCompleted = folderRawStats.maxOfOrNull { it.completedCount }?.coerceAtLeast(1) ?: 1

        val folderDistributions = folderRawStats
            .sortedWith(
                compareByDescending<QuadFolderStat> { it.completedCount }
                    .thenByDescending { it.totalCount }
            )
            .map { stat ->
                val sharePercent = if (completedCount > 0) {
                    ((stat.completedCount.toFloat() / completedCount.toFloat()) * 100f).roundToInt()
                } else {
                    0
                }
                val barFraction = if (completedCount > 0) {
                    (stat.completedCount.toFloat() / maxCompleted.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }
                FolderDistributionItem(
                    folder = stat.folder,
                    completedCount = stat.completedCount,
                    totalCount = stat.totalCount,
                    sharePercent = sharePercent,
                    barFraction = barFraction
                )
            }

        // En istikrarlı olunan alan (en fazla gün & tamamlanan görev dengesi)
        val bestFolderStat = folderRawStats
            .filter { it.completedCount > 0 }
            .maxWithOrNull(
                compareBy<QuadFolderStat> { it.distinctDays }
                    .thenBy { it.completedCount }
                    .thenBy { if (it.totalCount > 0) it.completedCount.toFloat() / it.totalCount.toFloat() else 0f }
            )

        val mostConsistentArea = bestFolderStat?.let { stat ->
            val ratio = if (stat.totalCount > 0) {
                ((stat.completedCount.toFloat() / stat.totalCount.toFloat()) * 100f).roundToInt()
            } else {
                100
            }
            val note = when {
                stat.distinctDays >= 3 -> "Bu alanda ${stat.distinctDays} farklı günde sakin bir devamlılık yakaladınız."
                stat.completedCount >= 2 -> "Bu ay en düzenli ilerlediğiniz alan burası oldu."
                else -> "Bu alanda ilk adımı başarıyla tamamladınız."
            }
            ConsistentAreaHighlight(
                folder = stat.folder,
                completedCount = stat.completedCount,
                activeDaysCount = stat.distinctDays,
                completionRatioPercent = ratio,
                calmNote = note
            )
        }

        // Rutinleri gözden geçirmek için en sık ertelenen görevler
        val mostSnoozedTasks = tasks
            .filter { it.snoozeCount > 0 }
            .sortedWith(
                compareByDescending<TaskItem> { it.snoozeCount }
                    .thenByDescending { it.lastSnoozedAtMillis ?: 0L }
            )
            .take(5)
            .map { task ->
                val folder = folderMap[task.folderId]
                val subName = folder?.subFolders?.firstOrNull { it.id == task.subFolderId }?.name
                SnoozedTaskItem(
                    task = task,
                    folder = folder,
                    subFolderName = subName
                )
            }

        val calmMessage = when {
            completedCount == 0 ->
                "Bu dönem için henüz tamamlanan kayıt görünmüyor. Hazır hissettiğinizde tek bir küçük adımla başlamanız yeterli."
            completionRatePercent >= 70 ->
                "Kendi ritminizi güzelce korumuşsunuz. Acele etmeden, adım adım ilerlemek en kalıcı yöntemdir."
            completionRatePercent >= 40 ->
                "Dengeli ve sürdürülebilir bir tempo yakalamışsınız. Önemli olan kusursuzluk değil, nazik bir devamlılık."
            else ->
                "Tamamladığınız her küçük adım değerlidir. Yoğun dönemlerde hedefleri biraz sadeleştirmek zihninizi rahatlatabilir."
        }

        return MonthlyReportData(
            year = year,
            monthZeroBased = monthZeroBased,
            periodLabel = periodLabel,
            completedCount = completedCount,
            totalRelevantCount = totalRelevantCount,
            completionRatePercent = completionRatePercent,
            activeDaysCount = activeDaysCount,
            folderDistributions = folderDistributions,
            mostConsistentArea = mostConsistentArea,
            mostSnoozedTasks = mostSnoozedTasks,
            calmReflectionMessage = calmMessage
        )
    }

    private data class QuadFolderStat(
        val folder: FolderItem,
        val completedCount: Int,
        val totalCount: Int,
        val distinctDays: Int
    )
}
