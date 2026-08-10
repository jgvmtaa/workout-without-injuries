package com.jgv.workoutplanner.data.local.model

import com.jgv.workoutplanner.domain.model.BodySide
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
 * Mapping between the persisted form and the domain model (README §21).
 *
 * ## Reading is tolerant, writing is exact
 * Every enum is stored by constant name. On the way in, a name this build does not
 * recognise is *dropped* rather than throwing: a catalog that removes a limitation
 * should cost the user that one checkbox, not their whole profile. On the way out,
 * collections are sorted by name so the same state always produces byte-identical JSON —
 * which makes the file diffable and the round-trip tests exact.
 *
 * ## The one place it is not tolerant
 * [PersistedProfile.toDomain] returns `null` if a *required* field cannot be parsed.
 * A profile missing its goal or experience level cannot generate a plan, and inventing a
 * default would mean planning around something the user never chose. Returning `null`
 * routes them back through onboarding, where the draft still holds their answers.
 */

// ---------------------------------------------------------------- Domain ← storage

/** Reads [PersistedDraft] into the domain, dropping any value this build cannot parse. */
fun PersistedDraft.toDomain(): OnboardingDraft = OnboardingDraft(
    hasAcceptedSafetyNotice = hasAcceptedSafetyNotice,
    goal = goal.toEnumOrNull<TrainingGoal>(),
    experienceLevel = experienceLevel.toEnumOrNull<ExperienceLevel>(),
    daysPerWeek = daysPerWeek,
    sessionDurationMinutes = sessionDurationMinutes,
    // Bodyweight is not a choice — it is always available and cannot be deselected
    // (README §25). Added on read regardless of what the file says, so a profile written
    // before that rule existed still satisfies it.
    availableEquipment = availableEquipment.toEnumSet<Equipment>() + Equipment.BODYWEIGHT,
    selectedInjuries = selectedInjuries.mapNotNullTo(mutableSetOf()) { it.toDomain() },
    confirmedLimitations = confirmedLimitations.toEnumSet(),
)

/** Reads [PersistedProfile] into the domain, or `null` if a required field is unusable. */
fun PersistedProfile.toDomain(): UserProfile? {
    val goal = goal.toEnumOrNull<TrainingGoal>() ?: return null
    val experience = experienceLevel.toEnumOrNull<ExperienceLevel>() ?: return null
    val split = preferredSplit.toEnumOrNull<WorkoutSplit>() ?: return null

    return UserProfile(
        goal = goal,
        experienceLevel = experience,
        daysPerWeek = daysPerWeek,
        sessionDurationMinutes = sessionDurationMinutes,
        preferredSplit = split,
        availableEquipment = availableEquipment.toEnumSet<Equipment>() + Equipment.BODYWEIGHT,
        selectedInjuries = selectedInjuries.mapNotNullTo(mutableSetOf()) { it.toDomain() },
        movementLimitations = movementLimitations.toEnumSet(),
        hasAcceptedSafetyNotice = hasAcceptedSafetyNotice,
    )
}

/**
 * Reads one selected injury, or `null` if the injury itself is unknown.
 *
 * Side and status fall back to their domain defaults instead: losing "left knee" is
 * survivable, losing the knee is not.
 */
fun PersistedInjury.toDomain(): SelectedInjury? {
    val id = injuryId.toEnumOrNull<InjuryId>() ?: return null
    return SelectedInjury(
        injuryId = id,
        affectedSide = affectedSide.toEnumOrNull<BodySide>() ?: BodySide.NOT_SPECIFIED,
        status = status.toEnumOrNull<InjuryStatus>() ?: InjuryStatus.HISTORICAL,
    )
}

// ---------------------------------------------------------------- Domain → storage

fun OnboardingDraft.toPersisted(): PersistedDraft = PersistedDraft(
    hasAcceptedSafetyNotice = hasAcceptedSafetyNotice,
    goal = goal?.name,
    experienceLevel = experienceLevel?.name,
    daysPerWeek = daysPerWeek,
    sessionDurationMinutes = sessionDurationMinutes,
    availableEquipment = availableEquipment.toSortedNames(),
    selectedInjuries = selectedInjuries.toPersisted(),
    confirmedLimitations = confirmedLimitations.toSortedNames(),
)

fun UserProfile.toPersisted(): PersistedProfile = PersistedProfile(
    goal = goal.name,
    experienceLevel = experienceLevel.name,
    daysPerWeek = daysPerWeek,
    sessionDurationMinutes = sessionDurationMinutes,
    preferredSplit = preferredSplit.name,
    availableEquipment = availableEquipment.toSortedNames(),
    selectedInjuries = selectedInjuries.toPersisted(),
    movementLimitations = movementLimitations.toSortedNames(),
    hasAcceptedSafetyNotice = hasAcceptedSafetyNotice,
)

// --------------------------------------------------------------------- Internals

private fun Set<SelectedInjury>.toPersisted(): List<PersistedInjury> =
    map {
        PersistedInjury(
            injuryId = it.injuryId.name,
            affectedSide = it.affectedSide.name,
            status = it.status.name,
        )
    }.sortedBy { it.injuryId }

private fun <T : Enum<T>> Set<T>.toSortedNames(): List<String> =
    map { it.name }.sorted()

private inline fun <reified T : Enum<T>> String?.toEnumOrNull(): T? =
    this?.let { name -> enumValues<T>().firstOrNull { it.name == name } }

private inline fun <reified T : Enum<T>> List<String>.toEnumSet(): Set<T> =
    mapNotNullTo(mutableSetOf()) { it.toEnumOrNull<T>() }
