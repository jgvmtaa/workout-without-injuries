# Follow-ups / deferred items

Running list of things noticed during implementation that are intentionally
deferred (not bugs, not blockers). Grouped by the phase where they should be
picked up. Check items off as they're handled.

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

- [ ] **Type the exercise route arguments.** `AppRoute.ExerciseDetails` and
  `AppRoute.ExerciseReplacement` carry `exerciseId: String`; README §17 types them
  as `ExerciseId`. Swap once the enum exists — Navigation's type-safe API handles
  enums natively, so it is a mechanical change in `navigation/` plus the two screens.
  _(Phase 1)_
- [ ] **Replace the placeholder route arguments.** `AppNavigation.kt` hands
  `PLACEHOLDER_EXERCISE_ID` / `PLACEHOLDER_WORKOUT_DAY_ID` to the detail and
  replacement destinations so they are reachable. Real ids come from the catalog
  (Phase 2) and the generated plan (Phase 5). _(Phase 1)_

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
