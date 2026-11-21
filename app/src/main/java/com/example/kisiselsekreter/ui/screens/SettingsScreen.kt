package com.example.kisiselsekreter.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.kisiselsekreter.data.DateTimeUtils
import com.example.kisiselsekreter.data.DefaultData
import com.example.kisiselsekreter.data.FolderItem
import com.example.kisiselsekreter.data.QuietHoursConfig
import com.example.kisiselsekreter.data.ThemePreference
import com.example.kisiselsekreter.ui.theme.KisiselSekreterTheme

@Composable
fun SettingsScreen(
    themePreference: ThemePreference,
    quietHours: QuietHoursConfig,
    folders: List<FolderItem>,
    taskCount: Int,
    hasNotificationPermission: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onSelectTheme: (ThemePreference) -> Unit,
    onUpdateQuietHours: (QuietHoursConfig) -> Unit,
    onTriggerTestReminder: () -> Unit,
    onExportBackupJson: () -> Unit,
    onImportBackupJson: () -> Unit,
    onAddFolderClick: () -> Unit,
    onRestoreDefaultFolders: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var pickingQuietStart by remember { mutableStateOf(false) }
    var pickingQuietEnd by remember { mutableStateOf(false) }

    val totalSubFolders = folders.sumOf { it.subFolders.size }
    val isCurrentlyInQuietHours = remember(quietHours) {
        DateTimeUtils.isWithinQuietHours(System.currentTimeMillis(), quietHours)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Görünüm ayarları
        item {
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
                        text = "Görünüm",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface
                    )
                    Text(
                        text = "Gözünüzü yormayan sıcak nude aydınlık mod veya mat antrasit karanlık mod arasında geçiş yapın.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemePreference.entries.forEach { option ->
                            val isSelected = option == themePreference
                            Surface(
                                onClick = { onSelectTheme(option) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) colors.primaryContainer else colors.surfaceContainerLow,
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) colors.primary else colors.outlineVariant
                                )
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 11.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = option.label,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isSelected) colors.onPrimaryContainer else colors.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Bildirimler ve Sessiz Saatler
        item {
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
                            imageVector = if (hasNotificationPermission) {
                                Icons.Outlined.NotificationsActive
                            } else {
                                Icons.Outlined.NotificationsOff
                            },
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bildirimler & Sessiz Saatler",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onSurface
                        )
                    }

                    Text(
                        text = if (hasNotificationPermission) {
                            "Bildirim izni açık. Planlı, rutin ve esnek görevlerinizin zamanı geldiğinde sistem bildirimi gönderilir."
                        } else {
                            "Planlı görevlerinizin zamanı geldiğinde sistem bildirimi alabilmek için bildirim izni verin."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )

                    if (!hasNotificationPermission) {
                        Button(
                            onClick = onRequestNotificationPermission,
                            shape = RoundedCornerShape(14.dp),
                            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Bildirim İzni Ver")
                        }
                    }

                    HorizontalDivider(thickness = 1.dp, color = colors.outlineVariant)

                    // Sessiz Saatler anahtarı
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Bedtime,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Sessiz Saatler",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.onSurface
                                )
                                Text(
                                    text = if (quietHours.enabled) {
                                        "${quietHours.formattedRange} arası bildirimler susturulur"
                                    } else {
                                        "Belirlediğiniz saat aralığında bildirimleri susturun"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = quietHours.enabled,
                            onCheckedChange = { enabled ->
                                onUpdateQuietHours(quietHours.copy(enabled = enabled))
                            }
                        )
                    }

                    if (quietHours.enabled) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            QuietTimeChip(
                                label = "Başlangıç",
                                timeText = DateTimeUtils.formatTime(quietHours.startHour, quietHours.startMinute),
                                onClick = { pickingQuietStart = true },
                                modifier = Modifier.weight(1f)
                            )
                            QuietTimeChip(
                                label = "Bitiş",
                                timeText = DateTimeUtils.formatTime(quietHours.endHour, quietHours.endMinute),
                                onClick = { pickingQuietEnd = true },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (isCurrentlyInQuietHours) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = colors.primaryContainer,
                                border = BorderStroke(1.dp, colors.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Şu an Sessiz Saatler aralığındasınız. Bildirimler ${DateTimeUtils.formatTime(quietHours.endHour, quietHours.endMinute)} sonrasına sakince ertelenir.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = onTriggerTestReminder,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, colors.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsActive,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test Hatırlatması Tetikle")
                    }
                }
            }
        }

        // 3. Veri Yedekleme ve Geri Yükleme (JSON)
        item {
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
                            imageVector = Icons.Outlined.Backup,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Veri Yedekleme (JSON)",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onSurface
                        )
                    }
                    Text(
                        text = "Tüm klasörlerinizi (${folders.size}), görevlerinizi ($taskCount) ve ayarlarınızı cihaz hafızasına JSON dosyası olarak kaydedebilir veya önceki yedeğinizden geri yükleyebilirsiniz.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )

                    Button(
                        onClick = onExportBackupJson,
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verileri Yedekle (JSON)")
                    }

                    OutlinedButton(
                        onClick = onImportBackupJson,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, colors.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.UploadFile,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Yedekten Geri Yükle")
                    }
                }
            }
        }

        // 4. Klasör yönetimi
        item {
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
                        text = "Klasör Yönetimi",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface
                    )
                    Text(
                        text = "Şu anda ${folders.size} ana klasör ve $totalSubFolders alt klasör bulunuyor.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onAddFolderClick,
                            shape = RoundedCornerShape(14.dp),
                            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Yeni Klasör")
                        }
                        OutlinedButton(
                            onClick = { showRestoreConfirm = true },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, colors.outline),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Varsayılanları Ekle")
                        }
                    }
                }
            }
        }

        // 5. Uygulama hakkında
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = colors.surfaceContainerLow,
                border = BorderStroke(1.dp, colors.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Kişisel Sekreter",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface
                    )
                    Text(
                        text = "Zihni yormayan, sakin renkler ve düzenli klasör yapısıyla günlük odağınızı korumanız için tasarlandı.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        thickness = 1.dp,
                        color = colors.outlineVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Sürüm 1.0 · Verileriniz DataStore ile yalnızca cihazınızda çevrimdışı saklanır.",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (pickingQuietStart) {
        QuietHoursTimePickerDialog(
            title = "Sessiz Saat Başlangıcı",
            initialHour = quietHours.startHour,
            initialMinute = quietHours.startMinute,
            onDismiss = { pickingQuietStart = false },
            onConfirm = { hour, minute ->
                onUpdateQuietHours(quietHours.copy(startHour = hour, startMinute = minute))
                pickingQuietStart = false
            }
        )
    }

    if (pickingQuietEnd) {
        QuietHoursTimePickerDialog(
            title = "Sessiz Saat Bitişi",
            initialHour = quietHours.endHour,
            initialMinute = quietHours.endMinute,
            onDismiss = { pickingQuietEnd = false },
            onConfirm = { hour, minute ->
                onUpdateQuietHours(quietHours.copy(endHour = hour, endMinute = minute))
                pickingQuietEnd = false
            }
        )
    }

    if (showRestoreConfirm) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = false },
            containerColor = colors.surface,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Varsayılan klasörler eklensin mi?",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onSurface
                )
            },
            text = {
                Text(
                    text = "Silinmiş olan varsayılan 9 klasörden eksikler geri eklenir. Kendi oluşturduğunuz klasörler ve kayıtlar korunur.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRestoreDefaultFolders()
                        showRestoreConfirm = false
                    },
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
                ) {
                    Text("Geri Yükle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirm = false }) {
                    Text("Vazgeç")
                }
            }
        )
    }
}

@Composable
private fun QuietTimeChip(
    label: String,
    timeText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = timeText,
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuietHoursTimePickerDialog(
    title: String,
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurface
            )
        },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(state = state)
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(state.hour, state.minute) },
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç")
            }
        }
    )
}

@Preview(showBackground = true, name = "Ayarlar Ekranı")
@Composable
private fun SettingsScreenPreview() {
    KisiselSekreterTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SettingsScreen(
                themePreference = ThemePreference.LIGHT,
                quietHours = QuietHoursConfig(enabled = true),
                folders = DefaultData.defaultFolders(),
                taskCount = DefaultData.defaultSampleTasks().size,
                hasNotificationPermission = true,
                onRequestNotificationPermission = {},
                onSelectTheme = {},
                onUpdateQuietHours = {},
                onTriggerTestReminder = {},
                onExportBackupJson = {},
                onImportBackupJson = {},
                onAddFolderClick = {},
                onRestoreDefaultFolders = {}
            )
        }
    }
}
