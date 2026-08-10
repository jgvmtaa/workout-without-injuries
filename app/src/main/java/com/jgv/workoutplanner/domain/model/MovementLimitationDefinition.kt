package com.jgv.workoutplanner.domain.model

import androidx.annotation.StringRes

/**
 * One selectable movement limitation, with the copy and grouping the limitations
 * screen needs (README §4.5, §6).
 *
 * The same shape as [InjuryDefinition] and [ExerciseDefinition]: the enum is the
 * identity, display copy lives in `strings.xml` and is reached through [nameRes].
 */
data class MovementLimitationDefinition(
    val id: MovementLimitation,
    @StringRes val nameRes: Int,
    val group: LimitationGroup,
)
