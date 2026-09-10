package com.jgv.workoutplanner.domain.model

/**
 * A movement the user has confirmed they need to avoid (spec §4.5).
 *
 * This is the single input the exercise filter runs on. Injuries only *suggest*
 * limitations; nothing here is ever set without the user confirming it (spec §2).
 *
 * Limitations describe movements, not diagnoses, so the same limitation can come from
 * unrelated injuries — which is exactly why the filter keys on these instead of on
 * [InjuryId].
 */
enum class MovementLimitation {
    // Impact and locomotion
    AVOID_HIGH_IMPACT,
    AVOID_JUMPING,
    AVOID_RUNNING,
    AVOID_RAPID_DIRECTION_CHANGE,

    // Knee
    AVOID_DEEP_KNEE_FLEXION,
    AVOID_KNEELING,
    AVOID_SINGLE_LEG_LOADING,
    AVOID_HEAVY_KNEE_LOADING,

    // Spine
    AVOID_LOADED_SPINAL_FLEXION,
    AVOID_LOADED_SPINAL_EXTENSION,
    AVOID_SPINAL_ROTATION,
    AVOID_UNSUPPORTED_HIP_HINGE,
    AVOID_HIGH_SPINAL_COMPRESSION,

    // Shoulder
    AVOID_OVERHEAD_PRESSING,
    AVOID_DEEP_SHOULDER_EXTENSION,
    AVOID_WIDE_GRIP_PRESSING,
    AVOID_SHOULDER_ABDUCTION,
    AVOID_INTERNAL_ROTATION_UNDER_LOAD,

    // Elbow
    AVOID_HEAVY_ELBOW_FLEXION,
    AVOID_HEAVY_ELBOW_EXTENSION,

    // Wrist and grip
    AVOID_LOADED_WRIST_EXTENSION,
    AVOID_LOADED_WRIST_FLEXION,
    AVOID_PRONATED_GRIP,
    AVOID_SUPINATED_GRIP,

    // Hip
    AVOID_DEEP_HIP_FLEXION,
    AVOID_WIDE_HIP_ABDUCTION,
    AVOID_UNILATERAL_BALANCE_DEMAND,
}
