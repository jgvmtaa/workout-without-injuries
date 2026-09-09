package com.jgv.workoutplanner.domain.usecase

import com.jgv.workoutplanner.domain.model.ExerciseDefinition
import com.jgv.workoutplanner.domain.model.ExerciseDifficulty
import com.jgv.workoutplanner.domain.model.ExerciseTag
import com.jgv.workoutplanner.domain.model.ExperienceLevel
import com.jgv.workoutplanner.domain.model.MovementLimitation
import com.jgv.workoutplanner.domain.model.MovementPattern
import com.jgv.workoutplanner.domain.model.PlanWarning
import com.jgv.workoutplanner.domain.model.PlannedExercise
import com.jgv.workoutplanner.domain.model.RankedExercise
import com.jgv.workoutplanner.domain.model.UserProfile
import com.jgv.workoutplanner.domain.model.WorkoutDay
import com.jgv.workoutplanner.domain.model.WorkoutDayFocus
import com.jgv.workoutplanner.domain.model.WorkoutPlan
import com.jgv.workoutplanner.domain.model.WorkoutPlanGenerationResult
import com.jgv.workoutplanner.domain.model.WorkoutPlanTemplate
import com.jgv.workoutplanner.domain.model.WorkoutSplit
import javax.inject.Inject

/**
 * Deterministically generates a workout plan from an eligible exercise set (README §12).
 *
 * Orchestrates: split → filter → group → rank → assemble (task 5.7).
 *
 * Filtering is delegated to [GetEligibleExercisesUseCase] which already implements
 * 5.2 (README §12.2): removes conflicting limitations, missing equipment, above-level.
 *
 * ## Determinism
 * - Available exercises sorted by [com.jgv.workoutplanner.domain.model.ExerciseId.name]
 * - Ranking tie-break on ExerciseId.name (README §12.5)
 * - No randomness, no timestamp, plan & day IDs derived from daysPerWeek+split (task spec)
 *
 * ## Partial handling (README §25)
 * Unfillable template slot ⇒ warning, not crash.
 *
 * ## Prescription (clarified)
 * Uses catalog [ExerciseDefinition.defaultPrescription] directly:
 * sets = range.start, repRange = full range, rest = restSeconds. No goal-based adjustment
 * in MVP.
 */
class GenerateWorkoutPlanUseCase @Inject constructor(
    private val determineSplitUseCase: DetermineWorkoutSplitUseCase,
    private val getEligibleUseCase: GetEligibleExercisesUseCase,
) {

    operator fun invoke(profile: UserProfile): WorkoutPlanGenerationResult {
        val split: WorkoutSplit = determineSplitUseCase(profile.daysPerWeek)
        val filtered = getEligibleUseCase(profile)
        val available = filtered.available
            .map { it.exercise }
            .sortedBy { it.id.name }

        val dayFocuses: List<WorkoutDayFocus> = resolveDayFocuses(profile.daysPerWeek, split)
        val planId = "plan-${profile.daysPerWeek}-${split.name}"

        val warnings = mutableListOf<PlanWarning>()
        val days = mutableListOf<WorkoutDay>()
        val globallyUsedIds = mutableSetOf<com.jgv.workoutplanner.domain.model.ExerciseId>()

        for ((dayIndex, focus) in dayFocuses.withIndex()) {
            val template = WorkoutPlanTemplate.slotsFor(focus)
            val selectedIds = mutableSetOf<com.jgv.workoutplanner.domain.model.ExerciseId>()
            val selectedPatterns = mutableSetOf<MovementPattern>()
            var unilateralCount = 0
            val planned = mutableListOf<PlannedExercise>()

            for ((slotIndex, slot) in template.withIndex()) {
                val isEarly = slotIndex < 2
                val perDayCandidates = available.filter { def ->
                    def.id !in selectedIds && def.movementPattern in slot.allowedPatterns
                }
                // Prefer exercises not yet used anywhere in the plan to give variation
                // when the same focus repeats (e.g. Upper 1 vs Upper 2). Reuse is allowed
                // only when every alternative is exhausted, keeping the generator partial-safe.
                val unusedCandidates = perDayCandidates.filter { it.id !in globallyUsedIds }
                val candidates = if (unusedCandidates.isNotEmpty()) unusedCandidates else perDayCandidates

                if (candidates.isEmpty()) {
                    warnings.add(
                        PlanWarning(
                            dayIndex = dayIndex,
                            dayFocus = focus,
                            slotId = slot.id,
                        ),
                    )
                    continue
                }

                val ranked = candidates.map { def ->
                    RankedExercise(def, scoreExercise(def, selectedPatterns, unilateralCount, isEarly, profile))
                }.sortedWith(
                    compareByDescending<RankedExercise> { it.score }
                        .thenBy { it.exercise.id.name },
                )

                val chosen = ranked.first().exercise

                // Build PlannedExercise from catalog default
                val prescription = chosen.defaultPrescription
                val plannedExercise = PlannedExercise(
                    exerciseId = chosen.id,
                    sets = prescription.sets.first,
                    repRange = prescription.reps,
                    restSeconds = prescription.restSeconds,
                    order = slotIndex,
                )
                planned.add(plannedExercise)
                selectedIds.add(chosen.id)
                globallyUsedIds.add(chosen.id)
                selectedPatterns.add(chosen.movementPattern)
                if (ExerciseTag.UNILATERAL in chosen.tags) unilateralCount++
            }

            // Sort planned by order (already in order, but ensure)
            val sortedPlanned = planned.sortedBy { it.order }
            val dayId = "${planId}-day-${dayIndex}-${focus.name.lowercase()}"
            val dayName = dayDisplayName(focus, dayIndex, dayFocuses)
            days.add(
                WorkoutDay(
                    id = dayId,
                    name = dayName,
                    focus = focus,
                    exercises = sortedPlanned,
                ),
            )
        }

        // Plan name: deterministic, human readable
        val planName = "${split.name} - ${profile.daysPerWeek} days"

        val plan = WorkoutPlan(
            id = planId,
            name = planName,
            days = days,
        )

        return WorkoutPlanGenerationResult(
            plan = plan,
            warnings = warnings,
            eligibleExerciseCount = available.size,
        )
    }

    private fun scoreExercise(
        def: ExerciseDefinition,
        alreadySelectedPatterns: Set<MovementPattern>,
        unilateralCount: Int,
        isEarlySlot: Boolean,
        profile: UserProfile,
    ): Int {
        var score = 0
        // +5 exact required movement pattern – after filtering, candidate is always exact,
        // so this is the base score for belonging to the slot.
        score += 5

        // +2 experience-matched difficulty — prefer exercises whose difficulty matches the user's level.
        // Previously this unconditionally awarded +2 to BEGINNER exercises, ignoring the user's actual level.
        // Now ranking respects experience:
        //   BEGINNER → +2 BEGINNER
        //   INTERMEDIATE → +2 INTERMEDIATE
        //   ADVANCED → +2 ADVANCED, +1 INTERMEDIATE (so ADV > INT > BEG for advanced users)
        score += when (profile.experienceLevel) {
            ExperienceLevel.BEGINNER -> if (def.difficulty == ExerciseDifficulty.BEGINNER) 2 else 0
            ExperienceLevel.INTERMEDIATE -> if (def.difficulty == ExerciseDifficulty.INTERMEDIATE) 2 else 0
            ExperienceLevel.ADVANCED -> when (def.difficulty) {
                ExerciseDifficulty.ADVANCED -> 2
                ExerciseDifficulty.INTERMEDIATE -> 1
                ExerciseDifficulty.BEGINNER -> 0
            }
        }

        // +2 machine-supported when balance limitations exist
        val hasBalanceLimitation = profile.movementLimitations.any {
            it == MovementLimitation.AVOID_UNILATERAL_BALANCE_DEMAND ||
                it == MovementLimitation.AVOID_SINGLE_LEG_LOADING
        }
        if (hasBalanceLimitation) {
            if (ExerciseTag.MACHINE_SUPPORTED in def.tags || ExerciseTag.CHEST_SUPPORTED in def.tags) {
                score += 2
            }
        }

        // +1 compound early slot (first 2 slots)
        if (isEarlySlot && ExerciseTag.COMPOUND in def.tags) {
            score += 1
        }

        // -2 duplicate movement
        if (def.movementPattern in alreadySelectedPatterns) {
            score -= 2
        }

        // -3 second unilateral
        if (ExerciseTag.UNILATERAL in def.tags && unilateralCount >= 1) {
            score -= 3
        }

        return score
    }

    private fun resolveDayFocuses(daysPerWeek: Int, split: WorkoutSplit): List<WorkoutDayFocus> =
        when (split) {
            WorkoutSplit.FULL_BODY -> List(daysPerWeek) { WorkoutDayFocus.FULL_BODY }
            WorkoutSplit.PUSH_PULL_LEGS -> {
                // 3 → PUSH,PULL,LEGS. For safety if daysPerWeek !=3, cycle.
                val cycle = listOf(WorkoutDayFocus.PUSH, WorkoutDayFocus.PULL, WorkoutDayFocus.LEGS)
                List(daysPerWeek) { idx -> cycle[idx % cycle.size] }
            }

            WorkoutSplit.UPPER_LOWER -> when (daysPerWeek) {
                4 -> listOf(
                    WorkoutDayFocus.UPPER_BODY,
                    WorkoutDayFocus.LOWER_BODY,
                    WorkoutDayFocus.UPPER_BODY,
                    WorkoutDayFocus.LOWER_BODY,
                )

                5 -> listOf(
                    WorkoutDayFocus.UPPER_BODY,
                    WorkoutDayFocus.LOWER_BODY,
                    WorkoutDayFocus.UPPER_BODY,
                    WorkoutDayFocus.LOWER_BODY,
                    WorkoutDayFocus.FULL_BODY,
                )

                else -> {
                    // fallback: alternate Upper/Lower, starting Upper, repeating
                    List(daysPerWeek) { idx ->
                        if (idx % 2 == 0) WorkoutDayFocus.UPPER_BODY else WorkoutDayFocus.LOWER_BODY
                    }
                }
            }
        }

    private fun dayDisplayName(focus: WorkoutDayFocus, dayIndex: Int, all: List<WorkoutDayFocus>): String {
        // If focus repeats, append counter of occurrence, else just focus label.
        val focusCount = all.take(dayIndex + 1).count { it == focus }
        val base = when (focus) {
            WorkoutDayFocus.FULL_BODY -> "Full Body"
            WorkoutDayFocus.UPPER_BODY -> "Upper"
            WorkoutDayFocus.LOWER_BODY -> "Lower"
            WorkoutDayFocus.PUSH -> "Push"
            WorkoutDayFocus.PULL -> "Pull"
            WorkoutDayFocus.LEGS -> "Legs"
        }
        return if (all.count { it == focus } > 1) "$base $focusCount" else base
    }
}
