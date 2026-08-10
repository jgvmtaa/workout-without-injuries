# Phase 3 — Onboarding & Local Profile Persistence

**Goal:** The full first-launch flow that produces a persisted `UserProfile`.

**Depends on:** Phases 1 (shell) and 2 (models/catalogs). **Blocks:** Phases 4–6
(they consume the persisted profile).

**Root README refs:** §3 (flow), §4.1–§4.5 (screens), §20 (UI state/events),
§21 (persistence), §27 Phase 3. Those `README §N` citations point at
[`docs/spec.md`](../docs/spec.md).

---

## 3.1 UI state/event pattern (README §20) — apply to every screen below
- [x] Each screen exposes one immutable `UiState`
- [x] Each screen has a `sealed interface *Event`
- [x] `*Route(viewModel, onContinue, onBack)` collects state; `*Screen(state, onEvent)` is previewable
- [x] No `NavController` inside screen composables

Two deviations, both deliberate:
- **Welcome has no ViewModel or UiState.** It collects nothing and remembers nothing;
  an empty state holder would be the shape of the pattern without its purpose.
- **`Continue` and `Back` are events but never reach the ViewModel.** The route maps
  them to its navigation lambdas. §20's example lists them on the event interface, and
  keeping them there means the screen has one event channel — which is what lets a
  Compose test drive it without a ViewModel — but navigation is not the ViewModel's job.

## 3.2 Welcome screen (README §4.1)
- [x] Copy: what the app does + non-medical disclaimer
- [x] Actions: Get started, Review safety information

## 3.3 Safety notice screen (README §4.2)
- [x] Show the four acknowledgements (not medical advice, stop on pain, follow clinician guidance, filters are general)
- [x] Require explicit acknowledgement to proceed
- [x] Persist `hasAcceptedSafetyNotice: Boolean`

One checkbox covers all four points rather than four separate ones: three of four ticked
is a state the app has no answer for. The flag is persisted on toggle, not on continue,
so navigation never races the write.

## 3.4 Training preferences screen (README §4.3)
- [x] Collect goal, experience, days/week, session duration, equipment, split
- [x] Only expose compatible day/split combinations (2→FullBody, 3→FullBody|PPL, 4→UL, 5→UL+optional)
- [x] MVP may auto-choose split from frequency (README §4.3 note)

The split is derived, not asked — §4.3 permits this — and shown read-only as the
frequency changes, so compatibility is structural rather than enforced. §4.3 and §12.1
disagree on three days (full body *or* PPL vs. PPL); `DetermineWorkoutSplitUseCase`
follows §12.1, which is the version written as code.

`Equipment.CARDIO_MACHINE` is omitted from the screen: the MVP catalog is strength-only,
so offering it would be offering something inert. `Equipment.BODYWEIGHT` is shown fixed
and disabled — always available, never a choice (README §25).

## 3.5 Injury history screen (README §4.4)
- [x] Group injury options by `BodyRegion`
- [x] Toggle `SelectedInjury` set; capture `status` (side ignored by engine in MVP)

Status is only offered for an injury that is selected, and is forgotten when it is
deselected. `affectedSide` is persisted but has no UI yet — the model keeps it so a
later unilateral feature needs no migration.

## 3.6 Movement limitations screen (README §4.5) — core input
- [x] `GetSuggestedLimitationsUseCase`: derive suggestions from selected injuries
- [x] Present suggestions as **unconfirmed**; user adds/removes/adds-manual
- [x] Copy makes clear suggestions are not auto-applied (README §4.5)

Suggestions and confirmations are separate fields in the UI state and neither is derived
from the other, so there is no representation of "suggested, therefore applied".
Suggested limitations appear in their own section and are excluded from the browsable
list below it, so no limitation is two checkboxes that have to stay in step.

## 3.7 Profile review screen (README §3, §4)
- [x] Summarize goal/schedule/experience/equipment/injuries/limitations
- [x] Confirm ⇒ assemble final `UserProfile`

The finish action is the one place in the flow that writes on continue rather than on
change, so the route waits for the ViewModel's finished signal before navigating.

## 3.8 Local persistence (README §21)
- [x] Choose Preferences DataStore (prototype) **or** Proto DataStore (grows into real app)
- [x] `ProfileDataStore` persists: onboarding completion, safety ack, profile, limitations
- [x] `DefaultProfileRepository` reads/writes via DataStore (Flow-based)
- [x] Do **not** introduce Room (README §21 deferral list)

Neither of the two options exactly: a typed `DataStore<PersistedState>` with a
kotlinx-serialization JSON codec. Typed schema and explicit defaults like Proto, without
the protobuf toolchain, using a dependency the project already had. Reads are tolerant —
an enum constant this build does not recognise is dropped rather than thrown — so a
catalog change costs the user a checkbox, not their profile.

Onboarding completion is derived from `profile != null` rather than stored as its own
flag; see docs/follow-ups.md for why, and for when that would need revisiting.

## 3.9 Returning-user routing (README §3)
- [x] On launch: if onboarding complete ⇒ Home, else ⇒ Welcome

`MainViewModel` reads the stored profile once — `NavHost` discards the back stack if
`startDestination` changes, so this must not be a live value.

## 3.10 Tests
- [x] Suggested-limitation test: ACL history suggests jumping/direction-change, **without** mutating the confirmed profile (README §24.3)
- [x] ViewModel tests: selection changes, continue-button state, persistence (README §24.5)

Beyond the two required, and agreed before writing: persistence round-trips including
maximal and unreadable states, label coverage for every domain enum, the
frequency-to-split mapping, and Compose tests for the §24.6 flows (written, not yet run —
they need a device).

---

## Completion criteria (README §27 Phase 3)
- [x] The user can leave and reopen the app without losing the profile.
- [x] Suggested limitations are never confirmed automatically.

Both hold in the code and are covered by tests, but neither has been confirmed on a
device — Gradle cannot run on the machine this was written on. See
[docs/follow-ups.md](../docs/follow-ups.md#pending-verification) before treating the
phase as closed.
