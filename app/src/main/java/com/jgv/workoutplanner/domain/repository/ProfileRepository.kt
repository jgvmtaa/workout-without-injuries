package com.jgv.workoutplanner.domain.repository

import com.jgv.workoutplanner.domain.model.OnboardingDraft
import com.jgv.workoutplanner.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Storage for the user's profile and the onboarding answers leading up to it
 * (README §19, §21).
 *
 * ## What README §21 asks to persist, and where it lives here
 * | §21 item                | Stored as |
 * |-------------------------|-----------|
 * | User profile            | [profile] |
 * | Movement limitations    | `UserProfile.movementLimitations`; before the profile exists, `OnboardingDraft.confirmedLimitations` |
 * | Safety acknowledgement  | `OnboardingDraft.hasAcceptedSafetyNotice` — accepted at step 2, long before a profile exists |
 * | Onboarding completion   | Derived: `profile != null` |
 *
 * Onboarding completion is deliberately *not* a separate flag. A stored boolean and a
 * stored profile can disagree; a derived one cannot, and the two things it could mean —
 * "the user finished the flow" and "there is a profile to plan from" — are the same
 * event here. Recorded in docs/follow-ups.md in case that stops being true.
 *
 * [profile] emits `null` until onboarding has produced one, which is also how the app
 * decides whether to start at Welcome or Home (README §3).
 */
interface ProfileRepository {

    /** The stored profile, re-emitting on every change. `null` before onboarding. */
    val profile: Flow<UserProfile?>

    /**
     * Onboarding answers collected so far, re-emitting on every change.
     *
     * Emits a default [OnboardingDraft] rather than `null` when nothing has been
     * answered yet, so screens have something to render without a special case.
     */
    val onboardingDraft: Flow<OnboardingDraft>

    /**
     * Applies [transform] to the stored draft atomically.
     *
     * A transform rather than a setter because each onboarding screen owns one slice of
     * the draft: read-modify-write from separate ViewModels would let a slow write on
     * one screen clobber a fast one on the next.
     */
    suspend fun updateDraft(transform: (OnboardingDraft) -> OnboardingDraft)

    /**
     * Writes [profile], replacing anything stored, and re-seeds the draft from it.
     *
     * Re-seeding rather than clearing: the profile screen reuses the onboarding
     * destinations to edit a saved profile (README §13), and those screens read the
     * draft — clearing it would show a returning user an empty form.
     */
    suspend fun saveProfile(profile: UserProfile)

    /**
     * Removes the stored profile *and* the draft, returning the app to its
     * pre-onboarding state.
     */
    suspend fun clearProfile()
}
