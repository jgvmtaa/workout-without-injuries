package com.jgv.workoutplanner.data.catalog

import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.domain.model.BodyRegion
import com.jgv.workoutplanner.domain.model.InjuryDefinition
import com.jgv.workoutplanner.domain.model.InjuryId
import com.jgv.workoutplanner.domain.model.MovementLimitation

/**
 * The static injury catalog (README §4.5).
 *
 * Maps every [InjuryId] to the [MovementLimitation]s it *might* imply. Nothing here
 * filters anything. The limitations screen shows these as a prompt, the user decides
 * which currently apply, and only that confirmed set reaches the engine (README §2).
 *
 * ## How the suggestions were chosen
 * Suggestions can afford to be broader than [ExerciseCatalog]'s conflicts, because the
 * user reviews and prunes them. The cost of an extra suggestion is one unchecked box;
 * the cost of a missing one is a movement nobody thought to ask about. So each entry
 * lists the movements commonly aggravating for that history — surgeries get the widest
 * set, undiagnosed pain a deliberately conservative one.
 *
 * They remain suggestions, not conclusions. Someone who tore an ACL a decade ago and
 * has trained on it since should be able to uncheck every box, and someone whose
 * clinician restricted something unrelated should be able to add it.
 */
object InjuryCatalog {

    val injuries: List<InjuryDefinition> = listOf(

        // ------------------------------------------------------------- Shoulder

        InjuryDefinition(
            id = InjuryId.SHOULDER_GENERAL,
            nameRes = R.string.injury_shoulder_general,
            bodyRegion = BodyRegion.SHOULDER,
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_OVERHEAD_PRESSING,
                MovementLimitation.AVOID_WIDE_GRIP_PRESSING,
                MovementLimitation.AVOID_DEEP_SHOULDER_EXTENSION,
            ),
        ),
        InjuryDefinition(
            id = InjuryId.SHOULDER_ROTATOR_CUFF,
            nameRes = R.string.injury_shoulder_rotator_cuff,
            bodyRegion = BodyRegion.SHOULDER,
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_OVERHEAD_PRESSING,
                MovementLimitation.AVOID_SHOULDER_ABDUCTION,
                MovementLimitation.AVOID_INTERNAL_ROTATION_UNDER_LOAD,
                MovementLimitation.AVOID_DEEP_SHOULDER_EXTENSION,
            ),
        ),
        InjuryDefinition(
            id = InjuryId.SHOULDER_INSTABILITY,
            nameRes = R.string.injury_shoulder_instability,
            bodyRegion = BodyRegion.SHOULDER,
            // Instability is about end-range positions rather than load.
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_DEEP_SHOULDER_EXTENSION,
                MovementLimitation.AVOID_WIDE_GRIP_PRESSING,
                MovementLimitation.AVOID_OVERHEAD_PRESSING,
            ),
        ),
        InjuryDefinition(
            id = InjuryId.SHOULDER_IMPINGEMENT,
            nameRes = R.string.injury_shoulder_impingement,
            bodyRegion = BodyRegion.SHOULDER,
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_OVERHEAD_PRESSING,
                MovementLimitation.AVOID_SHOULDER_ABDUCTION,
                MovementLimitation.AVOID_INTERNAL_ROTATION_UNDER_LOAD,
            ),
        ),
        InjuryDefinition(
            id = InjuryId.SHOULDER_SURGERY,
            nameRes = R.string.injury_shoulder_surgery,
            bodyRegion = BodyRegion.SHOULDER,
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_OVERHEAD_PRESSING,
                MovementLimitation.AVOID_WIDE_GRIP_PRESSING,
                MovementLimitation.AVOID_DEEP_SHOULDER_EXTENSION,
                MovementLimitation.AVOID_SHOULDER_ABDUCTION,
                MovementLimitation.AVOID_INTERNAL_ROTATION_UNDER_LOAD,
            ),
        ),
        InjuryDefinition(
            id = InjuryId.SHOULDER_UNDIAGNOSED_PAIN,
            nameRes = R.string.injury_shoulder_undiagnosed_pain,
            bodyRegion = BodyRegion.SHOULDER,
            // Without a diagnosis, suggest only the two positions most shoulder pain
            // objects to rather than guessing at a mechanism.
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_OVERHEAD_PRESSING,
                MovementLimitation.AVOID_SHOULDER_ABDUCTION,
            ),
        ),

        // ---------------------------------------------------------------- Elbow

        InjuryDefinition(
            id = InjuryId.ELBOW_GENERAL,
            nameRes = R.string.injury_elbow_general,
            bodyRegion = BodyRegion.ELBOW,
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_HEAVY_ELBOW_FLEXION,
                MovementLimitation.AVOID_HEAVY_ELBOW_EXTENSION,
            ),
        ),
        InjuryDefinition(
            id = InjuryId.ELBOW_TENDON,
            nameRes = R.string.injury_elbow_tendon,
            bodyRegion = BodyRegion.ELBOW,
            // Tendon pain at the elbow is usually grip-position dependent, so the grip
            // limitations matter as much as the elbow ones.
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_HEAVY_ELBOW_FLEXION,
                MovementLimitation.AVOID_HEAVY_ELBOW_EXTENSION,
                MovementLimitation.AVOID_PRONATED_GRIP,
                MovementLimitation.AVOID_SUPINATED_GRIP,
            ),
        ),
        InjuryDefinition(
            id = InjuryId.ELBOW_SURGERY,
            nameRes = R.string.injury_elbow_surgery,
            bodyRegion = BodyRegion.ELBOW,
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_HEAVY_ELBOW_FLEXION,
                MovementLimitation.AVOID_HEAVY_ELBOW_EXTENSION,
                MovementLimitation.AVOID_PRONATED_GRIP,
                MovementLimitation.AVOID_SUPINATED_GRIP,
            ),
        ),
        InjuryDefinition(
            id = InjuryId.ELBOW_UNDIAGNOSED_PAIN,
            nameRes = R.string.injury_elbow_undiagnosed_pain,
            bodyRegion = BodyRegion.ELBOW,
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_HEAVY_ELBOW_FLEXION,
                MovementLimitation.AVOID_HEAVY_ELBOW_EXTENSION,
            ),
        ),

        // ----------------------------------------------------------- Wrist/hand

        InjuryDefinition(
            id = InjuryId.WRIST_GENERAL,
            nameRes = R.string.injury_wrist_general,
            bodyRegion = BodyRegion.WRIST_HAND,
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_LOADED_WRIST_EXTENSION,
                MovementLimitation.AVOID_LOADED_WRIST_FLEXION,
            ),
        ),
        InjuryDefinition(
            id = InjuryId.WRIST_SURGERY,
            nameRes = R.string.injury_wrist_surgery,
            bodyRegion = BodyRegion.WRIST_HAND,
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_LOADED_WRIST_EXTENSION,
                MovementLimitation.AVOID_LOADED_WRIST_FLEXION,
                MovementLimitation.AVOID_PRONATED_GRIP,
                MovementLimitation.AVOID_SUPINATED_GRIP,
            ),
        ),
        InjuryDefinition(
            id = InjuryId.WRIST_UNDIAGNOSED_PAIN,
            nameRes = R.string.injury_wrist_undiagnosed_pain,
            bodyRegion = BodyRegion.WRIST_HAND,
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_LOADED_WRIST_EXTENSION,
                MovementLimitation.AVOID_LOADED_WRIST_FLEXION,
            ),
        ),

        // ----------------------------------------------------------- Lower back

        InjuryDefinition(
            id = InjuryId.LOWER_BACK_DISC_RELATED,
            nameRes = R.string.injury_lower_back_disc_related,
            bodyRegion = BodyRegion.LOWER_BACK,
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_LOADED_SPINAL_FLEXION,
                MovementLimitation.AVOID_UNSUPPORTED_HIP_HINGE,
                MovementLimitation.AVOID_HIGH_SPINAL_COMPRESSION,
                MovementLimitation.AVOID_SPINAL_ROTATION,
            ),
        ),
        InjuryDefinition(
            id = InjuryId.LOWER_BACK_GENERAL,
            nameRes = R.string.injury_lower_back_general,
            bodyRegion = BodyRegion.LOWER_BACK,
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_LOADED_SPINAL_FLEXION,
                MovementLimitation.AVOID_UNSUPPORTED_HIP_HINGE,
                MovementLimitation.AVOID_HIGH_SPINAL_COMPRESSION,
            ),
        ),
        InjuryDefinition(
            id = InjuryId.LOWER_BACK_SURGERY,
            nameRes = R.string.injury_lower_back_surgery,
            bodyRegion = BodyRegion.LOWER_BACK,
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_LOADED_SPINAL_FLEXION,
                MovementLimitation.AVOID_LOADED_SPINAL_EXTENSION,
                MovementLimitation.AVOID_SPINAL_ROTATION,
                MovementLimitation.AVOID_UNSUPPORTED_HIP_HINGE,
                MovementLimitation.AVOID_HIGH_SPINAL_COMPRESSION,
                MovementLimitation.AVOID_HIGH_IMPACT,
            ),
        ),
        InjuryDefinition(
            id = InjuryId.LOWER_BACK_RECURRENT_PAIN,
            nameRes = R.string.injury_lower_back_recurrent_pain,
            bodyRegion = BodyRegion.LOWER_BACK,
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_LOADED_SPINAL_FLEXION,
                MovementLimitation.AVOID_UNSUPPORTED_HIP_HINGE,
                MovementLimitation.AVOID_HIGH_SPINAL_COMPRESSION,
            ),
        ),
        InjuryDefinition(
            id = InjuryId.LOWER_BACK_UNDIAGNOSED_PAIN,
            nameRes = R.string.injury_lower_back_undiagnosed_pain,
            bodyRegion = BodyRegion.LOWER_BACK,
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_LOADED_SPINAL_FLEXION,
                MovementLimitation.AVOID_HIGH_SPINAL_COMPRESSION,
            ),
        ),

        // ------------------------------------------------------------------ Hip

        InjuryDefinition(
            id = InjuryId.HIP_GENERAL,
            nameRes = R.string.injury_hip_general,
            bodyRegion = BodyRegion.HIP,
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_DEEP_HIP_FLEXION,
                MovementLimitation.AVOID_WIDE_HIP_ABDUCTION,
            ),
        ),
        InjuryDefinition(
            id = InjuryId.HIP_IMPINGEMENT,
            nameRes = R.string.injury_hip_impingement,
            bodyRegion = BodyRegion.HIP,
            // Impingement is provoked by the bottom of a deep squat, which is deep hip
            // flexion and deep knee flexion arriving together.
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_DEEP_HIP_FLEXION,
                MovementLimitation.AVOID_WIDE_HIP_ABDUCTION,
                MovementLimitation.AVOID_DEEP_KNEE_FLEXION,
            ),
        ),
        InjuryDefinition(
            id = InjuryId.HIP_SURGERY,
            nameRes = R.string.injury_hip_surgery,
            bodyRegion = BodyRegion.HIP,
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_DEEP_HIP_FLEXION,
                MovementLimitation.AVOID_WIDE_HIP_ABDUCTION,
                MovementLimitation.AVOID_HIGH_IMPACT,
                MovementLimitation.AVOID_SINGLE_LEG_LOADING,
                MovementLimitation.AVOID_UNILATERAL_BALANCE_DEMAND,
            ),
        ),
        InjuryDefinition(
            id = InjuryId.HIP_UNDIAGNOSED_PAIN,
            nameRes = R.string.injury_hip_undiagnosed_pain,
            bodyRegion = BodyRegion.HIP,
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_DEEP_HIP_FLEXION,
                MovementLimitation.AVOID_WIDE_HIP_ABDUCTION,
            ),
        ),

        // ----------------------------------------------------------------- Knee

        InjuryDefinition(
            id = InjuryId.KNEE_ACL,
            nameRes = R.string.injury_knee_acl,
            bodyRegion = BodyRegion.KNEE,
            // The worked example from README §4.5.
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_JUMPING,
                MovementLimitation.AVOID_RAPID_DIRECTION_CHANGE,
            ),
        ),
        InjuryDefinition(
            id = InjuryId.KNEE_MENISCUS,
            nameRes = R.string.injury_knee_meniscus,
            bodyRegion = BodyRegion.KNEE,
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_DEEP_KNEE_FLEXION,
                MovementLimitation.AVOID_RAPID_DIRECTION_CHANGE,
                MovementLimitation.AVOID_KNEELING,
                MovementLimitation.AVOID_HIGH_IMPACT,
            ),
        ),
        InjuryDefinition(
            id = InjuryId.KNEE_PATELLAR_TENDON,
            nameRes = R.string.injury_knee_patellar_tendon,
            bodyRegion = BodyRegion.KNEE,
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_JUMPING,
                MovementLimitation.AVOID_HIGH_IMPACT,
                MovementLimitation.AVOID_HEAVY_KNEE_LOADING,
                MovementLimitation.AVOID_DEEP_KNEE_FLEXION,
            ),
        ),
        InjuryDefinition(
            id = InjuryId.KNEE_PATELLOFEMORAL,
            nameRes = R.string.injury_knee_patellofemoral,
            bodyRegion = BodyRegion.KNEE,
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_DEEP_KNEE_FLEXION,
                MovementLimitation.AVOID_HEAVY_KNEE_LOADING,
                MovementLimitation.AVOID_KNEELING,
                MovementLimitation.AVOID_RUNNING,
            ),
        ),
        InjuryDefinition(
            id = InjuryId.KNEE_SURGERY,
            nameRes = R.string.injury_knee_surgery,
            bodyRegion = BodyRegion.KNEE,
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_JUMPING,
                MovementLimitation.AVOID_HIGH_IMPACT,
                MovementLimitation.AVOID_RAPID_DIRECTION_CHANGE,
                MovementLimitation.AVOID_DEEP_KNEE_FLEXION,
                MovementLimitation.AVOID_KNEELING,
                MovementLimitation.AVOID_HEAVY_KNEE_LOADING,
                MovementLimitation.AVOID_SINGLE_LEG_LOADING,
            ),
        ),
        InjuryDefinition(
            id = InjuryId.KNEE_UNDIAGNOSED_PAIN,
            nameRes = R.string.injury_knee_undiagnosed_pain,
            bodyRegion = BodyRegion.KNEE,
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_DEEP_KNEE_FLEXION,
                MovementLimitation.AVOID_HEAVY_KNEE_LOADING,
                MovementLimitation.AVOID_HIGH_IMPACT,
            ),
        ),

        // ----------------------------------------------------------- Ankle/foot

        InjuryDefinition(
            id = InjuryId.ANKLE_SPRAIN,
            nameRes = R.string.injury_ankle_sprain,
            bodyRegion = BodyRegion.ANKLE_FOOT,
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_HIGH_IMPACT,
                MovementLimitation.AVOID_JUMPING,
                MovementLimitation.AVOID_RAPID_DIRECTION_CHANGE,
                MovementLimitation.AVOID_UNILATERAL_BALANCE_DEMAND,
            ),
        ),
        InjuryDefinition(
            id = InjuryId.ANKLE_ACHILLES,
            nameRes = R.string.injury_ankle_achilles,
            bodyRegion = BodyRegion.ANKLE_FOOT,
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_JUMPING,
                MovementLimitation.AVOID_RUNNING,
                MovementLimitation.AVOID_HIGH_IMPACT,
            ),
        ),
        InjuryDefinition(
            id = InjuryId.ANKLE_FOOT_SURGERY,
            nameRes = R.string.injury_ankle_foot_surgery,
            bodyRegion = BodyRegion.ANKLE_FOOT,
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_HIGH_IMPACT,
                MovementLimitation.AVOID_JUMPING,
                MovementLimitation.AVOID_RUNNING,
                MovementLimitation.AVOID_RAPID_DIRECTION_CHANGE,
                MovementLimitation.AVOID_UNILATERAL_BALANCE_DEMAND,
                MovementLimitation.AVOID_SINGLE_LEG_LOADING,
            ),
        ),
        InjuryDefinition(
            id = InjuryId.ANKLE_FOOT_UNDIAGNOSED_PAIN,
            nameRes = R.string.injury_ankle_foot_undiagnosed_pain,
            bodyRegion = BodyRegion.ANKLE_FOOT,
            suggestedLimitations = setOf(
                MovementLimitation.AVOID_HIGH_IMPACT,
                MovementLimitation.AVOID_JUMPING,
            ),
        ),
    )
}
