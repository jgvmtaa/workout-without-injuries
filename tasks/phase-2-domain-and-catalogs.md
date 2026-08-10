# Phase 2 — Domain Models & Static Catalogs

**Goal:** Pure-Kotlin domain layer plus the static exercise and injury catalogs.
This is the **recommended first coding task** (README §31) — prove the model
before building UI.

**Depends on:** Phase 1 package structure. **Blocks:** Phases 3–6.

**Root README refs:** §4.3–§4.5, §5, §6, §7, §10, §11, §26, §31.

---

## 2.1 Enums & primitive domain types
- [x] `TrainingGoal`, `ExperienceLevel` (README §4.3)
- [x] `Equipment`, `WorkoutSplit` (README §4.3)
- [x] `BodyRegion`, `InjuryId`, `BodySide`, `InjuryStatus` (README §4.4)
- [x] `MovementLimitation` — full enum (README §4.5)
- [x] `MuscleGroup`, `MovementPattern` (README §5.1, §5.2)
- [x] `ExerciseDifficulty`, `ExerciseTag`, `ExerciseId` (README §5.3)
- [x] `WorkoutDayFocus` (README §11)

## 2.2 Domain data classes
- [x] `SelectedInjury` (README §4.4) — keep `affectedSide` but engine ignores it in MVP
- [x] `InjuryDefinition` (id, nameRes, bodyRegion, suggestedLimitations) (README §4.5)
- [x] `ExercisePrescription` (sets/reps IntRange, restSeconds) (README §5.3)
- [x] `ExerciseDefinition` (all fields) (README §5.3)
- [x] `UserProfile` (README §10) — immutable collections at boundaries
- [x] `WorkoutPlan`, `WorkoutDay`, `PlannedExercise` (README §11) — reference `ExerciseId`, don't duplicate definitions

## 2.3 String resources strategy (README §6)
- [x] `strings.xml` entries for each exercise name + description
- [x] `strings.xml` entries for each injury and limitation label
- [x] Enforce: IDs are enum values, user copy is `@StringRes` — never strings as IDs

## 2.4 Exercise catalog (README §7, §26)
- [x] `ExerciseCatalog.kt` in `data/catalog/`
- [x] Populate ~45–60 exercises across the §26 distribution: **59 exercises**
  - [x] Chest 5–7 (**6**), Back 7–9 (**8**), Shoulders 6–8 (**8**), Biceps 3–5 (**4**), Triceps 4–6 (**5**)
  - [x] Quadriceps 6–8 (**7**), Hamstrings 4–6 (**5**), Glutes 5–7 (**7**), Calves 2–3 (**2**), Core 5–7 (**7**)
- [x] Ensure each movement pattern has alternatives across equipment types (README §26)
- [x] Set `conflictingLimitations` accurately per exercise (this drives filtering)

## 2.5 Injury catalog
- [x] `InjuryCatalog.kt` mapping each `InjuryId` → `InjuryDefinition` with suggested limitations
- [x] Encode example: `KNEE_ACL` ⇒ {AVOID_JUMPING, AVOID_RAPID_DIRECTION_CHANGE} (README §4.5)

## 2.6 Repository interfaces + static implementations (README §19, §27 Phase 2)
- [x] `ExerciseRepository`, `ProfileRepository`, `WorkoutPlanRepository` interfaces
- [x] `DefaultExerciseRepository` backed by the static catalog
- [x] Query exercises by muscle group; query injuries by body region

## 2.7 Catalog validation tests
- [x] Every `ExerciseId` has exactly one catalog entry (no missing/duplicate)
- [x] Every referenced `@StringRes` exists
- [x] Distribution counts fall within §26 ranges
- [x] Each movement pattern has ≥2 equipment alternatives

---

## Completion criteria (README §27 Phase 2)
- [x] Exercises can be queried by muscle group. — `DefaultExerciseRepositoryTest`
- [x] Injuries can be queried by body region. — `DefaultInjuryRepositoryTest`
- [x] Catalog validation tests pass. — 36 tests across 4 classes

**Verified by standalone kotlinc, not by Gradle.** The host this was written on cannot
run Gradle, so the tests were compiled and run directly (see
[toolchain.md](../docs/toolchain.md#workaround-type-checking-and-running-jvm-tests-without-gradle)).
That covers the criteria above, but not AGP, KSP, Hilt code generation, aapt, or lint.
Clear [Pending verification](../docs/follow-ups.md#pending-verification) on a machine
where Gradle works before building Phase 3 on top of this.

---

## Implementation notes

Decisions worth knowing before starting Phase 3, and the reasoning behind them.

**An `InjuryRepository` was added (2.6).** §19 names three repositories and none of
them owns injuries, but "injuries can be queried by body region" is a completion
criterion. Hanging that query off `ExerciseRepository` would have merged two
unrelated catalogs, so it got its own interface.

**`Equipment.BODYWEIGHT` is treated as real equipment.** Every exercise declares a
non-empty `requiredEquipment`, so bodyweight movements can be matched and substituted
like any other. Onboarding must always include `BODYWEIGHT` in the available set.

**Twelve exercise ids were added beyond the §5.3 list.** §5.3's 47 ids do not satisfy
the §26 distribution (back and hamstrings fall short) and leave `BARBELL` and
`RESISTANCE_BAND` almost unused. The additions are barbell and band variants plus
`SINGLE_LEG_ROMANIAN_DEADLIFT`, `NORDIC_HAMSTRING_CURL`, and `HANGING_KNEE_RAISE`.

**`LEG_EXTENSION` is filed under `MovementPattern.SQUAT`.** §5.2 has no knee-extension
pattern. Squat is the right *slot* for it in a plan template (§12.4 asks for "1 squat
or knee-dominant exercise"), and its `ISOLATION` tag keeps ranking able to prefer a
real compound squat.

**"≥2 equipment alternatives" is enforced with one carve-out.** A pattern whose
exercises are *all* bodyweight-only is exempt, since those are available to every user
regardless of equipment and there is nothing to substitute for. Only
`CORE_ANTI_EXTENSION` uses the exemption. Every pattern still needs ≥2 exercises.

**Six limitations currently exclude nothing:** the four impact/locomotion ones plus
`AVOID_SPINAL_ROTATION` and `AVOID_LOADED_WRIST_FLEXION`. Expected — the MVP catalog
has no plyometrics, running, loaded rotation, or loaded wrist flexion. Users can still
confirm them, and Phase 4 copy should not imply that confirming one changed the plan.

**With every limitation confirmed, 12 exercises still remain** across most muscle
groups. The "no plan is possible" empty state (§25) is unlikely to trigger, but Phase 5
should still handle it.
