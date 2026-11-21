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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kisiselsekreter.data.FolderItem
import com.example.kisiselsekreter.data.TaskItem
import com.example.kisiselsekreter.ui.theme.LocalIsDarkTheme

@Composable
fun FoldersScreen(
    folders: List<FolderItem>,
    tasks: List<TaskItem>,
    onFolderClick: (FolderItem) -> Unit,
    onEditFolderClick: (FolderItem) -> Unit,
    onAddFolderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val openTasksCount = tasks.count { !it.isCompleted }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Yaşam Alanlarım",
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${folders.size} klasör · $openTasksCount açık kayıt",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }

                Surface(
                    onClick = onAddFolderClick,
                    shape = RoundedCornerShape(14.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Yeni klasör ekle",
                            tint = colors.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Yeni Klasör",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurface
                        )
                    }
                }
            }
        }

        items(folders, key = { it.id }) { folder ->
            val openFolderTasks = tasks.filter { it.folderId == folder.id && !it.isCompleted }
            val plannedCount = openFolderTasks.count { it.isPlanned }
            val unplannedCount = openFolderTasks.count { !it.isPlanned }
            FolderCard(
                folder = folder,
                plannedCount = plannedCount,
                unplannedCount = unplannedCount,
                onClick = { onFolderClick(folder) },
                onEditClick = { onEditFolderClick(folder) }
            )
        }
    }
}

@Composable
private fun FolderCard(
    folder: FolderItem,
    plannedCount: Int,
    unplannedCount: Int,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val colors = MaterialTheme.colorScheme
    val containerColor = folder.colorTone.containerColor(isDark)
    val borderColor = folder.colorTone.borderColor(isDark)
    val accentColor = folder.colorTone.accentColor(isDark)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 152.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = CircleShape,
                    color = colors.surface.copy(alpha = if (isDark) 0.25f else 0.72f),
                    border = BorderStroke(1.dp, borderColor.copy(alpha = 0.7f)),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = folder.iconKey.icon,
                            contentDescription = folder.name,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "${folder.name} klasörünü düzenle",
                        tint = colors.onSurfaceVariant.copy(alpha = 0.75f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(5.dp))

                if (folder.subFolders.isNotEmpty()) {
                    Text(
                        text = folder.subFolders.joinToString(" · ") { it.name },
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                val summaryText = when {
                    plannedCount > 0 && unplannedCount > 0 -> "$plannedCount planlı · $unplannedCount not"
                    plannedCount > 0 -> "$plannedCount planlı iş"
                    unplannedCount > 0 -> "$unplannedCount planlanmamış"
                    folder.subFolders.isNotEmpty() -> "${folder.subFolders.size} alt klasör"
                    else -> "Sakin · kayıt yok"
                }

                Text(
                    text = summaryText,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
