package com.jgv.workoutplanner.domain.model

/**
 * Stable identifier for every injury the user can select (spec §4.4).
 *
 * The enum name is the identity; the user-facing label lives in `strings.xml` and is
 * reached through [InjuryDefinition.nameRes] (spec §6). Never use the label as an id.
 *
 * Every region ends with an `UNDIAGNOSED_PAIN` entry. That is deliberate: the app must
 * accept "something hurts and I don't know why" without asking the user to guess at a
 * diagnosis it is not qualified to confirm (spec §2).
 */
enum class InjuryId {
    SHOULDER_GENERAL,
    SHOULDER_ROTATOR_CUFF,
    SHOULDER_INSTABILITY,
    SHOULDER_IMPINGEMENT,
    SHOULDER_SURGERY,
    SHOULDER_UNDIAGNOSED_PAIN,

    ELBOW_GENERAL,
    ELBOW_TENDON,
    ELBOW_SURGERY,
    ELBOW_UNDIAGNOSED_PAIN,

    WRIST_GENERAL,
    WRIST_SURGERY,
    WRIST_UNDIAGNOSED_PAIN,

    LOWER_BACK_DISC_RELATED,
    LOWER_BACK_GENERAL,
    LOWER_BACK_SURGERY,
    LOWER_BACK_RECURRENT_PAIN,
    LOWER_BACK_UNDIAGNOSED_PAIN,

    HIP_GENERAL,
    HIP_IMPINGEMENT,
    HIP_SURGERY,
    HIP_UNDIAGNOSED_PAIN,

    KNEE_ACL,
    KNEE_MENISCUS,
    KNEE_PATELLAR_TENDON,
    KNEE_PATELLOFEMORAL,
    KNEE_SURGERY,
    KNEE_UNDIAGNOSED_PAIN,

    ANKLE_SPRAIN,
    ANKLE_ACHILLES,
    ANKLE_FOOT_SURGERY,
    ANKLE_FOOT_UNDIAGNOSED_PAIN,
}
