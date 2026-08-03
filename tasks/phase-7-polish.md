# Phase 7 — MVP Polish

**Goal:** Empty/edge states, accessibility, error/loading states, confirmation
dialogs, UI tests, and content/copy review — then verify MVP completion.

**Depends on:** Phases 1–6. **Blocks:** MVP sign-off.

**Root README refs:** §16 (home), §24.6 (Compose tests), §25 (edge states),
§27 Phase 7, §28 (MVP completion).

---

## 7.1 Home screen finalization (README §16)
- [ ] Current plan section + View plan
- [ ] Profile summary (e.g. "4 days · Build muscle · Intermediate")
- [ ] Active limitations count
- [ ] Exercise library entry point
- [ ] No analytics/history in first iteration (README §16)

## 7.2 Empty & edge states (README §25)
- [ ] No injuries selected ⇒ continue with no suggested limitations
- [ ] Injury selected, no limitation confirmed ⇒ allow continuation
- [ ] No eligible exercise for a slot ⇒ "No matching exercise…" message (no auto-reintroduction)
- [ ] All exercises excluded in a category ⇒ show the §25 guidance message
- [ ] Empty equipment ⇒ treat bodyweight as available OR require ≥1 selection
- [ ] Profile changed after generation ⇒ outdated-plan message (ties to Phase 6.4)

## 7.3 Accessibility
- [ ] Content descriptions / labels on interactive elements and icons
- [ ] Adequate touch targets and color contrast in `AppTheme`

## 7.4 Loading, error & confirmation UX
- [ ] Loading states use `LoadingContent`
- [ ] Error states are recoverable
- [ ] Confirmation dialogs for destructive actions (remove exercise, regenerate plan)

## 7.5 UI tests (README §24.6)
- [ ] Complete onboarding
- [ ] Select and deselect an injury
- [ ] Confirm suggested limitations
- [ ] Generate a plan
- [ ] Replace an exercise
- [ ] View an exclusion reason

## 7.6 Content & safety review (README §2, §27 Phase 7)
- [ ] Catalog-content review (counts, patterns, conflicting limitations sane)
- [ ] Safety-copy review — conservative, limitation-based, never diagnostic (README §2)

## 7.7 MVP completion verification (README §28)
- [ ] Walk the full §28 checklist end-to-end on a device/emulator
- [ ] Confirm profile + plan survive close/reopen
- [ ] Confirm no deferred feature leaked in (README §29)

---

## Completion criteria
- [ ] All §25 edge states handled gracefully.
- [ ] §24.6 Compose flows pass.
- [ ] Full §28 MVP checklist demonstrably works.
