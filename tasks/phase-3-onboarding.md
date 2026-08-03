# Phase 3 — Onboarding & Local Profile Persistence

**Goal:** The full first-launch flow that produces a persisted `UserProfile`.

**Depends on:** Phases 1 (shell) and 2 (models/catalogs). **Blocks:** Phases 4–6
(they consume the persisted profile).

**Root README refs:** §3 (flow), §4.1–§4.5 (screens), §20 (UI state/events),
§21 (persistence), §27 Phase 3.

---

## 3.1 UI state/event pattern (README §20) — apply to every screen below
- [ ] Each screen exposes one immutable `UiState`
- [ ] Each screen has a `sealed interface *Event`
- [ ] `*Route(viewModel, onContinue, onBack)` collects state; `*Screen(state, onEvent)` is previewable
- [ ] No `NavController` inside screen composables

## 3.2 Welcome screen (README §4.1)
- [ ] Copy: what the app does + non-medical disclaimer
- [ ] Actions: Get started, Review safety information

## 3.3 Safety notice screen (README §4.2)
- [ ] Show the four acknowledgements (not medical advice, stop on pain, follow clinician guidance, filters are general)
- [ ] Require explicit acknowledgement to proceed
- [ ] Persist `hasAcceptedSafetyNotice: Boolean`

## 3.4 Training preferences screen (README §4.3)
- [ ] Collect goal, experience, days/week, session duration, equipment, split
- [ ] Only expose compatible day/split combinations (2→FullBody, 3→FullBody|PPL, 4→UL, 5→UL+optional)
- [ ] MVP may auto-choose split from frequency (README §4.3 note)

## 3.5 Injury history screen (README §4.4)
- [ ] Group injury options by `BodyRegion`
- [ ] Toggle `SelectedInjury` set; capture `status` (side ignored by engine in MVP)

## 3.6 Movement limitations screen (README §4.5) — core input
- [ ] `GetSuggestedLimitationsUseCase`: derive suggestions from selected injuries
- [ ] Present suggestions as **unconfirmed**; user adds/removes/adds-manual
- [ ] Copy makes clear suggestions are not auto-applied (README §4.5)

## 3.7 Profile review screen (README §3, §4)
- [ ] Summarize goal/schedule/experience/equipment/injuries/limitations
- [ ] Confirm ⇒ assemble final `UserProfile`

## 3.8 Local persistence (README §21)
- [ ] Choose Preferences DataStore (prototype) **or** Proto DataStore (grows into real app)
- [ ] `ProfileDataStore` persists: onboarding completion, safety ack, profile, limitations
- [ ] `DefaultProfileRepository` reads/writes via DataStore (Flow-based)
- [ ] Do **not** introduce Room (README §21 deferral list)

## 3.9 Returning-user routing (README §3)
- [ ] On launch: if onboarding complete ⇒ Home, else ⇒ Welcome

## 3.10 Tests
- [ ] Suggested-limitation test: ACL history suggests jumping/direction-change, **without** mutating the confirmed profile (README §24.3)
- [ ] ViewModel tests: selection changes, continue-button state, persistence (README §24.5)

---

## Completion criteria (README §27 Phase 3)
- [ ] The user can leave and reopen the app without losing the profile.
- [ ] Suggested limitations are never confirmed automatically.
