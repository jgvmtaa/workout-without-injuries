package com.jgv.workoutplanner.domain.repository

import com.jgv.workoutplanner.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Storage for the user's profile (README §19, §21).
 *
 * Interface only in Phase 2 — the DataStore-backed implementation lands in Phase 3
 * alongside onboarding. Declared now so the use cases Phase 4 builds can depend on it
 * without waiting for persistence.
 *
 * [profile] emits `null` until onboarding has produced one, which is also how the app
 * decides whether to start at Welcome or Home.
 */
interface ProfileRepository {

    /** The stored profile, re-emitting on every change. `null` before onboarding. */
    val profile: Flow<UserProfile?>

    /** Writes [profile], replacing anything stored. */
    suspend fun saveProfile(profile: UserProfile)

    /** Removes the stored profile, returning the app to its pre-onboarding state. */
    suspend fun clearProfile()
}
