package com.jgv.workoutplanner.feature.onboarding

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.core.designsystem.AppTheme
import com.jgv.workoutplanner.domain.model.LimitationGroup
import com.jgv.workoutplanner.domain.model.MovementLimitation
import com.jgv.workoutplanner.feature.onboarding.injuries.InjuryGroupUiModel
import com.jgv.workoutplanner.feature.onboarding.injuries.InjuryHistoryEvent
import com.jgv.workoutplanner.feature.onboarding.injuries.InjuryHistoryScreen
import com.jgv.workoutplanner.feature.onboarding.injuries.InjuryHistoryUiState
import com.jgv.workoutplanner.feature.onboarding.injuries.InjuryUiModel
import com.jgv.workoutplanner.feature.onboarding.limitations.LimitationGroupUiModel
import com.jgv.workoutplanner.feature.onboarding.limitations.LimitationUiModel
import com.jgv.workoutplanner.feature.onboarding.limitations.MovementLimitationsEvent
import com.jgv.workoutplanner.feature.onboarding.limitations.MovementLimitationsScreen
import com.jgv.workoutplanner.feature.onboarding.limitations.MovementLimitationsUiState
import com.jgv.workoutplanner.feature.onboarding.safety.SafetyNoticeEvent
import com.jgv.workoutplanner.feature.onboarding.safety.SafetyNoticeScreen
import com.jgv.workoutplanner.feature.onboarding.safety.SafetyNoticeUiState
import com.jgv.workoutplanner.feature.onboarding.preferences.PreferencesEvent
import com.jgv.workoutplanner.feature.onboarding.preferences.PreferencesScreen
import com.jgv.workoutplanner.feature.onboarding.preferences.PreferencesUiState
import com.jgv.workoutplanner.feature.onboarding.review.ProfileReviewEvent
import com.jgv.workoutplanner.feature.onboarding.review.ProfileReviewScreen
import com.jgv.workoutplanner.feature.onboarding.review.ProfileReviewUiState
import com.jgv.workoutplanner.feature.onboarding.welcome.WelcomeScreen
import com.jgv.workoutplanner.domain.model.BodyRegion
import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.ExperienceLevel
import com.jgv.workoutplanner.domain.model.InjuryId
import com.jgv.workoutplanner.domain.model.InjuryStatus
import com.jgv.workoutplanner.domain.model.TrainingGoal
import com.jgv.workoutplanner.domain.model.WorkoutSplit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Compose tests for the onboarding flows README §24.6 calls out: selecting and
 * deselecting an injury, confirming suggested limitations, and the safety gate.
 *
 * These drive the stateless `*Screen` composables with a hand-built state and a
 * recording `onEvent`, so they test what the screen renders and emits without a
 * ViewModel, Hilt, or navigation in the way. The ViewModel side of the same behaviour is
 * covered by the JVM tests, which run in a second rather than a minute.
 */
class OnboardingScreensTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources

    private fun text(resId: Int): String = resources.getString(resId)

    // -------------------------------------------------------------- Safety notice

    @Test
    fun safetyNotice_showsAllFourAcknowledgements() {
        composeRule.setContent {
            AppTheme {
                SafetyNoticeScreen(state = SafetyNoticeUiState(isLoading = false), onEvent = {})
            }
        }

        SafetyNoticeUiState().acknowledgements.forEach { acknowledgement ->
            composeRule.onNodeWithText(text(acknowledgement)).assertIsDisplayed()
        }
    }

    @Test
    fun safetyNotice_blocksContinueUntilAcknowledged() {
        composeRule.setContent {
            AppTheme {
                SafetyNoticeScreen(state = SafetyNoticeUiState(isLoading = false), onEvent = {})
            }
        }

        composeRule.onNodeWithText(text(R.string.action_accept_safety)).assertIsNotEnabled()
        composeRule.onNodeWithText(text(R.string.action_accept_safety)).assertHeightIsAtLeast(48.dp)
        composeRule.onNodeWithText(text(R.string.safety_confirm_label)).assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun safetyNotice_allowsContinueOnceAcknowledged() {
        composeRule.setContent {
            AppTheme {
                SafetyNoticeScreen(
                    state = SafetyNoticeUiState(isLoading = false, isAcknowledged = true),
                    onEvent = {},
                )
            }
        }

        composeRule.onNodeWithText(text(R.string.action_accept_safety)).assertIsEnabled()
    }

    @Test
    fun safetyNotice_tickingTheBoxEmitsAcknowledgement() {
        val events = mutableListOf<SafetyNoticeEvent>()
        composeRule.setContent {
            AppTheme {
                SafetyNoticeScreen(
                    state = SafetyNoticeUiState(isLoading = false),
                    onEvent = { events += it },
                )
            }
        }

        composeRule.onNodeWithText(text(R.string.safety_confirm_label)).performClick()

        assertEquals(listOf(SafetyNoticeEvent.SetAcknowledged(true)), events)
    }

    // ------------------------------------------------------------ Injury history

    @Test
    fun injuryHistory_selectsAndDeselectsInAStatefulFlow() {
        val events = mutableListOf<InjuryHistoryEvent>()
        var selected by mutableStateOf(false)
        composeRule.setContent {
            AppTheme {
                InjuryHistoryScreen(
                    state = InjuryHistoryUiState(
                        isLoading = false,
                        injuryGroups = KNEE_GROUP,
                        selectedInjuries = if (selected) {
                            mapOf(InjuryId.KNEE_ACL to InjuryStatus.NOT_SPECIFIED)
                        } else {
                            emptyMap()
                        },
                    ),
                    onEvent = { event ->
                        events += event
                        if (event == InjuryHistoryEvent.ToggleInjury(InjuryId.KNEE_ACL)) {
                            selected = !selected
                        }
                    },
                )
            }
        }

        composeRule.onNodeWithText(text(R.string.injury_knee_acl)).performClick()
        composeRule.onNodeWithText(text(R.string.injury_knee_acl)).assertIsOn()
        composeRule.onNodeWithText(text(R.string.injury_knee_acl)).performClick()
        composeRule.onNodeWithText(text(R.string.injury_knee_acl)).assertIsOff()

        assertEquals(
            listOf(
                InjuryHistoryEvent.ToggleInjury(InjuryId.KNEE_ACL),
                InjuryHistoryEvent.ToggleInjury(InjuryId.KNEE_ACL),
            ),
            events,
        )
    }

    @Test
    fun injuryHistory_statusIsOnlyOfferedForSelectedInjuries() {
        composeRule.setContent {
            AppTheme {
                InjuryHistoryScreen(
                    state = InjuryHistoryUiState(isLoading = false, injuryGroups = KNEE_GROUP),
                    onEvent = {},
                )
            }
        }

        composeRule.onNodeWithText(text(R.string.injuries_status_title)).assertDoesNotExist()
    }

    @Test
    fun injuryHistory_selectedInjuryOffersItsStatus() {
        composeRule.setContent {
            AppTheme {
                InjuryHistoryScreen(
                    state = InjuryHistoryUiState(
                        isLoading = false,
                        injuryGroups = KNEE_GROUP,
                        selectedInjuries = mapOf(InjuryId.KNEE_ACL to InjuryStatus.HISTORICAL),
                    ),
                    onEvent = {},
                )
            }
        }

        composeRule.onNodeWithText(text(R.string.injury_knee_acl)).assertIsOn()
        composeRule.onNodeWithText(text(R.string.injuries_status_title)).assertIsDisplayed()
    }

    // -------------------------------------------------------- Movement limitations

    /**
     * The rendered form of README §2: a suggestion arrives as an unticked box, under copy
     * that says it has not been applied.
     */
    @Test
    fun limitations_suggestionsRenderUnconfirmed() {
        composeRule.setContent {
            AppTheme {
                MovementLimitationsScreen(
                    state = MovementLimitationsUiState(
                        isLoading = false,
                        suggested = listOf(JUMPING),
                        otherGroups = SHOULDER_GROUP,
                    ),
                    onEvent = {},
                )
            }
        }

        composeRule.onNodeWithText(text(R.string.limitations_suggested_description))
            .assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.limitation_avoid_jumping)).assertIsOff()
    }

    @Test
    fun limitations_confirmingASuggestionEmitsIt() {
        val events = mutableListOf<MovementLimitationsEvent>()
        composeRule.setContent {
            AppTheme {
                MovementLimitationsScreen(
                    state = MovementLimitationsUiState(
                        isLoading = false,
                        suggested = listOf(JUMPING),
                        otherGroups = SHOULDER_GROUP,
                    ),
                    onEvent = { events += it },
                )
            }
        }

        composeRule.onNodeWithText(text(R.string.limitation_avoid_jumping)).performClick()

        assertEquals(
            listOf(
                MovementLimitationsEvent.SetConfirmed(MovementLimitation.AVOID_JUMPING, true),
            ),
            events,
        )
    }

    @Test
    fun limitations_confirmedSuggestionRendersTicked() {
        composeRule.setContent {
            AppTheme {
                MovementLimitationsScreen(
                    state = MovementLimitationsUiState(
                        isLoading = false,
                        suggested = listOf(JUMPING),
                        otherGroups = SHOULDER_GROUP,
                        confirmed = setOf(MovementLimitation.AVOID_JUMPING),
                    ),
                    onEvent = {},
                )
            }
        }

        composeRule.onNodeWithText(text(R.string.limitation_avoid_jumping)).assertIsOn()
    }

    @Test
    fun limitations_noInjuryHistoryExplainsTheAbsenceOfSuggestions() {
        composeRule.setContent {
            AppTheme {
                MovementLimitationsScreen(
                    state = MovementLimitationsUiState(
                        isLoading = false,
                        otherGroups = SHOULDER_GROUP,
                    ),
                    onEvent = {},
                )
            }
        }

        composeRule.onNodeWithText(text(R.string.limitations_no_suggestions_description))
            .assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.action_continue)).assertIsEnabled()
    }

    /** Continuing with nothing confirmed is valid (README §25). */
    @Test
    fun limitations_continueIsNeverBlocked() {
        val events = mutableListOf<MovementLimitationsEvent>()
        composeRule.setContent {
            AppTheme {
                MovementLimitationsScreen(
                    state = MovementLimitationsUiState(
                        isLoading = false,
                        suggested = listOf(JUMPING),
                        otherGroups = SHOULDER_GROUP,
                    ),
                    onEvent = { events += it },
                )
            }
        }

        composeRule.onNodeWithText(text(R.string.action_continue)).performClick()

        assertTrue(MovementLimitationsEvent.Continue in events)
    }

    @Test
    fun completeOnboarding_reachesHomeThroughEveryStep() {
        var step by mutableIntStateOf(0)
        var safetyAccepted by mutableStateOf(false)

        composeRule.setContent {
            AppTheme {
                when (step) {
                    0 -> WelcomeScreen(
                        onGetStarted = { step = 1 },
                        onReviewSafety = { step = 1 },
                    )
                    1 -> SafetyNoticeScreen(
                        state = SafetyNoticeUiState(
                            isLoading = false,
                            isAcknowledged = safetyAccepted,
                        ),
                        onEvent = { event ->
                            when (event) {
                                is SafetyNoticeEvent.SetAcknowledged -> safetyAccepted = event.acknowledged
                                SafetyNoticeEvent.Continue -> step = 2
                                SafetyNoticeEvent.Back -> Unit
                            }
                        },
                    )
                    2 -> PreferencesScreen(
                        state = PreferencesUiState(
                            isLoading = false,
                            goal = TrainingGoal.BUILD_MUSCLE,
                            experienceLevel = ExperienceLevel.INTERMEDIATE,
                            daysPerWeek = 3,
                            sessionDurationMinutes = 45,
                            selectedEquipment = setOf(Equipment.BODYWEIGHT),
                            derivedSplit = WorkoutSplit.PUSH_PULL_LEGS,
                        ),
                        onEvent = { if (it == PreferencesEvent.Continue) step = 3 },
                    )
                    3 -> InjuryHistoryScreen(
                        state = InjuryHistoryUiState(isLoading = false, injuryGroups = KNEE_GROUP),
                        onEvent = { if (it == InjuryHistoryEvent.Continue) step = 4 },
                    )
                    4 -> MovementLimitationsScreen(
                        state = MovementLimitationsUiState(
                            isLoading = false,
                            otherGroups = SHOULDER_GROUP,
                        ),
                        onEvent = { if (it == MovementLimitationsEvent.Continue) step = 5 },
                    )
                    5 -> ProfileReviewScreen(
                        state = ProfileReviewUiState(
                            isLoading = false,
                            isComplete = true,
                            goalRes = R.string.goal_build_muscle,
                            experienceRes = R.string.experience_intermediate,
                            daysPerWeek = 3,
                            sessionDurationMinutes = 45,
                            splitRes = R.string.split_push_pull_legs,
                            equipment = listOf(R.string.equipment_bodyweight),
                        ),
                        onEvent = { if (it == ProfileReviewEvent.Confirm) step = 6 },
                    )
                    else -> Text(text = stringResource(R.string.screen_home))
                }
            }
        }

        composeRule.onNodeWithText(text(R.string.action_get_started)).performClick()
        composeRule.onNodeWithText(text(R.string.safety_confirm_label)).performClick()
        composeRule.onNodeWithText(text(R.string.action_accept_safety)).performClick()
        repeat(3) {
            composeRule.onNodeWithText(text(R.string.action_continue)).performClick()
        }
        composeRule.onNodeWithText(text(R.string.action_finish_setup)).performClick()

        composeRule.onNodeWithText(text(R.string.screen_home)).assertIsDisplayed()
    }

    private companion object {

        val KNEE_GROUP = listOf(
            InjuryGroupUiModel(
                region = BodyRegion.KNEE,
                regionNameRes = R.string.body_region_knee,
                injuries = listOf(
                    InjuryUiModel(InjuryId.KNEE_ACL, R.string.injury_knee_acl),
                    InjuryUiModel(InjuryId.KNEE_MENISCUS, R.string.injury_knee_meniscus),
                ),
            ),
        )

        val JUMPING = LimitationUiModel(
            MovementLimitation.AVOID_JUMPING,
            R.string.limitation_avoid_jumping,
        )

        val SHOULDER_GROUP = listOf(
            LimitationGroupUiModel(
                group = LimitationGroup.SHOULDER,
                groupNameRes = R.string.limitation_group_shoulder,
                limitations = listOf(
                    LimitationUiModel(
                        MovementLimitation.AVOID_OVERHEAD_PRESSING,
                        R.string.limitation_avoid_overhead_pressing,
                    ),
                ),
            ),
        )
    }
}
