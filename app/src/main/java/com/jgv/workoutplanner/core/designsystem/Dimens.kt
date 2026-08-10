package com.jgv.workoutplanner.core.designsystem

import androidx.compose.ui.unit.dp

/**
 * Shared spacing and sizing tokens (README §19 core/designsystem).
 *
 * Screens should reach for these instead of ad-hoc `dp` values so padding stays
 * consistent as the feature screens land in later phases.
 */
object Dimens {
    /** 4.dp — tight, e.g. gap between a label and its supporting text. */
    val SpacingExtraSmall = 4.dp

    /** 8.dp — inside a component, e.g. chip padding. */
    val SpacingSmall = 8.dp

    /** 16.dp — the default screen gutter and gap between list items. */
    val SpacingMedium = 16.dp

    /** 24.dp — separates sections within a screen. */
    val SpacingLarge = 24.dp

    /** 32.dp — separates major blocks, e.g. content from a primary action. */
    val SpacingExtraLarge = 32.dp

    /** Default horizontal gutter for screen content. */
    val ScreenPadding = 16.dp

    /** Minimum touch-target size (Material accessibility guidance). */
    val MinTouchTarget = 48.dp

    /** Corner radius for cards and other surface containers. */
    val CardCornerRadius = 12.dp

    /** Size of the illustrative icon in empty/loading states. */
    val PlaceholderIconSize = 48.dp

    /**
     * Tonal elevation for a bottom bar holding a primary action, so it reads as a layer
     * above the content it scrolls over rather than as the end of the page.
     */
    val BottomBarElevation = 3.dp
}
