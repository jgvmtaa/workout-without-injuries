# Phase 1 — Project Foundation

**Goal:** Navigable app shell with theme, DI, package structure, and placeholder
screens. **No business logic yet.**

**Depends on:** Phase 0. **Blocks:** Phases 3–7 (UI), enables Phase 2 in parallel.

**Root README refs:** §17 (navigation), §18 (architecture), §19 (module structure),
§22 (DI), §27 Phase 1.

---

## 1.1 Package structure (README §19)
  - [x] Create package tree under `com.jgv.workoutplanner`:
  - [x] `navigation/`, `core/ui/`, `core/designsystem/`
  - [x] `domain/model/`, `domain/repository/`, `domain/usecase/` — created empty
        (`.gitkeep`); populated in Phase 2
  - [x] `data/catalog/`, `data/local/`, `data/repository/` — created empty
        (`.gitkeep`); populated in Phases 2–3
  - [x] `feature/onboarding/{welcome,safety,preferences,injuries,limitations,review}/`
  - [x] `feature/{home,plan,exerciselibrary,exercisedetails,profile}/` —
        `ExerciseReplacementScreen` lives in `feature/plan/` because replacement is a
        plan edit (README §13)
  - [x] `di/`

## 1.2 Design system (README §19 core/designsystem)
- [x] `AppTheme.kt`, `Color.kt`, `Type.kt`, `Dimens.kt` (Material 3)
- [x] Light theme baseline (dark included, follows the system setting).
      Dynamic (Material You) color is deliberately **not** used — the palette carries
      meaning once availability states land, so it must be identical on every device.
      This replaces the Phase 0 `ui/theme/` package, which is deleted.

## 1.3 Core UI scaffolding (README §19 core/ui)
- [x] `AppScaffold.kt`, `AppTopBar.kt`
- [x] `LoadingContent.kt`, `EmptyContent.kt` placeholders
- [x] `PlaceholderScreen.kt` — temporary shared body for the stub screens; deleted
      screen-by-screen as real content lands in Phases 3–6. Phase 3 removed six callers
      (the onboarding screens); six remain — Home, Plan, Exercise library, Exercise
      details, Exercise replacement, Profile.

## 1.4 Navigation shell (README §17)
- [x] Define `AppRoute` sealed interface with all destinations (Welcome, SafetyNotice,
      Preferences, InjuryHistory, MovementLimitations, ProfileReview, Home, Plan,
      ExerciseLibrary, ExerciseDetails(exerciseId), ExerciseReplacement(dayId, exerciseId), Profile)
- [x] `AppNavigation.kt` with `NavHost` wiring every route
- [x] Use type-safe navigation (README §17); `@Serializable` routes
- [x] **Do not** pass `NavController` into screen composables (README §20) — the
      controller is confined to `AppNavigation.kt`; screens take plain lambdas

> **Note:** exercise arguments were typed `String` in Phase 1, not `ExerciseId` — that
> enum is Phase 2 catalog work, and Phase 1 carries no domain types. Phase 2 swapped
> them, so `AppRoute` now matches README §17 exactly.

## 1.5 Placeholder screens
- [x] A stub composable per destination showing its name
- [x] `@Preview` for each screen
- [x] Every destination reachable via a temporary debug menu or wired flow — the real
      flow is wired (Welcome → Safety → Preferences → Injuries → Limitations → Review →
      Home → Plan/Library/Profile → Details/Replacement), so no debug menu is needed.
      Plan and Library pass stand-in ids to the argument-carrying destinations until
      Phases 2 and 5 supply real ones.

## 1.6 DI bootstrap (README §22)
- [x] `di/AppModule.kt` with Hilt module skeleton — empty in Phase 1; Phase 2 added the
      repository bindings in `RepositoryModule`, Phase 3 the `DataStore` provider and
      the `@ApplicationScope` qualifier
- [x] ViewModels obtainable via `hilt-navigation-compose` — `HomeViewModel`
      (`@HiltViewModel`) is resolved with `hiltViewModel()` in `HomeRoute`

## 1.7 Architecture baseline (README §18)
- [x] Establish unidirectional data flow: Compose screen → ViewModel → use case →
      repository → local data source. `HomeRoute`/`HomeScreen`/`HomeViewModel` is the
      reference implementation of the top half; the lower half arrives with the
      repositories in Phases 2–3.
- [x] Screens receive immutable UI state and emit events (state hoisting) — no business
      state in composables. `HomeUiState` is the one-state-model-per-screen example.
- [x] Reserve the `domain/` layer for eligibility & plan-generation logic (kept off the
      UI layer) — packages created and left empty; filled by Phases 2 and 3, and still
      free of any Android or Compose dependency

---

## Completion criteria (README §27 Phase 1)
- [x] Every destination can be navigated to. — verified by walking the full flow on a
      running app (Welcome → Safety → Preferences → Injuries → Limitations → Review →
      Home → Plan / Library / Profile → Exercise details & replacement). Built and run
      from Android Studio.
- [x] Each screen has a preview.
- [x] No business logic is implemented yet.
