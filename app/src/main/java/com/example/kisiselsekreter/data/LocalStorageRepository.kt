package com.example.kisiselsekreter.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.kisiselsekreter.ui.theme.FolderColorTone
import com.example.kisiselsekreter.ui.theme.FolderIconKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

private val Context.secretaryDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "kisisel_sekreter_datastore"
)

data class PersistedSecretaryData(
    val themePreference: ThemePreference = ThemePreference.LIGHT,
    val quietHours: QuietHoursConfig = QuietHoursConfig(),
    val lastMonthlyReportSeenKey: String = "",
    val folders: List<FolderItem> = emptyList(),
    val tasks: List<TaskItem> = emptyList(),
    val isInitialized: Boolean = false
)

class LocalStorageRepository(context: Context) {

    private val dataStore = context.applicationContext.secretaryDataStore

    val dataFlow: Flow<PersistedSecretaryData> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val initialized = prefs[KEY_INITIALIZED] ?: false
            val theme = ThemePreference.fromNameOrDefault(prefs[KEY_THEME])
            val quietHours = readQuietHours(prefs)
            val lastMonthlyReportSeen = prefs[KEY_LAST_MONTHLY_REPORT_SEEN].orEmpty()
            val folders = parseFolders(prefs[KEY_FOLDERS])
            val tasks = parseTasks(prefs[KEY_TASKS])
            PersistedSecretaryData(
                themePreference = theme,
                quietHours = quietHours,
                lastMonthlyReportSeenKey = lastMonthlyReportSeen,
                folders = folders,
                tasks = tasks,
                isInitialized = initialized
            )
        }

    suspend fun getQuietHoursOnce(): QuietHoursConfig {
        val prefs = dataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .first()
        return readQuietHours(prefs)
    }

    suspend fun getSnapshotOnce(): PersistedSecretaryData {
        ensureInitialized()
        return dataFlow.first()
    }

    /**
     * Ana ekran widget'ı üzerinden doğrudan görev tamamlama işlemi.
     * Rutin / tekrarlayan esnek görevlerde geçmiş kaydı oluşturur ve bir sonraki döngüye taşır.
     * Dönen çift: (yeniden zamanlanacak görev varsa kendisi, yoksa null; güncel klasör listesi).
     */
    suspend fun completeTaskDirectly(taskId: String): Pair<TaskItem?, List<FolderItem>> {
        ensureInitialized()
        var nextTaskToSchedule: TaskItem? = null
        var currentFolders: List<FolderItem> = emptyList()

        dataStore.edit { prefs ->
            currentFolders = parseFolders(prefs[KEY_FOLDERS])
            val currentTasks = parseTasks(prefs[KEY_TASKS])
            val target = currentTasks.firstOrNull { it.id == taskId } ?: return@edit
            if (target.isCompleted) return@edit

            val now = System.currentTimeMillis()
            val finalCounter = if (target.hasCounter) target.counterTarget else target.counterCurrent
            val shouldRepeatRoutine = target.isPlanned && target.reminderType == ReminderType.ROUTINE
            val repeatAfterDays = target.flexibleRepeatAfterDays
            val shouldRepeatFlexible = target.isPlanned &&
                target.reminderType == ReminderType.FLEXIBLE &&
                repeatAfterDays != null &&
                repeatAfterDays > 0

            val updatedTasks = if (shouldRepeatRoutine || shouldRepeatFlexible) {
                val nextDate = if (shouldRepeatRoutine) {
                    DateTimeUtils.nextRoutineDateMillis(
                        fromDateMillis = target.scheduledDateMillis ?: DateTimeUtils.startOfDayMillis(0),
                        frequency = target.routineFrequency,
                        selectedWeekDays = target.routineWeekDays
                    )
                } else {
                    DateTimeUtils.startOfDayMillis(repeatAfterDays ?: 1)
                }

                val completedRecord = target.copy(
                    id = java.util.UUID.randomUUID().toString(),
                    reminderType = ReminderType.NONE,
                    scheduledDateMillis = DateTimeUtils.startOfDayMillis(0),
                    isCompleted = true,
                    completedAtMillis = now,
                    counterCurrent = finalCounter
                )
                val nextOccurrence = target.copy(
                    scheduledDateMillis = nextDate,
                    isCompleted = false,
                    completedAtMillis = null,
                    counterCurrent = if (target.hasCounter) 0 else target.counterCurrent
                )
                nextTaskToSchedule = nextOccurrence
                currentTasks.map { if (it.id == taskId) nextOccurrence else it } + completedRecord
            } else {
                currentTasks.map { task ->
                    if (task.id == taskId) {
                        task.copy(
                            isCompleted = true,
                            completedAtMillis = now,
                            counterCurrent = finalCounter
                        )
                    } else {
                        task
                    }
                }
            }
            prefs[KEY_TASKS] = serializeTasks(updatedTasks)
        }

        return nextTaskToSchedule to currentFolders
    }

    suspend fun ensureInitialized() {
        dataStore.edit { prefs ->
            val alreadyInitialized = prefs[KEY_INITIALIZED] ?: false
            if (!alreadyInitialized) {
                val defaultFolders = DefaultData.defaultFolders()
                val sampleTasks = DefaultData.defaultSampleTasks()
                prefs[KEY_FOLDERS] = serializeFolders(defaultFolders)
                prefs[KEY_TASKS] = serializeTasks(sampleTasks)
                if (prefs[KEY_THEME] == null) {
                    prefs[KEY_THEME] = ThemePreference.LIGHT.name
                }
                prefs[KEY_INITIALIZED] = true
                prefs[KEY_MIGRATED_REPORT_SEED] = true
            } else if (prefs[KEY_MIGRATED_REPORT_SEED] != true) {
                // Mevcut kullanıcı verilerini koruyarak yalnızca dokunulmamış örnek görevlere örnek erteleme sayısı ekle
                val existingTasks = parseTasks(prefs[KEY_TASKS])
                val hasAnySnooze = existingTasks.any { it.snoozeCount > 0 }
                if (!hasAnySnooze && existingTasks.isNotEmpty()) {
                    val sampleSnoozeById = DefaultData.defaultSampleTasks()
                        .filter { it.snoozeCount > 0 }
                        .associateBy { it.id }
                    val migrated = existingTasks.map { task ->
                        val sample = sampleSnoozeById[task.id]
                        if (sample != null) {
                            task.copy(
                                snoozeCount = sample.snoozeCount,
                                lastSnoozedAtMillis = sample.lastSnoozedAtMillis
                            )
                        } else {
                            task
                        }
                    }
                    prefs[KEY_TASKS] = serializeTasks(migrated)
                }
                prefs[KEY_MIGRATED_REPORT_SEED] = true
            }
        }
    }

    suspend fun saveThemePreference(preference: ThemePreference) {
        dataStore.edit { prefs ->
            prefs[KEY_THEME] = preference.name
        }
    }

    suspend fun saveQuietHours(config: QuietHoursConfig) {
        dataStore.edit { prefs ->
            prefs[KEY_QUIET_ENABLED] = config.enabled
            prefs[KEY_QUIET_START_HOUR] = config.startHour.coerceIn(0, 23)
            prefs[KEY_QUIET_START_MINUTE] = config.startMinute.coerceIn(0, 59)
            prefs[KEY_QUIET_END_HOUR] = config.endHour.coerceIn(0, 23)
            prefs[KEY_QUIET_END_MINUTE] = config.endMinute.coerceIn(0, 59)
        }
    }

    suspend fun saveLastMonthlyReportSeenKey(yearMonthKey: String) {
        dataStore.edit { prefs ->
            prefs[KEY_LAST_MONTHLY_REPORT_SEEN] = yearMonthKey
        }
    }

    suspend fun saveFolders(folders: List<FolderItem>) {
        dataStore.edit { prefs ->
            prefs[KEY_FOLDERS] = serializeFolders(folders)
        }
    }

    suspend fun saveTasks(tasks: List<TaskItem>) {
        dataStore.edit { prefs ->
            prefs[KEY_TASKS] = serializeTasks(tasks)
        }
    }

    suspend fun saveFoldersAndTasks(folders: List<FolderItem>, tasks: List<TaskItem>) {
        dataStore.edit { prefs ->
            prefs[KEY_FOLDERS] = serializeFolders(folders)
            prefs[KEY_TASKS] = serializeTasks(tasks)
        }
    }

    suspend fun restoreFromBackup(payload: BackupPayload) {
        dataStore.edit { prefs ->
            prefs[KEY_THEME] = payload.themePreference.name
            prefs[KEY_QUIET_ENABLED] = payload.quietHours.enabled
            prefs[KEY_QUIET_START_HOUR] = payload.quietHours.startHour.coerceIn(0, 23)
            prefs[KEY_QUIET_START_MINUTE] = payload.quietHours.startMinute.coerceIn(0, 59)
            prefs[KEY_QUIET_END_HOUR] = payload.quietHours.endHour.coerceIn(0, 23)
            prefs[KEY_QUIET_END_MINUTE] = payload.quietHours.endMinute.coerceIn(0, 59)
            prefs[KEY_FOLDERS] = serializeFolders(payload.folders)
            prefs[KEY_TASKS] = serializeTasks(payload.tasks)
            prefs[KEY_INITIALIZED] = true
            prefs[KEY_MIGRATED_REPORT_SEED] = true
        }
    }

    fun buildBackupJson(
        themePreference: ThemePreference,
        quietHours: QuietHoursConfig,
        folders: List<FolderItem>,
        tasks: List<TaskItem>
    ): String {
        val root = JSONObject().apply {
            put("app", BACKUP_APP_ID)
            put("schemaVersion", BACKUP_SCHEMA_VERSION)
            put("exportedAtMillis", System.currentTimeMillis())
            put("themePreference", themePreference.name)
            put(
                "quietHours",
                JSONObject().apply {
                    put("enabled", quietHours.enabled)
                    put("startHour", quietHours.startHour)
                    put("startMinute", quietHours.startMinute)
                    put("endHour", quietHours.endHour)
                    put("endMinute", quietHours.endMinute)
                }
            )
            put("folders", JSONArray(serializeFolders(folders)))
            put("tasks", JSONArray(serializeTasks(tasks)))
        }
        return root.toString(2)
    }

    fun parseBackupJson(rawJson: String): Result<BackupPayload> {
        return runCatching {
            val root = JSONObject(rawJson)
            val appId = root.optString("app", "")
            require(appId == BACKUP_APP_ID || (root.has("folders") && root.has("tasks"))) {
                "Dosya Kişisel Sekreter yedek formatında değil."
            }
            val foldersArray = root.optJSONArray("folders")
                ?: throw IllegalArgumentException("Yedek dosyasında klasör verisi bulunamadı.")
            val tasksArray = root.optJSONArray("tasks")
                ?: throw IllegalArgumentException("Yedek dosyasında görev verisi bulunamadı.")

            val folders = parseFoldersArray(foldersArray)
            require(folders.isNotEmpty()) {
                "Yedek dosyasında en az bir klasör bulunmalıdır."
            }
            val tasks = parseTasksArray(tasksArray)
            val theme = ThemePreference.fromNameOrDefault(root.optString("themePreference"))
            val quietObj = root.optJSONObject("quietHours")
            val quietHours = if (quietObj != null) {
                QuietHoursConfig(
                    enabled = quietObj.optBoolean("enabled", false),
                    startHour = quietObj.optInt("startHour", 22).coerceIn(0, 23),
                    startMinute = quietObj.optInt("startMinute", 0).coerceIn(0, 59),
                    endHour = quietObj.optInt("endHour", 8).coerceIn(0, 23),
                    endMinute = quietObj.optInt("endMinute", 0).coerceIn(0, 59)
                )
            } else {
                QuietHoursConfig()
            }

            BackupPayload(
                schemaVersion = root.optInt("schemaVersion", 1),
                exportedAtMillis = root.optLong("exportedAtMillis", System.currentTimeMillis()),
                themePreference = theme,
                quietHours = quietHours,
                folders = folders,
                tasks = tasks
            )
        }
    }

    private fun readQuietHours(prefs: Preferences): QuietHoursConfig {
        return QuietHoursConfig(
            enabled = prefs[KEY_QUIET_ENABLED] ?: false,
            startHour = (prefs[KEY_QUIET_START_HOUR] ?: 22).coerceIn(0, 23),
            startMinute = (prefs[KEY_QUIET_START_MINUTE] ?: 0).coerceIn(0, 59),
            endHour = (prefs[KEY_QUIET_END_HOUR] ?: 8).coerceIn(0, 23),
            endMinute = (prefs[KEY_QUIET_END_MINUTE] ?: 0).coerceIn(0, 59)
        )
    }

    private fun parseFolders(raw: String?): List<FolderItem> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            parseFoldersArray(JSONArray(raw))
        }.getOrElse { DefaultData.defaultFolders() }
    }

    private fun parseFoldersArray(array: JSONArray): List<FolderItem> = buildList {
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val subsArray = obj.optJSONArray("subFolders") ?: JSONArray()
            val subFolders = buildList {
                for (j in 0 until subsArray.length()) {
                    val subObj = subsArray.getJSONObject(j)
                    add(
                        SubFolder(
                            id = subObj.getString("id"),
                            name = subObj.getString("name")
                        )
                    )
                }
            }
            add(
                FolderItem(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    colorTone = FolderColorTone.fromNameOrDefault(obj.optString("colorTone")),
                    iconKey = FolderIconKey.fromNameOrDefault(obj.optString("iconKey")),
                    subFolders = subFolders,
                    isDefault = obj.optBoolean("isDefault", false)
                )
            )
        }
    }

    private fun serializeFolders(folders: List<FolderItem>): String {
        val array = JSONArray()
        folders.forEach { folder ->
            val subsArray = JSONArray()
            folder.subFolders.forEach { sub ->
                subsArray.put(
                    JSONObject().apply {
                        put("id", sub.id)
                        put("name", sub.name)
                    }
                )
            }
            array.put(
                JSONObject().apply {
                    put("id", folder.id)
                    put("name", folder.name)
                    put("colorTone", folder.colorTone.name)
                    put("iconKey", folder.iconKey.name)
                    put("subFolders", subsArray)
                    put("isDefault", folder.isDefault)
                }
            )
        }
        return array.toString()
    }

    private fun parseTasks(raw: String?): List<TaskItem> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            parseTasksArray(JSONArray(raw))
        }.getOrElse { emptyList() }
    }

    private fun parseTasksArray(array: JSONArray): List<TaskItem> = buildList {
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val subFolderId = obj.optNullableString("subFolderId")
            val scheduledDateMillis = obj.optNullableLong("scheduledDateMillis")
            val scheduledHour = obj.optNullableInt("scheduledHour")
            val scheduledMinute = obj.optNullableInt("scheduledMinute")
            val completedAtMillis = obj.optNullableLong("completedAtMillis")
            val flexibleRepeatAfterDays = obj.optNullableInt("flexibleRepeatAfterDays")
            val lastSnoozedAtMillis = obj.optNullableLong("lastSnoozedAtMillis")
            val weekDaysArray = obj.optJSONArray("routineWeekDays")
            val routineWeekDays = buildSet {
                if (weekDaysArray != null) {
                    for (j in 0 until weekDaysArray.length()) {
                        add(weekDaysArray.getInt(j))
                    }
                }
            }
            add(
                TaskItem(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    note = obj.optString("note", ""),
                    folderId = obj.getString("folderId"),
                    subFolderId = subFolderId,
                    isPlanned = obj.optBoolean("isPlanned", scheduledDateMillis != null),
                    scheduledDateMillis = scheduledDateMillis,
                    scheduledHour = scheduledHour,
                    scheduledMinute = scheduledMinute,
                    reminderType = ReminderType.fromNameOrDefault(obj.optString("reminderType")),
                    routineFrequency = RoutineFrequency.fromNameOrDefault(obj.optString("routineFrequency")),
                    routineWeekDays = routineWeekDays,
                    flexibleRepeatAfterDays = flexibleRepeatAfterDays,
                    flexibleTimeWindow = TimeWindow.fromNameOrDefault(obj.optString("flexibleTimeWindow")),
                    hasCounter = obj.optBoolean("hasCounter", false),
                    counterCurrent = obj.optInt("counterCurrent", 0),
                    counterTarget = obj.optInt("counterTarget", 0),
                    counterUnit = obj.optString("counterUnit", ""),
                    isCompleted = obj.optBoolean("isCompleted", false),
                    snoozeCount = obj.optInt("snoozeCount", 0).coerceAtLeast(0),
                    lastSnoozedAtMillis = lastSnoozedAtMillis,
                    createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis()),
                    completedAtMillis = completedAtMillis
                )
            )
        }
    }

    private fun serializeTasks(tasks: List<TaskItem>): String {
        val array = JSONArray()
        tasks.forEach { task ->
            val weekDaysArray = JSONArray()
            task.routineWeekDays.sorted().forEach { weekDaysArray.put(it) }
            array.put(
                JSONObject().apply {
                    put("id", task.id)
                    put("title", task.title)
                    put("note", task.note)
                    put("folderId", task.folderId)
                    put("subFolderId", task.subFolderId ?: JSONObject.NULL)
                    put("isPlanned", task.isPlanned)
                    put("scheduledDateMillis", task.scheduledDateMillis ?: JSONObject.NULL)
                    put("scheduledHour", task.scheduledHour ?: JSONObject.NULL)
                    put("scheduledMinute", task.scheduledMinute ?: JSONObject.NULL)
                    put("reminderType", task.reminderType.name)
                    put("routineFrequency", task.routineFrequency.name)
                    put("routineWeekDays", weekDaysArray)
                    put("flexibleRepeatAfterDays", task.flexibleRepeatAfterDays ?: JSONObject.NULL)
                    put("flexibleTimeWindow", task.flexibleTimeWindow.name)
                    put("hasCounter", task.hasCounter)
                    put("counterCurrent", task.counterCurrent)
                    put("counterTarget", task.counterTarget)
                    put("counterUnit", task.counterUnit)
                    put("isCompleted", task.isCompleted)
                    put("snoozeCount", task.snoozeCount)
                    put("lastSnoozedAtMillis", task.lastSnoozedAtMillis ?: JSONObject.NULL)
                    put("createdAtMillis", task.createdAtMillis)
                    put("completedAtMillis", task.completedAtMillis ?: JSONObject.NULL)
                }
            )
        }
        return array.toString()
    }

    private fun JSONObject.optNullableString(key: String): String? =
        if (has(key) && !isNull(key)) getString(key).takeIf { it.isNotBlank() } else null

    private fun JSONObject.optNullableLong(key: String): Long? =
        if (has(key) && !isNull(key)) getLong(key) else null

    private fun JSONObject.optNullableInt(key: String): Int? =
        if (has(key) && !isNull(key)) getInt(key) else null

    companion object {
        private const val BACKUP_APP_ID = "KisiselSekreter"
        private const val BACKUP_SCHEMA_VERSION = 1

        private val KEY_INITIALIZED = booleanPreferencesKey("seeded_v3")
        private val KEY_MIGRATED_REPORT_SEED = booleanPreferencesKey("migrated_report_seed_v1")
        private val KEY_THEME = stringPreferencesKey("theme_preference")
        private val KEY_FOLDERS = stringPreferencesKey("folders_json")
        private val KEY_TASKS = stringPreferencesKey("tasks_json")
        private val KEY_QUIET_ENABLED = booleanPreferencesKey("quiet_hours_enabled")
        private val KEY_QUIET_START_HOUR = intPreferencesKey("quiet_hours_start_hour")
        private val KEY_QUIET_START_MINUTE = intPreferencesKey("quiet_hours_start_minute")
        private val KEY_QUIET_END_HOUR = intPreferencesKey("quiet_hours_end_hour")
        private val KEY_QUIET_END_MINUTE = intPreferencesKey("quiet_hours_end_minute")
        private val KEY_LAST_MONTHLY_REPORT_SEEN = stringPreferencesKey("last_monthly_report_seen_key")
    }
}
