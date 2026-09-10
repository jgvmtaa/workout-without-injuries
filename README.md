# Workout Planner

Workout Planner is an Android app that creates local workout plans from training
preferences, available equipment, experience, and user-confirmed movement limitations.
This README is the reference for implemented app behavior. Visual coverage is tracked
in [tests.spec](tests.spec); the original numbered specification is in
[docs/spec.md](docs/spec.md).

## Product boundaries

- The app filters exercises; it does not diagnose injuries or prescribe rehabilitation.
- Injury-derived limitations are suggestions only and are never applied automatically.
- Users should stop any exercise that causes pain and follow clinician guidance.
- The MVP has no account, cloud service, subscription, social features, analytics,
  workout history, AI recommendations, or remote exercise catalog.

## First launch and onboarding

1. A new user starts on Welcome and can begin onboarding or review the safety notice.
2. The safety screen presents four acknowledgements. Continue remains disabled until
   the user accepts them.
3. Preferences collect training goal, experience, days per week, session duration,
   and equipment. Bodyweight is always available and cannot be deselected; cardio
   machines are not offered. The workout split is derived from weekly frequency.
4. Injury History groups injuries by body region. Selecting an injury exposes its
   status; affected side is stored but has no MVP control.
5. Movement Limitations separates injury-derived suggestions from the browsable list.
   Suggestions begin unconfirmed, and users may continue with none selected.
6. Profile Review summarizes the answers. Finishing saves the profile and opens Home.

A returning user with a saved profile starts on Home rather than Welcome.

## Home

- Shows the saved profile summary and active limitation count.
- Provides entry points to the exercise library and profile.
- Shows Generate plan when no plan exists and View plan when one is saved.
- Keeps an outdated plan visible after a material profile change and displays a warning.

## Exercise eligibility and library

- Every exercise is classified as Available, Excluded, or Unavailable.
- Confirmed limitation conflicts take safety priority and produce Excluded status.
- Missing equipment or excessive difficulty produces Unavailable status.
- The library supports normalized text search plus muscle-group and equipment filters.
- Rows show exercise metadata and availability; excluded counts use plural-aware copy.
- Empty results and an all-excluded result set have distinct guidance.
- Exercise Details shows the prescription, muscles, movement pattern, equipment, status,
  and every applicable exclusion reason.

## Plan generation

- Generation is deterministic: the same profile produces the same plan and identifiers.
- Weekly frequency selects a stable workout split and day template.
- Only eligible exercises using available equipment are selected.
- Exercises are not duplicated within a workout day.
- If a slot cannot be filled, the app returns a valid partial plan with a visible warning
  rather than reintroducing an excluded exercise or failing.
- The generated plan and its warnings are persisted locally.

## Plan viewing and editing

- The Plan screen groups exercises by workout day and shows sets, repetitions, and rest.
- Exercise actions include Details, Replace, Remove, Move up, and Move down.
- Removing an exercise and regenerating the plan require confirmation.
- Users may remove the final exercise; an empty day remains visible.
- An add row appears only while the day has remaining template capacity.
- Replacement and addition candidates must satisfy current profile filters, avoid day
  duplicates, and fit the day focus and remaining capacity.
- Replacement uses an explicit pending selection and confirmation.
- The exercise picker names the day it is adding to, and supports search and filters,
  clearing them, ordered multi-selection, and capacity limits without discarding
  selections when filters change.
- Expected edit failures appear as recoverable messages and refresh from stored state.

## Profile editing and outdated plans

- Profile shows saved goal, schedule, experience, equipment, injuries, and limitations.
- Preferences, injuries, and limitations reuse their onboarding screens in edit mode.
- Cancel discards the edit draft; intermediate Back preserves it within the edit flow.
- Saving an unchanged profile leaves the plan current.
- A material profile change preserves the existing plan but marks it outdated.
- While outdated, the plan remains visible but editing controls and stale warnings are
  hidden. Regeneration replaces prior manual edits and clears the outdated state only
  after the replacement plan is persisted.

## Persistence and failure behavior

- The profile and workout plan are stored in separate local DataStore files.
- Profile drafts survive process recreation; completed profiles and plans survive app
  close and relaunch.
- Corrupt persisted data resets to a recoverable empty state instead of crashing.
- Loading states are explicit, expected failures use typed results, and transient UI
  errors are shown through one-shot effects.

## Accessibility and presentation

- The app supports light and dark themes with a fixed semantic color palette.
- Interactive controls target at least 48 dp and expose labels or content descriptions.
- User-facing copy is localized through Android resources.
- Dense and stateful layouts are tracked by [tests.spec](tests.spec).
