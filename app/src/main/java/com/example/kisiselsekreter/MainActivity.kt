package com.example.kisiselsekreter

import android.Manifest
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kisiselsekreter.data.BackupPayload
import com.example.kisiselsekreter.data.DateTimeUtils
import com.example.kisiselsekreter.data.ThemePreference
import com.example.kisiselsekreter.notifications.ReminderNotificationHelper
import com.example.kisiselsekreter.notifications.TestReminderResult
import com.example.kisiselsekreter.ui.KisiselSekreterApp
import com.example.kisiselsekreter.ui.theme.KisiselSekreterTheme
import com.example.kisiselsekreter.ui.viewmodel.SecretaryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val viewModel: SecretaryViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            val systemInDark = isSystemInDarkTheme()
            val isDarkTheme = when (uiState.themePreference) {
                ThemePreference.SYSTEM -> systemInDark
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
            }

            val snackbarHostState = remember { SnackbarHostState() }
            val coroutineScope = rememberCoroutineScope()
            var pendingRestorePayload by remember { mutableStateOf<BackupPayload?>(null) }

            var hasNotificationPermission by remember {
                mutableStateOf(ReminderNotificationHelper.hasNotificationPermission(context))
            }
            var requestedOnce by rememberSaveable { mutableStateOf(false) }

            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { granted ->
                hasNotificationPermission = granted || ReminderNotificationHelper.hasNotificationPermission(context)
            }

            val requestNotificationPermission = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            // JSON Yedekleme (SAF CreateDocument)
            val exportBackupLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.CreateDocument("application/json")
            ) { uri ->
                if (uri == null) return@rememberLauncherForActivityResult
                coroutineScope.launch {
                    val jsonContent = viewModel.buildBackupJson()
                    val result = withContext(Dispatchers.IO) {
                        runCatching {
                            context.contentResolver.openOutputStream(uri)?.use { stream ->
                                stream.write(jsonContent.toByteArray(Charsets.UTF_8))
                                stream.flush()
                            } ?: error("Dosya yazılamadı.")
                        }
                    }
                    snackbarHostState.showSnackbar(
                        if (result.isSuccess) {
                            "Verileriniz JSON dosyası olarak yedeklendi."
                        } else {
                            "Yedekleme başarısız oldu: ${result.exceptionOrNull()?.localizedMessage ?: "Bilinmeyen hata"}"
                        }
                    )
                }
            }

            // JSON Yedekten Geri Yükleme (SAF OpenDocument)
            val importBackupLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { uri ->
                if (uri == null) return@rememberLauncherForActivityResult
                coroutineScope.launch {
                    val readResult = withContext(Dispatchers.IO) {
                        runCatching {
                            context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use {
                                it.readText()
                            } ?: error("Dosya okunamadı.")
                        }
                    }
                    if (readResult.isFailure) {
                        snackbarHostState.showSnackbar("Seçilen dosya okunamadı.")
                        return@launch
                    }
                    val parsed = viewModel.parseBackupJson(readResult.getOrThrow())
                    parsed.onSuccess { payload ->
                        pendingRestorePayload = payload
                    }.onFailure { error ->
                        snackbarHostState.showSnackbar(
                            "Geçersiz yedek dosyası: ${error.message ?: "JSON biçimi okunamadı."}"
                        )
                    }
                }
            }

            // Android 13+ cihazlarda ilk açılışta bir kez sakin şekilde bildirim izni iste
            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission && !requestedOnce) {
                    requestedOnce = true
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            DisposableEffect(isDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDarkTheme) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (isDarkTheme) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    }
                )
                onDispose {}
            }

            KisiselSekreterTheme(darkTheme = isDarkTheme) {
                KisiselSekreterApp(
                    uiState = uiState,
                    hasNotificationPermission = hasNotificationPermission,
                    snackbarHostState = snackbarHostState,
                    onRequestNotificationPermission = requestNotificationPermission,
                    onToggleTheme = { viewModel.toggleLightDark(isDarkTheme) },
                    onSelectThemePreference = viewModel::setThemePreference,
                    onUpdateQuietHours = viewModel::updateQuietHours,
                    onTriggerTestReminder = {
                        when (viewModel.triggerTestReminder()) {
                            TestReminderResult.SENT -> {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Test hatırlatması gönderildi.")
                                }
                            }
                            TestReminderResult.SILENCED_BY_QUIET_HOURS -> {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Sessiz Saatler (${uiState.quietHours.formattedRange}) aktif olduğu için test bildirimi susturuldu."
                                    )
                                }
                            }
                            TestReminderResult.MISSING_PERMISSION -> {
                                requestNotificationPermission()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Test hatırlatması için bildirim izni vermeniz gerekiyor.")
                                }
                            }
                        }
                    },
                    onExportBackupJson = {
                        val fileName = "kisisel_sekreter_yedek_${DateTimeUtils.formatBackupFileDate()}.json"
                        exportBackupLauncher.launch(fileName)
                    },
                    onImportBackupJson = {
                        importBackupLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                    },
                    onMarkMonthlyReportSeen = viewModel::markMonthlyReportSeen,
                    onOpenFolder = viewModel::openFolder,
                    onCloseFolder = viewModel::closeFolder,
                    onSelectSubFolder = viewModel::selectSubFolder,
                    onAddFolder = viewModel::addFolder,
                    onUpdateFolder = viewModel::updateFolder,
                    onDeleteFolder = viewModel::deleteFolder,
                    onAddSubFolder = viewModel::addSubFolder,
                    onUpdateSubFolder = viewModel::updateSubFolder,
                    onDeleteSubFolder = viewModel::deleteSubFolder,
                    onRestoreDefaultFolders = viewModel::restoreMissingDefaultFolders,
                    onAddTask = viewModel::addTask,
                    onAddQuickUnplannedTask = viewModel::addQuickUnplannedTask,
                    onUpdateTask = viewModel::updateTask,
                    onScheduleTask = viewModel::scheduleTask,
                    onSnoozeTask = viewModel::snoozeTask,
                    onToggleTaskComplete = viewModel::toggleTaskCompletion,
                    onIncrementTaskCounter = viewModel::incrementTaskCounter,
                    onDecrementTaskCounter = viewModel::decrementTaskCounter,
                    onDeleteTask = viewModel::deleteTask
                )

                pendingRestorePayload?.let { payload ->
                    val colors = MaterialTheme.colorScheme
                    AlertDialog(
                        onDismissRequest = { pendingRestorePayload = null },
                        containerColor = colors.surface,
                        shape = RoundedCornerShape(24.dp),
                        title = {
                            Text(
                                text = "Yedekten geri yüklensin mi?",
                                style = MaterialTheme.typography.titleLarge,
                                color = colors.onSurface
                            )
                        },
                        text = {
                            Text(
                                text = "${DateTimeUtils.formatBackupDateTime(payload.exportedAtMillis)} tarihli yedekte " +
                                    "${payload.folders.size} klasör ve ${payload.tasks.size} görev bulundu. " +
                                    "Mevcut veriler bu yedekle değiştirilecektir.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceVariant
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.restoreFromBackup(payload)
                                    pendingRestorePayload = null
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Yedek başarıyla geri yüklendi.")
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
                            ) {
                                Text("Geri Yükle")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { pendingRestorePayload = null }) {
                                Text("Vazgeç")
                            }
                        }
                    )
                }
            }
        }
    }
}
