# Phase 4 — Exercise Filtering / Eligibility Engine

**Goal:** A deterministic, Android-independent eligibility engine plus the
exercise library UI that surfaces availability and exclusion reasons.

**Depends on:** Phases 2 (models/catalog) and 3 (profile). **Blocks:** Phases 5–6.

**Root README refs:** §8 (engine), §9 (filtering behavior), §14 (library),
§15 (detail), §20 (UI state/events), §27 Phase 4.

---

## 4.1 Eligibility model (README §8)
- [x] `ExerciseEligibility(exercise, isEligible, exclusionReasons)`
- [x] `ExclusionReason` sealed interface: `ConflictingLimitation`, `MissingEquipment`, `AboveExperienceLevel`

## 4.2 Eligibility use case (README §8) — no Android/Compose imports
- [x] `EvaluateExerciseEligibilityUseCase(exercise, profile)`
- [x] Add `ConflictingLimitation` for each conflicting limitation ∩ profile limitations
- [x] Add `MissingEquipment` for each required-but-unavailable equipment
- [x] Add `AboveExperienceLevel` when experience doesn't support difficulty
- [x] `ExperienceLevel.supports(difficulty)` helper (README §8)
- [x] Decide: hide advanced-for-beginners vs. rank-only (README §8 note — MVP may rank)
  - **Decision (Phase 4):** MVP uses strict exclusion for determinism — BEGINNER only BEGINNER, INTERMEDIATE excludes ADVANCED, ADVANCED all. Deterministic and explainable per §27; ranking may reconsider in Phase 5.

## 4.3 Three-category filtering (README §9)
- [x] Classify each exercise as **Available** / **Excluded** / **Unavailable**
  - Priority: `ConflictingLimitation` → EXCLUDED (safety beats logistics), else `MissingEquipment` or `AboveExperienceLevel` → UNAVAILABLE, else AVAILABLE
- [x] `GetEligibleExercisesUseCase` returns categorized results for a profile
  - Deterministic: iterates catalog sorted by `ExerciseId.name`, buckets stable
- [x] Excluded exercises remain inspectable but are never auto-included (README §9)
- [x] Omit "Include anyway" override in MVP (README §9)

## 4.4 Exercise library screen (README §14)
- [x] Follow the §20 state/event pattern (immutable `UiState`, `sealed *Event`, previewable `Screen`, no `NavController`)
- [x] Muscle-group tabs/chips: All + 10 groups
- [x] Browse by muscle group; search by name; filter by equipment
  - Search normalizes underscores to spaces on both haystack and query so "chest press" matches MACHINE_CHEST_PRESS (fixed from initial implementation that only did lowercase contains on id name)
- [x] Show available and excluded rows with subtitle (muscle · equipment · tag)
- [x] Excluded rows show "Excluded by N limitation(s)"
  - Fixed: now uses `pluralStringResource(R.plurals.library_excluded_by_count_plural, count, count)` instead of same `R.string` both branches

## 4.5 Exercise detail screen (README §15)
- [x] Show name, description, primary/secondary muscles, movement pattern, equipment, default sets/reps
- [x] Show availability status + exclusion reasons
  - Fixed: `AboveExperienceLevel` reason now shows actual current vs required levels via `currentExperienceLevel` in UiState, not hardcoded "yours"
- [x] No videos/anatomical illustrations in MVP (README §15)

## 4.6 Tests (README §24.1, §24.2)
- [x] Exercise excluded when a conflicting limitation is selected (DUMBBELL_ROMANIAN_DEADLIFT + AVOID_UNSUPPORTED_HIP_HINGE)
- [x] Cable exercise unavailable without cable machine (missing-equipment reason) (CABLE_CHEST_FLY)
- [x] A unit test per limitation category
  - Knee, Spine ×2, Shoulder ×3, Elbow ×2, Wrist ×2, Hip ×2 = 12 tests + 6 documented non-excluding impact limitations
- [x] Filtering is deterministic for a fixed profile
  - Plus: search multi-word "chest press" test for normalization
- [x] Additional: ExperienceLevel.supports matrix (3 tests), total count == catalog, buckets sorted, priority conflict outranks equipment

---

## Completion criteria (README §27 Phase 4)
- [x] Every exclusion is explainable. (ExclusionReason sealed interface surfaces limitation/equipment/level — model is explainable; UI rendering is still unverified, see Pending below)
- [x] Filtering is deterministic. (sorted by ExerciseId.name, no random/timestamp, tested double-invoke)
- [x] Unit tests cover each limitation category. (18+7+3 tests, 186 total green — domain + search predicate only)

## Pending manual verification
No Compose UI tests in Phase 4 — automated coverage stops at the ViewModel boundary.
The following are implemented and compile, but have never been rendered:
- 4.4 muscle/equipment chips, row subtitle, empty state, "Excluded by N limitation(s)" plural rendering
- 4.5 detail fields, availability card colours, "Requires Advanced experience, you are Beginner"
- Completion criterion "every exclusion is explainable" is satisfied in the model layer;
  whether the explanation reaches the user is unverified.

## Verification
- JDK fix: AGP 8.7.3 requires JVM 11+, project uses Java 17 (`/usr/local/fbprojects/packages/java-runtime/prod/impl/17`). Default /usr/local/bin/java points to Java 8, so `export JAVA_HOME=.../prod/impl/17`. Use the `prod` alias rather than a numbered release — fbpkg deletes old versions as they roll, so a pinned number breaks within days.
- Gradle proxy: Maven Central rate-limited via fwdproxy (429 global_hostname_rps). Workaround: set proxy in `~/.gradle/gradle.properties` (`fwdproxy:8080`) and retry with sleeps (warm cache in one pass).
- `./gradlew :app:testDebugUnitTest` → 186 tests, 0 failures (22 suites including 3 new ExerciseLibraryViewModelTest)
- `./gradlew :app:assembleDebug` → BUILD SUCCESSFUL
