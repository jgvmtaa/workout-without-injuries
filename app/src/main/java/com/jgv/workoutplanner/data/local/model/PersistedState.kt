package com.jgv.workoutplanner.data.local.model

import kotlinx.serialization.Serializable

/**
 * The on-disk shape of everything onboarding persists (spec §21).
 *
 * ## Why this is not just `UserProfile`
 * A storage format and a domain model change for different reasons. Renaming an enum
 * constant is a free refactor in the domain; if the domain were serialised directly it
 * would silently orphan every stored profile that used the old name. So the persisted
 * form spells enums as [String] and the mapping in `PersistedStateMapping.kt` parses
 * them tolerantly — an unrecognised value is dropped, not thrown.
 *
 * ## Schema evolution
 * [schemaVersion] is written on every save and never read yet. It exists so the first
 * migration has somewhere to branch on; without it, version 1 data is indistinguishable
 * from version 2 data and the first breaking change has no safe landing.
 *
 * Every field has a default so a file written by an older build deserialises into a
 * newer one, and `Json { ignoreUnknownKeys = true }` covers the reverse — a file from a
 * *newer* build loses the fields this build doesn't know rather than failing to load.
 */
@Serializable
data class PersistedState(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val draft: PersistedDraft = PersistedDraft(),
    val profile: PersistedProfile? = null,
) {
    companion object {
        /** Bumped whenever a change to these classes needs a migration. */
        const val CURRENT_SCHEMA_VERSION: Int = 1
    }
}

/**
 * Onboarding answers collected so far.
 *
 * Everything is optional, because a draft is incomplete by definition — including
 * during the two screens before the first question is asked.
 */
@Serializable
data class PersistedDraft(
    val hasAcceptedSafetyNotice: Boolean = false,
    val goal: String? = null,
    val experienceLevel: String? = null,
    val daysPerWeek: Int? = null,
    val sessionDurationMinutes: Int? = null,
    val availableEquipment: List<String> = emptyList(),
    val selectedInjuries: List<PersistedInjury> = emptyList(),
    val confirmedLimitations: List<String> = emptyList(),
)

/**
 * A completed profile (spec §10).
 *
 * Unlike [PersistedDraft], the enum fields here are non-null: a profile that cannot be
 * fully reconstructed is not a profile. The mapping returns `null` for the whole thing
 * in that case, which puts the user back into onboarding with their draft intact rather
 * than into an app holding a half-built profile.
 */
@Serializable
data class PersistedProfile(
    val goal: String,
    val experienceLevel: String,
    val daysPerWeek: Int,
    val sessionDurationMinutes: Int,
    val preferredSplit: String,
    val availableEquipment: List<String> = emptyList(),
    val selectedInjuries: List<PersistedInjury> = emptyList(),
    val movementLimitations: List<String> = emptyList(),
    val hasAcceptedSafetyNotice: Boolean = false,
)

/** One selected injury (spec §4.4). */
@Serializable
data class PersistedInjury(
    val injuryId: String,
    val affectedSide: String,
    val status: String,
)
