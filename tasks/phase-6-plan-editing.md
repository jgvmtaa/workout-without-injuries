# Phase 6 — Plan Editing

**Goal:** Let users manually adjust a generated plan while keeping every edit
inside the profile's filters, and let returning users edit their profile without
silently changing the plan generated from the previous profile.

**Depends on:** Phase 5 (generated plan). **Blocks:** Phase 7 polish.

**Root README refs:** §13 (manual adjustment), §16 (home), §17 (Profile route),
§21 (persistence), §25 (outdated plan), §27 Phase 6.

---

## Decisions fixed for this phase

- Manual plan mutations go through one `UpdateWorkoutExerciseUseCase` with sealed
  operations. The UI does not edit or persist a `WorkoutPlan` directly.
- Plans keep the Phase 5 template capacity for each day focus. Manual edits may
  remove, replace, reorder, or add exercises up to that capacity.
- Every replacement and addition must satisfy the current profile's limitation,
  equipment, and experience filters. Filters are never relaxed to offer a result.
- Profile editing uses dedicated edit destinations while reusing the existing
  onboarding screen composables. Onboarding navigation remains unchanged.
- A profile change leaves the existing plan untouched and marks it outdated.
  Manual editing is unavailable until that plan is regenerated.
- Regeneration replaces the entire plan, including prior manual customizations.
  Phase 6 owns the confirmation for this destructive action; Phase 7 may polish it.
- Phase 6 adds domain, persistence, and ViewModel tests. The end-to-end Compose
  replacement flow remains in Phase 7.

## 6.1 Plan-editing contract (README §13, §20–§21)

- [ ] Add a sealed `WorkoutExerciseEdit` contract consumed by
      `UpdateWorkoutExerciseUseCase`:
  - `Remove(workoutDayId, exerciseId)`
  - `Replace(workoutDayId, currentExerciseId, replacementExerciseId)`
  - `Move(workoutDayId, exerciseId, targetIndex)`
  - `AddExercises(workoutDayId, exerciseIds)`
- [ ] Return a typed result rather than throwing or silently ignoring an edit. Cover:
      `Updated`, `Unchanged`, missing plan/day/exercise, invalid target index,
      ineligible replacement/addition, duplicate exercise, and exceeded capacity.
- [ ] Add an atomic update API to `WorkoutPlanRepository`, implemented with the
      existing `DataStore.updateData` transaction. Do not implement edits as
      `currentPlan.first()` followed by `savePlan()`.
- [ ] Preserve stored generation warnings and `requiresRegeneration` during manual
      plan mutations.
- [ ] Enforce these invariants inside the use case, not only in the UI:
  - A workout day contains no duplicate `ExerciseId` values.
  - `PlannedExercise.order` is normalized to contiguous `0..lastIndex` after remove
    or move.
  - Replacement retains the existing position.
  - Additions cannot exceed `WorkoutPlanTemplate.slotsFor(day.focus).size`.
  - Multi-exercise addition is all-or-nothing.
- [ ] Convert typed edit failures into a brief recoverable UI message and refresh the
      displayed plan.

## 6.2 Plan-screen actions (README §13)

- [ ] Add a trailing overflow menu to each exercise row with Details, Replace,
      Remove, Move up, and Move down actions.
- [ ] Remove immediately. Destructive-action confirmation for removal remains a
      Phase 7 task.
- [ ] Reorder by moving an exercise to a target index. Use up/down actions for the
      MVP; drag-and-drop is not part of Phase 6.
- [x] Regenerate the entire plan by reusing the Phase 5 generation use case.
- [ ] Confirm regeneration before replacing the stored plan. Copy must state that
      existing manual removals, replacements, additions, and ordering will be lost.
- [ ] Commit the regenerated plan, new warnings, and
      `requiresRegeneration = false` together. Do not clear the outdated state until
      the new plan is successfully persisted.
- [ ] Allow a user to remove the last exercise in a day. Render an empty manually
      edited day as "No exercises in this workout," not as a filtering failure.

## 6.3 Replace exercise flow (README §13, §17)

- [ ] Implement
      `GetExerciseReplacementsUseCase(currentExerciseId, profile, excludedExerciseIds)`.
- [ ] Build candidates from `GetEligibleExercisesUseCase(profile).available`, then
      exclude the current exercise and every exercise already in the target workout
      day. Exercises used on other days remain eligible.
- [ ] Rank candidates deterministically in these tiers:
  1. Same movement pattern and primary muscle.
  2. Same movement pattern.
  3. Same primary muscle.
  4. All other eligible exercises.
- [ ] Break ties with `ExerciseId.name`.
- [ ] Revalidate the chosen replacement in `UpdateWorkoutExerciseUseCase`; never
      trust a candidate list that may have become stale.
- [ ] Apply the replacement exercise's catalog-default sets, reps, and rest while
      retaining the replaced exercise's `order`.
- [ ] Keep the existing direct Replace action on the plan row.
- [x] `ExerciseReplacement(workoutDayId, exerciseId)` is a typed route and receives
      the real Phase 5 workout-day and exercise IDs.
- [ ] Replace the placeholder with immutable UI state, sealed events, a ViewModel,
      and a previewable screen.
- [ ] Show candidate names only. Selecting a row marks it as the pending choice;
      a separate confirmation action applies the replacement and returns to Plan.
- [ ] If there are no candidates, show an empty state without relaxing filters.
- [ ] If the route's plan, day, or exercise is stale, automatically return to Plan.

## 6.4 Add exercises to a workout day

- [ ] Compute remaining capacity as
      `WorkoutPlanTemplate.slotsFor(day.focus).size - day.exercises.size`.
      This includes slots Phase 5 could not initially fill as well as manually
      removed exercises.
- [ ] When capacity is positive, show one add row at the end of the workout-day list.
      Use plural-aware copy stating how many more exercises the day can contain.
      Hide the row when the day is full.
- [ ] Add a typed `ExercisePicker(workoutDayId)` destination. Keep the ordinary
      Exercise Library destination unchanged, but reuse its UI components where
      practical.
- [ ] Implement `GetExercisesForWorkoutDayUseCase(profile, dayFocus,
      excludedExerciseIds)`:
  - Start from profile-eligible exercises.
  - Keep exercises whose movement pattern occurs in at least one Phase 5 template
    slot for the target day focus.
  - Exclude exercises already in that workout day.
  - Sort deterministically by `ExerciseId.name`.
- [ ] Reuse catalog search, muscle-group filters, and equipment filters in the picker.
      Show exercise names only and retain selections when filters change.
- [ ] Allow selection of multiple exercises up to remaining capacity, then confirm
      once.
- [ ] Apply the selection with one atomic `AddExercises` operation. Use each
      exercise's catalog-default prescription and append exercises in selection order.
- [ ] Revalidate eligibility, day-focus compatibility, duplicates, and capacity at
      commit time.

## 6.5 Profile editing for a returning user (README §3, §16–§17, §20)

- [ ] Replace the `Profile` placeholder with `ProfileUiState`, sealed `ProfileEvent`,
      `ProfileViewModel`, and a previewable `ProfileScreen`.
- [ ] Show saved summaries for goal, schedule, experience, equipment, injuries, and
      confirmed limitations, with section-specific Edit actions. Keep preferred split
      derived/read-only, as in onboarding.
- [ ] Add dedicated typed destinations for editing Preferences, Injury History,
      Movement Limitations, and Profile Review. Reuse the onboarding screen
      composables with edit-specific navigation callbacks instead of adding mode
      branches to the onboarding routes.
- [ ] Use these short edit flows:
  - Preferences → Review.
  - Injury History → Movement Limitations → Review.
  - Movement Limitations → Review.
- [ ] Add atomic `ProfileRepository.resetDraftFromProfile()`. Call it before starting
      an edit session and when cancelling one so stale draft changes cannot leak into
      a later session.
- [ ] Treat the persisted draft as the edit session so changes survive process death.
      Back navigates within the edit flow; a separate Cancel action discards the whole
      session and returns to Profile.
- [ ] Add `ProfileEditReviewViewModel` and reuse `ProfileReviewScreen`. Saving uses
      `SaveProfileEditsUseCase`; it must not run onboarding completion navigation.
- [ ] After saving, return to Profile. Do not clear the application back stack or send
      a returning user through the remainder of onboarding.

## 6.6 Outdated-plan handling (README §25)

- [ ] Add `requiresRegeneration: Boolean = false` to the persisted workout-plan
      wrapper and expose it through `WorkoutPlanRepository`.
- [ ] Implement `SaveProfileEditsUseCase(ProfileRepository, WorkoutPlanRepository)`.
      It coordinates the cross-repository rule instead of putting plan behavior in
      `ProfileRepository` or a ViewModel.
- [ ] Compare the old and edited profiles before writing. Any changed `UserProfile`
      field except `hasAcceptedSafetyNotice` requires regeneration. An identical save
      is `Unchanged` and must not invalidate the plan.
- [ ] If a plan exists and the profile changed, persist
      `requiresRegeneration = true` before saving the profile. The stores are separate,
      so this ordering may produce a safe false-positive after failure but must never
      expose a changed profile with a falsely current plan.
- [ ] Do not create an outdated state when no plan exists.
- [ ] Show outdated-plan status on both Home and Plan. The Plan banner must say:
      "Your profile has changed. Regenerate the plan to apply the new limitations."
- [ ] While outdated, keep the existing plan visible but hide exercise edit menus and
      add rows. Regeneration remains available.
- [ ] Preserve previous generation warnings in storage while outdated, but hide them
      in the UI because they describe the prior profile.
- [ ] Never remove, replace, reorder, or add exercises automatically after a profile
      change.
- [ ] Regeneration replaces all earlier manual edits, recomputes warnings, and clears
      the outdated flag only after the replacement plan is persisted.

## 6.7 Tests (README §24.5)

- [ ] `GetExerciseReplacementsUseCase` tests cover limitation, equipment, and
      experience filters; current/day duplicate exclusion; every ranking tier; and the
      stable `ExerciseId.name` tie-break.
- [ ] `GetExercisesForWorkoutDayUseCase` tests cover profile filters, day-focus
      patterns, duplicate exclusion, and deterministic ordering.
- [ ] `UpdateWorkoutExerciseUseCase` tests cover remove, replace, move, atomic
      multi-add, default replacement/addition prescriptions, normalized order, capacity,
      duplicate rejection, ineligible choices, invalid IDs/indices, and no-op results.
- [ ] Repository/persistence tests prove remove, replace, reorder, additions, warnings,
      and `requiresRegeneration` survive reconstruction from local storage.
- [x] The Phase 5 generation test proves equivalent input produces an equivalent
      deterministic plan.
- [ ] Plan ViewModel tests prove regeneration replaces the stored plan and warnings,
      clears the outdated flag only after success, and exposes recoverable edit errors.
- [ ] Profile-edit tests cover draft initialization, cancellation, identical saves,
      changes with and without an existing plan, mark-before-save ordering, persisted
      invalidation, and returning to Profile.
- [ ] Home and Plan ViewModel tests cover outdated status, hidden stale warnings, and
      disabled editing.
- [ ] Exercise picker ViewModel tests cover search, muscle/equipment filters,
      selection retention across filters, capacity enforcement, and atomic confirmation.
- [ ] Keep full Compose journeys (including replace exercise) in Phase 7 as specified
      by README §24.6.

---

## Completion criteria (README §27 Phase 6)

- [ ] Remove, replace, move, and add operations preserve plan invariants and persist
      locally.
- [ ] Replacement and addition candidates respect every profile filter and never
      duplicate an exercise within a workout day.
- [ ] Returning users can save or cancel section-aware profile edits without entering
      the onboarding navigation flow.
- [ ] A material profile change persistently marks an existing plan outdated without
      modifying it.
- [ ] Outdated plans cannot be manually edited and are clearly identified on Home and
      Plan.
- [ ] Confirmed regeneration replaces manual edits, recalculates warnings, and clears
      the outdated state only after successful persistence.
- [ ] Phase 6 domain, persistence, and ViewModel tests pass.

**Phase 6 is ready to implement.** The remaining unchecked items above are
implementation work, not unresolved product or architecture decisions.
