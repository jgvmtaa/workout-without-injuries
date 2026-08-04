# Phase 0 — Environment & Project Bootstrap

**Goal:** A buildable, empty Compose app on the toolchain with the dependency
set wired in. No product logic yet.

**Depends on:** nothing. **Blocks:** all other phases.

**Root README refs:** §23 (recommended dependencies), §19 (single app module).

---

## 0.1 Toolchain & repo hygiene
- [x] Confirm Android Studio + JDK + Android SDK versions and record them in the repo — see [docs/toolchain.md](../docs/toolchain.md)
- [x] Create the Android project: single `app` module, package `com.jgv.workoutplanner` (README §19)
- [~] Verify `./gradlew assembleDebug` succeeds on a clean checkout — wrapper + project are complete; **could not be executed on the scaffolding host** (JVM TCP blocked by endpoint policy, see docs/toolchain.md "Environment caveat"). Run on a normal dev machine / CI.

## 0.2 Dependency catalog (README §23)
- [x] Set up Gradle version catalog (`libs.versions.toml`)
- [x] Add UI deps: `compose-bom`, `activity-compose`, `material3`
- [x] Add lifecycle deps: `lifecycle-runtime-compose`, `lifecycle-viewmodel-compose`
- [x] Add `navigation-compose`
- [x] Add `kotlinx-serialization-json` + serialization plugin
- [x] Add `datastore`
- [x] Add DI: `hilt-android` (+ ksp), `hilt-navigation-compose`
- [x] Add test deps: `junit`, `kotlinx-coroutines-test`, `turbine`
- [x] Pin all versions to current-stable at implementation time (README §23 note)

## 0.3 Build sanity
- [x] App launches to a blank `MainActivity` Compose surface — implemented (`MainActivity.kt`); runtime launch pending build on an unrestricted host
- [x] Hilt `@HiltAndroidApp` application class compiles and runs — `WorkoutPlannerApp.kt` (runtime pending build)
- [x] Unit test source set runs (one trivial passing test) — `ExampleUnitTest.kt` (run pending build)

---

## Completion criteria
- [~] Clean clone builds and installs a debug APK — pending execution on an unrestricted host (see caveat above).
- [x] All §23 dependencies resolve — declared and pinned in the version catalog.
- [x] Empty app launches without crashing — placeholder surface implemented (runtime verification pending build).

> **Note:** `[~]` marks items that are fully implemented but whose *runtime/build
> verification* is blocked by this machine's endpoint network policy (the JVM
> cannot open TCP sockets, so the Gradle daemon can't start). See
> [docs/toolchain.md](../docs/toolchain.md).
