package com.jgv.workoutplanner.domain.model

/**
 * The part of the body an injury belongs to (README §4.4).
 *
 * Used purely to group the injury-history screen; it plays no part in filtering.
 * Filtering runs on the [MovementLimitation]s the user confirms, never on the
 * injuries themselves.
 */
enum class BodyRegion {
    NECK,
    SHOULDER,
    ELBOW,
    WRIST_HAND,
    UPPER_BACK,
    LOWER_BACK,
    HIP,
    KNEE,
    ANKLE_FOOT,
}
