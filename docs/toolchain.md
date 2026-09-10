# Android Toolchain and Build Guide

This file explains how the Android app selects its toolchain, how to build it, and how
to diagnose environment failures. Build files are the source of truth for versions so
this document does not duplicate values that can drift.

## Version sources

| Component | Source of truth |
|---|---|
| Compilation JDK | `kotlin.jvmToolchain` in [`app/build.gradle.kts`](../app/build.gradle.kts) |
| Gradle / AGP JVM | `JAVA_HOME` locally; `java-version` in [`.github/workflows/ci.yml`](../.github/workflows/ci.yml) |
| Java/Kotlin bytecode target | `compileOptions` and `kotlinOptions` in [`app/build.gradle.kts`](../app/build.gradle.kts) |
| Gradle | [`gradle/wrapper/gradle-wrapper.properties`](../gradle/wrapper/gradle-wrapper.properties) |
| Plugins and libraries | [`gradle/libs.versions.toml`](../gradle/libs.versions.toml) |
| Android SDK levels | `compileSdk`, `targetSdk`, and `minSdk` in [`app/build.gradle.kts`](../app/build.gradle.kts) |
| Android Build Tools | The default selected by the pinned Android Gradle Plugin |

Those first three rows are three different things, and it is worth being precise
because the difference is easy to get wrong:

- **Compilation JDK** — what `javac`/`kotlinc` run on. `jvmToolchain` selects the required
  language version, and Gradle fails with "no matching toolchain" if none is available.
  It does not constrain vendor or patch level unless the build explicitly says so.
- **Gradle / AGP JVM** — what the Gradle daemon, and therefore AGP itself, runs on.
  This comes from `JAVA_HOME` locally and the CI workflow in automation. It must satisfy
  the Android Gradle Plugin's runtime requirement regardless of the compilation
  toolchain. A repository-level pin would require `org.gradle.java.home` or Gradle
  Daemon JVM criteria; the repository configures neither.
- **Bytecode target** — what class-file version is emitted, set by `compileOptions` and
  `kotlinOptions`. Independent of both JDKs above.

CI's `java-version` is deliberately a floating major, so security patches land without
a repository change. It therefore does not guarantee byte-identical output against a
developer's fixed local patch level.

Direct dependency versions are pinned in the Gradle version catalog at
[`gradle/libs.versions.toml`](../gradle/libs.versions.toml). Transitive
versions are *not* locked — they are whatever the pinned directs resolve to. Fixing
that means Gradle **dependency locking** (`./gradlew dependencies --write-locks`),
which is the only one of the two mechanisms that constrains *resolution*. A
`verification-metadata.xml` is a different tool for a different problem: it checks the
**integrity** of resolved artifacts via checksums and signatures, and does not stop a
version from changing. The repository configures neither mechanism.

### Compatibility constraints

These are constraints, not preferences — changing one may require changing the others:

- The Android Gradle Plugin determines the supported Gradle and runtime-JDK ranges.
- KSP versions are Kotlin-specific; update Kotlin and KSP together.
- The Compose compiler plugin is versioned in lockstep with Kotlin, so
  `kotlin-compose` and `kotlin` share a version reference.

## Android SDK

- Install the platform selected by `compileSdk`; let the pinned Android Gradle Plugin
  select its default compatible Build Tools version.
- The SDK location comes from `local.properties` (git-ignored) or `ANDROID_HOME`.
- The project is a standard Android Gradle Plugin project that a compatible Android
  Studio release opens directly.

## Building

```bash
./gradlew assembleDebug              # build the debug APK
./gradlew test                       # run JVM unit tests
./gradlew lint                       # Android lint
./gradlew connectedDebugAndroidTest  # instrumented tests on a device/emulator
./gradlew installDebug               # install on a connected device/emulator
```

The Gradle wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/`) is checked in and
selects the repository's Gradle version. A clean clone needs a compatible JDK and the
Android SDK platform selected by the build.

The configuration cache is enabled in `gradle.properties`. If a plugin turns out not to
support it, the failure names the plugin; set `org.gradle.configuration-cache=false` to
unblock and record the incompatibility.

## Continuous integration

[`.github/workflows/ci.yml`](../.github/workflows/ci.yml) runs on every push and pull
request to `main`: the configured JDK, then `test`, `lint`, and `assembleDebug`, with
test and lint reports uploaded as artifacts. Treat this workflow as the canonical
automated build environment.

CI excludes instrumented tests. Run `connectedDebugAndroidTest` on a compatible local
device or emulator. Adding device coverage to CI requires a separate emulator job, such
as one based on `reactivecircus/android-emulator-runner`.

## Validation guidance

Before submitting a change, run:

```bash
./gradlew test lint assembleDebug
./gradlew connectedDebugAndroidTest  # when a compatible device or emulator is available
```

The first command exercises unit tests, KSP, Hilt code generation, resource processing,
dexing, lint, and APK packaging. Inspect
`app/build/reports/lint-results-debug.html` rather than recording warning counts here,
because dependency-update results change as releases become available.

Run lint without `--offline`: dependency-version checks consult a remote index, so an
offline run can omit update warnings. Treat dependency updates as coordinated toolchain
work rather than suppressing those warnings.

The configuration cache is enabled. After changing build plugins, run the same Gradle
command twice and confirm that the second invocation reports `Configuration cache entry
reused`.

Run instrumented tests on a device or emulator whose API level is compatible with the
app. For an emulator, `emulator-check accel` reports whether hardware acceleration is
available before boot.

Persistence across process death requires manual or dedicated instrumentation coverage;
see [follow-ups.md](follow-ups.md#pending-verification).

## Screenshot test setup

Implementing the screenshot suite in [`tests.spec`](../tests.spec) requires:

- **Robolectric**, to run Compose layout on the JVM at a pinned SDK level.
- **Roborazzi** (Gradle plugin plus the Compose artifact), for capture and comparison.

Robolectric requires
`android.testOptions.unitTests.isIncludeAndroidResources = true` in
`app/build.gradle.kts`. Choose Robolectric and Roborazzi versions compatible with the
Kotlin and Compose versions in the catalog, add the dependencies there, and update this
section in the same change.

## Environment caveat (Gradle from an automation-spawned shell)

Use this section only for shells spawned by tooling — coding agents, hooks, or scripts
launched outside the user's own session. In a normal interactive terminal, use the
standard Gradle commands above.

Automation shells can fail in two distinct ways, each requiring a different response:

1. **Toolchain not on the default path.** The shell starts on a JDK too old for AGP and
   with no `ANDROID_HOME`, so the build dies during configuration — first with a minimum
   JVM-version error from AGP resolution, then, once a JDK is supplied, with "SDK
   location not found". Both are environment, not project: point `JAVA_HOME` at a full
   installed JDK containing both `java` and `javac`, and point `ANDROID_HOME` at the
   installed SDK. Setting `sdk.dir` in
   `local.properties` (git-ignored) is the durable alternative to exporting
   `ANDROID_HOME` every session. If Gradle cannot discover the compilation JDK selected
   by `jvmToolchain`, add its path to `org.gradle.java.installations.paths` in the
   **user-level** Gradle properties, never the repository's machine-independent file.
2. **Loopback blocked.** Some endpoint policies deny the JVM outbound TCP `connect()`,
   including loopback, even when other processes can reach a local proxy. Gradle runs
   the build in a separate daemon JVM reached over a loopback socket, so the symptom is
   distinctive: `./gradlew --version` succeeds, the daemon logs `Daemon server started`,
   and the client then fails with `Could not connect to the Gradle daemon`. `--no-daemon`
   does not help because it still forks.

In either case you are in the wrong kind of shell. Build from your own terminal,
Android Studio, or CI.

## Note on emulator installs

IDE "Run" builds mark the debug APK `testOnly`, so a manual install needs the
`-t` flag: `adb install -t -r app/build/intermediates/apk/debug/app-debug.apk`.
Also ensure the emulator has free disk (a full `/data` makes `install-create`
fail with a generic "Unknown failure"); wipe AVD data or size up userdata if so.
