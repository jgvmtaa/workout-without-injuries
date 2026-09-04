package com.jgv.workoutplanner.domain.usecase

import com.jgv.workoutplanner.domain.model.OnboardingDraft
import com.jgv.workoutplanner.domain.model.UserProfile
import com.jgv.workoutplanner.domain.repository.ProfileRepository
import com.jgv.workoutplanner.domain.repository.WorkoutPlanRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Cross-repository rule for profile editing (Phase 6 §6.5, §6.6).
 *
 * Compares old vs new profile (except hasAcceptedSafetyNotice). If changed,
 * markRequiresRegeneration before saving profile to avoid exposing changed profile
 * with falsely current plan. Ordering may produce safe false-positive after failure.
 * If identical -> Unchanged and must not invalidate.
 */
class SaveProfileEditsUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val workoutPlanRepository: WorkoutPlanRepository,
    private val determineSplitUseCase: DetermineWorkoutSplitUseCase,
) {

    sealed interface Result {
        data object Unchanged : Result
        data object Updated : Result
    }

    suspend operator fun invoke(editedDraft: OnboardingDraft): Result {
        val oldProfile = profileRepository.profile.first()

        // If no old profile (should not happen in edit flow) treat as changed
        if (oldProfile == null) {
            val newProfile = editedDraft.toUserProfile(
                determineSplitUseCase(editedDraft.daysPerWeek ?: 3),
            ) ?: return Result.Unchanged
            workoutPlanRepository.markRequiresRegeneration()
            profileRepository.saveProfile(newProfile)
            return Result.Updated
        }

        val newProfile = editedDraft.toUserProfile(
            determineSplitUseCase(editedDraft.daysPerWeek ?: oldProfile.daysPerWeek),
        ) ?: return Result.Unchanged

        if (oldProfile == newProfile) {
            return Result.Unchanged
        }

        if (isMaterialChange(oldProfile, newProfile)) {
            // Mark before save — repository atomically checks whether plan exists.
            workoutPlanRepository.markRequiresRegeneration()
        }
        profileRepository.saveProfile(newProfile)
        return Result.Updated
    }

    private fun isMaterialChange(old: UserProfile, new: UserProfile): Boolean {
        if (old.goal != new.goal) return true
        if (old.experienceLevel != new.experienceLevel) return true
        if (old.daysPerWeek != new.daysPerWeek) return true
        if (old.sessionDurationMinutes != new.sessionDurationMinutes) return true
        if (old.preferredSplit != new.preferredSplit) return true
        if (old.availableEquipment != new.availableEquipment) return true
        if (old.selectedInjuries != new.selectedInjuries) return true
        if (old.movementLimitations != new.movementLimitations) return true
        // hasAcceptedSafetyNotice ignored per spec
        return false
    }
}
