package com.jgv.workoutplanner.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jgv.workoutplanner.feature.exercisedetails.ExerciseDetailsScreen
import com.jgv.workoutplanner.feature.exerciselibrary.ExerciseLibraryScreen
import com.jgv.workoutplanner.feature.home.HomeRoute
import com.jgv.workoutplanner.feature.onboarding.injuries.InjuryHistoryScreen
import com.jgv.workoutplanner.feature.onboarding.limitations.MovementLimitationsScreen
import com.jgv.workoutplanner.feature.onboarding.preferences.PreferencesScreen
import com.jgv.workoutplanner.feature.onboarding.review.ProfileReviewScreen
import com.jgv.workoutplanner.feature.onboarding.safety.SafetyNoticeScreen
import com.jgv.workoutplanner.feature.onboarding.welcome.WelcomeScreen
import com.jgv.workoutplanner.feature.plan.ExerciseReplacementScreen
import com.jgv.workoutplanner.feature.plan.PlanScreen
import com.jgv.workoutplanner.feature.profile.ProfileScreen

// Stand-in arguments so the argument-carrying destinations are reachable before the
// catalog (Phase 2) and the generated plan (Phase 5) can supply real ids.
private const val PLACEHOLDER_EXERCISE_ID = "MACHINE_CHEST_PRESS"
private const val PLACEHOLDER_WORKOUT_DAY_ID = "day-1"

/**
 * Navigation shell wiring every destination in [AppRoute] (README §17).
 *
 * The `NavController` lives here and nowhere else: screens are handed plain lambdas,
 * which keeps them previewable and testable in isolation (README §20).
 *
 * Onboarding is the entry flow for now. Phase 3 decides the start destination from
 * the persisted onboarding-completion flag instead.
 */
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val goBack: () -> Unit = { navController.popBackStack() }

    NavHost(
        navController = navController,
        startDestination = AppRoute.Welcome,
        modifier = modifier,
    ) {
        composable<AppRoute.Welcome> {
            WelcomeScreen(
                onGetStarted = { navController.navigate(AppRoute.SafetyNotice) },
                onReviewSafety = { navController.navigate(AppRoute.SafetyNotice) },
            )
        }

        composable<AppRoute.SafetyNotice> {
            SafetyNoticeScreen(
                onAccept = { navController.navigate(AppRoute.Preferences) },
                onBack = goBack,
            )
        }

        composable<AppRoute.Preferences> {
            PreferencesScreen(
                onContinue = { navController.navigate(AppRoute.InjuryHistory) },
                onBack = goBack,
            )
        }

        composable<AppRoute.InjuryHistory> {
            InjuryHistoryScreen(
                onContinue = { navController.navigate(AppRoute.MovementLimitations) },
                onBack = goBack,
            )
        }

        composable<AppRoute.MovementLimitations> {
            MovementLimitationsScreen(
                onContinue = { navController.navigate(AppRoute.ProfileReview) },
                onBack = goBack,
            )
        }

        composable<AppRoute.ProfileReview> {
            ProfileReviewScreen(
                onFinish = {
                    // Onboarding is done: drop it from the back stack so the system
                    // back button from Home exits instead of re-entering the flow.
                    navController.navigate(AppRoute.Home) {
                        popUpTo(AppRoute.Welcome) { inclusive = true }
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
                    navController.navigate(AppRoute.ExerciseDetails(PLACEHOLDER_EXERCISE_ID))
                },
                onReplaceExercise = {
                    navController.navigate(
                        AppRoute.ExerciseReplacement(
                            workoutDayId = PLACEHOLDER_WORKOUT_DAY_ID,
                            exerciseId = PLACEHOLDER_EXERCISE_ID,
                        ),
                    )
                },
                onBack = goBack,
            )
        }

        composable<AppRoute.ExerciseLibrary> {
            ExerciseLibraryScreen(
                onOpenExerciseDetails = {
                    navController.navigate(AppRoute.ExerciseDetails(PLACEHOLDER_EXERCISE_ID))
                },
                onBack = goBack,
            )
        }

        composable<AppRoute.ExerciseDetails> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.ExerciseDetails>()
            ExerciseDetailsScreen(
                exerciseId = route.exerciseId,
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
