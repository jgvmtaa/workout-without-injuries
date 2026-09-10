package com.jgv.workoutplanner.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jgv.workoutplanner.feature.exercisedetails.ExerciseDetailsRoute
import com.jgv.workoutplanner.feature.exerciselibrary.ExerciseLibraryRoute
import com.jgv.workoutplanner.feature.exercisepicker.ExercisePickerRoute
import com.jgv.workoutplanner.feature.home.HomeRoute
import com.jgv.workoutplanner.feature.onboarding.injuries.InjuryHistoryRoute
import com.jgv.workoutplanner.feature.onboarding.limitations.MovementLimitationsRoute
import com.jgv.workoutplanner.feature.onboarding.preferences.PreferencesRoute
import com.jgv.workoutplanner.feature.onboarding.review.ProfileReviewRoute
import com.jgv.workoutplanner.feature.onboarding.safety.SafetyNoticeRoute
import com.jgv.workoutplanner.feature.onboarding.welcome.WelcomeScreen
import com.jgv.workoutplanner.feature.plan.ExerciseReplacementRoute
import com.jgv.workoutplanner.feature.plan.PlanRoute
import com.jgv.workoutplanner.feature.profile.ProfileRoute
import com.jgv.workoutplanner.feature.profile.edit.ProfileEditInjuryHistoryRoute
import com.jgv.workoutplanner.feature.profile.edit.ProfileEditMovementLimitationsRoute
import com.jgv.workoutplanner.feature.profile.edit.ProfileEditPreferencesRoute
import com.jgv.workoutplanner.feature.profile.edit.ProfileEditReviewRoute
import com.jgv.workoutplanner.navigation.AppRoute.ProfileEditOrigin

/**
 * Navigation shell wiring every destination in [AppRoute] (spec §17).
 *
 * The `NavController` lives here and nowhere else: screens are handed plain lambdas,
 * which keeps them previewable and testable in isolation (spec §20).
 *
 * @param startDestination decided by
 *   [com.jgv.workoutplanner.MainViewModel] from the stored profile — Welcome on first
 *   launch, Home for a returning user (spec §3). Passed in rather than read here so
 *   this stays a pure function of its arguments, and because changing it after
 *   composition would rebuild the graph and discard the back stack.
 */
@Composable
fun AppNavigation(
    startDestination: AppRoute,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val goBack: () -> Unit = { navController.popBackStack() }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable<AppRoute.Welcome> {
            WelcomeScreen(
                onGetStarted = { navController.navigate(AppRoute.SafetyNotice) },
                onReviewSafety = { navController.navigate(AppRoute.SafetyNotice) },
            )
        }

        composable<AppRoute.SafetyNotice> {
            SafetyNoticeRoute(
                onContinue = { navController.navigate(AppRoute.Preferences) },
                onBack = goBack,
            )
        }

        composable<AppRoute.Preferences> {
            PreferencesRoute(
                onContinue = { navController.navigate(AppRoute.InjuryHistory) },
                onBack = goBack,
            )
        }

        composable<AppRoute.InjuryHistory> {
            InjuryHistoryRoute(
                onContinue = { navController.navigate(AppRoute.MovementLimitations) },
                onBack = goBack,
            )
        }

        composable<AppRoute.MovementLimitations> {
            MovementLimitationsRoute(
                onContinue = { navController.navigate(AppRoute.ProfileReview) },
                onBack = goBack,
            )
        }

        composable<AppRoute.ProfileReview> {
            ProfileReviewRoute(
                onFinish = {
                    // Onboarding is done: drop it from the back stack so the system
                    // back button from Home exits instead of re-entering the flow.
                    navController.navigate(AppRoute.Home) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onBack = goBack,
            )
        }

        composable<AppRoute.Home> {
            HomeRoute(
                onOpenPlan = { navController.navigate(AppRoute.Plan) },
                onOpenExerciseLibrary = { navController.navigate(AppRoute.ExerciseLibrary) },
                onOpenProfile = { navController.navigate(AppRoute.Profile) },
            )
        }

        composable<AppRoute.Plan> {
            PlanRoute(
                onOpenExerciseDetails = { exerciseId ->
                    navController.navigate(AppRoute.ExerciseDetails(exerciseId))
                },
                onReplaceExercise = { dayId, exerciseId ->
                    navController.navigate(
                        AppRoute.ExerciseReplacement(
                            workoutDayId = dayId,
                            exerciseId = exerciseId,
                        ),
                    )
                },
                onOpenExercisePicker = { dayId ->
                    navController.navigate(AppRoute.ExercisePicker(workoutDayId = dayId))
                },
                onBack = goBack,
            )
        }

        composable<AppRoute.ExerciseLibrary> {
            ExerciseLibraryRoute(
                onOpenExerciseDetails = { exerciseId ->
                    navController.navigate(AppRoute.ExerciseDetails(exerciseId))
                },
                onBack = goBack,
            )
        }

        composable<AppRoute.ExerciseDetails> {
            ExerciseDetailsRoute(
                onBack = goBack,
            )
        }

        composable<AppRoute.ExerciseReplacement> {
            ExerciseReplacementRoute(
                onBack = goBack,
            )
        }

        composable<AppRoute.ExercisePicker> {
            ExercisePickerRoute(
                onBack = goBack,
            )
        }

        composable<AppRoute.Profile> {
            ProfileRoute(
                onEditPreferences = { navController.navigate(AppRoute.ProfileEditPreferences) },
                onEditInjuries = { navController.navigate(AppRoute.ProfileEditInjuryHistory) },
                onEditLimitations = {
                    navController.navigate(
                        AppRoute.ProfileEditMovementLimitations(origin = ProfileEditOrigin.Profile),
                    )
                },
                onBack = goBack,
            )
        }

        // ---- Profile editing destinations (spec §13)

        composable<AppRoute.ProfileEditPreferences> {
            ProfileEditPreferencesRoute(
                onContinueToReview = { navController.navigate(AppRoute.ProfileEditReview) },
                onCancel = {
                    navController.popBackStack(AppRoute.Profile, inclusive = false)
                },
            )
        }

        composable<AppRoute.ProfileEditInjuryHistory> {
            ProfileEditInjuryHistoryRoute(
                onContinueToLimitations = {
                    navController.navigate(
                        AppRoute.ProfileEditMovementLimitations(origin = ProfileEditOrigin.InjuryHistory),
                    )
                },
                onCancel = {
                    navController.popBackStack(AppRoute.Profile, inclusive = false)
                },
            )
        }

        composable<AppRoute.ProfileEditMovementLimitations> {
            ProfileEditMovementLimitationsRoute(
                onContinueToReview = { navController.navigate(AppRoute.ProfileEditReview) },
                onCancelToProfile = {
                    navController.popBackStack(AppRoute.Profile, inclusive = false)
                },
                onBackToInjuries = goBack,
            )
        }

        composable<AppRoute.ProfileEditReview> {
            ProfileEditReviewRoute(
                onFinishedToProfile = {
                    navController.popBackStack(AppRoute.Profile, inclusive = false)
                },
                onBack = goBack,
                onCancelToProfile = {
                    navController.popBackStack(AppRoute.Profile, inclusive = false)
                },
            )
        }
    }
}
