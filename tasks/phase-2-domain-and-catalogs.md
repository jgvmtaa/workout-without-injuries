# Phase 2 — Domain Models & Static Catalogs

**Goal:** Pure-Kotlin domain layer plus the static exercise and injury catalogs.
This is the **recommended first coding task** (README §31) — prove the model
before building UI.

**Depends on:** Phase 1 package structure. **Blocks:** Phases 3–6.

**Root README refs:** §4.3–§4.5, §5, §6, §7, §10, §11, §26, §31.

---

## 2.1 Enums & primitive domain types
- [ ] `TrainingGoal`, `ExperienceLevel` (README §4.3)
- [ ] `Equipment`, `WorkoutSplit` (README §4.3)
- [ ] `BodyRegion`, `InjuryId`, `BodySide`, `InjuryStatus` (README §4.4)
- [ ] `MovementLimitation` — full enum (README §4.5)
- [ ] `MuscleGroup`, `MovementPattern` (README §5.1, §5.2)
- [ ] `ExerciseDifficulty`, `ExerciseTag`, `ExerciseId` (README §5.3)
- [ ] `WorkoutDayFocus` (README §11)

## 2.2 Domain data classes
- [ ] `SelectedInjury` (README §4.4) — keep `affectedSide` but engine ignores it in MVP
- [ ] `InjuryDefinition` (id, nameRes, bodyRegion, suggestedLimitations) (README §4.5)
- [ ] `ExercisePrescription` (sets/reps IntRange, restSeconds) (README §5.3)
- [ ] `ExerciseDefinition` (all fields) (README §5.3)
- [ ] `UserProfile` (README §10) — immutable collections at boundaries
- [ ] `WorkoutPlan`, `WorkoutDay`, `PlannedExercise` (README §11) — reference `ExerciseId`, don't duplicate definitions

## 2.3 String resources strategy (README §6)
- [ ] `strings.xml` entries for each exercise name + description
- [ ] `strings.xml` entries for each injury and limitation label
- [ ] Enforce: IDs are enum values, user copy is `@StringRes` — never strings as IDs

## 2.4 Exercise catalog (README §7, §26)
- [ ] `ExerciseCatalog.kt` in `data/catalog/`
- [ ] Populate ~45–60 exercises across the §26 distribution:
  - [ ] Chest 5–7, Back 7–9, Shoulders 6–8, Biceps 3–5, Triceps 4–6
  - [ ] Quadriceps 6–8, Hamstrings 4–6, Glutes 5–7, Calves 2–3, Core 5–7
- [ ] Ensure each movement pattern has alternatives across equipment types (README §26)
- [ ] Set `conflictingLimitations` accurately per exercise (this drives filtering)

## 2.5 Injury catalog
- [ ] `InjuryCatalog.kt` mapping each `InjuryId` → `InjuryDefinition` with suggested limitations
- [ ] Encode example: `KNEE_ACL` ⇒ {AVOID_JUMPING, AVOID_RAPID_DIRECTION_CHANGE} (README §4.5)

## 2.6 Repository interfaces + static implementations (README §19, §27 Phase 2)
- [ ] `ExerciseRepository`, `ProfileRepository`, `WorkoutPlanRepository` interfaces
- [ ] `DefaultExerciseRepository` backed by the static catalog
- [ ] Query exercises by muscle group; query injuries by body region

## 2.7 Catalog validation tests
- [ ] Every `ExerciseId` has exactly one catalog entry (no missing/duplicate)
- [ ] Every referenced `@StringRes` exists
- [ ] Distribution counts fall within §26 ranges
- [ ] Each movement pattern has ≥2 equipment alternatives

---

## Completion criteria (README §27 Phase 2)
- [ ] Exercises can be queried by muscle group.
- [ ] Injuries can be queried by body region.
- [ ] Catalog validation tests pass.
