package com.example.kisiselsekreter.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderCopy
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.rounded.FolderCopy
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Today
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    FOLDERS(
        label = "Klasörler",
        selectedIcon = Icons.Rounded.FolderCopy,
        unselectedIcon = Icons.Outlined.FolderCopy
    ),
    TODAY(
        label = "Bugün",
        selectedIcon = Icons.Rounded.Today,
        unselectedIcon = Icons.Outlined.Today
    ),
    HISTORY_REPORT(
        label = "Geçmiş & Rapor",
        selectedIcon = Icons.Rounded.Insights,
        unselectedIcon = Icons.Outlined.Insights
    ),
    SETTINGS(
        label = "Ayarlar",
        selectedIcon = Icons.Rounded.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
}
