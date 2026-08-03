# Phase 6 — Plan Editing

**Goal:** Let users manually adjust a generated plan while keeping every edit
inside the profile's filters.

**Depends on:** Phase 5 (generated plan). **Blocks:** Phase 7 polish.

**Root README refs:** §13 (manual adjustment), §16 (home), §17 (Profile route),
§25 (outdated plan), §27 Phase 6.

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

## 6.4 Profile & limitations editing — returning user (README §3, §16, §17)
This is the trigger that makes 6.5 meaningful: a returning user must be able to
change their profile after onboarding.
- [ ] Implement the `Profile` destination (`feature/profile/`) — the §17 `Profile` route
- [ ] Show editable profile: goal, schedule, experience, equipment, injuries, limitations
- [ ] Reuse the onboarding screens/state where practical (preferences, injuries, limitations)
- [ ] Follow the §20 state/event pattern (immutable `UiState`, `sealed *Event`, previewable `Screen`)
- [ ] Saving an edited profile persists via `ProfileRepository` and flags the plan (feeds 6.5)

## 6.5 Outdated-plan handling (README §25)
- [ ] Track `requiresRegeneration: Boolean` when profile changes after generation
- [ ] Show "Your profile has changed. Regenerate the plan…" banner
- [ ] Never silently modify an existing plan

## 6.6 Tests (README §24.5)
- [ ] Replacements respect all profile filters (no excluded/unavailable result)
- [ ] Remove/replace/reorder persist locally
- [ ] Regeneration produces a deterministic plan
- [ ] Editing the profile sets `requiresRegeneration`

---

## Completion criteria (README §27 Phase 6)
- [ ] Replacements respect all profile filters.
- [ ] Plan edits persist locally.
