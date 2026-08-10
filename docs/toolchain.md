# Toolchain & Build Notes (Phase 0)

This file records the exact toolchain the Android MVP is built against, per
Phase 0 task 0.1 ("record versions in the repo").

## Pinned versions

| Component            | Version            | Where pinned                         |
|----------------------|--------------------|--------------------------------------|
| JDK                  | Temurin 17 (17.0.8)| `compileOptions` / `kotlinOptions` (JVM 17) |
| Gradle               | 8.9                | `gradle/wrapper/gradle-wrapper.properties` |
| Android Gradle Plugin| 8.7.3              | `gradle/libs.versions.toml` (`agp`)  |
| Kotlin               | 2.0.21             | `gradle/libs.versions.toml` (`kotlin`) |
| KSP                  | 2.0.21-1.0.28      | `gradle/libs.versions.toml` (`ksp`)  |
| compileSdk / targetSdk | 35               | `app/build.gradle.kts`               |
| minSdk               | 24                 | `app/build.gradle.kts`               |
| Build Tools          | 35.0.0             | Android SDK                          |

All library versions are pinned in the Gradle version catalog at
[`gradle/libs.versions.toml`](../gradle/libs.versions.toml) (README §23).

## Android SDK (dev machine used for scaffolding)

- Location: `/opt/android_sdk` (see `local.properties`, git-ignored)
- Platforms installed: `android-31`, `android-33`, `android-34`, `android-35`, `android-36`
- Build tools: `35.0.0`
- `cmdline-tools/latest` and `platform-tools` present
- No Android Studio installed on this machine; the project was scaffolded by
  hand and is a standard AGP project that Android Studio (Koala / Ladybug or
  newer) can open directly.

## Building

Standard commands (require a JVM with normal network access — see caveat below):

```bash
./gradlew assembleDebug     # build the debug APK
./gradlew test              # run JVM unit tests
./gradlew installDebug      # install on a connected device/emulator
```

The Gradle wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/`) is checked in
and pinned to Gradle 8.9, so a clean clone needs only a JDK 17 and the Android
SDK.

## Build verification status

**Phase 1 — verified building, installing, and launching** via Android Studio 2024.3
on an Android 14 emulator: AGP produced `app-debug.apk`, it installed, and
`MainActivity` rendered the placeholder surface with no crash.

**Phases 2 and 3 — not yet verified by a real build.** Both were written on the host
described in the caveat below, where Gradle cannot run, so nothing in them has been
through AGP, KSP, Hilt code generation, aapt, lint, or a device. What *was* verified,
with [`tools/verify-no-gradle.sh`](../tools/verify-no-gradle.sh):

- every file under `app/src/main`, `app/src/test` and `app/src/androidTest` compiles,
  Compose screens included, using the real Compose and kotlinx-serialization compiler
  plugins;
- all 155 JVM unit tests pass.

That covers compilation and logic, and nothing else. Hilt's dependency graph in
particular is unvalidated — a missing binding compiles clean under the script and fails
at KSP. Before treating either phase as done, run `./gradlew test assembleDebug` in
Android Studio and work through the checklist in
[follow-ups.md](follow-ups.md#pending-verification).

### Environment caveat (Gradle from an automation-spawned shell)

**This does not apply to a normal interactive terminal.** `./gradlew test` works fine
from Terminal/iTerm on the scaffolding Mac. What follows applies to shells spawned by
tooling — coding agents, hooks, scripts launched outside the user's own session — whose
process tree the endpoint policy has not granted network access.

In such a shell, the endpoint policy denies the JVM all outbound TCP `connect()` —
**including loopback**. `curl` and Python reach the corporate proxy at
`localhost:10054`, but a JVM started there — Temurin 17 *and* Android Studio's bundled
JBR 21 — gets `SocketException: Operation not permitted` on every connect, even to
`127.0.0.1`.

Gradle runs the build in a separate daemon JVM reached over a loopback socket, so the
symptom is distinctive: `./gradlew --version` succeeds (no daemon needed), the daemon
process starts and logs `Daemon server started`, and the client then fails with
`Could not connect to the Gradle daemon`. `--no-daemon` does not help — it still forks.

If you hit that, you are in the wrong kind of shell. Build from your own terminal,
Android Studio, or CI; or use the script below for a fast compile-and-unit-test check
that needs no sockets at all.

### Workaround: compiling and running JVM tests without Gradle

The block is on *socket connect*, not on running a JVM, so the Kotlin compiler can be
invoked directly. Everything needed is already on the machine: Android Studio ships
kotlinc 2.0.21 — the version this project pins — and a Gradle sync leaves every
dependency, both compiler plugins, and `kotlin-compiler-embeddable` in the module cache.

[`tools/verify-no-gradle.sh`](../tools/verify-no-gradle.sh) does this. Run it from the
repo root:

```bash
tools/verify-no-gradle.sh           # compile everything, then run the unit tests
tools/verify-no-gradle.sh --quiet   # same, without the toolchain banner
```

It compiles `app/src/main`, `app/src/test` and `app/src/androidTest` in one pass with
the Compose and serialization compiler plugins, then runs every JUnit class in
`app/src/test`. Roughly 30 seconds from cold.

Two details worth knowing if it ever needs fixing:

- **`R` is generated from `strings.xml`** by a Python block in the script, standing in
  for aapt. That is not just scaffolding — a reference to a string that does not exist
  becomes a compile error, which is a check the IDE would otherwise be the only one
  doing.
- **Embeddable everything.** The compiler is `kotlin-compiler-embeddable` from the
  Gradle cache, not `kotlinc/lib/kotlin-compiler.jar`, because the Compose plugin
  published to Maven is built against the shaded compiler. Mixing the two fails with
  "the provided plugin ... is not compatible with this version of compiler". The
  embeddable compiler also bundles none of its own runtime dependencies, so the script
  supplies stdlib, reflect, script-runtime, trove4j, annotations and coroutines by hand.

**Limits.** No aapt, no KSP, no Hilt code generation, no lint, no packaging, and nothing
on a device. A missing Hilt binding or a resource that only exists in a preview will get
through. It is a fast correctness check, not a build — still build in Android Studio or
CI before calling something verified.

### Note on emulator installs

IDE "Run" builds mark the debug APK `testOnly`, so a manual install needs the
`-t` flag: `adb install -t -r app/build/intermediates/apk/debug/app-debug.apk`.
Also ensure the emulator has free disk (a full `/data` makes `install-create`
fail with a generic "Unknown failure"); wipe AVD data or size up userdata if so.
