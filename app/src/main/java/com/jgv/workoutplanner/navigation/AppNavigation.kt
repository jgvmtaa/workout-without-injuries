package com.jgv.workoutplanner.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jgv.workoutplanner.feature.exercisedetails.ExerciseDetailsRoute
import com.jgv.workoutplanner.feature.exerciselibrary.ExerciseLibraryRoute
import com.jgv.workoutplanner.feature.home.HomeRoute
import com.jgv.workoutplanner.feature.onboarding.injuries.InjuryHistoryRoute
import com.jgv.workoutplanner.feature.onboarding.limitations.MovementLimitationsRoute
import com.jgv.workoutplanner.feature.onboarding.preferences.PreferencesRoute
import com.jgv.workoutplanner.feature.onboarding.review.ProfileReviewRoute
import com.jgv.workoutplanner.feature.onboarding.safety.SafetyNoticeRoute
import com.jgv.workoutplanner.feature.onboarding.welcome.WelcomeScreen
import com.jgv.workoutplanner.feature.plan.ExerciseReplacementScreen
import com.jgv.workoutplanner.feature.plan.PlanScreen
import com.jgv.workoutplanner.feature.profile.ProfileScreen

// Workout day ids are generated with the plan, so this stays a stand-in until Phase 5.
private const val PLACEHOLDER_WORKOUT_DAY_ID = "day-1"

private val SAMPLE_EXERCISE_ID = com.jgv.workoutplanner.domain.model.ExerciseId.MACHINE_CHEST_PRESS

/**
 * Navigation shell wiring every destination in [AppRoute] (README §17).
 *
 * The `NavController` lives here and nowhere else: screens are handed plain lambdas,
 * which keeps them previewable and testable in isolation (README §20).
 *
 * @param startDestination decided by
 *   [com.jgv.workoutplanner.MainViewModel] from the stored profile — Welcome on first
 *   launch, Home for a returning user (README §3). Passed in rather than read here so
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
            PlanScreen(
                onOpenExerciseDetails = {
                    navController.navigate(AppRoute.ExerciseDetails(SAMPLE_EXERCISE_ID))
                },
                onReplaceExercise = {
                    navController.navigate(
                        AppRoute.ExerciseReplacement(
                            workoutDayId = PLACEHOLDER_WORKOUT_DAY_ID,
                            exerciseId = SAMPLE_EXERCISE_ID,
                        ),
                    )
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

        composable<AppRoute.ExerciseReplacement> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.ExerciseReplacement>()
            ExerciseReplacementScreen(
                workoutDayId = route.workoutDayId,
                exerciseId = route.exerciseId,
                onReplacementChosen = goBack,
                onBack = goBack,
            )
        }

        composable<AppRoute.Profile> {
            ProfileScreen(
                onEditPreferences = { navController.navigate(AppRoute.Preferences) },
                onEditInjuries = { navController.navigate(AppRoute.InjuryHistory) },
                onEditLimitations = { navController.navigate(AppRoute.MovementLimitations) },
                onBack = goBack,
            )
        }
    }
}
