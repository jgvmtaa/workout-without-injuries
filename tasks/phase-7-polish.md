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
- [ ] Replace the placeholder launcher icon with adaptive + legacy production assets
- [x] Resolve the app base-theme decision in [docs/follow-ups.md](../docs/follow-ups.md)

## 7.7 MVP completion verification (README §28)
- [ ] Walk the full §28 checklist end-to-end on a device/emulator
- [ ] Confirm profile + plan survive close/reopen
- [x] Confirm no deferred feature leaked in (README §29)

---

## Completion criteria
- [x] All §25 edge states handled gracefully.
- [ ] §24.6 Compose flows pass.
- [ ] Full §28 MVP checklist demonstrably works.

Implementation note: the Phase 7 Compose tests compile into the instrumentation APK,
but still need to run on a connected device/emulator before the §24.6 completion
criterion can be checked.

## Remaining work

- Replace the placeholder launcher icon with production adaptive and legacy assets.
- Run `connectedDebugAndroidTest` on a device/emulator and confirm all §24.6 flows pass.
- Walk the complete §28 MVP flow on a device/emulator.
- Force-stop/reopen the app and confirm both profile and current plan persist.
