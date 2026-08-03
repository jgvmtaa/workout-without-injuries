# Phase 5 — Plan Generation

**Goal:** Deterministically generate a workout plan from an eligible exercise set.

**Depends on:** Phase 4 (eligibility). **Blocks:** Phase 6 (editing).

**Root README refs:** §11 (plan model), §12 (algorithm), §16 (home), §20 (UI state/events),
§24.4 (tests), §27 Phase 5.

---

## 5.1 Split selection (README §12.1)
- [ ] `determineSplit(daysPerWeek)`: 2→FULL_BODY, 3→PUSH_PULL_LEGS, 4→UPPER_LOWER, else→UPPER_LOWER

## 5.2 Catalog filtering step (README §12.2)
- [ ] Remove exercises conflicting with confirmed limitations
- [ ] Remove exercises requiring unavailable equipment
- [ ] Remove exercises above experience level (if strict exclusion chosen)

## 5.3 Grouping (README §12.3)
- [ ] Group remaining exercises by primary muscle, movement pattern, and workout-day focus

## 5.4 Workout-day templates (README §12.4)
- [ ] Full-body template (squat/knee, hinge/hip-ext, push, pull, secondary upper, core)
- [ ] Upper-body template (h-push, v/secondary push, h-pull, v-pull, shoulder iso, biceps, triceps)
- [ ] Lower-body template (knee-dominant, hinge, glute, hamstring iso, calf, core)

## 5.5 Deterministic ranking (README §12.5)
- [ ] `RankedExercise(exercise, score)`
- [ ] Scoring: +5 exact pattern, +3 preferred equipment, +2 beginner-friendly,
      +2 machine-supported when balance limitations exist, +1 compound early slot,
      −2 duplicate movement, −3 second unilateral
- [ ] **Break ties by `ExerciseId.name`** (stable) so same profile ⇒ same plan

## 5.6 Selection & assembly
- [ ] Fill each template slot from top-ranked eligible candidates without duplicates
- [ ] Build `WorkoutPlan` → `WorkoutDay` → `PlannedExercise` referencing `ExerciseId` (README §11)
- [ ] Partial-plan handling: unfillable slot yields a warning, not a crash (README §12.4, §25)

## 5.7 Persistence (README §21)
- [ ] `WorkoutPlanDataStore` + `DefaultWorkoutPlanRepository` persist the current plan
- [ ] `GenerateWorkoutPlanUseCase` orchestrates split→filter→group→rank→assemble

## 5.8 Plan screen (README §11, §16)
- [ ] Follow the §20 state/event pattern (immutable `UiState`, `sealed *Event`, previewable `Screen`, no `NavController`)
- [ ] Render generated plan by day with sets/reps/rest

## 5.9 Tests (README §24.4)
- [ ] Every generated exercise is eligible
- [ ] Every generated exercise uses available equipment
- [ ] Required movement slots filled where possible
- [ ] No exercise appears twice in the same workout
- [ ] Same input ⇒ same plan (determinism)
- [ ] Missing categories ⇒ warning, not crash
- [ ] Extremely restrictive profile ⇒ valid partial result

---

## Completion criteria (README §27 Phase 5)
- [ ] No generated plan contains an excluded exercise.
- [ ] Equivalent input always generates the same output.
- [ ] Restrictive profiles do not crash generation.
