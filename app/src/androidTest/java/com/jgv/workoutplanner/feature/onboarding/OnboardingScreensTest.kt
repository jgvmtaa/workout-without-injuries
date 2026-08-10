package com.jgv.workoutplanner.feature.onboarding

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
import com.jgv.workoutplanner.domain.model.BodyRegion
import com.jgv.workoutplanner.domain.model.InjuryId
import com.jgv.workoutplanner.domain.model.InjuryStatus
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
    fun injuryHistory_selectingAndDeselectingEmitsToggle() {
        val events = mutableListOf<InjuryHistoryEvent>()
        composeRule.setContent {
            AppTheme {
                InjuryHistoryScreen(
                    state = InjuryHistoryUiState(isLoading = false, injuryGroups = KNEE_GROUP),
                    onEvent = { events += it },
                )
            }
        }

        composeRule.onNodeWithText(text(R.string.injury_knee_acl)).performClick()

        assertEquals(listOf(InjuryHistoryEvent.ToggleInjury(InjuryId.KNEE_ACL)), events)
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
