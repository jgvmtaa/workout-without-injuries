# Workout Without Injuries — Application Specification

This document is the source of truth for product requirements and application behavior.
Behavior-changing code and tests must update it in the same change.

## 1. Product objective and scope

Workout Without Injuries creates an editable workout plan from a locally stored user
profile. The profile contains training preferences, available equipment, injury
history, and movement limitations explicitly confirmed by the user.

The application is local-first and deterministic:

- It requires no account, backend, network connection, or cloud synchronization.
- The same profile and catalog produce the same generated plan and identifiers.
- The app stores one profile, one onboarding draft, and one current workout plan.
- The exercise and injury catalogs ship with the application.

## 2. Safety contract

- The app does not diagnose injuries, prescribe rehabilitation, or replace clinical
  advice.
- Injury history only suggests movement limitations. A suggestion never changes
  eligibility until the user explicitly confirms it.
- Eligibility is based on confirmed limitations, available equipment, and experience.
- The app explains each conflict without declaring an exercise universally safe or
  unsafe for a diagnosis.
- An exercise with a confirmed limitation conflict is never generated or offered as a
  replacement or addition.
- Users are instructed to stop exercises that cause pain and follow clinician guidance.

## 3. Application flow and startup

On startup, the app reads the stored profile before creating the navigation graph:

- While the profile is unresolved, it displays the shared loading state.
- Without a valid profile, it starts at Welcome.
- With a valid profile, it starts at Home.
- The start destination is chosen once so later profile writes do not rebuild the
  navigation graph or discard its back stack.

The onboarding route is:

```text
Welcome → Safety notice → Preferences → Injury history
        → Movement limitations → Profile review → Home
```

Completing onboarding removes the onboarding destinations from the back stack. Home
links to the current plan, exercise library, and profile.

## 4. Onboarding

Every onboarding answer is persisted to an `OnboardingDraft` as it changes. Navigating
back or leaving the app therefore preserves the draft. The completed profile is written
only from Profile Review.

### 4.1 Welcome

Welcome explains the app's purpose and safety boundary. Both primary actions open the
Safety Notice; neither mutates stored state.

### 4.2 Safety notice

The screen presents four points:

- The app is not medical advice.
- Users should stop exercises that cause pain.
- Recent injuries, surgery, and clinician restrictions require professional guidance.
- General filters may not match an individual case.

The acknowledgement is stored immediately when toggled. Continue is enabled only while
it is acknowledged.

### 4.3 Training preferences

The required selections are:

| Field | Supported values |
|---|---|
| Goal | General fitness, build muscle, build strength, improve endurance |
| Experience | Beginner, intermediate, advanced |
| Days per week | 2, 3, 4, 5 |
| Session duration | 30, 45, 60, 75 minutes |
| Equipment | Any selectable combination; Bodyweight is implicit |

Bodyweight is always available, is not displayed as a selectable choice, and cannot be
removed. Cardio machines are not offered because the catalog contains strength
exercises. Equipment beyond Bodyweight is optional.

The workout split is derived from weekly frequency and displayed read-only. Continue is
enabled only after goal, experience, days, and duration have values.

### 4.4 Injury history

The static injury catalog groups choices by body region. Selecting an injury records an
`InjuryId` and status; the default status is Historical. Deselecting it also removes its
stored status. Selecting no injuries is valid.

`SelectedInjury` can store Left, Right, Both, or Not Specified as an affected side, but
the UI does not currently collect side and the filtering engine does not use it. Injury
status also does not alter suggestions or eligibility.

### 4.5 Movement limitations

The screen contains two sources of choices:

- Suggestions derived from the selected injuries.
- All remaining limitations, grouped for browsing.

Suggestions start unchecked and are never copied into the confirmed set automatically.
Users may confirm suggested limitations, add unrelated limitations, or continue with
none. A confirmed limitation remains confirmed if the injury that suggested it is later
removed; it moves into the browsable groups instead.

Only the confirmed set is copied into `UserProfile.movementLimitations` and used by the
eligibility engine.

### 4.6 Profile review

Profile Review displays the selected preferences, derived split, equipment, injuries
with statuses, and confirmed limitations in deterministic catalog order. Missing values
are visible and Finish remains disabled until the draft is complete.

Finish derives the split again, converts the complete draft to a `UserProfile`, persists
it, and navigates to Home only after the write succeeds.

## 5. Exercise and injury data

The catalogs are Kotlin data, addressed by stable enum identifiers rather than display
strings. Display names and descriptions are Android resources.

### 5.1 Muscle groups

An exercise has one primary muscle group and zero or more secondary groups. Supported
groups are Chest, Back, Shoulders, Biceps, Triceps, Quadriceps, Hamstrings, Glutes,
Calves, and Core.

### 5.2 Movement patterns

Each exercise has one movement pattern. Templates and replacement ranking use patterns
rather than display names. Patterns cover pushes, pulls, squats, lunges, hinges, knee
flexion, hip movements, elbow movements, shoulder movements, calf raises, core work,
and carries.

### 5.3 Exercise definition and prescription

Each `ExerciseDefinition` contains:

- Stable ID and localized name/description resources.
- Primary and secondary muscles.
- Movement pattern and required equipment.
- Difficulty.
- Conflicting movement limitations.
- Ranking tags such as compound, unilateral, or machine-supported.
- Default set range, repetition range, and rest duration.

Generated and manually added exercises use the first value of the default set range,
the complete repetition range, and the default rest duration. Session duration and goal
do not alter prescriptions or template size.

## 6. Identifiers and resources

- Domain and persisted references use enum names or generated stable IDs, never display
  strings.
- All user-facing copy comes from Android resources.
- Enum labels are resolved in the UI layer; domain logic does not depend on Android
  `Context` or resources.
- Catalog collections and persisted enum-name collections use stable ordering.

## 7. Catalog contract

`ExerciseCatalog`, `InjuryCatalog`, and `MovementLimitationCatalog` are the canonical
content sets. Their tests enforce identifier uniqueness, resource coverage, grouping,
valid prescriptions, injury-to-limitation references, and availability of useful
alternatives.

Every exercise requires at least one equipment value. Bodyweight is represented as
equipment so bodyweight exercises participate in the same eligibility logic.

## 8. Exercise eligibility

Eligibility evaluation returns every applicable reason:

- `ConflictingLimitation` for each overlap between an exercise's conflicts and the
  user's confirmed limitations.
- `MissingEquipment` for each required item absent from the profile.
- `AboveExperienceLevel` when the exercise exceeds the user's supported difficulty.

Experience support is strict:

| User experience | Supported exercise difficulty |
|---|---|
| Beginner | Beginner |
| Intermediate | Beginner and Intermediate |
| Advanced | Beginner, Intermediate, and Advanced |

The result is deterministic and independent of Android UI code.

## 9. Availability categories

Every exercise appears in exactly one category:

- **Available:** no exclusion reasons.
- **Excluded:** at least one confirmed limitation conflict.
- **Unavailable:** no limitation conflict, but equipment is missing or difficulty is
  above the user's level.

Safety takes priority: an exercise with both a limitation conflict and a logistical
reason is Excluded. Excluded and unavailable exercises remain inspectable in the
library, but neither can enter a generated or edited plan.

## 10. User profile

`UserProfile` is an immutable value containing goal, experience, days per week, session
duration, derived split, available equipment, selected injuries, confirmed movement
limitations, and safety acknowledgement.

Collections are sets so selection order cannot affect generation. Onboarding completion
is derived from the existence of a valid profile rather than stored as a separate flag.

## 11. Workout plan

The app stores one `WorkoutPlan` containing stable plan ID, display name, and ordered
days. Each `WorkoutDay` has an ID, name, focus, and ordered planned exercises. A planned
exercise references an `ExerciseId` and stores sets, repetition range, rest duration,
and order; it does not duplicate the catalog definition.

Supported day focuses are Full Body, Upper Body, Lower Body, Push, Pull, and Legs.

## 12. Plan generation

Generation is deterministic and produces a valid plan even when some template slots
cannot be filled. Opening Plan automatically generates and persists a plan when a
profile exists but no plan is stored. An unexpected generation failure leaves the
existing state intact and shows a recoverable message.

### 12.1 Split and weekly schedule

| Days | Split | Day sequence |
|---:|---|---|
| 2 | Full Body | Full Body, Full Body |
| 3 | Push/Pull/Legs | Push, Pull, Legs |
| 4 | Upper/Lower | Upper, Lower, Upper, Lower |
| 5 | Upper/Lower | Upper, Lower, Upper, Lower, Full Body |

Repeated focuses receive occurrence numbers in their display names. Plan and day IDs
are derived from the frequency, split, day index, and focus; no random or time-based
value is used.

### 12.2 Candidate filtering

Only Available exercises are candidates. The generator therefore rejects limitation
conflicts, missing equipment, and unsupported difficulty before filling slots.

### 12.3 Candidate grouping

Each template slot accepts a defined set of movement patterns. Candidates must match one
of those patterns and must not duplicate another exercise in the same workout day.

The generator prefers exercises not already used elsewhere in the weekly plan. It may
reuse one across different days only when all globally unused alternatives for the slot
are exhausted.

### 12.4 Day templates

| Focus | Ordered slots |
|---|---|
| Full Body | Knee-dominant; hinge/hip extension; push; pull; secondary upper; core |
| Upper | Horizontal push; vertical push; horizontal pull; vertical pull; shoulder isolation; biceps; triceps |
| Lower / Legs | Knee-dominant; hinge; glute; hamstring isolation; calf; core |
| Push | Horizontal push; vertical push; shoulder isolation; triceps; secondary push |
| Pull | Horizontal pull; vertical pull; rear-delt/external rotation; biceps; secondary pull |

An unfillable slot produces a persisted `PlanWarning` identifying its day and slot. It
does not cause generation to fail or reintroduce an ineligible exercise.

### 12.5 Ranking and determinism

Candidates receive the following score:

- `+5` for matching the slot, which every candidate does.
- Experience preference: `+2` for the user's exact level; Advanced users receive `+1`
  for Intermediate exercises.
- `+2` for machine-supported or chest-supported exercises when the profile avoids
  unilateral balance demand or single-leg loading.
- `+1` for a compound exercise in either of the first two slots.
- `-2` when its movement pattern is already used in that day.
- `-3` when it would be the second unilateral exercise in that day.

Ties use `ExerciseId.name`. Eligible inputs, candidate order, identifiers, day names,
and output are stable for the same profile and catalog.

## 13. Plan and profile editing

All plan mutations are atomic and revalidate against the latest stored profile.

- **Remove:** requires confirmation, may leave a workout day empty, and normalizes the
  remaining order values.
- **Replace:** excludes the current exercise and day duplicates. Candidates rank by
  same pattern plus primary muscle, same pattern, same primary muscle, then all other
  eligible exercises; ties use stable ID order. Confirmation applies the catalog's
  default prescription at the existing position.
- **Move:** moves one exercise to a valid position and normalizes order values. The first
  and last rows disable impossible directions.
- **Add:** allows an ordered multi-selection of eligible, day-compatible, nonduplicate
  exercises up to the template's remaining capacity. Clearing filters does not clear
  pending selections.
- **Regenerate:** requires confirmation because it replaces manual edits.

Expected failures—missing or outdated plan, missing day or exercise, invalid position,
ineligible or duplicate exercise, and exceeded capacity—produce typed results and
recoverable UI messages rather than exceptions. Replacement and picker routes navigate
back when their route arguments no longer identify editable plan state.

Profile editing reuses the onboarding preference, injury, limitation, and review
screens. Entering an edit flow copies the profile into the draft. Cancel discards draft
changes and returns to Profile; Back preserves intermediate changes within the flow.
Saving an unchanged profile leaves the plan current. Any material profile change marks
an existing plan outdated before saving the profile.

## 14. Exercise library

The library:

- Shows all three availability categories and their counts.
- Filters by primary muscle and required equipment.
- Searches normalized exercise IDs, primary-muscle IDs, and movement-pattern IDs.
- Preserves filters until explicitly cleared.
- Distinguishes an empty filtered result from a nonempty result in which every exercise
  is Excluded.
- Opens details for any listed exercise, including Excluded and Unavailable items.

## 15. Exercise details

Details display the localized name and description, primary and secondary muscles,
movement pattern, required equipment, default prescription, availability category, and
every applicable reason. Without a profile, the exercise information remains visible
and the availability card asks the user to complete onboarding.

## 16. Home and profile

Home displays the saved profile summary, active limitation count, links to the library
and profile, and one plan action:

- Generate plan when no plan is stored.
- View plan when a plan exists.

A material profile change keeps the current plan visible and marks it outdated. The
Profile screen displays preferences, equipment, injuries with statuses, and confirmed
limitations, including explicit empty states.

## 17. Navigation

Navigation uses serializable typed routes. Exercise details carry `ExerciseId`;
replacement carries workout-day ID plus exercise ID; the picker carries workout-day ID.
Feature screens receive callbacks rather than a navigation controller.

The Plan, Replacement, and Picker screens validate route state against stored data.
Stale replacement or picker routes return to the previous destination.

## 18. Architecture

The app is a single-activity Compose application using unidirectional data flow:

```text
Compose screen → event → ViewModel → use case → repository → local data source
```

- Screens render immutable UI state and emit events.
- ViewModels combine repository flows and expose one-shot navigation/error effects.
- Domain models and eligibility/generation logic do not depend on Android UI APIs.
- Repositories isolate static catalogs and persistence.
- Hilt supplies repositories, stores, use cases, and ViewModels.

## 19. Project structure and shared UI

The app uses one Gradle application module organized into `core`, `data`, `domain`,
`feature`, `navigation`, and `di` packages. Shared UI provides the app scaffold, top bar,
loading and empty states, selection controls, enum labels, dimensions, typography, and
theme.

Business rules belong in domain use cases or repositories, not composables. Static
catalogs remain Kotlin data until content management requires a different source.

## 20. UI state and events

Each feature exposes a route composable, a stateless screen composable, an immutable UI
state model, and typed events. Route composables collect lifecycle-aware state and own
navigation/effect handling. Screen composables remain previewable and testable without a
navigation controller or repository.

Loading, empty, error, dialog, menu, and selection states must be explicit. Timed state
must not be required to reproduce a stable visual state.

## 21. Persistence

Typed DataStore stores JSON in two private files:

- `profile.json`: schema version, onboarding draft, and completed profile.
- `workout_plan.json`: current plan, generation warnings, and outdated flag.

Draft changes are persisted immediately. A completed profile and generated or edited
plan survive process recreation and relaunch. The plan store contains at most one plan;
there is no workout history.

Persistence mappings store enum names and sort collections for deterministic output.
Unknown optional enum values are dropped on read. Unknown exercise IDs are dropped from
stored plans, and days with unknown focuses are dropped. If a required profile field
cannot be parsed, the profile is treated as absent and onboarding resumes with the draft.
Bodyweight is restored into every loaded draft and profile.

Corrupt files reset to an empty recoverable state. Schema versions must be advanced and
migrations added before making incompatible persisted-model changes.

## 22. Dependency injection

Hilt provides DataStore instances, repositories, use cases, and ViewModels. Profile and
workout-plan DataStores are separate singleton dependencies. Repository interfaces are
bound to their local implementations.

## 23. Dependencies and toolchain

Gradle build files and the version catalog are the source of truth for plugins,
libraries, Android SDK levels, and language targets. See
[`toolchain.md`](toolchain.md) for setup, build commands, CI, and troubleshooting.

## 24. Testing contract

Tests should emphasize deterministic domain behavior and state transitions. Unit tests
must not depend on catalog iteration accidents, wall-clock time, or network access.

### 24.1 Eligibility tests

Cover every exclusion reason, the experience matrix, multiple simultaneous reasons,
and the priority of limitation conflicts over unavailable reasons.

### 24.2 Equipment tests

Cover Bodyweight invariants, missing-equipment reasons, selectable equipment, and
equipment filtering.

### 24.3 Suggested-limitation tests

Verify injury mappings, deduplication across injuries, and the invariant that suggestions
do not mutate the confirmed limitation set.

### 24.4 Generation and editing tests

Verify eligibility, template compatibility, capacity, duplicate prevention, stable
ranking and identifiers, partial-plan warnings, atomic edits, outdated-plan rejection,
and persistence of generated and manually edited plans.

### 24.5 ViewModel tests

Verify validation gates, persisted selection changes, filter behavior, ordered pending
selection, cancellation, save ordering, regeneration, typed failures, and one-shot
effects.

### 24.6 UI tests

Instrumented tests cover critical navigation and interaction flows. Screenshot tests
cover every stable visual branch across light and dark themes, supported font scales,
right-to-left layout, and all required scroll positions.

## 25. Empty, stale, and failure states

- No injuries and no confirmed limitations are valid onboarding choices.
- No eligible candidate for a template slot creates a warning and a partial plan.
- An all-excluded library result shows safety guidance rather than a generic empty state.
- A workout day remains visible when its final exercise is removed.
- Add controls appear only while a day has remaining template capacity.
- A profile change preserves the plan but marks it outdated. While outdated, plan edits,
  add controls, and stored partial-plan warnings are hidden until regeneration succeeds.
- Generation failures and rejected edits show recoverable messages.
- A missing profile, plan, route target, or catalog entry produces a defensive empty,
  loading, or back-navigation path rather than a crash.

## 26. Catalog quality

The catalog must provide meaningful variation across muscles, movement patterns,
equipment, and difficulty. Each day-template slot should have alternatives where the
available catalog permits. Catalog tests enforce structural validity and intentional
coverage boundaries.

Movement limitations that do not match any catalog exercise remain selectable: they are
valid user constraints even when the current strength catalog has no relevant movement.

## 27. Accessibility and presentation

- User-facing copy is localized through Android resources.
- Counted copy uses plural-aware resources. Singular and plural forms are distinct
  user-visible states, not a formatting detail.
- Interactive controls target at least 48 dp and expose text labels or content
  descriptions.
- The app supports light and dark themes using a fixed semantic color palette.
- Layouts must support large text, scrolling, and right-to-left mirroring.
- Destructive actions that would discard an exercise or manual plan edits require
  confirmation.

## 28. Completion criteria

The application contract is satisfied when a user can complete onboarding, generate a
deterministic eligible plan, understand exclusions, browse the catalog, edit the plan,
edit the profile with correct invalidation behavior, and recover the profile and plan
after relaunch without violating the safety contract.

Automated coverage must pass at the unit, lint, build, and instrumented levels. Visual
coverage must include every stable rendering branch and supported display variant.

## 29. Out-of-scope features

The application does not provide accounts, cloud synchronization, a backend, workout
history or execution logging, per-set tracking, progressive-overload calculations, pain
tracking, recovery questionnaires, exercise videos, user-created exercises, social
features, wearable integration, nutrition planning, clinician tools, subscriptions, or
AI-generated plans.

Any future natural-language input may propose structured profile values for explicit
user review, but it must not diagnose a condition, directly generate a workout, or
bypass deterministic eligibility rules.
