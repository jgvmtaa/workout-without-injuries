package com.jgv.workoutplanner.domain.model

/**
 * How the limitations screen groups [MovementLimitation]s (spec §4.5).
 *
 * Twenty-seven checkboxes in one list is unreadable, and the screen has to be
 * scannable — the user is looking for the two or three that describe them. The
 * grouping mirrors the sections [MovementLimitation] is already commented into, so the
 * enum and the screen cannot drift apart.
 *
 * This is presentation structure, not filtering input: the engine reads the confirmed
 * limitations and never the group.
 */
enum class LimitationGroup {
    IMPACT_AND_LOCOMOTION,
    KNEE,
    SPINE,
    SHOULDER,
    ELBOW,
    WRIST_AND_GRIP,
    HIP,
}
