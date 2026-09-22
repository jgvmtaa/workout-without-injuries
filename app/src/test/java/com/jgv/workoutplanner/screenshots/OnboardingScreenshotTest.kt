package com.jgv.workoutplanner.screenshots

import androidx.compose.ui.test.junit4.createComposeRule
import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.core.ui.ONBOARDING_CONTENT_TAG
import com.jgv.workoutplanner.core.ui.labelRes
import com.jgv.workoutplanner.data.catalog.InjuryCatalog
import com.jgv.workoutplanner.data.catalog.MovementLimitationCatalog
import com.jgv.workoutplanner.domain.model.ExperienceLevel
import com.jgv.workoutplanner.domain.model.InjuryId
import com.jgv.workoutplanner.domain.model.InjuryStatus
import com.jgv.workoutplanner.domain.model.MovementLimitation
import com.jgv.workoutplanner.domain.model.TrainingGoal
import com.jgv.workoutplanner.domain.model.TrainingOptions
import com.jgv.workoutplanner.domain.model.WorkoutSplit
import com.jgv.workoutplanner.feature.onboarding.injuries.InjuryGroupUiModel
import com.jgv.workoutplanner.feature.onboarding.injuries.InjuryHistoryScreen
import com.jgv.workoutplanner.feature.onboarding.injuries.InjuryHistoryUiState
import com.jgv.workoutplanner.feature.onboarding.injuries.InjuryUiModel
import com.jgv.workoutplanner.feature.onboarding.limitations.LimitationGroupUiModel
import com.jgv.workoutplanner.feature.onboarding.limitations.LimitationUiModel
import com.jgv.workoutplanner.feature.onboarding.limitations.MovementLimitationsScreen
import com.jgv.workoutplanner.feature.onboarding.limitations.MovementLimitationsUiState
import com.jgv.workoutplanner.feature.onboarding.review.ProfileReviewScreen
import com.jgv.workoutplanner.feature.onboarding.review.ProfileReviewUiState
import com.jgv.workoutplanner.feature.onboarding.review.ReviewInjuryUiModel
import com.jgv.workoutplanner.feature.onboarding.preferences.PreferencesScreen
import com.jgv.workoutplanner.feature.onboarding.preferences.PreferencesUiState
import com.jgv.workoutplanner.feature.onboarding.safety.SafetyNoticeScreen
import com.jgv.workoutplanner.feature.onboarding.safety.SafetyNoticeUiState
import com.jgv.workoutplanner.feature.onboarding.welcome.WelcomeScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Onboarding screenshot cases: welcome and safety notice. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(
    sdk = [35],
    qualifiers = LIGHT_QUALIFIERS,
)
class OnboardingScreenshotTest {

    @get:Rule
    val roborazziRule = screenshotRule()

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `should show welcome steps and actions in light theme`() {
        captureScreenshot("welcome-light-1_0") {
            WelcomeScreen(onGetStarted = {}, onReviewSafety = {})
        }
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should show welcome steps and actions in dark theme`() {
        captureScreenshot("welcome-dark-1_0") {
            WelcomeScreen(onGetStarted = {}, onReviewSafety = {})
        }
    }

    @Test
    fun `should disable accept action when safety is unacknowledged in light theme`() {
        captureScreenshot("safety-unacknowledged-light-1_0") {
            SafetyNoticeScreen(
                state = SafetyNoticeUiState(isLoading = false, isAcknowledged = false),
                onEvent = {},
            )
        }
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should disable accept action when safety is unacknowledged in dark theme`() {
        captureScreenshot("safety-unacknowledged-dark-1_0") {
            SafetyNoticeScreen(
                state = SafetyNoticeUiState(isLoading = false, isAcknowledged = false),
                onEvent = {},
            )
        }
    }

    @Test
    fun `should disable accept action when safety is unacknowledged at large text`() {
        captureScreenshot(
            fileName = "safety-unacknowledged-light-1_3",
            fontScale = 1.3f,
        ) {
            SafetyNoticeScreen(
                state = SafetyNoticeUiState(isLoading = false, isAcknowledged = false),
                onEvent = {},
            )
        }
    }

    @Test
    fun `should enable accept action when safety is acknowledged in light theme`() {
        captureScreenshot("safety-acknowledged-light-1_0") {
            SafetyNoticeScreen(
                state = SafetyNoticeUiState(isLoading = false, isAcknowledged = true),
                onEvent = {},
            )
        }
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should enable accept action when safety is acknowledged in dark theme`() {
        captureScreenshot("safety-acknowledged-dark-1_0") {
            SafetyNoticeScreen(
                state = SafetyNoticeUiState(isLoading = false, isAcknowledged = true),
                onEvent = {},
            )
        }
    }

    @Test
    fun `should keep continue disabled when preferences are incomplete in light theme`() {
        composeRule.captureScrollable(
            baseName = "preferences-incomplete",
            variant = "light-1_0",
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            PreferencesScreen(
                state = PreferencesUiState(isLoading = false),
                onEvent = {},
            )
        }
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should keep continue disabled when preferences are incomplete in dark theme`() {
        composeRule.captureScrollable(
            baseName = "preferences-incomplete",
            variant = "dark-1_0",
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            PreferencesScreen(
                state = PreferencesUiState(isLoading = false),
                onEvent = {},
            )
        }
    }

    @Test
    fun `should keep continue disabled when preferences are incomplete at large text`() {
        composeRule.captureScrollable(
            baseName = "preferences-incomplete",
            variant = "light-1_3",
            fontScale = 1.3f,
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            PreferencesScreen(
                state = PreferencesUiState(isLoading = false),
                onEvent = {},
            )
        }
    }

    @Test
    fun `should keep continue disabled when preferences are incomplete at largest text`() {
        composeRule.captureScrollable(
            baseName = "preferences-incomplete",
            variant = "light-2_0",
            fontScale = 2f,
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            PreferencesScreen(
                state = PreferencesUiState(isLoading = false),
                onEvent = {},
            )
        }
    }

    @Test
    fun `should enable continue with derived split when preferences are complete in light theme`() {
        composeRule.captureScrollable(
            baseName = "preferences-complete",
            variant = "light-1_0",
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            PreferencesScreen(
                state = completePreferences,
                onEvent = {},
            )
        }
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should enable continue with derived split when preferences are complete in dark theme`() {
        composeRule.captureScrollable(
            baseName = "preferences-complete",
            variant = "dark-1_0",
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            PreferencesScreen(
                state = completePreferences,
                onEvent = {},
            )
        }
    }

    @Test
    fun `should enable continue with derived split when preferences are complete at large text`() {
        composeRule.captureScrollable(
            baseName = "preferences-complete",
            variant = "light-1_3",
            fontScale = 1.3f,
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            PreferencesScreen(
                state = completePreferences,
                onEvent = {},
            )
        }
    }

    @Test
    fun `should enable continue with derived split when preferences are complete at largest text`() {
        composeRule.captureScrollable(
            baseName = "preferences-complete",
            variant = "light-2_0",
            fontScale = 2f,
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            PreferencesScreen(
                state = completePreferences,
                onEvent = {},
            )
        }
    }

    @Test
    @Config(qualifiers = RTL_QUALIFIERS)
    fun `should enable continue with derived split when preferences are complete in mirrored layout`() {
        composeRule.captureScrollable(
            baseName = "preferences-complete",
            variant = "light-1_0-rtl",
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            PreferencesScreen(
                state = completePreferences,
                onEvent = {},
            )
        }
    }

    @Test
    fun `should list grouped injuries with no selection in light theme`() {
        composeRule.captureScrollable(
            baseName = "injuries-none-selected",
            variant = "light-1_0",
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            InjuryHistoryScreen(
                state = InjuryHistoryUiState(
                    isLoading = false,
                    injuryGroups = fullInjuryGroups,
                ),
                onEvent = {},
            )
        }
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should list grouped injuries with no selection in dark theme`() {
        composeRule.captureScrollable(
            baseName = "injuries-none-selected",
            variant = "dark-1_0",
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            InjuryHistoryScreen(
                state = InjuryHistoryUiState(
                    isLoading = false,
                    injuryGroups = fullInjuryGroups,
                ),
                onEvent = {},
            )
        }
    }

    @Test
    fun `should show status control for the selected injury in light theme`() {
        composeRule.captureScrollable(
            baseName = "injuries-selected",
            variant = "light-1_0",
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            InjuryHistoryScreen(
                state = InjuryHistoryUiState(
                    isLoading = false,
                    injuryGroups = fullInjuryGroups,
                    selectedInjuries = mapOf(InjuryId.KNEE_ACL to InjuryStatus.RECOVERING),
                ),
                onEvent = {},
            )
        }
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should show status control for the selected injury in dark theme`() {
        composeRule.captureScrollable(
            baseName = "injuries-selected",
            variant = "dark-1_0",
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            InjuryHistoryScreen(
                state = InjuryHistoryUiState(
                    isLoading = false,
                    injuryGroups = fullInjuryGroups,
                    selectedInjuries = mapOf(InjuryId.KNEE_ACL to InjuryStatus.RECOVERING),
                ),
                onEvent = {},
            )
        }
    }

    @Test
    fun `should show status control for the selected injury at large text`() {
        composeRule.captureScrollable(
            baseName = "injuries-selected",
            variant = "light-1_3",
            fontScale = 1.3f,
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            InjuryHistoryScreen(
                state = InjuryHistoryUiState(
                    isLoading = false,
                    injuryGroups = fullInjuryGroups,
                    selectedInjuries = mapOf(InjuryId.KNEE_ACL to InjuryStatus.RECOVERING),
                ),
                onEvent = {},
            )
        }
    }

    @Test
    fun `should show no-suggestions card with browsable limitations in light theme`() {
        composeRule.captureScrollable(
            baseName = "limitations-no-suggestions",
            variant = "light-1_0",
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            MovementLimitationsScreen(
                state = MovementLimitationsUiState(
                    isLoading = false,
                    otherGroups = browsableGroups(excluding = emptySet()),
                ),
                onEvent = {},
            )
        }
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should show no-suggestions card with browsable limitations in dark theme`() {
        composeRule.captureScrollable(
            baseName = "limitations-no-suggestions",
            variant = "dark-1_0",
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            MovementLimitationsScreen(
                state = MovementLimitationsUiState(
                    isLoading = false,
                    otherGroups = browsableGroups(excluding = emptySet()),
                ),
                onEvent = {},
            )
        }
    }

    @Test
    fun `should show unchecked suggestions separately in light theme`() {
        composeRule.captureScrollable(
            baseName = "limitations-unconfirmed",
            variant = "light-1_0",
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            MovementLimitationsScreen(
                state = MovementLimitationsUiState(
                    isLoading = false,
                    suggested = suggestedLimitations,
                    otherGroups = browsableGroups(excluding = kneeAclSuggestions),
                ),
                onEvent = {},
            )
        }
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should show unchecked suggestions separately in dark theme`() {
        composeRule.captureScrollable(
            baseName = "limitations-unconfirmed",
            variant = "dark-1_0",
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            MovementLimitationsScreen(
                state = MovementLimitationsUiState(
                    isLoading = false,
                    suggested = suggestedLimitations,
                    otherGroups = browsableGroups(excluding = kneeAclSuggestions),
                ),
                onEvent = {},
            )
        }
    }

    @Test
    fun `should show unchecked suggestions separately at large text`() {
        composeRule.captureScrollable(
            baseName = "limitations-unconfirmed",
            variant = "light-1_3",
            fontScale = 1.3f,
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            MovementLimitationsScreen(
                state = MovementLimitationsUiState(
                    isLoading = false,
                    suggested = suggestedLimitations,
                    otherGroups = browsableGroups(excluding = kneeAclSuggestions),
                ),
                onEvent = {},
            )
        }
    }

    @Test
    fun `should show confirmed suggested and browsed limitations in light theme`() {
        composeRule.captureScrollable(
            baseName = "limitations-confirmed",
            variant = "light-1_0",
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            MovementLimitationsScreen(
                state = MovementLimitationsUiState(
                    isLoading = false,
                    suggested = suggestedLimitations,
                    otherGroups = browsableGroups(excluding = kneeAclSuggestions),
                    confirmed = setOf(
                        MovementLimitation.AVOID_JUMPING,
                        MovementLimitation.AVOID_OVERHEAD_PRESSING,
                    ),
                ),
                onEvent = {},
            )
        }
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should show confirmed suggested and browsed limitations in dark theme`() {
        composeRule.captureScrollable(
            baseName = "limitations-confirmed",
            variant = "dark-1_0",
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            MovementLimitationsScreen(
                state = MovementLimitationsUiState(
                    isLoading = false,
                    suggested = suggestedLimitations,
                    otherGroups = browsableGroups(excluding = kneeAclSuggestions),
                    confirmed = setOf(
                        MovementLimitation.AVOID_JUMPING,
                        MovementLimitation.AVOID_OVERHEAD_PRESSING,
                    ),
                ),
                onEvent = {},
            )
        }
    }

    @Test
    fun `should show complete populated profile in light theme`() {
        composeRule.captureScrollable(
            baseName = "profile-review",
            variant = "light-1_0",
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            ProfileReviewScreen(
                state = populatedReview,
                onEvent = {},
            )
        }
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should show complete populated profile in dark theme`() {
        composeRule.captureScrollable(
            baseName = "profile-review",
            variant = "dark-1_0",
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            ProfileReviewScreen(
                state = populatedReview,
                onEvent = {},
            )
        }
    }

    @Test
    fun `should show complete populated profile at large text`() {
        composeRule.captureScrollable(
            baseName = "profile-review",
            variant = "light-1_3",
            fontScale = 1.3f,
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            ProfileReviewScreen(
                state = populatedReview,
                onEvent = {},
            )
        }
    }

    @Test
    @Config(qualifiers = RTL_QUALIFIERS)
    fun `should show complete populated profile in mirrored layout`() {
        composeRule.captureScrollable(
            baseName = "profile-review",
            variant = "light-1_0-rtl",
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            ProfileReviewScreen(
                state = populatedReview,
                onEvent = {},
            )
        }
    }

    @Test
    fun `should show bodyweight-only profile with empty sections in light theme`() {
        composeRule.captureScrollable(
            baseName = "profile-review-minimal",
            variant = "light-1_0",
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            ProfileReviewScreen(
                state = ProfileReviewUiState(
                    isLoading = false,
                    isComplete = true,
                    goalRes = R.string.goal_general_fitness,
                    experienceRes = R.string.experience_beginner,
                    daysPerWeek = 2,
                    sessionDurationMinutes = 30,
                    splitRes = R.string.split_full_body,
                    equipment = listOf(R.string.equipment_bodyweight),
                ),
                onEvent = {},
            )
        }
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should show bodyweight-only profile with empty sections in dark theme`() {
        composeRule.captureScrollable(
            baseName = "profile-review-minimal",
            variant = "dark-1_0",
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            ProfileReviewScreen(
                state = ProfileReviewUiState(
                    isLoading = false,
                    isComplete = true,
                    goalRes = R.string.goal_general_fitness,
                    experienceRes = R.string.experience_beginner,
                    daysPerWeek = 2,
                    sessionDurationMinutes = 30,
                    splitRes = R.string.split_full_body,
                    equipment = listOf(R.string.equipment_bodyweight),
                ),
                onEvent = {},
            )
        }
    }

    @Test
    fun `should show missing values and error when profile is incomplete in light theme`() {
        composeRule.captureScrollable(
            baseName = "profile-review-incomplete",
            variant = "light-1_0",
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            ProfileReviewScreen(
                state = ProfileReviewUiState(isLoading = false, isComplete = false),
                onEvent = {},
            )
        }
    }

    @Test
    @Config(qualifiers = DARK_QUALIFIERS)
    fun `should show missing values and error when profile is incomplete in dark theme`() {
        composeRule.captureScrollable(
            baseName = "profile-review-incomplete",
            variant = "dark-1_0",
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            ProfileReviewScreen(
                state = ProfileReviewUiState(isLoading = false, isComplete = false),
                onEvent = {},
            )
        }
    }

    @Test
    fun `should show missing values and error when profile is incomplete at large text`() {
        composeRule.captureScrollable(
            baseName = "profile-review-incomplete",
            variant = "light-1_3",
            fontScale = 1.3f,
            scrollTag = ONBOARDING_CONTENT_TAG,
        ) {
            ProfileReviewScreen(
                state = ProfileReviewUiState(isLoading = false, isComplete = false),
                onEvent = {},
            )
        }
    }

    private companion object {
        // Representative maximum: every selectable equipment checked, so all
        // row treatments render; the split derives from the 3-day frequency.
        val completePreferences = PreferencesUiState(
            isLoading = false,
            goal = TrainingGoal.BUILD_MUSCLE,
            experienceLevel = ExperienceLevel.INTERMEDIATE,
            daysPerWeek = 3,
            sessionDurationMinutes = 60,
            selectedEquipment = TrainingOptions.SELECTABLE_EQUIPMENT.toSet(),
            derivedSplit = WorkoutSplit.PUSH_PULL_LEGS,
        )

        // Full catalog grouped exactly as production shows it: catalog order
        // within each region, regions head-to-toe by declaration order.
        val fullInjuryGroups: List<InjuryGroupUiModel> =
            InjuryCatalog.injuries
                .groupBy { it.bodyRegion }
                .toSortedMap(compareBy { it.ordinal })
                .map { (region, definitions) ->
                    InjuryGroupUiModel(
                        region = region,
                        regionNameRes = region.labelRes,
                        injuries = definitions.map { InjuryUiModel(id = it.id, nameRes = it.nameRes) },
                    )
                }

        // Production-real suggestions: what a torn ACL implies.
        val kneeAclSuggestions: Set<MovementLimitation> =
            InjuryCatalog.injuries.first { it.id == InjuryId.KNEE_ACL }.suggestedLimitations

        val suggestedLimitations: List<LimitationUiModel> =
            MovementLimitationCatalog.limitations
                .filter { it.id in kneeAclSuggestions }
                .map { LimitationUiModel(id = it.id, nameRes = it.nameRes) }

        // Browsable catalog minus the suggestions, dropping groups left empty —
        // the same rule the ViewModel applies, so suggested rows never double up.
        fun browsableGroups(excluding: Set<MovementLimitation>): List<LimitationGroupUiModel> =
            MovementLimitationCatalog.groupedForDisplay()
                .mapNotNull { (group, definitions) ->
                    val remaining = definitions.filterNot { it.id in excluding }
                    if (remaining.isEmpty()) {
                        null
                    } else {
                        LimitationGroupUiModel(
                            group = group,
                            groupNameRes = group.labelRes,
                            limitations = remaining.map { LimitationUiModel(id = it.id, nameRes = it.nameRes) },
                        )
                    }
                }

        val populatedReview = ProfileReviewUiState(
            isLoading = false,
            isComplete = true,
            goalRes = R.string.goal_build_muscle,
            experienceRes = R.string.experience_intermediate,
            daysPerWeek = 3,
            sessionDurationMinutes = 60,
            splitRes = R.string.split_push_pull_legs,
            equipment = listOf(
                R.string.equipment_bodyweight,
                R.string.equipment_dumbbells,
                R.string.equipment_bench,
            ),
            injuries = listOf(
                ReviewInjuryUiModel(
                    nameRes = R.string.injury_knee_acl,
                    statusRes = R.string.injury_status_historical,
                ),
            ),
            limitations = listOf(
                R.string.limitation_avoid_jumping,
                R.string.limitation_avoid_deep_knee_flexion,
            ),
        )
    }
}
