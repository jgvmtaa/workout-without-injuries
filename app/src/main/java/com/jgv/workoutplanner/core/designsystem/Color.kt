package com.jgv.workoutplanner.core.designsystem

import androidx.compose.ui.graphics.Color

// App palette (spec §19 core/designsystem). Light is the MVP baseline; the dark
// values exist so the app degrades gracefully when the system is in dark mode.
//
// Deliberately app-owned rather than Material You dynamic color: exercise
// availability copy leans on colour (available / excluded / unavailable) later
// on, so the palette needs to be predictable across devices.

// Light
val PrimaryLight = Color(0xFF2F6B4F)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFB6F1CE)
val OnPrimaryContainerLight = Color(0xFF002114)

val SecondaryLight = Color(0xFF4E6355)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFD0E8D6)
val OnSecondaryContainerLight = Color(0xFF0C1F15)

val TertiaryLight = Color(0xFF3B6470)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFBFE9F7)
val OnTertiaryContainerLight = Color(0xFF001F27)

val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

val BackgroundLight = Color(0xFFFBFDF8)
val OnBackgroundLight = Color(0xFF191C1A)
val SurfaceLight = Color(0xFFFBFDF8)
val OnSurfaceLight = Color(0xFF191C1A)
val SurfaceVariantLight = Color(0xFFDCE5DC)
val OnSurfaceVariantLight = Color(0xFF404942)
val OutlineLight = Color(0xFF707972)

// Dark
val PrimaryDark = Color(0xFF9AD5B3)
val OnPrimaryDark = Color(0xFF003825)
val PrimaryContainerDark = Color(0xFF145138)
val OnPrimaryContainerDark = Color(0xFFB6F1CE)

val SecondaryDark = Color(0xFFB4CCBA)
val OnSecondaryDark = Color(0xFF213529)
val SecondaryContainerDark = Color(0xFF374B3F)
val OnSecondaryContainerDark = Color(0xFFD0E8D6)

val TertiaryDark = Color(0xFFA3CDDB)
val OnTertiaryDark = Color(0xFF033541)
val TertiaryContainerDark = Color(0xFF224C58)
val OnTertiaryContainerDark = Color(0xFFBFE9F7)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

val BackgroundDark = Color(0xFF191C1A)
val OnBackgroundDark = Color(0xFFE1E3DF)
val SurfaceDark = Color(0xFF191C1A)
val OnSurfaceDark = Color(0xFFE1E3DF)
val SurfaceVariantDark = Color(0xFF404942)
val OnSurfaceVariantDark = Color(0xFFC0C9C0)
val OutlineDark = Color(0xFF8A938B)
