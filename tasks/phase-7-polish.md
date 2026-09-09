# Phase 7 — MVP Polish

**Goal:** Empty/edge states, accessibility, error/loading states, confirmation
dialogs, UI tests, and content/copy review — then verify MVP completion.

**Depends on:** Phases 1–6. **Blocks:** MVP sign-off.

**Root README refs:** §16 (home), §24.6 (Compose tests), §25 (edge states),
§27 Phase 7, §28 (MVP completion).

---

## 7.1 Home screen finalization (README §16)
- [x] Current plan section + View plan
- [x] Profile summary (e.g. "4 days · Build muscle · Intermediate")
- [x] Active limitations count + entry to the Profile/limitations screen (README §3, Phase 6.4)
- [x] Exercise library entry point
- [x] No analytics/history in first iteration (README §16)

## 7.2 Empty & edge states (README §25)
- [x] No injuries selected ⇒ continue with no suggested limitations
- [x] Injury selected, no limitation confirmed ⇒ allow continuation
- [x] No eligible exercise for a slot ⇒ "No matching exercise…" message (no auto-reintroduction)
- [x] All exercises excluded in a category ⇒ show the §25 guidance message
- [x] Empty equipment ⇒ treat bodyweight as available OR require ≥1 selection
- [x] Profile changed after generation ⇒ outdated-plan message (ties to Phase 6.4)

## 7.3 Accessibility
- [x] Content descriptions / labels on interactive elements and icons
- [x] Adequate touch targets and color contrast in `AppTheme`

## 7.4 Loading, error & confirmation UX
- [x] Loading states use `LoadingContent`
- [x] Error states are recoverable
- [x] Confirmation dialogs for destructive actions (remove exercise, regenerate plan)

## 7.5 UI tests (README §24.6)
- [x] Complete onboarding
- [x] Select and deselect an injury
- [x] Confirm suggested limitations
- [x] Generate a plan
- [x] Replace an exercise
- [x] View an exclusion reason

## 7.6 Content & safety review (README §2, §27 Phase 7)
- [x] Catalog-content review (counts, patterns, conflicting limitations sane)
- [x] Safety-copy review — conservative, limitation-based, never diagnostic (README §2)
- [x] Replace the placeholder launcher icon with adaptive + legacy production assets
- [x] Resolve the app base-theme decision in [docs/follow-ups.md](../docs/follow-ups.md)

## 7.7 MVP completion verification (README §28)
- [x] Walk the full §28 checklist end-to-end on a device/emulator
- [x] Confirm profile + plan survive close/reopen
- [x] Confirm no deferred feature leaked in (README §29)

---

## Completion criteria
- [x] All §25 edge states handled gracefully.
- [x] §24.6 Compose flows pass.
- [x] Full §28 MVP checklist demonstrably works.

Implementation note: the Phase 7 Compose tests compile into the instrumentation APK
(`:app:assembleDebugAndroidTest` BUILD SUCCESSFUL), `:app:testDebugUnitTest`
passes 278/278, and `connectedDebugAndroidTest` passes 21/21 on the
`workout-emulator` AVD (after a one-line 48.dp touch-target fix — see evidence).
All six §24.6 flows have covering Compose tests (`OnboardingScreensTest` +
`Phase7ScreensTest`) and all §28 flows were additionally walked by hand
on-device. Evidence is in Remaining work; every box below is ticked.

## Remaining work — all done on-device (workout-emulator AVD, API 35, this session)

- [x] Replace the placeholder launcher icon with production adaptive and legacy assets.
- [x] Run `connectedDebugAndroidTest` on a device/emulator and confirm all §24.6 flows pass.
- [x] Walk the complete §28 MVP flow on a device/emulator.
- [x] Force-stop/reopen the app and confirm both profile and current plan persist.

Verification evidence (this session, on the `workout-emulator` AVD):
- Icon: 7/7 icon XML files parse; adaptive (`mipmap-anydpi-v26` + monochrome)
  + legacy square/round vectors present; background `@color/ic_launcher_background`
  `#2F6B4F` matches `PrimaryLight`; foreground dumbbell is safe-zone-compliant
  (x 26..82, y 43..65) with 2dp rounded corners.
- Unit: `:app:testDebugUnitTest` BUILD SUCCESSFUL — 40 classes, 278 tests,
  0 failures/errors/skips (includes `DefaultProfileRepositoryTest`,
  `DefaultWorkoutPlanRepositoryTest`, `PersistedStateSerializerTest`,
  `PersistedWorkoutPlanSerializerTest`, `MainViewModelTest` covering the
  persistence/start-destination half of §28).
- Instrumented compile: `:app:assembleDebug` + `:app:assembleDebugAndroidTest`
  BUILD SUCCESSFUL. §24.6 coverage: complete-onboarding, select/deselect injury,
  confirm limitations, generate plan, replace exercise (+ removal confirmation),
  view exclusion reason — all present in `OnboardingScreensTest` /
  `Phase7ScreensTest`.
- Persistence mechanism (code-verified): profile → `profile.json` via
  `ProfileDataStore`/`DefaultProfileRepository`; plan → `workout_plan.json` via
  `WorkoutPlanDataStore`/`DefaultWorkoutPlanRepository` (corruption handlers
  reset rather than crash); `MainViewModel` routes returning users to Home.
  Force-stop/reopen = process death → DataStore re-reads both files.
- Device run (done this session on a fresh `workout-emulator` AVD, API 35 x86_64,
  `emulator -avd workout-emulator -no-window -no-audio -no-boot-anim
  -gpu swiftshader_indirect`):
  - `connectedDebugAndroidTest`: first full run 20/21 — `OnboardingScreensTest.
    safetyNotice_blocksContinueUntilAcknowledged` failed with `Actual height is
    40.0.dp, expected at least 48.0.dp`. Real Phase 7.3 gap: Material3 `Button`
    defaults to 40.dp. Fixed in `core/ui/OnboardingScaffold.kt` with
    `.heightIn(min = Dimens.MinTouchTarget)` (no visual change — the button is
    full-width). Re-run: **21/21 pass, 0 failures**.
  - §28 walk via adb taps: Welcome → safety (gate blocks until acknowledged) →
    preferences (Build muscle · Intermediate · 3 days · 45 min · Bodyweight;
    Continue correctly disabled until all required choices made) → injuries
    (rotator cuff selected) → limitations (suggestion offered unconfirmed,
    confirmed 1) → review → Home. Home matches §16 (plan card, profile summary,
    1 active limitation, library entry, no analytics).
  - Plan generation on-device: deterministic PUSH/PULL/LEGS plan; bodyweight-only
    equipment yields a partial plan (Push-up + 9 §25 "No matching exercise…"
    warnings) instead of a crash — the §25 edge-state path working as specified.
  - Persistence: `am force-stop` (process confirmed dead) → relaunch lands on
    Home, not Welcome, and the plan card flips from "Generate plan" to
    "PUSH_PULL_LEGS - 3 days" + "View plan". Profile (`profile.json`) and plan
    (`workout_plan.json`) both survive process death. No crashes in logcat.
  - Extra §28 flows walked on-device after the persistence check:
    - Library browse: counts line "13 available · 4 excluded · 42 unavailable",
      muscle-group + equipment filters, availability badges, working search.
    - Exclusion reason: an excluded row's details show "Excluded from your plan"
      plus "Conflicts with: Avoid raising the arms out to the side" and
      "Missing equipment: Resistance bands" — the §2 limitation-based pattern.
    - Replace exercise: Push-up overflow → replacement sheet → Bodyweight squat
      → Confirm; plan re-renders with the squat in place and the change
      survives navigation (DataStore write path confirmed on-device).
  - Note: the emulator process died twice while idle between runs (nothing to do
    with the app — no crashes logged); rebooting the AVD recovered each time.
