# UI Screenshot Test Specification

## Purpose

Capture every stable, visually distinct state required by
[`docs/spec.md`](docs/spec.md), the source of truth for application behavior. Update
this file in the same change whenever that behavior adds or changes a visual state.

This specification is complete when every rendering branch in a screen composable is
represented by at least one case below. A case may cover several independent branches.
Combinations that render no new pixels do not require separate captures.

## Harness

Screenshots are byte-compared, so every input that can move a pixel is pinned here. Two
machines that follow this section must produce identical PNGs; if they do not, the
difference is a harness bug, not a UI change.

- Renderer: Roborazzi over Robolectric native graphics, run as a JVM unit test. No
  emulator — an emulator's GPU, system UI, and font stack vary per host and cannot be
  pinned. (Not yet in the build: adding the Roborazzi plugin and the Robolectric
  dependency is prerequisite work for this spec.)
- SDK: `@Config(sdk = [35])`, matching `compileSdk`. Not `minSdk` 24 — the baseline
  tracks what the app is compiled and shipped against.
- Device qualifier: `en-rUS-w360dp-h800dp-notlong-port-notnight-xhdpi`, giving a
  720 x 1600 px surface at density 2.0. Qualifier order is fixed by the platform and
  verified with `aapt2`; locale leads and density trails. The locale and night segments
  are replaced per variant; nothing else varies.
- Theme is driven by the qualifier, not by `isSystemInDarkTheme()` being stubbed —
  `AppTheme` reads the configuration, and the palette is fixed (no Material You), so
  theme is the only colour input.
- Comparison: `changeThreshold = 0.001` (0.1% of pixels). Exact equality is too brittle
  for text antialiasing across JDK builds; anything larger hides a shifted row.
- Baselines live in `app/src/test/screenshots/`. Failure diffs are written to
  `build/outputs/roborazzi/` and are not committed.
- File name: `<case>[-<scroll>]-<variant>.png`, e.g. `SC-016-plan-complete-top-dark-1_0.png`.

## Capture contract

- Run every case with stable fixture data, a fixed clock, animations disabled, and
  deterministic list ordering.
- Drive the stateless screen composable directly with a fixture `UiState`. Do not
  construct a ViewModel: the ViewModels read repositories and their emission timing is
  not part of what these images assert.
- Capture the complete app window consistently, including app bars and app-owned bottom
  bars, while excluding cursors, focus rings, the keyboard, and transient system UI
  unless the case explicitly requires them.
- Wait for Compose to become idle before comparison. Loading and generating cases use a
  deliberately held state instead of timing a real operation.
- Overlays are held open, not timed. Dialogs (`showRegenerateConfirm`, `pendingRemoval`)
  and snackbars (`SnackbarHostState`) are hoisted into the screen's parameters, so a
  fixture opens them. The one exception is the plan row overflow menu: its expanded
  state is remembered inside `PlannedExerciseRow`, so SC-018 and SC-048 through SC-050
  must click the row's more-actions button and await idle before capturing. Any change
  that hoists that state should delete this paragraph.
- Prefer the longest real labels and representative maximum list sizes that the product
  permits so wrapping, clipping, and scrolling are exercised deterministically. Where a
  case names a specific string as "longest", it is the longest in `values/strings.xml`
  for that group — verify it still is when copy changes.

### Scroll positions

**Scroll requirement is a property of (case, variant), not of the case.** Larger text
grows content without growing the viewport, so a case that fits in one frame at 1.0x can
need two at 1.3x and four at 2.0x. Assigning offsets per case would silently drop the
bottom of every dense screen at exactly the scale where clipping is most likely.

The harness computes the requirement instead of trusting a tag:

1. For each (case, variant), lay out the screen and measure the scrollable content
   height `H` against the viewport height `V`.
2. Required frames `N = ceil(H / (V - 64dp))`, the overlap keeping a row from falling
   between two frames. `N = 1` means one unsuffixed image.
3. Capture at offsets `0, (V - 64dp), 2(V - 64dp), …`, clamping the last to the content
   end so the final frame is flush with the bottom.
4. Assert that the committed baseline set for that (case, variant) is exactly the `N`
   images this produced — no more, no fewer. A missing or orphaned image fails the run.

Suffixes: `-top` and `-bottom` when `N = 2`; `-top`, `-middle`, `-bottom` when `N = 3`;
`-p1` … `-pN` when `N > 3` — large text on the onboarding lists, and the full
injury and limitation catalogs even at baseline.

The `[tall]`, `[tall+]`, and `[tall++]` tags below therefore document the expected
`N` **at the baseline variant only** (2, 3, and 4 or more respectively; untagged
means 1). They are a review aid
— they let a reader see at a glance which cases are long — not the enforcement
mechanism. Step 4 is the enforcement mechanism. Two consequences:

- `N` is monotonic in font scale: a variant may add frames, never remove them. A run
  that measures fewer frames at 1.3x than at 1.0x is a harness bug.
- When a baseline tag and the measured `N` disagree, the measurement is right; correct
  the tag in that change.

The catalogue sizes that drive most of these: 59 exercises, 32 injuries, 27 movement
limitations, 10 muscle groups, 8 filterable equipment types.

## Visual-variant matrix

Theme and font scale are independent inputs, so capturing their full cross product buys
almost nothing: a 1.3x layout shift is the same shift in dark, and the previous
all-four-combinations rule contradicted this file's own "no new pixels" clause while
costing well over 200 images. The budget is spent instead on two axes that were missing
and that screenshots are uniquely good at — mirroring and large text.

| Tier | Variant suffix | Applies to |
| --- | --- | --- |
| Baseline | `-light-1_0` | every case |
| Dark | `-dark-1_0` | every case |
| Large text | `-light-1_3` | cases tagged `[dense]` or `[dense+]` |
| Largest text | `-light-2_0` | cases tagged `[dense+]` |
| Mirrored | `-light-1_0-rtl` | cases tagged `[rtl]` |

- `[dense]` marks a layout where 1.3x text is expected to rewrap, clip, or push a
  control off its row.
- `[dense+]` adds 2.0x, Android's real maximum. The app is not translated, so 2.0x is
  the only way the longest-line failures show up.
- `[rtl]` runs the `ar-rXB` pseudolocale, which mirrors layout while keeping the strings
  legible. Mirroring is a layout property that the resource tests cannot reach; the
  targets are the asymmetric rows — `Icons.AutoMirrored` back arrows, the plan row's
  index/weight/overflow arrangement, `SummaryRow`'s `TextAlign.End`, and the
  text-plus-trailing-control rows in the library, picker, and replacement lists.

A case is complete when, for every variant its tags require, the committed baselines
match the frame count the harness measures for that variant (see Scroll positions,
step 4). Because the count is measured per variant rather than declared per case, adding
a row to a dense screen fails the run at 2.0x even when 1.0x still fits.

## Screenshot cases

### Onboarding

- [x] `SC-001-welcome` — Welcome headline, disclaimer, three setup steps, Get started,
  and Review safety actions. No back affordance.
- [x] `SC-002-safety-unacknowledged` — Four acknowledgements, unchecked confirmation,
  and disabled Accept safety action. `[dense]`
- [x] `SC-003-safety-acknowledged` — Checked confirmation and enabled Accept safety
  action.
- [x] `SC-004-preferences-incomplete` — Required preference fields unanswered, no
  derived-split card, fixed disabled Bodyweight row, and Continue disabled.
  `[tall+]` `[dense+]`
- [x] `SC-005-preferences-complete` — Goal, experience, schedule, duration, selected
  equipment, fixed Bodyweight, derived split, and Continue enabled.
  `[tall+]` `[dense+]` `[rtl]`
- [x] `SC-006-injuries-none-selected` — Grouped injury list with no selected-count label
  or status controls. `[tall++]`
- [x] `SC-007-injuries-selected` — Non-zero selected count and the selected injury's
  status control. `[tall++]` `[dense]`
- [x] `SC-008-limitations-no-suggestions` — No-suggestions card, browsable limitations,
  no selected-count label, and enabled continuation. `[tall++]`
- [x] `SC-009-limitations-unconfirmed` — Injury-derived suggestions shown separately,
  all unchecked, and no selected-count label. `[tall++]` `[dense]`
- [x] `SC-010-limitations-confirmed` — Confirmed suggested and manually browsed
  limitations, with the non-zero selected-count label. `[tall++]`
- [x] `SC-011-profile-review` — Complete populated profile, including non-empty injuries
  and limitations, with Finish setup enabled. `[tall]` `[dense]` `[rtl]`
- [x] `SC-044-profile-review-minimal` — Complete profile with Bodyweight only, no
  injuries, and no limitations; both empty-section messages are visible. `[tall]`
- [x] `SC-045-profile-review-incomplete` — Missing-value labels, incomplete-profile error,
  and disabled Finish setup action. `[tall]` `[dense]`

### Home

- [ ] `SC-012-home-no-plan` — Complete profile summary, zero-limitations copy, library
  and profile entry points, and Generate plan action. `[dense]`
- [ ] `SC-013-home-current-plan` — Saved plan name, non-zero limitation count, complete
  profile summary, and View plan action. `[dense]`
- [ ] `SC-014-home-outdated-plan` — Saved plan remains visible with the profile-changed
  warning and View plan action. `[tall]` `[dense]`
- [ ] `SC-046-home-profile-unavailable` — Defensive state produced when Home observes no
  saved profile: missing profile-summary value, zero-limitations copy, and no-plan action.

### Plan

- [ ] `SC-015-plan-not-generated` — Empty plan state with Plan fallback title, no
  Regenerate action in the app bar, and Generate plan action. `[dense]`
- [ ] `SC-016-plan-complete` — Generated workout days, exercise prescriptions, dividers,
  and a full-capacity day with no add row. `[tall+]` `[dense+]` `[rtl]`
- [ ] `SC-017-plan-partial` — Partial plan with multiple localized no-match warnings and
  valid exercises retained below them. `[tall]` `[dense+]`
- [ ] `SC-018-plan-menu-middle-row` — Middle-row menu with Details, Replace, Move up,
  Move down, and Remove all enabled. `[dense]`
- [ ] `SC-019-plan-add-one-slot` — Editable workout day with one remaining slot and the
  singular add-exercise label. `[dense]`
- [ ] `SC-020-plan-empty-day` — Manually emptied editable day showing "No exercises in
  this workout" and its add-exercise row.
- [ ] `SC-021-plan-outdated` — Outdated warning with exercise menus, add rows, and stored
  partial-plan warnings all hidden. `[tall]` `[dense]`
- [ ] `SC-022-plan-remove-dialog` — Remove-exercise confirmation dialog over a populated
  plan. `[dense]`
- [ ] `SC-023-plan-regenerate-dialog` — Regeneration dialog warning that manual edits
  will be lost. `[dense+]`
- [ ] `SC-047-plan-generating` — Held generation state with "Generating your plan…" rather
  than the generic loading message.
- [ ] `SC-048-plan-menu-first-row` — First-row menu with Move up disabled and Move down
  enabled.
- [ ] `SC-049-plan-menu-last-row` — Last-row menu with Move up enabled and Move down
  disabled.
- [ ] `SC-050-plan-menu-only-row` — Single-row menu with both movement actions disabled.
- [ ] `SC-051-plan-add-multiple-slots` — Editable workout day with at least two remaining
  slots and the plural add-exercise label. `[dense]`

### Exercise library and details

- [ ] `SC-024-library-default` — Empty search with no clear icon, no Clear filters action,
  availability counts, unselected filters, row subtitles, and Available, Excluded, and
  Unavailable row treatments. The excluded fixture has one conflicting limitation.
  `[tall+]` `[dense+]` `[rtl]`
- [ ] `SC-025-library-filtered` — Active search with clear icon, selected muscle and
  equipment filters, Clear filters action, and matching results. `[tall]` `[dense]`
- [ ] `SC-026-library-empty-search` — No-results content for the active query and filters.
  `[dense]`
- [ ] `SC-027-library-all-excluded` — Non-empty result set where every row is excluded,
  including safety guidance and a row excluded by multiple limitations to exercise
  plural copy. `[tall]` `[dense+]`
- [ ] `SC-028-details-available` — Full exercise information, multiple secondary-muscle
  chips, equipment, prescription, and Available card. `[tall]` `[dense]` `[rtl]`
- [ ] `SC-029-details-excluded` — Exercise with no secondary muscles and an Excluded card
  listing multiple conflicting limitations *and* a missing-equipment reason. The mixed
  fixture is required: `availability()` promotes any limitation conflict to Excluded, so
  the equipment wording renders inside the error-container card and nowhere else.
  `[tall]` `[dense+]`
- [ ] `SC-030-details-unavailable` — Unavailable card containing both missing-equipment
  and above-experience reasons, including the user's current level. `[tall]` `[dense+]`
- [ ] `SC-052-details-profile-required` — Exercise information remains visible when no
  profile eligibility is available, with the onboarding-required availability card.
  `[tall]`

### Plan editing

- [ ] `SC-031-replacement-unselected` — Eligible replacement candidates with no selected
  styling and confirmation disabled. `[dense]` `[rtl]`
- [ ] `SC-032-replacement-selected` — One pending replacement with selected styling and
  confirmation enabled.
- [ ] `SC-033-replacement-empty` — No eligible candidates message and no confirmation
  bottom bar. `[dense]`
- [ ] `SC-034-picker-default` — Day-scoped title, zero selected, disabled plain-label
  confirmation, empty search with no clear icon, no Clear filters action, unselected
  filters, capacity summary, and enabled candidate rows. `[tall]` `[dense+]` `[rtl]`
- [ ] `SC-035-picker-selected` — Active search with clear icon and selected filters, the
  Clear filters action, ordered selections, selected-row styling, and confirmation count.
  `[tall]` `[dense+]` — at 2.0x the capacity summary and Clear filters share one row and
  are the likeliest pair in the app to collide.
- [ ] `SC-036-picker-empty` — No candidate rows for the active search and filters, with
  the Clear filters action and the rest of the picker still visible. `[dense]`
- [ ] `SC-053-picker-capacity-reached` — Selection count equals day capacity; selected
  rows remain enabled and every unselected row is visibly disabled. `[tall]` `[dense]`

### Profile and edit mode

- [ ] `SC-037-profile-summary` — Saved goal, schedule, experience, split, multiple pieces
  of equipment, injuries, and limitations with all Edit actions. `[tall]` `[dense]` `[rtl]`
- [ ] `SC-038-edit-preferences` — Complete Preferences state in edit mode with Cancel
  visible. `[tall+]` `[dense]`
- [ ] `SC-039-edit-injuries` — Selected Injury History state in edit mode with Cancel
  visible. `[tall+]`
- [ ] `SC-040-edit-limitations` — Suggested and confirmed Movement Limitations state in
  edit mode with Cancel visible. `[tall+]`
- [ ] `SC-041-edit-review` — Complete populated Profile Review in edit mode with Cancel
  and Save changes actions. `[tall]` `[dense]`
- [ ] `SC-054-profile-minimal` — Valid Bodyweight-only profile with no injuries or
  limitations, showing both empty-section messages and all Edit actions. `[dense]`
- [ ] `SC-055-profile-unavailable` — Defensive no-profile state with preference values and
  equipment omitted, empty injury and limitation messages, and Edit actions retained.

### Shared and transient visual states

- [x] `SC-042-loading` — Generic application loading component. It represents every
  screen that renders the same `LoadingContent` with the default message.
- [ ] `SC-043-plan-error-snackbar` — "Selected exercise is not eligible for your current
  profile." over a populated plan, verifying snackbar wrapping and bottom placement. This
  is the longest of the five `plan_edit_failed_*` strings; the outdated-plan message is
  shorter and does not test wrapping. `[dense+]`
- [ ] `SC-056-replacement-error-snackbar` — The same longest edit-failure message on
  Replacement with its confirmation bottom bar present. `[dense+]`
- [ ] `SC-057-picker-error-snackbar` — The same longest edit-failure message on the
  Picker with its confirmation bottom bar present. `[dense+]`
- [ ] `SC-058-plan-generation-error-snackbar` — "Could not generate the plan. Check your
  profile and try again." over the empty-plan body. The longest snackbar string in the
  app, and the only one that appears over centred empty content rather than a list.
  `[dense+]`

## States intentionally excluded from screenshots

These states either leave the screen before a stable frame exists, render pixels that
are already covered, or cannot be reached at all:

- Stale replacement and picker routes immediately navigate back; verify them with
  navigation/effect tests.
- A null exercise definition renders the same generic loading component as `SC-042`.
- A planned exercise without a catalog definition is unreachable while `ExerciseId` and
  the exhaustive local catalog remain in sync; catalog completeness belongs to a unit
  test.
- Eligibility with no current experience level is an inconsistent internal state; the
  ViewModel emits both values together. The screen still handles it rather than claiming
  Beginner, but there is nothing to capture.
- Start-destination selection changes routing but uses the same generic loading component.
- An exercise with no required equipment would render an empty Equipment section, but
  every catalog entry lists at least Bodyweight; this is a catalog invariant, not a case.
- The Unavailable availability card can never list a limitation conflict, because
  `availability()` promotes any conflict to Excluded. The branch was removed rather than
  given a case.
- Home has no plan-name fallback state: `hasPlan` is derived from `planName`, so the two
  cannot disagree.
- The plan's empty state has no disabled Generate button: the generating state replaces
  that whole body, so the button is never on screen mid-generation.

## Covered outside screenshot tests

Use interaction, navigation, ViewModel, repository, persistence, resource, or semantic
tests for behavior that a static image cannot prove:

- Navigation, back-stack behavior, and stale-route auto-navigation.
- Process-death persistence and returning-user routing.
- Control callbacks, disabled-control behavior, and selection retention while filters
  change — including that clearing the picker's filters keeps the pending selection.
- Eligibility, ordering, duplicate prevention, capacity enforcement, and atomic-edit
  invariants.
- Save ordering, cancellation, generation failures, and outdated-plan transitions.
- Mapping every typed edit failure to its exact localized snackbar copy; screenshots
  cover each distinct snackbar host layout using the longest message.
- Touch-target dimensions, content descriptions, accessibility semantics, and screen
  reader behavior.
- Resource localization and plural selection; screenshots cover layout-sensitive
  singular, plural, and longest-copy representatives in `en-US`, plus mirroring in the
  `ar-rXB` pseudolocale.

## Maintenance rule

When a composable adds or changes a conditional rendering branch, overlay, empty state,
or supported visual configuration, update this file in the same change. The change is
complete only when the branch is mapped to an existing case, assigned a new case, or
listed above as unreachable with the reason it cannot be built.

When copy changes, re-check the strings this file names as longest: `SC-043`, `SC-056`,
`SC-057`, and `SC-058` are only meaningful while their message really is the longest in
its group.
