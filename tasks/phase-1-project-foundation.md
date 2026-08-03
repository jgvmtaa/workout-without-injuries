# Phase 1 — Project Foundation

**Goal:** Navigable app shell with theme, DI, package structure, and placeholder
screens. **No business logic yet.**

**Depends on:** Phase 0. **Blocks:** Phases 3–7 (UI), enables Phase 2 in parallel.

**Root README refs:** §17 (navigation), §18 (architecture), §19 (module structure),
§22 (DI), §27 Phase 1.

---

## 1.1 Package structure (README §19)
- [ ] Create package tree under `com.jgv.workoutplanner`:
  - [ ] `navigation/`, `core/ui/`, `core/designsystem/`
  - [ ] `domain/model/`, `domain/repository/`, `domain/usecase/`
  - [ ] `data/catalog/`, `data/local/`, `data/repository/`
  - [ ] `feature/onboarding/{welcome,safety,preferences,injuries,limitations,review}/`
  - [ ] `feature/{home,plan,exerciselibrary,exercisedetails,profile}/`
  - [ ] `di/`

## 1.2 Design system (README §19 core/designsystem)
- [ ] `AppTheme.kt`, `Color.kt`, `Type.kt`, `Dimens.kt` (Material 3)
- [ ] Light theme baseline (dark optional for MVP)

## 1.3 Core UI scaffolding (README §19 core/ui)
- [ ] `AppScaffold.kt`, `AppTopBar.kt`
- [ ] `LoadingContent.kt`, `EmptyContent.kt` placeholders

## 1.4 Navigation shell (README §17)
- [ ] Define `AppRoute` sealed interface with all destinations (Welcome, SafetyNotice,
      Preferences, InjuryHistory, MovementLimitations, ProfileReview, Home, Plan,
      ExerciseLibrary, ExerciseDetails(exerciseId), ExerciseReplacement(dayId, exerciseId), Profile)
- [ ] `AppNavigation.kt` with `NavHost` wiring every route
- [ ] Use type-safe navigation (README §17); `@Serializable` routes
- [ ] **Do not** pass `NavController` into screen composables (README §20)

## 1.5 Placeholder screens
- [ ] A stub composable per destination showing its name
- [ ] `@Preview` for each screen
- [ ] Every destination reachable via a temporary debug menu or wired flow

## 1.6 DI bootstrap (README §22)
- [ ] `di/AppModule.kt` with Hilt module skeleton
- [ ] ViewModels obtainable via `hilt-navigation-compose`

---

## Completion criteria (README §27 Phase 1)
- [ ] Every destination can be navigated to.
- [ ] Each screen has a preview.
- [ ] No business logic is implemented yet.
