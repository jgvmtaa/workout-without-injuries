# Follow-ups / deferred items

Running list of things noticed during implementation that are intentionally
deferred (not bugs, not blockers). Grouped by the phase where they should be
picked up. Check items off as they're handled.

## Pending verification

Unlike the rest of this file, these are **not** deferred by choice — they are checks
that could not run on the machine Phase 2 was written on (see
[toolchain.md](toolchain.md#build-verification-status)). Clear them on a machine where
Gradle works, before building on top of Phase 2.

- [ ] **Build it.** `./gradlew test assembleDebug` in Android Studio. Nothing in
  Phase 2 has been through AGP, KSP, Hilt code generation, aapt, or lint. The 36 unit
  tests pass under a standalone kotlinc harness, but that harness runs neither the
  Hilt processor nor resource compilation. _(Phase 2)_
- [ ] **Check the two changed previews render:** `ExerciseDetailsScreen` and
  `ExerciseReplacementScreen`. Both signatures changed from `String` to `ExerciseId`.
  _(Phase 2)_
- [ ] **Walk the argument-carrying destinations:** Plan → exercise details, Plan →
  replace exercise, Library → exercise details. Each should show
  `Exercise: MACHINE_CHEST_PRESS`. This is the only behavioural change Phase 2 makes
  to the running app — it exercises the `ExerciseId` enum through the nav argument.
  Navigation 2.8.4 maps `SerialKind.ENUM` to `NavType.EnumType` with no `typeMap`
  needed, but that was confirmed by reading the library, not by running it. _(Phase 2)_
- [ ] **Expect ~28 unused-resource lint warnings.** The `limitation_*` strings have no
  consumer until the Phase 3 limitations screen. Lint is warning-only here (no `lint`
  block in `app/build.gradle.kts`, and the repo has no CI), so this is noise rather
  than breakage — but confirm the count is only those strings. _(Phase 2)_

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

## Address during Phase 2 (domain & catalogs)

- [x] **Type the exercise route arguments.** `AppRoute.ExerciseDetails` and
  `AppRoute.ExerciseReplacement` carry `exerciseId: String`; README §17 types them
  as `ExerciseId`. Swap once the enum exists — Navigation's type-safe API handles
  enums natively, so it is a mechanical change in `navigation/` plus the two screens.
  _(Phase 1 → done in Phase 2)_
- [x] **Replace the placeholder route arguments.** `AppNavigation.kt` hands
  `PLACEHOLDER_EXERCISE_ID` / `PLACEHOLDER_WORKOUT_DAY_ID` to the detail and
  replacement destinations so they are reachable. Real ids come from the catalog
  (Phase 2) and the generated plan (Phase 5). _(Phase 1 → partly done in Phase 2:
  the exercise argument is now a real catalog id, renamed `SAMPLE_EXERCISE_ID`.
  `PLACEHOLDER_WORKOUT_DAY_ID` stays until Phase 5 generates day ids.)_

## Address during Phase 4 (filtering) and Phase 5 (plan generation)

- [ ] **Six limitations exclude no exercise.** `AVOID_HIGH_IMPACT`, `AVOID_JUMPING`,
  `AVOID_RUNNING`, `AVOID_RAPID_DIRECTION_CHANGE`, `AVOID_SPINAL_ROTATION`, and
  `AVOID_LOADED_WRIST_FLEXION` match nothing in the catalog, because the MVP has no
  plyometric, running, loaded-rotation, or loaded-wrist-flexion exercises. Confirming
  one currently changes nothing. Don't write Phase 4 copy that implies otherwise, and
  revisit if conditioning work is ever added. _(Phase 2)_
- [ ] **`Equipment.CARDIO_MACHINE` has no exercises.** README §4.3 lists it as a
  selectable option, but the MVP catalog is strength-only, so selecting it is inert.
  Either hide it in the Phase 3 preferences screen or leave it and accept that it does
  nothing until conditioning exists. _(Phase 2)_
- [ ] **Isometric holds have no duration field.** `ExercisePrescription` models
  sets/reps/rest, so `FRONT_PLANK` and `SIDE_PLANK` use `reps = 1..1` and state the
  hold duration in their description. Fine for reading, awkward for a plan UI that
  renders "1 rep". Add a duration to the prescription when Phase 5 or 6 needs to show
  or edit it. _(Phase 2)_

## Address before/during Phases 3–6 (feature work)

- [ ] **Delete `core/ui/PlaceholderScreen.kt`.** Every stub screen shares it; it
  should disappear as each screen gets real content, along with the
  `placeholder_*` strings. _(Phase 1)_
- [ ] **Decide the start destination.** `AppNavigation` always starts at
  `Welcome`. Once onboarding completion is persisted (Phase 3), start at `Home` for
  returning users. _(Phase 1)_
- [ ] **Give profile editing its own exit path.** The Profile screen's edit actions
  reuse the onboarding destinations (`Preferences`, `InjuryHistory`,
  `MovementLimitations`), so "Continue" walks the user through the remainder of the
  onboarding flow instead of returning to Profile. Editing needs to save and return
  to Profile — and, per README §13/§25, warn that the existing plan is now outdated.
  Decide whether that is a nav-graph change (an edit sub-graph) or a mode flag on the
  shared screens. _(Phase 1)_

## Address before any release build (post-MVP / when shipping)

- [ ] **Release signing + minification.** The `release` build type in
  `app/build.gradle.kts` has `isMinifyEnabled = false` and no `signingConfig`.
  Set up a signing config and decide on R8/minify + `proguard-rules.pro` before
  producing a release APK/AAB. _(Phase 0)_

## Environment / FYI (no code change expected)

- Command-line Gradle cannot run on the primary scaffolding Mac (endpoint policy
  blocks shell-launched JVM TCP, so the Gradle daemon can't start). Build via
  Android Studio or CI. See [toolchain.md](toolchain.md).
- Emulator install gotchas: a full `/data` makes `install-create` fail with a
  generic "Unknown failure" (wipe AVD data / size up userdata); IDE debug APKs
  are `testOnly`, so manual `adb install` needs `-t`. See [toolchain.md](toolchain.md).
- Local build shows a benign native-lib strip warning
  (`libandroidx.graphics.path.so`, `libdatastore_shared_counter.so`) when the
  NDK strip tool isn't present; the APK is still valid and CI is unaffected.
- `local.properties` is machine-specific and git-ignored; a fresh clone needs
  `sdk.dir` or `ANDROID_HOME`.
