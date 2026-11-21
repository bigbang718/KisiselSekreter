package com.example.kisiselsekreter.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.kisiselsekreter.data.FolderItem
import com.example.kisiselsekreter.data.SubFolder
import com.example.kisiselsekreter.ui.theme.FolderColorTone
import com.example.kisiselsekreter.ui.theme.FolderIconKey
import com.example.kisiselsekreter.ui.theme.LocalIsDarkTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FolderEditorDialog(
    existingFolder: FolderItem?,
    onDismiss: () -> Unit,
    onSave: (name: String, colorTone: FolderColorTone, iconKey: FolderIconKey) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val isDark = LocalIsDarkTheme.current
    val colors = MaterialTheme.colorScheme

    var name by remember(existingFolder) { mutableStateOf(existingFolder?.name ?: "") }
    var selectedTone by remember(existingFolder) {
        mutableStateOf(existingFolder?.colorTone ?: FolderColorTone.LINEN_SAND)
    }
    var selectedIcon by remember(existingFolder) {
        mutableStateOf(existingFolder?.iconKey ?: FolderIconKey.FOLDER)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = if (existingFolder == null) "Yeni Klasör" else "Klasörü Düzenle",
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Klasör adı") },
                    placeholder = { Text("Örn. Projelerim") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Renk tonu · ${selectedTone.displayName}",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FolderColorTone.entries.forEach { tone ->
                            val isSelected = tone == selectedTone
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(tone.containerColor(isDark))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) colors.onSurface else tone.borderColor(isDark),
                                        shape = CircleShape
                                    )
                                    .clickable { selectedTone = tone },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = tone.displayName,
                                        tint = colors.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "İkon",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FolderIconKey.entries.forEach { iconKey ->
                            val isSelected = iconKey == selectedIcon
                            Surface(
                                onClick = { selectedIcon = iconKey },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) selectedTone.containerColor(isDark) else colors.surfaceContainerLow,
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) selectedTone.accentColor(isDark) else colors.outlineVariant
                                )
                            ) {
                                Icon(
                                    imageVector = iconKey.icon,
                                    contentDescription = iconKey.displayName,
                                    tint = if (isSelected) colors.onSurface else colors.onSurfaceVariant,
                                    modifier = Modifier
                                        .padding(9.dp)
                                        .size(20.dp)
                                )
                            }
                        }
                    }
                }

                if (existingFolder != null && onDelete != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = onDelete,
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Text(
                            text = "Klasörü sil",
                            color = colors.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name.trim(), selectedTone, selectedIcon) },
                enabled = name.trim().isNotEmpty(),
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

@Composable
fun SubFolderEditorDialog(
    parentFolderName: String,
    existingSubFolder: SubFolder?,
    onDismiss: () -> Unit,
    onSave: (name: String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    var name by remember(existingSubFolder) { mutableStateOf(existingSubFolder?.name ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Text(
                    text = if (existingSubFolder == null) "Yeni Alt Klasör" else "Alt Klasörü Düzenle",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onSurface
                )
                Text(
                    text = parentFolderName,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Alt klasör adı") },
                    placeholder = { Text("Örn. Notlarım") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (existingSubFolder != null && onDelete != null) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = onDelete) {
                            Text(
                                text = "Alt klasörü sil",
                                color = colors.error
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name.trim()) },
                enabled = name.trim().isNotEmpty(),
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
