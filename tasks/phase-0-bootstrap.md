# Phase 0 — Environment & Project Bootstrap

**Goal:** A buildable, empty Compose app on the toolchain with the dependency
set wired in. No product logic yet.

**Depends on:** nothing. **Blocks:** all other phases.

**Root README refs:** §23 (recommended dependencies), §19 (single app module).

---

## 0.1 Toolchain & repo hygiene
- [ ] Confirm Android Studio + JDK + Android SDK versions and record them in the repo
- [ ] Create the Android project: single `app` module, package `com.example.injuryplanner` (README §19)
- [ ] Add `.gitignore` entries for Android build artifacts (`/build`, `.gradle`, `local.properties`)
- [ ] Verify `./gradlew assembleDebug` succeeds on a clean checkout

## 0.2 Dependency catalog (README §23)
- [ ] Set up Gradle version catalog (`libs.versions.toml`)
- [ ] Add UI deps: `compose-bom`, `activity-compose`, `material3`
- [ ] Add lifecycle deps: `lifecycle-runtime-compose`, `lifecycle-viewmodel-compose`
- [ ] Add `navigation-compose`
- [ ] Add `kotlinx-serialization-json` + serialization plugin
- [ ] Add `datastore`
- [ ] Add DI: `hilt-android` (+ kapt/ksp), `hilt-navigation-compose`
- [ ] Add test deps: `junit`, `kotlinx-coroutines-test`, `turbine`
- [ ] Pin all versions to current-stable at implementation time (README §23 note)

## 0.3 Build sanity
- [ ] App launches to a blank `MainActivity` Compose surface
- [ ] Hilt `@HiltAndroidApp` application class compiles and runs
- [ ] Unit test source set runs (one trivial passing test)

---

## Completion criteria
- [ ] Clean clone builds and installs a debug APK.
- [ ] All §23 dependencies resolve.
- [ ] Empty app launches without crashing.
