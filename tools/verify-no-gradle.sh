#!/usr/bin/env bash
#
# Compile the app and run the JVM unit tests without Gradle.
#
# Why this exists: on some machines the Gradle daemon cannot be reached from a shell
# (see docs/toolchain.md — an endpoint policy denies shell-launched JVMs all outbound
# TCP, including loopback), so `./gradlew test` cannot run even though the JDK, the
# Kotlin compiler and every dependency are present. The block is on socket connect, not
# on running a JVM, so kotlinc can be driven directly.
#
# What it covers: every file under app/src/main and app/src/test, Compose screens
#   included — the Compose and serialization compiler plugins are both in the Gradle
#   cache — then runs every JUnit test class in app/src/test.
#
# What it does NOT cover: aapt (R is generated from strings.xml by the Python block
#   below, so a missing string is a compile error but a missing drawable is not), KSP,
#   Hilt code generation and its dependency-graph validation, lint, packaging, and
#   instrumented tests. A missing Hilt binding compiles fine here and fails in Android
#   Studio. Treat this as a fast correctness check, not a build: run
#   `./gradlew test assembleDebug` before calling anything verified.
#
# Everything it uses comes from the Gradle cache and the Android Studio install, so run
# a Gradle sync in the IDE once before the first run.
#
# Usage: tools/verify-no-gradle.sh [--quiet]

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD_DIR="$REPO_ROOT/build/verify-no-gradle"
QUIET="${1:-}"

log() { [[ "$QUIET" == "--quiet" ]] || echo "$@"; }

# --------------------------------------------------------------------- toolchain

STUDIO_KOTLINC="/Applications/Android Studio.app/Contents/plugins/Kotlin/kotlinc"
STUDIO_JBR="/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/java"

if [[ -n "${KOTLINC_HOME:-}" ]]; then
  KOTLINC="$KOTLINC_HOME"
elif [[ -d "$STUDIO_KOTLINC" ]]; then
  KOTLINC="$STUDIO_KOTLINC"
else
  echo "error: kotlinc not found. Set KOTLINC_HOME to a Kotlin 2.0.21 distribution." >&2
  exit 1
fi

if [[ -x "$STUDIO_JBR" ]]; then
  JAVA="$STUDIO_JBR"
elif [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" ]]; then
  JAVA="$JAVA_HOME/bin/java"
else
  JAVA="$(command -v java)"
fi

ANDROID_SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-/opt/android_sdk}}"
# Match the project's compileSdk so this checks against the same API surface AGP would.
COMPILE_SDK="$(sed -nE 's/^[[:space:]]*compileSdk[[:space:]]*=[[:space:]]*([0-9]+).*/\1/p' \
  "$REPO_ROOT/app/build.gradle.kts" | head -1)"
ANDROID_JAR="$ANDROID_SDK/platforms/android-${COMPILE_SDK}/android.jar"
if [[ ! -f "$ANDROID_JAR" ]]; then
  ANDROID_JAR="$(ls -d "$ANDROID_SDK"/platforms/android-*/android.jar 2>/dev/null | sort -V | tail -1 || true)"
fi
if [[ -z "$ANDROID_JAR" || ! -f "$ANDROID_JAR" ]]; then
  echo "error: no android.jar under $ANDROID_SDK/platforms. Set ANDROID_HOME." >&2
  exit 1
fi

GRADLE_CACHE="${GRADLE_USER_HOME:-$HOME/.gradle}/caches/modules-2/files-2.1"
if [[ ! -d "$GRADLE_CACHE" ]]; then
  echo "error: no Gradle module cache at $GRADLE_CACHE. Sync the project once in Android Studio." >&2
  exit 1
fi

log "kotlinc:    $KOTLINC"
log "java:       $JAVA"
log "android.jar: $ANDROID_JAR"

rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/classes" "$BUILD_DIR/gen" "$BUILD_DIR/aar"

# ------------------------------------------------------------------- classpath

CP_PARTS=("$ANDROID_JAR" "$KOTLINC/lib/kotlin-stdlib.jar" "$KOTLINC/lib/kotlin-reflect.jar")

# One jar per artifact, highest version. Globbing the cache instead would put two
# coroutines versions on the classpath — a transitive 1.6.4 alongside the pinned 1.9.0 —
# and the tests then die with IncompatibleClassChangeError rather than a useful message.
add_jar() {
  local coordinate="$1" # group/artifact
  local dir="$GRADLE_CACHE/$coordinate"
  if [[ ! -d "$dir" ]]; then
    echo "warning: $coordinate not in the Gradle cache" >&2
    return 0
  fi
  local version
  version="$(find "$dir" -maxdepth 1 -mindepth 1 -type d -exec basename {} \; | sort -V | tail -1)"
  local jar
  jar="$(find "$dir/$version" -name '*.jar' ! -name '*-sources.jar' ! -name '*-javadoc.jar' \
    | head -1 || true)"
  [[ -n "$jar" ]] && CP_PARTS+=("$jar")
}

add_jar androidx.annotation/annotation-jvm
add_jar androidx.lifecycle/lifecycle-common-jvm
add_jar org.jetbrains.kotlinx/kotlinx-coroutines-core-jvm
add_jar org.jetbrains.kotlinx/kotlinx-coroutines-test-jvm
add_jar org.jetbrains.kotlinx/kotlinx-serialization-core-jvm
add_jar org.jetbrains.kotlinx/kotlinx-serialization-json-jvm
add_jar androidx.datastore/datastore-preferences-core-jvm
add_jar androidx.datastore/datastore-core-okio-jvm
add_jar com.squareup.okio/okio-jvm
add_jar javax.inject/javax.inject
add_jar jakarta.inject/jakarta.inject-api
add_jar com.google.dagger/dagger
add_jar com.google.dagger/hilt-core
add_jar junit/junit
add_jar org.hamcrest/hamcrest-core
add_jar app.cash.turbine/turbine-jvm

# AAR dependencies: the classes are inside classes.jar. Every AAR in the cache is
# included — Compose alone pulls in a dozen — deduplicated the same way, highest version
# per artifact.
while IFS= read -r artifact_dir; do
  version="$(find "$artifact_dir" -maxdepth 1 -mindepth 1 -type d -exec basename {} \; \
    | sort -V | tail -1)"
  aar="$(find "$artifact_dir/$version" -name '*.aar' | head -1 || true)"
  [[ -z "$aar" ]] && continue
  dest="$BUILD_DIR/aar/$(basename "$(dirname "$artifact_dir")")-$(basename "$artifact_dir")"
  mkdir -p "$dest"
  (cd "$dest" && unzip -oq "$aar" classes.jar 2>/dev/null) || true
  [[ -f "$dest/classes.jar" ]] && CP_PARTS+=("$dest/classes.jar")
done < <(find "$GRADLE_CACHE" -name '*.aar' | sed -E 's|/[^/]+/[^/]+/[^/]+\.aar$||' | sort -u)

CP="$(IFS=:; echo "${CP_PARTS[*]}")"

# ------------------------------------------------------ generated R (aapt stand-in)

# aapt normally generates R. Parsing strings.xml instead costs nothing and adds a real
# check: a reference to a string that does not exist becomes a compile error.
python3 - "$REPO_ROOT" "$BUILD_DIR/gen/R.kt" <<'PY'
import re, sys, xml.etree.ElementTree as ET

repo, out = sys.argv[1], sys.argv[2]
tree = ET.parse(f"{repo}/app/src/main/res/values/strings.xml")
root = tree.getroot()

strings = [e.get("name") for e in root.findall("string")]
plurals = [e.get("name") for e in root.findall("plurals")]

def block(kind, names):
    body = "\n".join(
        f"        const val {n}: Int = {i + 1}" for i, n in enumerate(sorted(names))
    )
    return f"    object {kind} {{\n{body}\n    }}"

with open(out, "w") as f:
    f.write("package com.jgv.workoutplanner\n\n")
    f.write("/** Stand-in for the aapt-generated R, built from res/values/strings.xml. */\n")
    f.write("object R {\n")
    f.write(block("string", strings) + "\n")
    f.write(block("plurals", plurals) + "\n")
    f.write("}\n")

print(f"generated R with {len(strings)} strings and {len(plurals)} plurals")
PY

# --------------------------------------------------------------- source selection

MAIN="$REPO_ROOT/app/src/main/java/com/jgv/workoutplanner"
TEST="$REPO_ROOT/app/src/test/java/com/jgv/workoutplanner"
ANDROID_TEST="$REPO_ROOT/app/src/androidTest/java/com/jgv/workoutplanner"

# androidTest is compiled but not run — it needs a device. Type-checking it here still
# catches the usual breakage: a screen signature that changed and a test that did not.
SOURCES=("$BUILD_DIR/gen/R.kt")
while IFS= read -r file; do
  SOURCES+=("$file")
done < <(find "$MAIN" "$TEST" "$ANDROID_TEST" -name '*.kt' | sort)

log "compiling ${#SOURCES[@]} files"

# Compiler plugins:
#   serialization — @Serializable on AppRoute and the persisted models
#   compose       — @Composable, without which the screens do not compile
#
# Both are the `-embeddable` artifacts from the Gradle cache, and the compiler driven
# below is `kotlin-compiler-embeddable` to match. A plugin built against the shaded
# compiler cannot be loaded by the unshaded one — mixing them fails with "the provided
# plugin ... is not compatible with this version of compiler". Same Kotlin version
# either way; only the packaging differs.
PLUGIN_ARG=()
for plugin in kotlin-serialization-compiler-plugin-embeddable kotlin-compose-compiler-plugin-embeddable; do
  jar="$(find "$GRADLE_CACHE/org.jetbrains.kotlin/$plugin" -name '*.jar' 2>/dev/null \
    | grep -vE 'sources|javadoc' | sort -V | tail -1 || true)"
  if [[ -n "$jar" ]]; then
    PLUGIN_ARG+=(-Xplugin="$jar")
  else
    echo "warning: $plugin not in the Gradle cache" >&2
  fi
done

COMPILER_JAR="$(find "$GRADLE_CACHE/org.jetbrains.kotlin/kotlin-compiler-embeddable" \
  -name '*.jar' 2>/dev/null | grep -vE 'sources|javadoc' | sort -V | tail -1 || true)"
if [[ -z "$COMPILER_JAR" ]]; then
  echo "error: kotlin-compiler-embeddable not in the Gradle cache." >&2
  exit 1
fi

# Unlike the fat kotlin-compiler.jar, the embeddable one bundles none of its own runtime
# dependencies — stdlib, coroutines and trove all have to be supplied.
COROUTINES_JAR="$(find "$GRADLE_CACHE/org.jetbrains.kotlinx/kotlinx-coroutines-core-jvm" \
  -name '*.jar' 2>/dev/null | grep -vE 'sources|javadoc' | sort -V | tail -1)"
COMPILER_CP="$COMPILER_JAR"
COMPILER_CP="$COMPILER_CP:$KOTLINC/lib/kotlin-stdlib.jar"
COMPILER_CP="$COMPILER_CP:$KOTLINC/lib/kotlin-reflect.jar"
COMPILER_CP="$COMPILER_CP:$KOTLINC/lib/kotlin-script-runtime.jar"
COMPILER_CP="$COMPILER_CP:$KOTLINC/lib/trove4j.jar"
COMPILER_CP="$COMPILER_CP:$KOTLINC/lib/annotations-13.0.jar"
COMPILER_CP="$COMPILER_CP:$COROUTINES_JAR"

"$JAVA" -cp "$COMPILER_CP" \
  org.jetbrains.kotlin.cli.jvm.K2JVMCompiler \
  -jvm-target 17 \
  -nowarn \
  -no-stdlib \
  -no-reflect \
  -Xdisable-default-scripting-plugin \
  -classpath "$CP" \
  -d "$BUILD_DIR/classes" \
  "${PLUGIN_ARG[@]}" \
  "${SOURCES[@]}"

log "compiled OK"

# ------------------------------------------------------------------------- tests

TEST_CP="$BUILD_DIR/classes:$CP:$KOTLINC/lib/kotlin-stdlib.jar:$KOTLINC/lib/kotlin-reflect.jar"

TEST_CLASSES=()
while IFS= read -r file; do
  rel="${file#"$TEST"/}"
  TEST_CLASSES+=("com.jgv.workoutplanner.${rel%.kt}")
done < <(find "$TEST" -name '*Test.kt' | sort)

# Turn path separators into package separators.
for i in "${!TEST_CLASSES[@]}"; do
  TEST_CLASSES[$i]="${TEST_CLASSES[$i]//\//.}"
done

log "running ${#TEST_CLASSES[@]} test classes"
"$JAVA" -cp "$TEST_CP" org.junit.runner.JUnitCore "${TEST_CLASSES[@]}"
