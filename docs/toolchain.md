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

## Environment caveat (build not run on the scaffolding host)

The machine this project was scaffolded on enforces an endpoint network policy
that **denies the JVM all outbound TCP `connect()` calls — including loopback**
(verified: `curl`/Python reach the corporate proxy at `localhost:10054`, but the
Temurin JVM gets `SocketException: Operation not permitted` on every connect,
even to `127.0.0.1`).

Gradle's architecture runs the build in a separate daemon JVM that the launcher
talks to over a loopback TCP socket, so `./gradlew assembleDebug` cannot start a
build on this host. This is an environment restriction, not a project defect.

**To verify the build**, run the commands above on a developer machine / CI
runner with normal JVM networking (or where the JVM is allowlisted). Everything
needed is committed.
