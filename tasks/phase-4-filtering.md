# Phase 4 — Exercise Filtering / Eligibility Engine

**Goal:** A deterministic, Android-independent eligibility engine plus the
exercise library UI that surfaces availability and exclusion reasons.

**Depends on:** Phases 2 (models/catalog) and 3 (profile). **Blocks:** Phases 5–6.

**Root README refs:** §8 (engine), §9 (filtering behavior), §14 (library),
§15 (detail), §27 Phase 4.

---

## 4.1 Eligibility model (README §8)
- [ ] `ExerciseEligibility(exercise, isEligible, exclusionReasons)`
- [ ] `ExclusionReason` sealed interface: `ConflictingLimitation`, `MissingEquipment`, `AboveExperienceLevel`

## 4.2 Eligibility use case (README §8) — no Android/Compose imports
- [ ] `EvaluateExerciseEligibilityUseCase(exercise, profile)`
- [ ] Add `ConflictingLimitation` for each conflicting limitation ∩ profile limitations
- [ ] Add `MissingEquipment` for each required-but-unavailable equipment
- [ ] Add `AboveExperienceLevel` when experience doesn't support difficulty
- [ ] `ExperienceLevel.supports(difficulty)` helper (README §8)
- [ ] Decide: hide advanced-for-beginners vs. rank-only (README §8 note — MVP may rank)

## 4.3 Three-category filtering (README §9)
- [ ] Classify each exercise as **Available** / **Excluded** / **Unavailable**
- [ ] `GetEligibleExercisesUseCase` returns categorized results for a profile
- [ ] Excluded exercises remain inspectable but are never auto-included (README §9)
- [ ] Omit "Include anyway" override in MVP (README §9)

## 4.4 Exercise library screen (README §14)
- [ ] Muscle-group tabs/chips: All + 10 groups
- [ ] Browse by muscle group; search by name; filter by equipment
- [ ] Show available and excluded rows with subtitle (muscle · equipment · tag)
- [ ] Excluded rows show "Excluded by N limitation(s)"

## 4.5 Exercise detail screen (README §15)
- [ ] Show name, description, primary/secondary muscles, movement pattern, equipment, default sets/reps
- [ ] Show availability status + exclusion reasons
- [ ] No videos/anatomical illustrations in MVP (README §15)

## 4.6 Tests (README §24.1, §24.2)
- [ ] Exercise excluded when a conflicting limitation is selected
- [ ] Cable exercise unavailable without cable machine (missing-equipment reason)
- [ ] A unit test per limitation category
- [ ] Filtering is deterministic for a fixed profile

---

## Completion criteria (README §27 Phase 4)
- [ ] Every exclusion is explainable.
- [ ] Filtering is deterministic.
- [ ] Unit tests cover each limitation category.
