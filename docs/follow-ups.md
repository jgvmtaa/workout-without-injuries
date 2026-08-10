# Follow-ups / deferred items

Running list of things noticed during implementation that are intentionally
deferred (not bugs, not blockers). Grouped by the phase where they should be
picked up. Check items off as they're handled.

## Pending verification

Unlike the rest of this file, these are **not** deferred by choice — they are checks
that cannot run on the machine this was written on (see
[toolchain.md](toolchain.md#build-verification-status)). Clear them on a machine where
Gradle works.

What *has* been checked here, with [`tools/verify-no-gradle.sh`](../tools/verify-no-gradle.sh):
every file under `app/src/main`, `app/src/test` and `app/src/androidTest` compiles
against the real Compose and serialization compiler plugins, and all 155 JVM unit tests
pass. What that script cannot do is aapt, KSP, Hilt code generation, lint, packaging, or
anything on a device — hence the list below.

- [ ] **Build it.** `./gradlew test assembleDebug` in Android Studio. Nothing in Phase 2
  or Phase 3 has been through AGP, KSP, Hilt code generation, aapt, or lint. In
  particular Hilt's dependency graph is unvalidated: Phase 3 adds a
  `DataStore<PersistedState>` provider, an `@ApplicationScope` qualifier, five
  `@HiltViewModel` classes and a `MainViewModel` obtained with `by viewModels()`. A
  missing binding compiles clean under the script and fails at KSP. _(Phase 2, Phase 3)_
- [ ] **Run the instrumented tests.** `app/src/androidTest` now holds
  `OnboardingScreensTest` (README §24.6). It type-checks but has never executed — it
  needs an emulator. _(Phase 3)_
- [ ] **Walk the onboarding flow on a device.** Welcome → safety → preferences →
  injuries → limitations → review → home, then force-stop and reopen: the app should
  land on Home, and reopening mid-flow should resume with the answers intact. This is
  the Phase 3 completion criterion and only a real device proves it. _(Phase 3)_
- [ ] **Check the two changed previews render:** `ExerciseDetailsScreen` and
  `ExerciseReplacementScreen`. Both signatures changed from `String` to `ExerciseId`.
  _(Phase 2)_
- [ ] **Walk the argument-carrying destinations:** Plan → exercise details, Plan →
  replace exercise, Library → exercise details. Each should show
  `Exercise: MACHINE_CHEST_PRESS`. Navigation 2.8.4 maps `SerialKind.ENUM` to
  `NavType.EnumType` with no `typeMap` needed, but that was confirmed by reading the
  library, not by running it. _(Phase 2)_
- [x] **~28 unused-resource lint warnings for `limitation_*`.** Resolved: the Phase 3
  limitations screen consumes all 27 via `MovementLimitationCatalog`, and a scan of
  `strings.xml` against the source finds no unreferenced string except `app_name`
  (used from the manifest). _(Phase 2 → done in Phase 3)_

## Address before/ during Phase 7 (MVP polish)

- [ ] **Real launcher icon.** The current icon (`app/src/main/res/mipmap*`,
  `drawable/ic_launcher_*`) is a placeholder hand-drawn dumbbell vector with no
  proper PNG densities. Replace with a designed app icon (adaptive + legacy
  densities). _(Phase 0)_
- [ ] **App base theme.** `res/values/themes.xml` uses the framework
  `android:Theme.Material.*` rather than a Material3 XML theme, deliberately to
  avoid pulling in `com.google.android.material`. Revisit if we add a splash
  screen (`androidx.core:core-splashscreen`) or need XML-level system-bar /
  status-bar styling. _(Phase 0)_
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
  renders "1 rep". Add a duration to the prescription when Phase 5 or 6 needs to show
  or edit it. _(Phase 2)_
- [ ] **`WorkoutPlanRepository` has no implementation.** Interface only, as in Phase 2.
  Nothing generates a plan yet and README §27 lists plan persistence as Phase 5 work, so
  `RepositoryModule` binds no implementation. Add one alongside the generator. _(Phase 2)_
- [ ] **`PLACEHOLDER_WORKOUT_DAY_ID` in `AppNavigation`.** Still a stand-in; real day
  ids come from the generated plan. _(Phase 1)_

## Address before/during Phases 4–6 (feature work)

- [ ] **Delete `core/ui/PlaceholderScreen.kt`.** Phase 3 removed six of its callers
  (the onboarding screens). Six remain: Home, Plan, Exercise library, Exercise details,
  Exercise replacement, and Profile. It and the `placeholder_*` strings should disappear
  with the last of them. _(Phase 1)_
- [x] **Decide the start destination.** Done in Phase 3: `MainViewModel` reads the
  stored profile once and `MainActivity` starts the graph at `Home` or `Welcome`
  accordingly. Deliberately decided once rather than observed — `NavHost` rebuilds its
  graph when `startDestination` changes, which would discard the back stack the moment
  onboarding saved a profile. _(Phase 1 → done in Phase 3)_
- [ ] **Give profile editing its own exit path.** The Profile screen's edit actions reuse
  the onboarding destinations (`Preferences`, `InjuryHistory`, `MovementLimitations`), so
  "Continue" walks the user through the remainder of the onboarding flow instead of
  returning to Profile. Phase 3 made this half-better and half-worse: the screens now
  show the saved values rather than blank forms, because `saveProfile` re-seeds the
  draft — but they are real editors now, so the wrong exit path is a visible bug rather
  than a placeholder oddity. Editing needs to save and return to Profile and, per README
  §13/§25, warn that the existing plan is now outdated. Decide whether that is a
  nav-graph change (an edit sub-graph) or a mode flag on the shared screens.
  _(Phase 1, sharpened in Phase 3)_

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

- Command-line Gradle cannot run on the primary scaffolding Mac (endpoint policy
  blocks shell-launched JVM TCP, so the Gradle daemon can't start). Re-confirmed
  during Phase 3: `./gradlew --version` works, `./gradlew test` fails with "Could not
  connect to the Gradle daemon", with or without `--no-daemon`. Build via Android
  Studio or CI, and use [`tools/verify-no-gradle.sh`](../tools/verify-no-gradle.sh) for
  a fast compile-and-unit-test check in between. See [toolchain.md](toolchain.md).
- Emulator install gotchas: a full `/data` makes `install-create` fail with a
  generic "Unknown failure" (wipe AVD data / size up userdata); IDE debug APKs
  are `testOnly`, so manual `adb install` needs `-t`. See [toolchain.md](toolchain.md).
- Local build shows a benign native-lib strip warning
  (`libandroidx.graphics.path.so`, `libdatastore_shared_counter.so`) when the
  NDK strip tool isn't present; the APK is still valid and CI is unaffected.
- `local.properties` is machine-specific and git-ignored; a fresh clone needs
  `sdk.dir` or `ANDROID_HOME`.
