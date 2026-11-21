package com.example.kisiselsekreter.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateTimeUtils {

    private val localeTr = Locale.forLanguageTag("tr-TR")

    fun startOfDayMillis(offsetDays: Int = 0): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, offsetDays)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun millisAgo(daysAgo: Int, hour: Int, minute: Int = 0): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun computeTriggerAtMillis(
        dateMillis: Long?,
        hour: Int?,
        minute: Int?,
        reminderType: ReminderType,
        timeWindow: TimeWindow
    ): Long? {
        val baseDate = dateMillis ?: return null
        val cal = Calendar.getInstance().apply {
            timeInMillis = baseDate
            val effectiveHour: Int
            val effectiveMinute: Int
            when {
                hour != null && minute != null -> {
                    effectiveHour = hour
                    effectiveMinute = minute
                }
                reminderType == ReminderType.FLEXIBLE && timeWindow != TimeWindow.NONE -> {
                    effectiveHour = timeWindow.defaultHour
                    effectiveMinute = timeWindow.defaultMinute
                }
                else -> {
                    effectiveHour = 9
                    effectiveMinute = 0
                }
            }
            set(Calendar.HOUR_OF_DAY, effectiveHour)
            set(Calendar.MINUTE, effectiveMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun nextRoutineDateMillis(
        fromDateMillis: Long,
        frequency: RoutineFrequency,
        selectedWeekDays: Set<Int>
    ): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = maxOf(fromDateMillis, startOfDayMillis(0))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        when (frequency) {
            RoutineFrequency.DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
            RoutineFrequency.WEEKLY -> cal.add(Calendar.DAY_OF_YEAR, 7)
            RoutineFrequency.MONTHLY -> cal.add(Calendar.MONTH, 1)
            RoutineFrequency.SELECTED_DAYS -> {
                if (selectedWeekDays.isEmpty()) {
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                } else {
                    repeat(7) {
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                        val isoDay = calendarDayToIso(cal.get(Calendar.DAY_OF_WEEK))
                        if (isoDay in selectedWeekDays) return cal.timeInMillis
                    }
                }
            }
        }
        return cal.timeInMillis
    }

    fun calendarDayToIso(calendarDayOfWeek: Int): Int = when (calendarDayOfWeek) {
        Calendar.MONDAY -> 1
        Calendar.TUESDAY -> 2
        Calendar.WEDNESDAY -> 3
        Calendar.THURSDAY -> 4
        Calendar.FRIDAY -> 5
        Calendar.SATURDAY -> 6
        Calendar.SUNDAY -> 7
        else -> 1
    }

    fun weekDayShortLabel(isoDay: Int): String = when (isoDay) {
        1 -> "Pzt"
        2 -> "Sal"
        3 -> "Çar"
        4 -> "Per"
        5 -> "Cum"
        6 -> "Cmt"
        7 -> "Paz"
        else -> ""
    }

    fun computeSnoozePlus2Hours(nowMillis: Long = System.currentTimeMillis()): Triple<Long, Int, Int> {
        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            add(Calendar.HOUR_OF_DAY, 2)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        return Triple(cal.timeInMillis, hour, minute)
    }

    fun computeSnoozeThisEvening(nowMillis: Long = System.currentTimeMillis()): Triple<Long, Int, Int> {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        return when {
            currentHour < 20 -> Triple(startOfDayMillis(0), 20, 0)
            currentHour < 22 -> Triple(startOfDayMillis(0), 22, 0)
            else -> Triple(startOfDayMillis(1), 20, 0)
        }
    }

    fun computeSnoozeTomorrow(task: TaskItem): Triple<Long, Int?, Int?> {
        val targetDate = startOfDayMillis(1)
        val keepWindowOnly = task.reminderType == ReminderType.FLEXIBLE &&
            task.flexibleTimeWindow != TimeWindow.NONE &&
            task.scheduledHour == null
        return if (keepWindowOnly) {
            Triple(targetDate, null, null)
        } else {
            Triple(targetDate, task.scheduledHour ?: 9, task.scheduledMinute ?: 0)
        }
    }

    fun computeSnoozeNextWeek(task: TaskItem): Triple<Long, Int?, Int?> {
        val targetDate = startOfDayMillis(7)
        val keepWindowOnly = task.reminderType == ReminderType.FLEXIBLE &&
            task.flexibleTimeWindow != TimeWindow.NONE &&
            task.scheduledHour == null
        return if (keepWindowOnly) {
            Triple(targetDate, null, null)
        } else {
            Triple(targetDate, task.scheduledHour ?: 9, task.scheduledMinute ?: 0)
        }
    }

    fun normalizeUtcDatePickerMillisToLocalDay(utcMillis: Long): Long {
        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = utcMillis
        }
        val localCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
            set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return localCal.timeInMillis
    }

    fun localDayToUtcDatePickerMillis(localMillis: Long): Long {
        val localCal = Calendar.getInstance().apply {
            timeInMillis = localMillis
        }
        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(
                localCal.get(Calendar.YEAR),
                localCal.get(Calendar.MONTH),
                localCal.get(Calendar.DAY_OF_MONTH)
            )
        }
        return utcCal.timeInMillis
    }

    fun isSameDay(firstMillis: Long, secondMillis: Long): Boolean {
        val c1 = Calendar.getInstance().apply { timeInMillis = firstMillis }
        val c2 = Calendar.getInstance().apply { timeInMillis = secondMillis }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
            c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    fun isToday(millis: Long?): Boolean {
        if (millis == null) return false
        return isSameDay(millis, System.currentTimeMillis())
    }

    fun isTomorrow(millis: Long?): Boolean {
        if (millis == null) return false
        return isSameDay(millis, startOfDayMillis(1))
    }

    fun isYesterday(millis: Long?): Boolean {
        if (millis == null) return false
        return isSameDay(millis, startOfDayMillis(-1))
    }

    fun isBeforeToday(millis: Long?): Boolean {
        if (millis == null) return false
        return millis < startOfDayMillis(0)
    }

    fun formatTime(hour: Int, minute: Int): String {
        return String.format(localeTr, "%02d:%02d", hour, minute)
    }

    fun formatTimeFromMillis(millis: Long): String {
        return SimpleDateFormat("HH:mm", localeTr).format(Date(millis))
    }

    fun formatDateShort(dateMillis: Long): String {
        return when {
            isToday(dateMillis) -> "Bugün"
            isTomorrow(dateMillis) -> "Yarın"
            isYesterday(dateMillis) -> "Dün"
            else -> SimpleDateFormat("d MMM", localeTr).format(Date(dateMillis))
        }
    }

    fun formatDateGroupHeader(millis: Long): String {
        return when {
            isToday(millis) -> "Bugün"
            isYesterday(millis) -> "Dün"
            else -> SimpleDateFormat("d MMMM yyyy, EEEE", localeTr).format(Date(millis))
        }
    }

    fun dayBucketKey(millis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun formatScheduledLabel(
        dateMillis: Long?,
        hour: Int?,
        minute: Int?,
        timeWindow: TimeWindow = TimeWindow.NONE
    ): String {
        val dateText = dateMillis?.let { formatDateShort(it) } ?: "Tarih seçilmedi"
        return when {
            hour != null && minute != null -> "$dateText · ${formatTime(hour, minute)}"
            timeWindow != TimeWindow.NONE -> "$dateText · ${timeWindow.label}"
            else -> dateText
        }
    }

    fun currentYearMonth(nowMillis: Long = System.currentTimeMillis()): Pair<Int, Int> {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        return cal.get(Calendar.YEAR) to cal.get(Calendar.MONTH)
    }

    fun shiftMonth(year: Int, monthZeroBased: Int, deltaMonths: Int): Pair<Int, Int> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthZeroBased)
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, deltaMonths)
        }
        return cal.get(Calendar.YEAR) to cal.get(Calendar.MONTH)
    }

    fun isFirstDayOfMonth(nowMillis: Long = System.currentTimeMillis()): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        return cal.get(Calendar.DAY_OF_MONTH) == 1
    }

    fun yearMonthKey(year: Int, monthZeroBased: Int): String {
        return String.format(Locale.US, "%04d-%02d", year, monthZeroBased + 1)
    }

    fun currentYearMonthKey(nowMillis: Long = System.currentTimeMillis()): String {
        val (year, month) = currentYearMonth(nowMillis)
        return yearMonthKey(year, month)
    }

    fun monthRangeMillis(year: Int, monthZeroBased: Int): Pair<Long, Long> {
        val startCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthZeroBased)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = (startCal.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
        }
        return startCal.timeInMillis to endCal.timeInMillis
    }

    fun formatMonthYear(year: Int, monthZeroBased: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthZeroBased)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val formatted = SimpleDateFormat("MMMM yyyy", localeTr).format(cal.time)
        return formatted.replaceFirstChar { ch ->
            if (ch.isLowerCase()) ch.titlecase(localeTr) else ch.toString()
        }
    }

    fun formatBackupDateTime(millis: Long): String {
        return SimpleDateFormat("d MMMM yyyy, HH:mm", localeTr).format(Date(millis))
    }

    fun formatBackupFileDate(millis: Long = System.currentTimeMillis()): String {
        return SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.US).format(Date(millis))
    }

    fun isWithinQuietHours(
        timeMillis: Long,
        quietHours: QuietHoursConfig
    ): Boolean {
        if (!quietHours.enabled) return false
        val cal = Calendar.getInstance().apply { timeInMillis = timeMillis }
        val currentMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val startMinutes = quietHours.startHour * 60 + quietHours.startMinute
        val endMinutes = quietHours.endHour * 60 + quietHours.endMinute
        if (startMinutes == endMinutes) return false
        return if (startMinutes < endMinutes) {
            currentMinutes in startMinutes until endMinutes
        } else {
            // Gece yarısını geçen aralık (ör. 22:00 - 08:00)
            currentMinutes >= startMinutes || currentMinutes < endMinutes
        }
    }

    fun nextAllowedTimeAfterQuietHours(
        timeMillis: Long,
        quietHours: QuietHoursConfig
    ): Long {
        if (!isWithinQuietHours(timeMillis, quietHours)) return timeMillis
        val cal = Calendar.getInstance().apply { timeInMillis = timeMillis }
        val currentMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val startMinutes = quietHours.startHour * 60 + quietHours.startMinute
        val endMinutes = quietHours.endHour * 60 + quietHours.endMinute

        if (startMinutes > endMinutes && currentMinutes >= startMinutes) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        cal.set(Calendar.HOUR_OF_DAY, quietHours.endHour)
        cal.set(Calendar.MINUTE, quietHours.endMinute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
