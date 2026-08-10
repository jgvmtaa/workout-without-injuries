package com.jgv.workoutplanner.domain.model

import androidx.annotation.StringRes

/**
 * One selectable injury and the limitations it *may* imply (README §4.5).
 *
 * [suggestedLimitations] is a prompt, never a decision. The limitations screen offers
 * them and the user confirms, removes, or adds to them; nothing reaches the filter
 * without that confirmation (README §2, §4.5). The app is not diagnosing — it is
 * asking a better question than "what hurts?".
 */
data class InjuryDefinition(
    val id: InjuryId,
    @StringRes val nameRes: Int,
    val bodyRegion: BodyRegion,
    val suggestedLimitations: Set<MovementLimitation>,
)
