package com.jgv.workoutplanner.data.repository

import com.jgv.workoutplanner.data.local.ProfileDataStore
import com.jgv.workoutplanner.data.local.model.toDomain
import com.jgv.workoutplanner.data.local.model.toPersisted
import com.jgv.workoutplanner.domain.model.OnboardingDraft
import com.jgv.workoutplanner.domain.model.UserProfile
import com.jgv.workoutplanner.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [ProfileRepository] backed by [ProfileDataStore] (README §19, §21, §27 Phase 3).
 *
 * The repository is where storage stops and the domain starts: DataStore holds
 * `PersistedState`, every caller above here sees [UserProfile] and [OnboardingDraft].
 *
 * Both flows are `distinctUntilChanged` because a write to one half of the state
 * re-emits the whole record — without it, ticking a limitation would wake up every
 * collector of [profile] with an identical value.
 */
@Singleton
class DefaultProfileRepository @Inject constructor(
    private val dataStore: ProfileDataStore,
) : ProfileRepository {

    override val profile: Flow<UserProfile?> =
        dataStore.state
            .map { it.profile?.toDomain() }
            .distinctUntilChanged()

    override val onboardingDraft: Flow<OnboardingDraft> =
        dataStore.state
            .map { it.draft.toDomain() }
            .distinctUntilChanged()

    override suspend fun updateDraft(transform: (OnboardingDraft) -> OnboardingDraft) {
        dataStore.update { stored ->
            stored.copy(draft = transform(stored.draft.toDomain()).toPersisted())
        }
    }

    override suspend fun saveProfile(profile: UserProfile) {
        dataStore.update { stored ->
            stored.copy(
                profile = profile.toPersisted(),
                // Kept in step with the profile so the onboarding screens can be reused
                // to edit it (see ProfileRepository.saveProfile).
                draft = OnboardingDraft.from(profile).toPersisted(),
            )
        }
    }

    override suspend fun clearProfile() {
        dataStore.update { stored ->
            stored.copy(profile = null, draft = OnboardingDraft().toPersisted())
        }
    }
}
