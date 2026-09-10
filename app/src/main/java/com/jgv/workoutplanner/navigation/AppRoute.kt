package com.jgv.workoutplanner.navigation

import androidx.annotation.Keep
import com.jgv.workoutplanner.domain.model.ExerciseId
import kotlinx.serialization.Serializable

/**
 * Every destination in the app (spec §17).
 *
 * Routes are `@Serializable` so Navigation Compose's type-safe API can carry the
 * arguments — no string route templates and no manual argument parsing.
 *
 * Exercise arguments are typed as [ExerciseId] rather than `String` (spec §17), so an
 * unknown exercise cannot be navigated to. `workoutDayId` stays a `String` because
 * [com.jgv.workoutplanner.domain.model.WorkoutDay] ids are generated per plan, not
 * enumerated.
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
        val exerciseId: ExerciseId,
    ) : AppRoute

    /** Alternatives for one exercise within one day of the plan. */
    @Serializable
    data class ExerciseReplacement(
        val workoutDayId: String,
        val exerciseId: ExerciseId,
    ) : AppRoute

    /** Saved profile: preferences, injuries, limitations. */
    @Serializable
    data object Profile : AppRoute

    // ----------------------------------------------------------------
    // Profile editing destinations (spec §13)

    @Serializable
    data object ProfileEditPreferences : AppRoute

    @Serializable
    data object ProfileEditInjuryHistory : AppRoute

    @Serializable
    @Keep
    enum class ProfileEditOrigin {
        Profile,
        InjuryHistory,
    }

    @Serializable
    data class ProfileEditMovementLimitations(
        val origin: ProfileEditOrigin,
    ) : AppRoute

    @Serializable
    data object ProfileEditReview : AppRoute

    // ----------------------------------------------------------------
    // Plan editing (spec §13)

    @Serializable
    data class ExercisePicker(
        val workoutDayId: String,
    ) : AppRoute
}
