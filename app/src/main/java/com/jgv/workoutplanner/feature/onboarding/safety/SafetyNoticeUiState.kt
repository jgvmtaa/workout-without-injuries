package com.jgv.workoutplanner.feature.onboarding.safety

import com.jgv.workoutplanner.R

/**
 * Immutable state for the safety notice (spec §4.2, §20).
 *
 * [isAcknowledged] is loaded from the stored draft rather than starting at `false`, so a
 * user who accepted and then navigated back is not asked to accept twice.
 */
data class SafetyNoticeUiState(
    val isLoading: Boolean = true,
    val isAcknowledged: Boolean = false,
) {

    /**
     * The four points spec §4.2 requires the user to see, as string resources.
     *
     * Held in the state rather than hard-coded in the composable so a Compose test can
     * assert all four are shown without duplicating the list.
     */
    val acknowledgements: List<Int> = ACKNOWLEDGEMENTS

    /** Acknowledgement is explicit: nothing proceeds until the box is ticked. */
    val canContinue: Boolean get() = isAcknowledged

    private companion object {
        val ACKNOWLEDGEMENTS = listOf(
            R.string.safety_ack_not_medical_advice,
            R.string.safety_ack_stop_on_pain,
            R.string.safety_ack_follow_clinician,
            R.string.safety_ack_filters_are_general,
        )
    }
}

/**
 * Everything the safety notice can do (spec §20).
 *
 * [Continue] and [Back] are navigation intents: the route maps them to its lambdas and
 * the ViewModel never sees them. They live here anyway so the screen has one event
 * channel — which is what makes it previewable and drivable from a Compose test without
 * a ViewModel.
 */
sealed interface SafetyNoticeEvent {

    /** The acknowledgement checkbox was toggled. Persisted immediately. */
    data class SetAcknowledged(val acknowledged: Boolean) : SafetyNoticeEvent

    data object Continue : SafetyNoticeEvent

    data object Back : SafetyNoticeEvent
}
