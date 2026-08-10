package com.jgv.workoutplanner.domain.model

/**
 * The movement an exercise is built around (README §5.2).
 *
 * This is what makes substitution possible: two exercises sharing a pattern train the
 * same job, so one can stand in for the other when the first is filtered out. Plan
 * templates are written in terms of patterns rather than named exercises (README §12.4),
 * and the catalog guarantees each pattern in use has alternatives across equipment
 * types (README §26).
 *
 * The set is deliberately coarse. Knee-extension isolation (leg extension) is filed
 * under [SQUAT] as knee-dominant work rather than earning its own value — the
 * distinction that matters to a plan template is "which slot does this fill", and the
 * [ExerciseTag.ISOLATION] tag already separates it from a compound squat when ranking.
 */
enum class MovementPattern {
    HORIZONTAL_PUSH,
    VERTICAL_PUSH,
    HORIZONTAL_PULL,
    VERTICAL_PULL,
    SQUAT,
    LUNGE,
    HIP_HINGE,
    KNEE_FLEXION,
    HIP_EXTENSION,
    HIP_ABDUCTION,
    HIP_ADDUCTION,
    ELBOW_FLEXION,
    ELBOW_EXTENSION,
    SHOULDER_ABDUCTION,
    SHOULDER_EXTERNAL_ROTATION,
    CALF_RAISE,
    CORE_ANTI_EXTENSION,
    CORE_ANTI_ROTATION,
    CORE_FLEXION,
    CARRY,
}
