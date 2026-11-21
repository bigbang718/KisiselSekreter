package com.example.kisiselsekreter.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class FolderColorTone(
    val displayName: String,
    val lightContainer: Color,
    val lightBorder: Color,
    val lightAccent: Color,
    val darkContainer: Color,
    val darkBorder: Color,
    val darkAccent: Color
) {
    LINEN_SAND(
        displayName = "Sıcak Keten / Kum Beji",
        lightContainer = Color(0xFFF2E9DC),
        lightBorder = Color(0xFFE3D5C1),
        lightAccent = Color(0xFF8A7152),
        darkContainer = Color(0xFF312B23),
        darkBorder = Color(0xFF473F34),
        darkAccent = Color(0xFFD0BA9E)
    ),
    PALE_STONE(
        displayName = "Soluk Taş / Gri-Bej",
        lightContainer = Color(0xFFEAE7E1),
        lightBorder = Color(0xFFD8D3CA),
        lightAccent = Color(0xFF6E6860),
        darkContainer = Color(0xFF2D2B28),
        darkBorder = Color(0xFF423F3B),
        darkAccent = Color(0xFFC2BBB0)
    ),
    POWDER_ROSE(
        displayName = "Pudra Gül / Yumuşak Kil",
        lightContainer = Color(0xFFF3E4E0),
        lightBorder = Color(0xFFE4CDC7),
        lightAccent = Color(0xFF996F67),
        darkContainer = Color(0xFF352A28),
        darkBorder = Color(0xFF4C3D3A),
        darkAccent = Color(0xFFD9B4AD)
    ),
    EUCALYPTUS_SAGE(
        displayName = "Çok Açık Okaliptüs / Adaçayı",
        lightContainer = Color(0xFFE6EDE6),
        lightBorder = Color(0xFFD0DDD1),
        lightAccent = Color(0xFF5D7762),
        darkContainer = Color(0xFF262F28),
        darkBorder = Color(0xFF3A473D),
        darkAccent = Color(0xFFA8C2AD)
    ),
    LATTE_EARTH(
        displayName = "Latte / Açık Toprak",
        lightContainer = Color(0xFFEDE2D5),
        lightBorder = Color(0xFFDECDB9),
        lightAccent = Color(0xFF83674D),
        darkContainer = Color(0xFF332B23),
        darkBorder = Color(0xFF4A3F34),
        darkAccent = Color(0xFFCCB297)
    ),
    IVORY_CHAMPAGNE(
        displayName = "Kırık Fildişi / Şampanya",
        lightContainer = Color(0xFFF6F1E4),
        lightBorder = Color(0xFFE7DFC9),
        lightAccent = Color(0xFF8C7D56),
        darkContainer = Color(0xFF332F25),
        darkBorder = Color(0xFF494335),
        darkAccent = Color(0xFFD3C6A0)
    ),
    SOFT_OLIVE(
        displayName = "Yumuşak Zeytin-Bej",
        lightContainer = Color(0xFFECECE0),
        lightBorder = Color(0xFFDBDACA),
        lightAccent = Color(0xFF6F7252),
        darkContainer = Color(0xFF2E2F25),
        darkBorder = Color(0xFF434537),
        darkAccent = Color(0xFFBDBE9E)
    ),
    PEACH_DUST(
        displayName = "Şeftali Tozu / Sıcak Kayısı",
        lightContainer = Color(0xFFF5E6DC),
        lightBorder = Color(0xFFE7D0C1),
        lightAccent = Color(0xFFA07258),
        darkContainer = Color(0xFF362B25),
        darkBorder = Color(0xFF4D3E36),
        darkAccent = Color(0xFFDDB69F)
    ),
    WARM_STONE_GREY(
        displayName = "Nötr Sıcak Taş Grisi",
        lightContainer = Color(0xFFE9E6E2),
        lightBorder = Color(0xFFD6D1CA),
        lightAccent = Color(0xFF6E6A65),
        darkContainer = Color(0xFF2D2B29),
        darkBorder = Color(0xFF423F3C),
        darkAccent = Color(0xFFBEB9B3)
    );

    fun containerColor(isDark: Boolean): Color = if (isDark) darkContainer else lightContainer
    fun borderColor(isDark: Boolean): Color = if (isDark) darkBorder else lightBorder
    fun accentColor(isDark: Boolean): Color = if (isDark) darkAccent else lightAccent

    companion object {
        fun fromNameOrDefault(name: String?): FolderColorTone =
            entries.firstOrNull { it.name == name } ?: WARM_STONE_GREY
    }
}

enum class FolderIconKey(
    val displayName: String,
    val icon: ImageVector
) {
    HOME("Ev", Icons.Outlined.Home),
    SCHOOL("Eğitim", Icons.Outlined.School),
    SPA("Bakım", Icons.Outlined.Spa),
    HEALING("Şifa", Icons.Outlined.Eco),
    BOOK("Okuma", Icons.Outlined.MenuBook),
    SPIRITUAL("Maneviyat", Icons.Outlined.SelfImprovement),
    QURAN("Kur'an", Icons.Outlined.AutoStories),
    SOCIAL("Sosyal", Icons.Outlined.Group),
    FOLDER("Klasör", Icons.Outlined.Folder),
    NOTE("Not", Icons.Outlined.EditNote),
    WORK("Çalışma", Icons.Outlined.WorkOutline),
    HEART("Favori", Icons.Outlined.FavoriteBorder),
    BOOKMARK("Ayraç", Icons.Outlined.BookmarkBorder);

    companion object {
        fun fromNameOrDefault(name: String?): FolderIconKey =
            entries.firstOrNull { it.name == name } ?: FOLDER
    }
}
