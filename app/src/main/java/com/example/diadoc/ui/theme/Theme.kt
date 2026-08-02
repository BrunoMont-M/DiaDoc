package com.example.diadoc.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
)

class DiaDocCustomColors(
    val moduleNutrition: Color,
    val moduleExercise: Color,
    val moduleHydration: Color,
    val alertDanger: Color,
    val alertWarning: Color,
    val alertGood: Color
)

val LocalDiaDocColors = staticCompositionLocalOf {
    DiaDocCustomColors(Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified)
}

object DiaDocTheme {
    val colors: DiaDocCustomColors
        @Composable
        get() = LocalDiaDocColors.current
}

@Composable
fun DiaDocTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    paleta: Int = 0, // 0: Púrpura (Default), 1: Azul, 2: Verde
    dynamicColor: Boolean = false, // Lo forzamos a false para que aplique siempre nuestra paleta
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> {
            when (paleta) {
                1 -> DarkColorScheme.copy(primary = PrimaryBlueDark, primaryContainer = PrimaryContainerBlueDark, onPrimaryContainer = OnPrimaryContainerBlueDark)
                2 -> DarkColorScheme.copy(primary = PrimaryGreenDark, primaryContainer = PrimaryContainerGreenDark, onPrimaryContainer = OnPrimaryContainerGreenDark)
                else -> DarkColorScheme
            }
        }
        else -> {
            when (paleta) {
                1 -> LightColorScheme.copy(primary = PrimaryBlueLight, primaryContainer = PrimaryContainerBlueLight, onPrimaryContainer = OnPrimaryContainerBlueLight)
                2 -> LightColorScheme.copy(primary = PrimaryGreenLight, primaryContainer = PrimaryContainerGreenLight, onPrimaryContainer = OnPrimaryContainerGreenLight)
                else -> LightColorScheme
            }
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    val customColors = DiaDocCustomColors(
        moduleNutrition = if (darkTheme) NutritionGreenDark else NutritionGreenLight,
        moduleExercise = if (darkTheme) ExerciseOrangeDark else ExerciseOrangeLight,
        moduleHydration = if (darkTheme) HydrationBlueDark else HydrationBlueLight,
        alertDanger = if (darkTheme) AlertDangerDark else AlertDangerLight,
        alertWarning = if (darkTheme) AlertWarningDark else AlertWarningLight,
        alertGood = if (darkTheme) AlertGoodDark else AlertGoodLight
    )

    CompositionLocalProvider(LocalDiaDocColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}