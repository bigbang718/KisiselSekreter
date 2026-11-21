package com.example.kisiselsekreter.widget

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.glance.color.ColorProvider
import androidx.glance.color.ColorProviders
import androidx.glance.material3.ColorProviders as Material3ColorProviders
import androidx.glance.unit.ColorProvider as GlanceColorProvider
import com.example.kisiselsekreter.data.ThemePreference
import com.example.kisiselsekreter.ui.theme.DarkBackground
import com.example.kisiselsekreter.ui.theme.DarkError
import com.example.kisiselsekreter.ui.theme.DarkOnError
import com.example.kisiselsekreter.ui.theme.DarkOnPrimary
import com.example.kisiselsekreter.ui.theme.DarkOnPrimaryContainer
import com.example.kisiselsekreter.ui.theme.DarkOnSecondary
import com.example.kisiselsekreter.ui.theme.DarkOnSecondaryContainer
import com.example.kisiselsekreter.ui.theme.DarkOutline
import com.example.kisiselsekreter.ui.theme.DarkOutlineVariant
import com.example.kisiselsekreter.ui.theme.DarkPrimary
import com.example.kisiselsekreter.ui.theme.DarkPrimaryContainer
import com.example.kisiselsekreter.ui.theme.DarkSecondary
import com.example.kisiselsekreter.ui.theme.DarkSecondaryContainer
import com.example.kisiselsekreter.ui.theme.DarkSurface
import com.example.kisiselsekreter.ui.theme.DarkSurfaceContainer
import com.example.kisiselsekreter.ui.theme.DarkSurfaceVariant
import com.example.kisiselsekreter.ui.theme.DarkTextPrimary
import com.example.kisiselsekreter.ui.theme.DarkTextSecondary
import com.example.kisiselsekreter.ui.theme.FolderColorTone
import com.example.kisiselsekreter.ui.theme.LightBackground
import com.example.kisiselsekreter.ui.theme.LightError
import com.example.kisiselsekreter.ui.theme.LightOnError
import com.example.kisiselsekreter.ui.theme.LightOnPrimary
import com.example.kisiselsekreter.ui.theme.LightOnPrimaryContainer
import com.example.kisiselsekreter.ui.theme.LightOnSecondary
import com.example.kisiselsekreter.ui.theme.LightOnSecondaryContainer
import com.example.kisiselsekreter.ui.theme.LightOutline
import com.example.kisiselsekreter.ui.theme.LightOutlineVariant
import com.example.kisiselsekreter.ui.theme.LightPrimary
import com.example.kisiselsekreter.ui.theme.LightPrimaryContainer
import com.example.kisiselsekreter.ui.theme.LightSecondary
import com.example.kisiselsekreter.ui.theme.LightSecondaryContainer
import com.example.kisiselsekreter.ui.theme.LightSurface
import com.example.kisiselsekreter.ui.theme.LightSurfaceContainer
import com.example.kisiselsekreter.ui.theme.LightSurfaceVariant
import com.example.kisiselsekreter.ui.theme.LightTextPrimary
import com.example.kisiselsekreter.ui.theme.LightTextSecondary

object SecretaryWidgetColors {

    private val LightScheme = lightColorScheme(
        primary = LightPrimary,
        onPrimary = LightOnPrimary,
        primaryContainer = LightPrimaryContainer,
        onPrimaryContainer = LightOnPrimaryContainer,
        secondary = LightSecondary,
        onSecondary = LightOnSecondary,
        secondaryContainer = LightSecondaryContainer,
        onSecondaryContainer = LightOnSecondaryContainer,
        background = LightBackground,
        onBackground = LightTextPrimary,
        surface = LightSurface,
        onSurface = LightTextPrimary,
        surfaceVariant = LightSurfaceVariant,
        onSurfaceVariant = LightTextSecondary,
        outline = LightOutline,
        outlineVariant = LightOutlineVariant,
        error = LightError,
        onError = LightOnError
    )

    private val DarkScheme = darkColorScheme(
        primary = DarkPrimary,
        onPrimary = DarkOnPrimary,
        primaryContainer = DarkPrimaryContainer,
        onPrimaryContainer = DarkOnPrimaryContainer,
        secondary = DarkSecondary,
        onSecondary = DarkOnSecondary,
        secondaryContainer = DarkSecondaryContainer,
        onSecondaryContainer = DarkOnSecondaryContainer,
        background = DarkBackground,
        onBackground = DarkTextPrimary,
        surface = DarkSurface,
        onSurface = DarkTextPrimary,
        surfaceVariant = DarkSurfaceVariant,
        onSurfaceVariant = DarkTextSecondary,
        outline = DarkOutline,
        outlineVariant = DarkOutlineVariant,
        error = DarkError,
        onError = DarkOnError
    )

    private val AdaptiveProviders: ColorProviders = Material3ColorProviders(
        light = LightScheme,
        dark = DarkScheme
    )

    private val ForcedLightProviders: ColorProviders = Material3ColorProviders(
        light = LightScheme,
        dark = LightScheme
    )

    private val ForcedDarkProviders: ColorProviders = Material3ColorProviders(
        light = DarkScheme,
        dark = DarkScheme
    )

    fun colorProvidersFor(preference: ThemePreference): ColorProviders = when (preference) {
        ThemePreference.SYSTEM -> AdaptiveProviders
        ThemePreference.LIGHT -> ForcedLightProviders
        ThemePreference.DARK -> ForcedDarkProviders
    }

    fun widgetBackground(preference: ThemePreference): GlanceColorProvider = when (preference) {
        ThemePreference.SYSTEM -> ColorProvider(day = LightBackground, night = DarkBackground)
        ThemePreference.LIGHT -> ColorProvider(day = LightBackground, night = LightBackground)
        ThemePreference.DARK -> ColorProvider(day = DarkBackground, night = DarkBackground)
    }

    fun taskCardSurface(preference: ThemePreference): GlanceColorProvider = when (preference) {
        ThemePreference.SYSTEM -> ColorProvider(day = LightSurface, night = DarkSurface)
        ThemePreference.LIGHT -> ColorProvider(day = LightSurface, night = LightSurface)
        ThemePreference.DARK -> ColorProvider(day = DarkSurface, night = DarkSurface)
    }

    fun softPillSurface(preference: ThemePreference): GlanceColorProvider = when (preference) {
        ThemePreference.SYSTEM -> ColorProvider(day = LightSurfaceContainer, night = DarkSurfaceContainer)
        ThemePreference.LIGHT -> ColorProvider(day = LightSurfaceContainer, night = LightSurfaceContainer)
        ThemePreference.DARK -> ColorProvider(day = DarkSurfaceContainer, night = DarkSurfaceContainer)
    }

    fun folderAccent(tone: FolderColorTone, preference: ThemePreference): GlanceColorProvider = when (preference) {
        ThemePreference.SYSTEM -> ColorProvider(day = tone.lightAccent, night = tone.darkAccent)
        ThemePreference.LIGHT -> ColorProvider(day = tone.lightAccent, night = tone.lightAccent)
        ThemePreference.DARK -> ColorProvider(day = tone.darkAccent, night = tone.darkAccent)
    }

    fun folderContainer(tone: FolderColorTone, preference: ThemePreference): GlanceColorProvider = when (preference) {
        ThemePreference.SYSTEM -> ColorProvider(day = tone.lightContainer, night = tone.darkContainer)
        ThemePreference.LIGHT -> ColorProvider(day = tone.lightContainer, night = tone.lightContainer)
        ThemePreference.DARK -> ColorProvider(day = tone.darkContainer, night = tone.darkContainer)
    }
}
