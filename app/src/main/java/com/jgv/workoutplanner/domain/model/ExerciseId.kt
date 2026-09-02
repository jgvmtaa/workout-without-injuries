package com.jgv.workoutplanner.domain.model

import androidx.annotation.Keep

/**
 * Stable identifier for every exercise in the catalog (README §5.3, §6).
 *
 * The enum name is the identity and is what gets persisted, passed between screens,
 * and referenced by a [WorkoutPlan]. Display names live in `strings.xml` and are
 * reached through [ExerciseDefinition.nameRes] — never use a display name as an id.
 *
 * Grouped by primary muscle to match the catalog's own ordering, which keeps the two
 * files readable side by side. Order carries no meaning: ranking ties break on
 * [name], so plan generation stays deterministic regardless of how this list grows
 * (README §12.5).
 */
@Keep
enum class ExerciseId {
    // Chest
    PUSH_UP,
    MACHINE_CHEST_PRESS,
    DUMBBELL_BENCH_PRESS,
    INCLINE_DUMBBELL_PRESS,
    BARBELL_BENCH_PRESS,
    CABLE_CHEST_FLY,

    // Back
    LAT_PULLDOWN,
    ASSISTED_PULL_UP,
    PULL_UP,
    SEATED_CABLE_ROW,
    CHEST_SUPPORTED_DUMBBELL_ROW,
    ISO_LATERAL_ROW,
    BARBELL_ROW,
    BAND_ROW,

    // Shoulders
    MACHINE_SHOULDER_PRESS,
    SEATED_DUMBBELL_SHOULDER_PRESS,
    DUMBBELL_LATERAL_RAISE,
    CABLE_LATERAL_RAISE,
    BAND_LATERAL_RAISE,
    CABLE_REAR_DELT_FLY,
    FACE_PULL,
    BAND_PULL_APART,

    // Biceps
    DUMBBELL_CURL,
    HAMMER_CURL,
    CABLE_CURL,
    BAND_CURL,

    // Triceps
    CABLE_TRICEPS_PRESSDOWN,
    OVERHEAD_CABLE_TRICEPS_EXTENSION,
    BAND_TRICEPS_PRESSDOWN,
    ASSISTED_DIP,
    DIP,

    // Quadriceps
    BODYWEIGHT_SQUAT,
    GOBLET_SQUAT,
    LEG_PRESS,
    LEG_EXTENSION,
    BULGARIAN_SPLIT_SQUAT,
    WALKING_LUNGE,
    STEP_UP,

    // Hamstrings
    DUMBBELL_ROMANIAN_DEADLIFT,
    SINGLE_LEG_ROMANIAN_DEADLIFT,
    SEATED_LEG_CURL,
    LYING_LEG_CURL,
    NORDIC_HAMSTRING_CURL,

    // Glutes
    GLUTE_BRIDGE,
    HIP_THRUST,
    CABLE_PULL_THROUGH,
    HIP_ABDUCTION_MACHINE,
    BAND_HIP_ABDUCTION,
    HIP_ADDUCTION_MACHINE,
    BAND_HIP_ADDUCTION,

    // Calves
    STANDING_CALF_RAISE,
    SEATED_CALF_RAISE,

    // Core
    DEAD_BUG,
    BIRD_DOG,
    FRONT_PLANK,
    SIDE_PLANK,
    PALLOF_PRESS,
    CABLE_CRUNCH,
    HANGING_KNEE_RAISE,
}
