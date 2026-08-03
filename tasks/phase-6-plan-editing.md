# Phase 6 — Plan Editing

**Goal:** Let users manually adjust a generated plan while keeping every edit
inside the profile's filters.

**Depends on:** Phase 5 (generated plan). **Blocks:** Phase 7 polish.

**Root README refs:** §13 (manual adjustment), §25 (outdated plan), §27 Phase 6.

---

## 6.1 Edit actions on the plan screen (README §13)
- [ ] Remove exercise
- [ ] Reorder exercises (update `PlannedExercise.order`)
- [ ] Regenerate entire plan (reuses Phase 5 use case)

## 6.2 Replace exercise flow (README §13)
- [ ] `GetExerciseReplacementsUseCase(currentExerciseId, profile)` returns eligible alternatives
- [ ] Prefer alternatives matching: same movement pattern, same primary muscle,
      available equipment, confirmed limitations
- [ ] Flow: tap exercise → Replace → show eligible alternatives → select replacement
- [ ] `ExerciseReplacement(workoutDayId, exerciseId)` route wired (README §17)

## 6.3 Persistence of edits (README §21)
- [ ] `UpdateWorkoutExerciseUseCase` applies remove/replace/reorder and persists
- [ ] Edits survive app restart

## 6.4 Outdated-plan handling (README §25)
- [ ] Track `requiresRegeneration: Boolean` when profile changes after generation
- [ ] Show "Your profile has changed. Regenerate the plan…" banner
- [ ] Never silently modify an existing plan

## 6.5 Tests (README §24.5)
- [ ] Replacements respect all profile filters (no excluded/unavailable result)
- [ ] Remove/replace/reorder persist locally
- [ ] Regeneration produces a deterministic plan
- [ ] Profile change sets `requiresRegeneration`

---

## Completion criteria (README §27 Phase 6)
- [ ] Replacements respect all profile filters.
- [ ] Plan edits persist locally.
