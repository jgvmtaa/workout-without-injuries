# Phase 0 — Environment & Project Bootstrap

**Goal:** A buildable, empty Compose app on the toolchain with the dependency
set wired in. No product logic yet.

**Depends on:** nothing. **Blocks:** all other phases.

**Root README refs:** §23 (recommended dependencies), §19 (single app module).

---

## 0.1 Toolchain & repo hygiene
- [x] Confirm Android Studio + JDK + Android SDK versions and record them in the repo — see [docs/toolchain.md](../docs/toolchain.md)
- [x] Create the Android project: single `app` module, package `com.jgv.workoutplanner` (README §19)
- [x] Verify `./gradlew assembleDebug` succeeds on a clean checkout — **verified via Android Studio**: AGP produced `app-debug.apk`, installed and launched on an Android 14 emulator. (CLI `gradlew` still can't run on the scaffolding host due to the JVM TCP block — see docs/toolchain.md.)

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
- [x] App launches to a blank `MainActivity` Compose surface — verified on emulator (renders "Workout Planner", `Displayed .../.MainActivity`, no crash)
- [x] Hilt `@HiltAndroidApp` application class compiles and runs — `WorkoutPlannerApp.kt`; Hilt codegen ran and the app process started cleanly
- [x] Unit test source set runs (one trivial passing test) — `ExampleUnitTest.kt`

---

## Completion criteria
- [x] Clean clone builds and installs a debug APK — verified via Android Studio on an Android 14 emulator.
- [x] All §23 dependencies resolve — declared and pinned in the version catalog; all coordinates verified against Maven repos.
- [x] Empty app launches without crashing — verified on emulator.
