# Phase 5 — Plan Generation

**Goal:** Deterministically generate a workout plan from an eligible exercise set.

**Depends on:** Phase 4 (eligibility). **Blocks:** Phase 6 (editing).

**Root README refs:** §11 (plan model), §12 (algorithm), §16 (home), §20 (UI state/events),
§24.4 (tests), §27 Phase 5.

---

## 5.1 Split selection (README §12.1)
- [x] `determineSplit(daysPerWeek)`: 2→FULL_BODY, 3→PUSH_PULL_LEGS, 4→UPPER_LOWER, else→UPPER_LOWER
  - `DetermineWorkoutSplitUseCase` landed in Phase 3; reused here. Mapping: 2→FULL_BODY,
    3→PUSH_PULL_LEGS, 4→UPPER_LOWER, else→UPPER_LOWER. PPL=3× PUSH/PULL/LEGS.
    4-day = Upper/Lower/Upper/Lower, 5-day = Upper/Lower/Upper/Lower/Full (confirmed).

## 5.2 Catalog filtering step (README §12.2)
- [x] Remove exercises conflicting with confirmed limitations
- [x] Remove exercises requiring unavailable equipment
- [x] Remove exercises above experience level (if strict exclusion chosen)
  - Delegated to existing `GetEligibleExercisesUseCase` (Phase 4). Strict exclusion
    per Phase 4 decision: BEGINNER→only BEGINNER, INTERMEDIATE excludes ADVANCED.

## 5.3 Grouping (README §12.3)
- [x] Group remaining exercises by primary muscle, movement pattern, and workout-day focus
  - Filtering produces `available` list sorted by `ExerciseId.name`. Grouping is implicit:
    template slots filter by `MovementPattern` set, which is pattern+focus grouping.

## 5.4 Workout-day templates (README §12.4)
- [x] Full-body template (squat/knee, hinge/hip-ext, push, pull, secondary upper, core) – 6 slots
- [x] Upper-body template (h-push, v/secondary push, h-pull, v-pull, shoulder iso, biceps, triceps) – 7 slots
- [x] Lower-body template (knee-dominant, hinge, glute, hamstring iso, calf, core) – 6 slots
- [x] PUSH template (h-push, v-push, shoulder iso, triceps, secondary push) – 5 slots
- [x] PULL template (h-pull, v-pull, rear-delt/ext-rot, biceps, secondary pull) – 5 slots
- [x] LEGS reuses lower-body template – 6 slots
  - Implemented in `WorkoutPlanTemplate` (Android-independent). Fixed sizes; session duration ignored in MVP (Phase 7).

## 5.5 Deterministic ranking (README §12.5)
- [x] `RankedExercise(exercise, score)`
- [x] Scoring: +5 exact pattern, +2 beginner-friendly, +2 machine-supported when balance limitations exist, +1 compound early slot, −2 duplicate movement, −3 second unilateral
  - +3 preferred equipment dropped as vacuous post-filter (confirmed). `+2 BEGINNER`, `+2 MACHINE_SUPPORTED/CHEST_SUPPORTED when AVOID_UNILATERAL_BALANCE_DEMAND or AVOID_SINGLE_LEG_LOADING`, `+1 COMPOUND in first 2 slots`.
- [x] **Break ties by `ExerciseId.name`** (stable) so same profile ⇒ same plan
  - `compareByDescending score thenBy id.name`.

## 5.6 Selection & assembly
- [x] Fill each template slot from top-ranked eligible candidates without duplicates
  - Per-day dedup via `selectedIds`, per-day pattern tracking for `-2 duplicate`, unilateral count for `-3`.
  - **Fix (identical days):** added `globallyUsedIds` across week – prefer unused candidates when same focus repeats (Upper 1 vs Upper 2) to avoid byte-identical days; reuse allowed only when alternatives exhausted. Determinism preserved.
- [x] Build `WorkoutPlan` → `WorkoutDay` → `PlannedExercise` referencing `ExerciseId` (README §11)
  - Deterministic IDs: `plan-${daysPerWeek}-${split}` and `${planId}-day-${index}-${focus.lowercase()}`.
  - Prescription = catalog defaults: sets = range.start, repRange = full range, restSeconds (no goal adjustment in MVP).
- [x] Partial-plan handling: unfillable slot yields a warning, not a crash (README §12.4, §25)
  - `PlanWarning(dayIndex, dayFocus, slotId)` + `WorkoutPlanGenerationResult(plan, warnings, eligibleCount)`; Phase 7 moved warning copy to localized UI resources.

## 5.7 Persistence (README §21)
- [x] `WorkoutPlanDataStore` + `DefaultWorkoutPlanRepository` persist the current plan
  - Separate file `workout_plan.json` via `DataStore<StoredWorkoutPlan>` (not `profile.json`) per decision.
  - `PersistedWorkoutPlanSerializer` + tolerant mapping (unknown ExerciseId dropped, not crash).
  - `AppModule` provides second DataStore, `RepositoryModule` binds implementation.
- [x] `GenerateWorkoutPlanUseCase` orchestrates split→filter→group→rank→assemble
  - `determineSplit(profile.daysPerWeek)` → `getEligible(profile)` → rank → assemble → `WorkoutPlanGenerationResult`.

## 5.8 Plan screen (README §11, §16)
- [x] Follow the §20 state/event pattern (immutable `UiState`, `sealed *Event`, previewable `Screen`, no `NavController`)
  - `PlanUiState(isLoading, isGenerating, planName, days: List<WorkoutDayUiModel>, warnings, hasNoPlan)`,
    `PlanEvent` sealed, `PlanRoute` resolves ViewModel + auto-generates on entry.
  - `HomeUiState`/`HomeViewModel`/`HomeScreen` updated to real implementation (was placeholder).
- [x] Render generated plan by day with sets/reps/rest
  - `PlanScreen` LazyColumn: warnings card (README §25 copy), day cards, exercise rows `sets x repRange • rest`.
  - `AppNavigation` now uses `PlanRoute` with real day/exercise IDs, removing placeholder constant.

## 5.9 Tests (README §24.4)
- [x] Every generated exercise is eligible – `GenerateWorkoutPlanUseCaseTest`
- [x] Every generated exercise uses available equipment
- [x] Required movement slots filled where possible – generous profile has 0 warnings and full template sizes
- [x] No exercise appears twice in the same workout – per-day distinct check for 4-day and 5-day
- [x] Same input ⇒ same plan (determinism) – double invoke + id/day/ex collection equality
- [x] Missing categories ⇒ warning, not crash – BODYWEIGHT-only + limiting limitations yields warnings
- [x] Extremely restrictive profile ⇒ valid partial result – BEGINNER + BODYWEIGHT + all limitations → plan valid, warnings, no crash
- [x] Additional: plan ID format, 4-day Upper/Lower pattern, 5-day with FullBody, PPL 3-day, prescription defaults, ranking balance path
  - 14 tests in `GenerateWorkoutPlanUseCaseTest`, 3 in `PersistedWorkoutPlanMappingTest`
  - Total project: 203 unit tests, 0 failures, `./gradlew assembleDebug` green (catalog = 59 exercises).

---

## Completion criteria (README §27 Phase 5)
- [x] No generated plan contains an excluded exercise. (checked via eligibility + limitation intersect)
- [x] Equivalent input always generates the same output. (determinism test + IDs deterministic)
- [x] Restrictive profiles do not crash generation. (extremely restrictive + missing categories → warnings)

**Phase 5 is closed.** All templates, ranking with stable tie-break, partial handling with warnings, separate `workout_plan.json` DataStore, real Plan/Home screens, and full test coverage are in place. Phase 6 can build on it – replacing an exercise with an eligible alternative.
