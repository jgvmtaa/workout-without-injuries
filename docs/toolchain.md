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

**Phase 2 — not yet verified by a real build.** It was written on the host described
in the caveat below, where Gradle cannot run, so nothing in it has been through AGP,
KSP, Hilt code generation, aapt, or lint. What *was* verified, using the kotlinc
workaround further down:

- the domain models, both catalogs, and both repositories compile clean;
- their 36 unit tests pass;
- the whole `app/src/main/java` tree type-checks, including the Compose and DI files
  (bytecode generation then fails inside the hand-assembled classpath, which is a
  limitation of the workaround rather than a problem in the source).

Before treating Phase 2 as done, run `./gradlew test assembleDebug` in Android Studio
and work through the checklist in [follow-ups.md](follow-ups.md#pending-verification).

### Environment caveat (command-line Gradle on the scaffolding host)

The machine this project was scaffolded on enforces an endpoint network policy
that **denies shell-launched JVMs all outbound TCP `connect()` — including
loopback** (verified: `curl`/Python reach the corporate proxy at
`localhost:10054`, but any JVM started from the shell — Temurin 17 *and* Android
Studio's bundled JBR 21 — gets `SocketException: Operation not permitted` on
every connect, even to `127.0.0.1`). Network access is granted to the Android
Studio *GUI app's* process tree, not to shell-spawned java.

Because Gradle runs the build in a separate daemon JVM reached over a loopback
socket, `./gradlew assembleDebug` from a terminal cannot start a build on this
host. **Use Android Studio (or a CI runner) to build here.** On a developer
machine / CI with normal JVM networking, the command-line `./gradlew` commands
above work directly.

### Workaround: type-checking and running JVM tests without Gradle

The block is on *socket connect*, not on running a JVM, so the Kotlin compiler can be
invoked directly. Android Studio bundles kotlinc 2.0.21 — the version this project
pins — and every dependency is already extracted in the Gradle cache. That is enough
to compile the pure-Kotlin layers (domain, catalogs, repositories) and run their JUnit
tests on this host:

```bash
JAVA="/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/java"
KOTLINC="/Applications/Android Studio.app/Contents/plugins/Kotlin/kotlinc"
CACHE=~/.gradle/caches/modules-2/files-2.1

# Dependency jars (junit, hamcrest, javax.inject, coroutines, androidx.annotation)
# live under $CACHE; find them with:
#   find $CACHE -name '*.jar' | grep -vE 'sources|javadoc'

"$JAVA" -cp "$KOTLINC/lib/kotlin-compiler.jar" \
  org.jetbrains.kotlin.cli.jvm.K2JVMCompiler \
  -jvm-target 17 -classpath "$DEPS" -d out \
  path/to/R-stub app/src/main/java/com/jgv/workoutplanner/domain \
  app/src/main/java/com/jgv/workoutplanner/data app/src/test/java/...

"$JAVA" -cp "out:$DEPS:$KOTLINC/lib/kotlin-stdlib.jar" \
  org.junit.runner.JUnitCore com.jgv.workoutplanner.data.catalog.ExerciseCatalogTest
```

Catalog code references `R.string.*`, which aapt normally generates. Generate a
stand-in instead — an `object R { object string { const val <name> = <n> } }` built by
parsing `res/values/strings.xml`. It costs nothing and adds a real check: a reference
to a string that does not exist becomes a compile error.

**Limits.** This does not run aapt, KSP, Hilt code generation, lint, or the Compose
compiler plugin, so it does not replace a build. Compose sources type-check under this
setup but fail during bytecode generation (the hand-assembled classpath is not the one
AGP builds). Treat it as a fast correctness check for the non-Android layers, and
still build in Android Studio or CI before calling something verified.

### Note on emulator installs

IDE "Run" builds mark the debug APK `testOnly`, so a manual install needs the
`-t` flag: `adb install -t -r app/build/intermediates/apk/debug/app-debug.apk`.
Also ensure the emulator has free disk (a full `/data` makes `install-create`
fail with a generic "Unknown failure"); wipe AVD data or size up userdata if so.
