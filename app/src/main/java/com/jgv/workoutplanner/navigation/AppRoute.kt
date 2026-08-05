package com.jgv.workoutplanner.navigation

import kotlinx.serialization.Serializable

/**
 * Every destination in the app (README §17).
 *
 * Routes are `@Serializable` so Navigation Compose's type-safe API can carry the
 * arguments — no string route templates and no manual argument parsing.
 *
 * Argument types stay primitive for now. README §17 types the exercise arguments as
 * `ExerciseId`; that enum is Phase 2 work, so these hold the raw id and get swapped
 * over when the domain model lands (tracked in docs/follow-ups.md).
 */
@Serializable
sealed interface AppRoute {

    /** Onboarding entry point. */
    @Serializable
    data object Welcome : AppRoute

    /** Safety acknowledgement the user must accept before onboarding continues. */
    @Serializable
    data object SafetyNotice : AppRoute

    /** Goal, experience, schedule, duration, equipment. */
    @Serializable
    data object Preferences : AppRoute

    /** Injury history, grouped by body region. */
    @Serializable
    data object InjuryHistory : AppRoute

    /** Suggested and manual movement limitations, confirmed by the user. */
    @Serializable
    data object MovementLimitations : AppRoute

    /** Final onboarding step: review the profile before generating a plan. */
    @Serializable
    data object ProfileReview : AppRoute

    /** Post-onboarding start screen. */
    @Serializable
    data object Home : AppRoute

    /** The current workout plan. */
    @Serializable
    data object Plan : AppRoute

    /** Browsable exercise catalog. */
    @Serializable
    data object ExerciseLibrary : AppRoute

    /** Details for a single exercise. */
    @Serializable
    data class ExerciseDetails(
        val exerciseId: String,
    ) : AppRoute

    /** Alternatives for one exercise within one day of the plan. */
    @Serializable
    data class ExerciseReplacement(
        val workoutDayId: String,
        val exerciseId: String,
    ) : AppRoute

    /** Saved profile: preferences, injuries, limitations. */
    @Serializable
    data object Profile : AppRoute
}
