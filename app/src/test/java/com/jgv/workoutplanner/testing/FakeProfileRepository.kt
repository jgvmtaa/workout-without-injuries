package com.jgv.workoutplanner.testing

import com.jgv.workoutplanner.domain.model.OnboardingDraft
import com.jgv.workoutplanner.domain.model.UserProfile
import com.jgv.workoutplanner.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory [ProfileRepository] for ViewModel tests.
 *
 * Behaves like the real one where behaviour is observable: `saveProfile` re-seeds the
 * draft from the profile, `clearProfile` resets both. A double that skipped those would
 * let a ViewModel test pass while the app misbehaved.
 *
 * [savedProfiles] records every write so a test can assert not just the end state but
 * that exactly one save happened.
 */
class FakeProfileRepository(
    initialProfile: UserProfile? = null,
    initialDraft: OnboardingDraft = OnboardingDraft(),
    private val operationLog: MutableList<String>? = null,
) : ProfileRepository {

    private val _profile = MutableStateFlow(initialProfile)
    private val _draft = MutableStateFlow(initialDraft)

    override val profile: Flow<UserProfile?> = _profile.asStateFlow()
    override val onboardingDraft: Flow<OnboardingDraft> = _draft.asStateFlow()

    /** Every profile written, in order. */
    val savedProfiles: MutableList<UserProfile> = mutableListOf()

    /** The draft as it stands now, for assertions that do not need to collect. */
    val currentDraft: OnboardingDraft get() = _draft.value

    override suspend fun updateDraft(transform: (OnboardingDraft) -> OnboardingDraft) {
        _draft.value = transform(_draft.value)
    }

    override suspend fun saveProfile(profile: UserProfile) {
        operationLog?.add("saveProfile")
        savedProfiles += profile
        _profile.value = profile
        _draft.value = OnboardingDraft.from(profile)
    }

    override suspend fun resetDraftFromProfile() {
        val p = _profile.value
        _draft.value = if (p != null) OnboardingDraft.from(p) else OnboardingDraft()
    }

    override suspend fun clearProfile() {
        _profile.value = null
        _draft.value = OnboardingDraft()
    }
}
