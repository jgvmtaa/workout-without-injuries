package com.jgv.workoutplanner.data.catalog

import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.domain.model.LimitationGroup
import com.jgv.workoutplanner.domain.model.MovementLimitation
import com.jgv.workoutplanner.domain.model.MovementLimitationDefinition

/**
 * Label and grouping for every [MovementLimitation] (spec §4.5, §6).
 *
 * This mapping lets the limitations screen render the `limitation_*` resources for the
 * enum. It is the third catalog
 * alongside [ExerciseCatalog] and [InjuryCatalog], and structurally the simplest —
 * there is no judgement here, only copy and ordering.
 *
 * Order within a group is the order the screen renders, and it follows
 * [MovementLimitation]'s own declaration order so the two files read the same way.
 */
object MovementLimitationCatalog {

    val limitations: List<MovementLimitationDefinition> = listOf(

        // ------------------------------------------------ Impact and locomotion

        MovementLimitationDefinition(
            id = MovementLimitation.AVOID_HIGH_IMPACT,
            nameRes = R.string.limitation_avoid_high_impact,
            group = LimitationGroup.IMPACT_AND_LOCOMOTION,
        ),
        MovementLimitationDefinition(
            id = MovementLimitation.AVOID_JUMPING,
            nameRes = R.string.limitation_avoid_jumping,
            group = LimitationGroup.IMPACT_AND_LOCOMOTION,
        ),
        MovementLimitationDefinition(
            id = MovementLimitation.AVOID_RUNNING,
            nameRes = R.string.limitation_avoid_running,
            group = LimitationGroup.IMPACT_AND_LOCOMOTION,
        ),
        MovementLimitationDefinition(
            id = MovementLimitation.AVOID_RAPID_DIRECTION_CHANGE,
            nameRes = R.string.limitation_avoid_rapid_direction_change,
            group = LimitationGroup.IMPACT_AND_LOCOMOTION,
        ),

        // ------------------------------------------------------------------ Knee

        MovementLimitationDefinition(
            id = MovementLimitation.AVOID_DEEP_KNEE_FLEXION,
            nameRes = R.string.limitation_avoid_deep_knee_flexion,
            group = LimitationGroup.KNEE,
        ),
        MovementLimitationDefinition(
            id = MovementLimitation.AVOID_KNEELING,
            nameRes = R.string.limitation_avoid_kneeling,
            group = LimitationGroup.KNEE,
        ),
        MovementLimitationDefinition(
            id = MovementLimitation.AVOID_SINGLE_LEG_LOADING,
            nameRes = R.string.limitation_avoid_single_leg_loading,
            group = LimitationGroup.KNEE,
        ),
        MovementLimitationDefinition(
            id = MovementLimitation.AVOID_HEAVY_KNEE_LOADING,
            nameRes = R.string.limitation_avoid_heavy_knee_loading,
            group = LimitationGroup.KNEE,
        ),

        // ----------------------------------------------------------------- Spine

        MovementLimitationDefinition(
            id = MovementLimitation.AVOID_LOADED_SPINAL_FLEXION,
            nameRes = R.string.limitation_avoid_loaded_spinal_flexion,
            group = LimitationGroup.SPINE,
        ),
        MovementLimitationDefinition(
            id = MovementLimitation.AVOID_LOADED_SPINAL_EXTENSION,
            nameRes = R.string.limitation_avoid_loaded_spinal_extension,
            group = LimitationGroup.SPINE,
        ),
        MovementLimitationDefinition(
            id = MovementLimitation.AVOID_SPINAL_ROTATION,
            nameRes = R.string.limitation_avoid_spinal_rotation,
            group = LimitationGroup.SPINE,
        ),
        MovementLimitationDefinition(
            id = MovementLimitation.AVOID_UNSUPPORTED_HIP_HINGE,
            nameRes = R.string.limitation_avoid_unsupported_hip_hinge,
            group = LimitationGroup.SPINE,
        ),
        MovementLimitationDefinition(
            id = MovementLimitation.AVOID_HIGH_SPINAL_COMPRESSION,
            nameRes = R.string.limitation_avoid_high_spinal_compression,
            group = LimitationGroup.SPINE,
        ),

        // -------------------------------------------------------------- Shoulder

        MovementLimitationDefinition(
            id = MovementLimitation.AVOID_OVERHEAD_PRESSING,
            nameRes = R.string.limitation_avoid_overhead_pressing,
            group = LimitationGroup.SHOULDER,
        ),
        MovementLimitationDefinition(
            id = MovementLimitation.AVOID_DEEP_SHOULDER_EXTENSION,
            nameRes = R.string.limitation_avoid_deep_shoulder_extension,
            group = LimitationGroup.SHOULDER,
        ),
        MovementLimitationDefinition(
            id = MovementLimitation.AVOID_WIDE_GRIP_PRESSING,
            nameRes = R.string.limitation_avoid_wide_grip_pressing,
            group = LimitationGroup.SHOULDER,
        ),
        MovementLimitationDefinition(
            id = MovementLimitation.AVOID_SHOULDER_ABDUCTION,
            nameRes = R.string.limitation_avoid_shoulder_abduction,
            group = LimitationGroup.SHOULDER,
        ),
        MovementLimitationDefinition(
            id = MovementLimitation.AVOID_INTERNAL_ROTATION_UNDER_LOAD,
            nameRes = R.string.limitation_avoid_internal_rotation_under_load,
            group = LimitationGroup.SHOULDER,
        ),

        // ----------------------------------------------------------------- Elbow

        MovementLimitationDefinition(
            id = MovementLimitation.AVOID_HEAVY_ELBOW_FLEXION,
            nameRes = R.string.limitation_avoid_heavy_elbow_flexion,
            group = LimitationGroup.ELBOW,
        ),
        MovementLimitationDefinition(
            id = MovementLimitation.AVOID_HEAVY_ELBOW_EXTENSION,
            nameRes = R.string.limitation_avoid_heavy_elbow_extension,
            group = LimitationGroup.ELBOW,
        ),

        // -------------------------------------------------------- Wrist and grip

        MovementLimitationDefinition(
            id = MovementLimitation.AVOID_LOADED_WRIST_EXTENSION,
            nameRes = R.string.limitation_avoid_loaded_wrist_extension,
            group = LimitationGroup.WRIST_AND_GRIP,
        ),
        MovementLimitationDefinition(
            id = MovementLimitation.AVOID_LOADED_WRIST_FLEXION,
            nameRes = R.string.limitation_avoid_loaded_wrist_flexion,
            group = LimitationGroup.WRIST_AND_GRIP,
        ),
        MovementLimitationDefinition(
            id = MovementLimitation.AVOID_PRONATED_GRIP,
            nameRes = R.string.limitation_avoid_pronated_grip,
            group = LimitationGroup.WRIST_AND_GRIP,
        ),
        MovementLimitationDefinition(
            id = MovementLimitation.AVOID_SUPINATED_GRIP,
            nameRes = R.string.limitation_avoid_supinated_grip,
            group = LimitationGroup.WRIST_AND_GRIP,
        ),

        // ------------------------------------------------------------------- Hip

        MovementLimitationDefinition(
            id = MovementLimitation.AVOID_DEEP_HIP_FLEXION,
            nameRes = R.string.limitation_avoid_deep_hip_flexion,
            group = LimitationGroup.HIP,
        ),
        MovementLimitationDefinition(
            id = MovementLimitation.AVOID_WIDE_HIP_ABDUCTION,
            nameRes = R.string.limitation_avoid_wide_hip_abduction,
            group = LimitationGroup.HIP,
        ),
        MovementLimitationDefinition(
            id = MovementLimitation.AVOID_UNILATERAL_BALANCE_DEMAND,
            nameRes = R.string.limitation_avoid_unilateral_balance_demand,
            group = LimitationGroup.HIP,
        ),
    )

    private val byId: Map<MovementLimitation, MovementLimitationDefinition> =
        limitations.associateBy { it.id }

    /**
     * The definition behind [id].
     *
     * Non-null by construction: [MovementLimitationCatalogTest] asserts one entry per
     * enum constant.
     */
    fun definition(id: MovementLimitation): MovementLimitationDefinition =
        requireNotNull(byId[id]) { "No catalog entry for limitation $id" }

    /**
     * Every limitation grouped for display, in [LimitationGroup] declaration order.
     *
     * Groups with no limitations are impossible — every constant is catalogued — so the
     * screen can render this map directly.
     */
    fun groupedForDisplay(): Map<LimitationGroup, List<MovementLimitationDefinition>> =
        limitations
            .groupBy { it.group }
            .toSortedMap(compareBy { it.ordinal })
}
