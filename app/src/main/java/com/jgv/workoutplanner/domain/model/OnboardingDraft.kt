package com.jgv.workoutplanner.domain.model

/**
 * Onboarding answers collected so far (spec §3, §4.1–§4.5).
 *
 * Onboarding spans six destinations, each with its own ViewModel, but the later
 * screens need what the earlier ones collected: the limitations screen derives its
 * suggestions from [selectedInjuries], and review summarises everything. This is where
 * that accumulating state lives, and it is persisted after every change — so closing
 * the app half way through onboarding costs nothing, and each ViewModel can read and
 * write the draft independently instead of sharing one object in memory.
 *
 * Every field a [UserProfile] requires is nullable or empty here, because a draft is
 * by definition incomplete. [toUserProfile] is the only way across that boundary and
 * returns `null` until it isn't.
 *
 * [confirmedLimitations] holds only what the user ticked. Suggestions are derived from
 * [selectedInjuries] on demand and never merged in — that separation is what keeps the
 * core product rule enforceable (spec §2, §4.5).
 */
data class OnboardingDraft(
    val hasAcceptedSafetyNotice: Boolean = false,
    val goal: TrainingGoal? = null,
    val experienceLevel: ExperienceLevel? = null,
    val daysPerWeek: Int? = null,
    val sessionDurationMinutes: Int? = null,
    val availableEquipment: Set<Equipment> = setOf(Equipment.BODYWEIGHT),
    val selectedInjuries: Set<SelectedInjury> = emptySet(),
    val confirmedLimitations: Set<MovementLimitation> = emptySet(),
) {

    /** True once every answer a [UserProfile] needs has been given. */
    val isComplete: Boolean
        get() = hasAcceptedSafetyNotice &&
            goal != null &&
            experienceLevel != null &&
            daysPerWeek != null &&
            sessionDurationMinutes != null &&
            availableEquipment.isNotEmpty()

    /**
     * Assembles the final profile, or `null` if the draft is still incomplete.
     *
     * @param split the split derived from [daysPerWeek]
     *   (see [com.jgv.workoutplanner.domain.usecase.DetermineWorkoutSplitUseCase]).
     *   Passed in rather than derived here so this stays a plain data mapping.
     */
    fun toUserProfile(split: WorkoutSplit): UserProfile? {
        if (!isComplete) return null
        return UserProfile(
            goal = goal!!,
            experienceLevel = experienceLevel!!,
            daysPerWeek = daysPerWeek!!,
            sessionDurationMinutes = sessionDurationMinutes!!,
            preferredSplit = split,
            availableEquipment = availableEquipment,
            selectedInjuries = selectedInjuries,
            movementLimitations = confirmedLimitations,
            hasAcceptedSafetyNotice = hasAcceptedSafetyNotice,
        )
    }

    companion object {

        /**
         * A draft pre-filled from an existing [profile], so the onboarding screens can be
         * reused to edit a saved profile instead of starting blank (spec §13).
         *
         * The split is not carried back: it is derived from [UserProfile.daysPerWeek], so
         * re-deriving it keeps a single source of truth.
         */
        fun from(profile: UserProfile): OnboardingDraft = OnboardingDraft(
            hasAcceptedSafetyNotice = profile.hasAcceptedSafetyNotice,
            goal = profile.goal,
            experienceLevel = profile.experienceLevel,
            daysPerWeek = profile.daysPerWeek,
            sessionDurationMinutes = profile.sessionDurationMinutes,
            availableEquipment = profile.availableEquipment,
            selectedInjuries = profile.selectedInjuries,
            confirmedLimitations = profile.movementLimitations,
        )
    }
}
