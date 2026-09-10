# Follow-ups / deferred items

Running list of things noticed during implementation that are intentionally
deferred (not bugs, not blockers). Grouped by the phase where they should be
picked up. Check items off as they're handled.

## Pending verification

Unlike the rest of this file, these are **not** deferred by choice — they are checks
that could not run from the shell Phases 2 and 3 were written in, which cannot reach the
Gradle daemon (see
[toolchain.md](toolchain.md#environment-caveat-gradle-from-an-automation-spawned-shell)).
All of Phase 3's have since been cleared from an ordinary terminal and a device:
`./gradlew test assembleDebug` and `./gradlew connectedDebugAndroidTest` both pass, and
the flow has been walked by hand including a force-stop.

One Phase 2 spot-check is still open. It needs Android Studio previews, not a device —
so it stays here rather than being assumed from a green build.

- [x] **Build it.** `./gradlew test assembleDebug` passes. Phases 2 and 3 have now been
  through AGP, KSP, Hilt code generation, aapt and lint — so the Hilt graph is valid,
  including Phase 3's `DataStore<PersistedState>` provider, the `@ApplicationScope`
  qualifier, five `@HiltViewModel` classes and the `MainViewModel` obtained with
  `by viewModels()`. _(Phase 2, Phase 3)_
- [x] **Run the instrumented tests.** `./gradlew connectedDebugAndroidTest` passes:
  `OnboardingScreensTest` (README §24.6) runs green on a device. _(Phase 3)_
- [x] **Walk the onboarding flow by hand.** Passes: Welcome → safety → preferences →
  injuries → limitations → review → home, force-stopped and reopened. The profile
  survives app death and a returning user lands on Home — the Phase 3 completion
  criterion, and the one thing no test covers, since the instrumented tests drive the
  screens but never kill the process. _(Phase 3)_
- [ ] **Check the two changed previews render:** `ExerciseDetailsScreen` and
  `ExerciseReplacementScreen`. Both signatures changed from `String` to `ExerciseId`.
  _(Phase 2)_
- [x] **Walk the argument-carrying destinations:** Plan → exercise details, Plan →
  replace exercise, Library → exercise details. Verified on the `workout-emulator`
  AVD (Phase 7 session): Library → an excluded row opens details showing the
  conflicting limitation + missing equipment; Plan → Push-up overflow → Replace →
  Bodyweight squat lands in the plan; Plan → overflow → Details opens the
  Bodyweight squat page showing "Available". Enum args arrive intact, no `typeMap`
  needed. _(Phase 2 → done in Phase 7)_
- [x] **~28 unused-resource lint warnings for `limitation_*`.** Resolved: the Phase 3
  limitations screen consumes all 27 via `MovementLimitationCatalog`, and a scan of
  `strings.xml` against the source finds no unreferenced string except `app_name`
  (used from the manifest). _(Phase 2 → done in Phase 3)_

## Address before/ during Phase 7 (MVP polish)

- [x] **Real launcher icon.** Resolved in Phase 7: replaced the placeholder with a
  designed production set — adaptive icon (`mipmap-anydpi-v26`, background +
  foreground + monochrome) plus self-contained legacy square/round vectors
  (`mipmap/`), theme-matched background `@color/ic_launcher_background`
  (`#2F6B4F`, = `PrimaryLight`) and a safe-zone-compliant rounded dumbbell
  foreground. All 7 icon XML files parse. Vector adaptive + vector legacy is the
  current production pattern (no PNG densities needed; they would only bloat the
  APK). _(Phase 0 → done in Phase 7)_
- [x] **App base theme.** Reviewed in Phase 7 and retained intentionally. The app is a
  single Compose activity, `AppTheme` owns every rendered surface, and
  `enableEdgeToEdge()` owns system-bar treatment. Pulling in the Views Material library
  only to rename the otherwise invisible launch-window theme would add no user-facing
  value. Revisit together with `androidx.core:core-splashscreen` if a branded launch
  screen is added. _(Phase 0 → resolved in Phase 7)_
- [ ] **The start-up loading state is a spinner.** `MainActivity` shows
  `LoadingContent` while the profile is read from disk. It is one or two frames in
  practice, so a spinner that flashes may read worse than nothing — consider
  `androidx.core:core-splashscreen`, which is designed for exactly this window.
  _(Phase 3)_
- [ ] **Onboarding has no progress indicator.** Six steps with no sense of how many
  remain. A step counter or progress bar in `OnboardingScaffold` would be a small,
  contained addition. _(Phase 3)_

## Address during Phase 4 (filtering) and Phase 5 (plan generation)

- [ ] **Six limitations exclude no exercise.** `AVOID_HIGH_IMPACT`, `AVOID_JUMPING`,
  `AVOID_RUNNING`, `AVOID_RAPID_DIRECTION_CHANGE`, `AVOID_SPINAL_ROTATION`, and
  `AVOID_LOADED_WRIST_FLEXION` match nothing in the catalog, because the MVP has no
  plyometric, running, loaded-rotation, or loaded-wrist-flexion exercises. Confirming
  one currently changes nothing. Don't write Phase 4 copy that implies otherwise, and
  revisit if conditioning work is ever added. Note that the Phase 3 limitations screen
  still *offers* all six — hiding them would be worse, since a user who needs to avoid
  jumping should be able to say so before the catalog can act on it. _(Phase 2)_
- [x] **`Equipment.CARDIO_MACHINE` has no exercises.** Resolved in Phase 3: it is
  omitted from the preferences screen via `TrainingOptions.SELECTABLE_EQUIPMENT`. The
  enum constant stays, and `PreferencesViewModelTest` pins the omission so re-adding it
  is deliberate. Revisit when conditioning exists. _(Phase 2 → done in Phase 3)_
- [ ] **Isometric holds have no duration field.** `ExercisePrescription` models
  sets/reps/rest, so `FRONT_PLANK` and `SIDE_PLANK` use `reps = 1..1` and state the
  hold duration in their description. Fine for reading, awkward for a plan UI that
  renders "1..1 reps". Phase 5 kept catalog defaults verbatim for MVP (task decision);
  add a duration to the prescription when Phase 6 editing or Phase 7 polish needs to
  show or edit it. _(Phase 2, still open after Phase 5)_
- [x] **`WorkoutPlanRepository` has no implementation.** Implemented in Phase 5:
  `WorkoutPlanDataStore` + `PersistedWorkoutPlanSerializer` + `StoredWorkoutPlan` backed
  by separate `workout_plan.json` (not `profile.json`). `DefaultWorkoutPlanRepository`
  with `Flow<WorkoutPlan?>` + `savePlan`/`clearPlan`, bound in `RepositoryModule`,
  DataStore provided in `AppModule` with same corruption handler as profile. Mapping
  tolerant: unknown ExerciseId dropped, unknown focus dropped day, not crash.
  _(Phase 2 → done in Phase 5)_
- [x] **`PLACEHOLDER_WORKOUT_DAY_ID` in `AppNavigation`.** Removed in Phase 5: `PlanRoute`
  generates real day ids `${planId}-day-${index}-${focus.lowercase()}` and passes them to
  `ExerciseReplacement`. `SAMPLE_EXERCISE_ID` also removed. _(Phase 1 → done in Phase 5)_

## Decisions taken in Phase 5, recorded because they affect Phase 6–7

- **Catalog has 59 exercises, not 57.** The task file said 57 but `ExerciseCatalog.exercises.size` is 59 at Phase 5 start. Verified via test count. All phase docs updated to 59. _(Phase 5)_
- **Split distribution (confirmed):** 2→FULL_BODY × days, 3→PUSH/PULL/LEGS (task 5.1), 4→Upper/Lower/Upper/Lower, 5→Upper/Lower/Upper/Lower/FullBody (option a, confirmed). `PUSH`=5 slots (h-push, v-push, shoulder-iso, triceps, secondary-push), `PULL`=5 slots, `LEGS`/`LOWER`=6 slots, `UPPER`=7 slots, `FULL`=6 slots. Deterministic day names include occurrence count when focus repeats. _(Phase 5)_
- **Scoring: +3 preferred equipment dropped.** Post-filter all candidates already satisfy equipment availability, so +3 is vacuous. Confirmed to drop term entirely. Final scoring: +5 exact pattern (base), +2 BEGINNER, +2 MACHINE_SUPPORTED/CHEST_SUPPORTED when balance limitation present (AVOID_UNILATERAL_BALANCE_DEMAND or AVOID_SINGLE_LEG_LOADING), +1 COMPOUND early slot (first 2 slots), -2 duplicate movement pattern, -3 second UNILATERAL. Tie-break stable on `ExerciseId.name`. _(Phase 5)_
- **Prescription uses catalog defaults directly.** `sets = prescription.sets.first`, `repRange = full range`, `restSeconds` as-is. No goal-based adjustment in MVP (confirmed). Keeps generation deterministic and explainable. _(Phase 5)_
- **Plan IDs deterministic:** `plan-${daysPerWeek}-${split.name}`, day `${planId}-day-${index}-${focus.lowercase()}` (confirmed). No UUID, no timestamp, same profile ⇒ same IDs ⇒ same plan (README §12.5 + task). _(Phase 5)_
- **Separate `workout_plan.json` DataStore.** Not mixed into `profile.json`. `StoredWorkoutPlan(plan?)` wrapper with nullable plan, `PersistedWorkoutPlanSerializer` (ignoreUnknownKeys, encodeDefaults, prettyPrint) + corruption handler resetting to empty. `AppModule` provides second DataStore, `RepositoryModule` binds repo. Mapping round-trip tested. _(Phase 5)_
- **Partial-plan handling via warnings, not crash.** `WorkoutPlanGenerationResult(plan, warnings, eligibleCount)` where `PlanWarning(dayIndex, dayFocus, slotId)` identifies each unfillable slot. The UI resolves the README §25 copy from resources rather than persisting English text. Extremely restrictive profile (BEGINNER+BODYWEIGHT+all limitations) yields valid partial result with warnings, not exception – tested in `GenerateWorkoutPlanUseCaseTest`. _(Phase 5, localized in Phase 7)_
- **Generation trigger: Home button + auto-generate on Plan entry.** `HomeScreen` shows Generate (no plan) vs View (has plan). `PlanRoute` `LaunchedEffect` calls `viewModel.autoGenerateIfNeeded()` when `hasNoPlan`. `PlanViewModel` combines profile + currentPlan + warningsFlow + generatingFlow into `PlanUiState`. Regenerate available via TopBar action. _(Phase 5)_
- **Placeholder screens after Phase 5:** Home and Plan were real; Profile and
  ExerciseReplacement remained for Phase 6. Both became real in Phase 6, and Phase 7
  removed the now-unused `PlaceholderScreen.kt`. _(Phase 5 → resolved in Phase 7)_
- **Build verification:** `./gradlew :app:testDebugUnitTest` 203 tests, 0 failures (14 new `GenerateWorkoutPlanUseCaseTest` + 3 `PersistedWorkoutPlanMappingTest`). `./gradlew :app:assembleDebug` BUILD SUCCESSFUL. _(Phase 5)_

## Address before/during Phases 4–6 (feature work)

- [x] **Delete `core/ui/PlaceholderScreen.kt`.** Removed after the final real feature
  screens landed, along with the obsolete placeholder strings. _(Phase 1 → done in Phase 7)_
- [x] **Decide the start destination.** Done in Phase 3: `MainViewModel` reads the
  stored profile once and `MainActivity` starts the graph at `Home` or `Welcome`
  accordingly. Deliberately decided once rather than observed — `NavHost` rebuilds its
  graph when `startDestination` changes, which would discard the back stack the moment
  onboarding saved a profile. _(Phase 1 → done in Phase 3)_
- [x] **Give profile editing its own exit path.** Phase 6 added dedicated profile-edit
  destinations and session state; save/cancel returns to Profile, and material changes
  mark an existing plan outdated. _(Phase 1 → done in Phase 6)_

## Address during Phase 4 (filtering) — found and fixed

- [x] **Search matched `ExerciseId.name` with underscores.** `ExerciseLibraryViewModel.matchesQuery` did `lowercase().contains(lower)` on `MACHINE_CHEST_PRESS`, so multi-word query "chest press" returned nothing. Fixed to normalize underscores to spaces on both haystack and query (`replace('_',' ')`) and added `ExerciseLibraryViewModelTest` with two-word "chest press" and "chest_press" cases. _(Phase 4)_
- [x] **Plural never fired.** `ExerciseLibraryScreen` used `if (count==1) R.string.library_excluded_by_count else R.string.library_excluded_by_count` — same id both branches, and `R.plurals.library_excluded_by_count_plural` existed but was unused. Fixed to `pluralStringResource(R.plurals...., count, count)`. _(Phase 4)_
- [x] **Hardcoded literal "yours" in `ExerciseDetailsScreen`.** `AboveExperienceLevel` reason formatted with `"yours"` literal, producing "you are yours". Fixed by adding `currentExperienceLevel` to `ExerciseDetailsUiState` via `ProfileRepository` and resolving both required and current via `labelRes`. _(Phase 4)_
- [x] **FlowRow experimental + `stringResource` inside `joinToString` / `try/catch`.** Compile errors: `FlowRow` is `ExperimentalLayoutApi`, and Compose disallows `stringResource` inside `joinToString` lambda and inside `try/catch` around composable. Fixed with `@OptIn(ExperimentalLayoutApi)` and pre-resolving labels outside lambdas. _(Phase 4)_

## Decisions taken in Phase 4, recorded because they affect Phase 5–6

- **Experience level uses strict exclusion for MVP.** `ExperienceLevel.supports()` : BEGINNER→only BEGINNER, INTERMEDIATE→!=ADVANCED, ADVANCED→all. `EvaluateExerciseEligibilityUseCase` adds `AboveExperienceLevel` and `GetEligibleExercisesUseCase` buckets it as UNAVAILABLE. README §8 notes rank-only as alternative — deterministic strict exclusion chosen for §27 Phase 4 criteria and testability; ranking may reconsider in Phase 5 generator. _(Phase 4)_
- **Three-category priority: limitation outranks equipment.** `EXCLUDED` if any `ConflictingLimitation`, else `UNAVAILABLE` if `MissingEquipment` or `AboveExperienceLevel`. This matches README §2 safety priority and keeps "excluded" meaning safety-relevant. Tested. _(Phase 4)_
- **Exercise library search is id-based, not display-name-based.** ViewModel cannot resolve `@StringRes` without Context, so search is on `ExerciseId.name` normalized. Display-name search would require Context or repository exposing search string — deferred to polish, documented in ViewModel KDoc. _(Phase 4)_
- **Maven Central rate-limited via fwdproxy (429).** `repo.maven.apache.org` / `repo1.maven.org` hit global RPS limit (`[Raindrop] Ratelimit by OnRequestRateLimitFilter`). Workaround in this environment: set proxy in `~/.gradle/gradle.properties` (`fwdproxy:8080`) and warm cache in one pass with sleeps (`./gradlew testDebugUnitTest --refresh-dependencies` retries). Successful build now has `junit`, `turbine`, `kotlin-parcelize-runtime-1.9.22`, `kotlin-android-extensions-runtime-1.9.22` cached after wait. _(Phase 4)_
- **JDK 17 required, default is Java 8.** AGP 8.7.3 requires JVM 11+. `/usr/local/bin/java` points to 8, so builds need `export JAVA_HOME=/usr/local/fbprojects/packages/java-runtime/prod/impl/17`. Use the `prod` alias, not a version number — the numbered directories are fbpkg releases that are deleted as they roll (524 was replaced by 525 overnight, breaking every pinned path). Documented in toolchain and Phase 4 task completion notes. _(Phase 4)_

## Decisions taken in Phase 3, recorded here because they are worth revisiting

- **Onboarding completion is derived, not stored.** README §21 lists it as its own
  persisted item; `ProfileRepository` derives it from `profile != null` instead, because
  a stored boolean and a stored profile can disagree and a derived one cannot. If
  "finished onboarding" and "has a profile to plan from" ever stop being the same event
  — a skippable onboarding, say — this needs a real flag. _(Phase 3)_
- **`PersistedState.schemaVersion` is written but never read.** There is nothing to
  migrate from yet. It exists so the first migration has somewhere to branch; the first
  breaking change to the persisted models should bump it and add the migration.
  _(Phase 3)_
- **The draft is not versioned separately from the profile.** Both live in one
  `PersistedState` record, so any write rewrites the whole file. It is a few hundred
  bytes, and `distinctUntilChanged` keeps the extra emissions off the UI, so this is
  fine until the plan is stored in the same file. _(Phase 3)_
- **Suggestions do not say which injury prompted them.** The limitations screen shows a
  suggestion without naming its source, which would be useful context ("suggested by:
  rotator cuff injury") and is cheap to compute — `GetSuggestedLimitationsUseCase` has
  the mapping. Left out to keep Phase 3 to its task list. _(Phase 3)_

## Address before any release build (post-MVP / when shipping)

- [ ] **Release signing + minification.** The `release` build type in
  `app/build.gradle.kts` has `isMinifyEnabled = false` and no `signingConfig`.
  Set up a signing config and decide on R8/minify + `proguard-rules.pro` before
  producing a release APK/AAB. _(Phase 0)_
- [ ] **The profile is stored unencrypted.** `profile.json` in the app's private
  `datastore` directory holds injury history. Private storage is the normal place for
  it and the MVP has no account or sync, but it is health-adjacent data — worth a
  decision before shipping. _(Phase 3)_

## Environment / FYI (no code change expected)

- Gradle works from an interactive terminal on the scaffolding Mac, but **not from a
  shell spawned by tooling** (coding agents, hooks): the endpoint policy grants network
  per process tree, and those trees do not get it, so the client cannot reach the daemon
  over loopback. Symptom: `./gradlew --version` succeeds, the daemon logs "Daemon server
  started", and the build fails with "Could not connect to the Gradle daemon" — with or
  without `--no-daemon`. Nothing is wrong with the project. Build from an interactive
  terminal, Android Studio, or CI. See [toolchain.md](toolchain.md).
- Emulator install gotchas: a full `/data` makes `install-create` fail with a
  generic "Unknown failure" (wipe AVD data / size up userdata); IDE debug APKs
  are `testOnly`, so manual `adb install` needs `-t`. See [toolchain.md](toolchain.md).
- Local build shows a benign native-lib strip warning
  (`libandroidx.graphics.path.so`, `libdatastore_shared_counter.so`) when the
  NDK strip tool isn't present; the APK is still valid and CI is unaffected.
- `local.properties` is machine-specific and git-ignored; a fresh clone needs
  `sdk.dir` or `ANDROID_HOME`.
