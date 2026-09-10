# UI Screenshot Test Specification

## Purpose

Capture the visually distinct states of the implemented Workout Planner UI. The
application code is the source of truth when historical task notes disagree with
the implementation.

## Baseline capture configuration

- Android phone viewport in portrait orientation.
- Light theme and default font scale for every case below.
- Stable fixture data, locale, clock, and animation state.
- No cursor, focus ring, keyboard, or transient system UI unless required by the case.

## Screenshot cases

### Onboarding

- [ ] `SC-001-welcome` — Welcome copy, disclaimer, Get started, and safety-review actions.
- [ ] `SC-002-safety-unacknowledged` — Four acknowledgements, unchecked confirmation, disabled continue action.
- [ ] `SC-003-safety-acknowledged` — Checked confirmation and enabled continue action.
- [ ] `SC-004-preferences-incomplete` — Required preference fields unanswered and continue disabled.
- [ ] `SC-005-preferences-complete` — Goal, experience, schedule, duration, equipment, fixed Bodyweight, and derived split.
- [ ] `SC-006-injuries-none-selected` — Grouped injury list with no status controls.
- [ ] `SC-007-injuries-selected` — Selected injury with its status control visible.
- [ ] `SC-008-limitations-no-suggestions` — No injury-derived suggestions and continuation available.
- [ ] `SC-009-limitations-unconfirmed` — Suggested limitations shown separately and unchecked.
- [ ] `SC-010-limitations-confirmed` — Confirmed suggestion plus the remaining browsable limitations.
- [ ] `SC-011-profile-review` — Completed profile summary and finish action.

### Home

- [ ] `SC-012-home-no-plan` — Profile summary, limitation count, library entry, and Generate plan action.
- [ ] `SC-013-home-current-plan` — Current plan summary and View plan action.
- [ ] `SC-014-home-outdated-plan` — Existing plan with the profile-changed warning.

### Plan

- [ ] `SC-015-plan-not-generated` — Empty plan state with Generate plan action.
- [ ] `SC-016-plan-complete` — Generated workout days with exercise prescriptions.
- [ ] `SC-017-plan-partial` — Partial plan with localized no-match warnings.
- [ ] `SC-018-plan-overflow-menu` — Exercise actions: Details, Replace, Remove, Move up, and Move down.
- [ ] `SC-019-plan-add-capacity` — A workout day with its add-exercise row visible.
- [ ] `SC-020-plan-empty-day` — A manually emptied day showing “No exercises in this workout”.
- [ ] `SC-021-plan-outdated` — Outdated warning with edit menus, add rows, and stale warnings hidden.
- [ ] `SC-022-plan-remove-dialog` — Remove-exercise confirmation dialog.
- [ ] `SC-023-plan-regenerate-dialog` — Regeneration warning that manual edits will be lost.

### Exercise library and details

- [ ] `SC-024-library-default` — Availability counts, filters, search, row subtitles, and availability badges.
- [ ] `SC-025-library-filtered` — Active search, muscle-group filter, and equipment filter.
- [ ] `SC-026-library-empty-search` — No results for the current search and filters.
- [ ] `SC-027-library-all-excluded` — All results excluded with safety guidance.
- [ ] `SC-028-details-available` — Exercise information and Available status.
- [ ] `SC-029-details-excluded` — Excluded status with conflicting-limitation reasons.
- [ ] `SC-030-details-unavailable` — Unavailable status with equipment or experience reasons.

### Plan editing

- [ ] `SC-031-replacement-unselected` — Eligible replacement candidates with confirmation disabled.
- [ ] `SC-032-replacement-selected` — Pending replacement selection with confirmation enabled.
- [ ] `SC-033-replacement-empty` — No eligible replacement candidates.
- [ ] `SC-034-picker-default` — Exercise picker with search and filter controls.
- [ ] `SC-035-picker-selected` — Filtered, ordered selection near the remaining day capacity.
- [ ] `SC-036-picker-empty` — No picker results for the active filters.

### Profile and edit mode

- [ ] `SC-037-profile-summary` — Saved goal, schedule, experience, equipment, injuries, and limitations with Edit actions.
- [ ] `SC-038-edit-preferences` — Preferences in edit mode with Cancel visible.
- [ ] `SC-039-edit-injuries` — Injury history in edit mode with Cancel visible.
- [ ] `SC-040-edit-limitations` — Movement limitations in edit mode with Cancel visible.
- [ ] `SC-041-edit-review` — Profile review in edit mode with Cancel and Save actions.

### Shared states

- [ ] `SC-042-loading` — Representative application loading state.
- [ ] `SC-043-recoverable-error` — Representative recoverable-error snackbar over its owning screen.

## Visual-variant coverage

In addition to the baseline captures, capture Preferences, Movement Limitations,
Plan, Exercise Library, Exercise Details, Exercise Picker, and Profile Review in:

- Dark theme at the default font scale.
- Light theme at 1.3x font scale.

## Covered outside screenshot tests

Use interaction, navigation, ViewModel, repository, or persistence tests for:

- Navigation and back-stack behavior.
- Process-death persistence and returning-user routing.
- Selection retention while filters change.
- Eligibility, ordering, capacity, and atomic-edit invariants.
- Save ordering and outdated-plan state transitions.
- Touch-target dimensions, content descriptions, and accessibility semantics.

