package com.jgv.workoutplanner.testing

import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.ExperienceLevel
import com.jgv.workoutplanner.domain.model.InjuryId
import com.jgv.workoutplanner.domain.model.InjuryStatus
import com.jgv.workoutplanner.domain.model.MovementLimitation
import com.jgv.workoutplanner.domain.model.OnboardingDraft
import com.jgv.workoutplanner.domain.model.SelectedInjury
import com.jgv.workoutplanner.domain.model.TrainingGoal
import com.jgv.workoutplanner.domain.model.UserProfile
import com.jgv.workoutplanner.domain.model.WorkoutSplit

/**
 * Builders for the domain values tests need.
 *
 * Every parameter has a default, so a test names only the field it is about — which
 * keeps the interesting value from being buried in nine lines of boilerplate.
 */

fun userProfile(
    goal: TrainingGoal = TrainingGoal.GENERAL_FITNESS,
    experienceLevel: ExperienceLevel = ExperienceLevel.BEGINNER,
    daysPerWeek: Int = 3,
    sessionDurationMinutes: Int = 45,
    preferredSplit: WorkoutSplit = WorkoutSplit.PUSH_PULL_LEGS,
    availableEquipment: Set<Equipment> = setOf(Equipment.BODYWEIGHT),
    selectedInjuries: Set<SelectedInjury> = emptySet(),
    movementLimitations: Set<MovementLimitation> = emptySet(),
    hasAcceptedSafetyNotice: Boolean = true,
): UserProfile = UserProfile(
    goal = goal,
    experienceLevel = experienceLevel,
    daysPerWeek = daysPerWeek,
    sessionDurationMinutes = sessionDurationMinutes,
    preferredSplit = preferredSplit,
    availableEquipment = availableEquipment,
    selectedInjuries = selectedInjuries,
    movementLimitations = movementLimitations,
    hasAcceptedSafetyNotice = hasAcceptedSafetyNotice,
)

/** A draft with every question answered, ready to become a profile. */
fun completeDraft(
    goal: TrainingGoal = TrainingGoal.BUILD_MUSCLE,
    experienceLevel: ExperienceLevel = ExperienceLevel.INTERMEDIATE,
    daysPerWeek: Int = 3,
    sessionDurationMinutes: Int = 60,
    availableEquipment: Set<Equipment> = setOf(Equipment.BODYWEIGHT, Equipment.DUMBBELLS),
    selectedInjuries: Set<SelectedInjury> = emptySet(),
    confirmedLimitations: Set<MovementLimitation> = emptySet(),
): OnboardingDraft = OnboardingDraft(
    hasAcceptedSafetyNotice = true,
    goal = goal,
    experienceLevel = experienceLevel,
    daysPerWeek = daysPerWeek,
    sessionDurationMinutes = sessionDurationMinutes,
    availableEquipment = availableEquipment,
    selectedInjuries = selectedInjuries,
    confirmedLimitations = confirmedLimitations,
)

fun injury(
    id: InjuryId,
    status: InjuryStatus = InjuryStatus.HISTORICAL,
): SelectedInjury = SelectedInjury(injuryId = id, status = status)
