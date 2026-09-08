# Task Breakdown — Workout Planner (Android MVP)

This directory decomposes the [specification](../docs/spec.md) into a hierarchical,
**sequentially workable** task list. Each phase builds on the previous one, so
work them top-to-bottom. Within a phase, tasks are ordered by dependency.

## How to use this

- Each phase lives in its own file (`phase-N-*.md`).
- Tasks use `- [ ]` checkboxes. Check them off as you complete them.
- Every task references the source section(s) of the spec, e.g. `(README §5.3)`.
  Those `README §N` citations point at [`docs/spec.md`](../docs/spec.md), which was
  the root README until it was replaced by a short product summary.
- A phase is **done** only when its *Completion criteria* (last section of each
  phase file) are all satisfied.

## Dependency order (do not skip ahead)

```
Phase 0  Environment & project bootstrap        (prerequisite for everything)
   ↓
Phase 1  Project foundation (nav shell, DI, theme, placeholder screens)
   ↓
Phase 2  Domain models & static catalogs         ← recommended first CODING task (README §31)
   ↓
Phase 3  Onboarding & local profile persistence
   ↓
Phase 4  Exercise filtering / eligibility engine
   ↓
Phase 5  Plan generation
   ↓
Phase 6  Plan editing
   ↓
Phase 7  MVP polish (empty states, a11y, tests, copy review)
```

## Phase index

| Phase | File | Goal | Status | Spec ref |
|-------|------|------|--------|----------|
| 0 | [phase-0-bootstrap.md](phase-0-bootstrap.md) | Repo, toolchain, build green | ✅ Done | §23 |
| 1 | [phase-1-project-foundation.md](phase-1-project-foundation.md) | Nav shell + DI + theme + placeholders | ✅ Done | §17–§19, §22, §27 |
| 2 | [phase-2-domain-and-catalogs.md](phase-2-domain-and-catalogs.md) | Domain models + exercise/injury catalogs | ✅ Done¹ | §4.3–§7, §10–§11, §26, §31 |
| 3 | [phase-3-onboarding.md](phase-3-onboarding.md) | Onboarding flow + profile persistence | ✅ Done | §4.1–§4.5, §20, §21 |
| 4 | [phase-4-filtering.md](phase-4-filtering.md) | Eligibility engine + library filters | ✅ Done | §8, §9, §14, §15, §20 |
| 5 | [phase-5-plan-generation.md](phase-5-plan-generation.md) | Deterministic plan generator | ✅ Done² | §11, §12, §16 |
| 6 | [phase-6-plan-editing.md](phase-6-plan-editing.md) | Edit plan + edit profile/limitations | ✅ Done | §13, §16, §17, §25 |
| 7 | [phase-7-polish.md](phase-7-polish.md) | Empty/edge states, a11y, tests, copy | 🟨 In progress | §24, §25, §28 |

¹ Complete, but two spot-checks from Phase 2 are still unticked — the `ExerciseId` nav
argument and the two changed previews. Neither blocks Phase 4. See
[docs/follow-ups.md § Pending verification](../docs/follow-ups.md#pending-verification).

² 59 exercises (not 57) — task file corrected during Phase 5. Deterministic IDs
`plan-${days}-${split}`, days `${planId}-day-${index}-${focus}`. 203 unit tests green.

## Guardrails that apply to every phase (README §2, §29)

- The app **filters**, it does **not** diagnose. Use conservative, limitation-based copy.
- Suggested limitations are **never** applied silently — the user confirms them.
- Plan generation must be **deterministic**: same profile ⇒ same plan.
- Keep the eligibility & plan-generation engine **independent of Android/Compose**.
- Do **not** build any deferred feature (LLM, auth, cloud, logging, Room, etc.) — see §29.

**Out of scope:** README §30 (post-MVP direction — v1.1 tracking, v1.2 adaptation,
v1.3 NL onboarding, v1.4 remote catalog) is intentionally **not** broken into tasks
here. It's future work, addressed only after the §28 MVP is complete.

## Definition of MVP completion (README §28)

The MVP is complete when a new user can: accept the safety notice; set goal /
schedule / experience / equipment; select injuries; review & confirm suggested
limitations; generate a deterministic plan; understand exclusions; browse the
library by muscle group; replace an exercise with an eligible alternative; and
close/reopen the app without losing profile or plan.
