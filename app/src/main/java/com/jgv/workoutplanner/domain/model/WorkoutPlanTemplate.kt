package com.jgv.workoutplanner.domain.model

/**
 * Workout-day templates that drive deterministic plan generation (spec §12.4).
 *
 * Each [WorkoutDayFocus] maps to a list of [TemplateSlot]s. A slot allows one or more
 * [MovementPattern]s; the generator picks the highest-ranked eligible exercise whose pattern
 * is allowed and that hasn't been used earlier that day (spec §12.5 / §12.4).
 *
 * ## Why these slots
 * - FULL_BODY: 6 slots – squat/knee, hinge/hip-ext, push, pull, secondary upper, core
 *   (spec §12.4 example)
 * - UPPER_BODY: 7 slots – h-push, v/secondary push, h-pull, v-pull, shoulder iso, biceps,
 *   triceps (spec §12.4 example)
 * - LOWER_BODY: 6 slots – knee-dominant, hinge, glute, hamstring iso, calf, core
 *   (spec §12.4 example)
 * - PUSH/PULL/LEGS: dedicated templates for the three-day split.
 *   PUSH = h-push, v-push, shoulder abduction, triceps, secondary push.
 *   PULL = h-pull, v-pull, shoulder ext rotation, biceps, secondary pull.
 *   LEGS reuses lower-body template.
 *
 * Sizes are fixed; session duration does not change them (spec §5.3, §12.4).
 */
data class TemplateSlot(
    val id: String,
    val allowedPatterns: Set<MovementPattern>,
    val description: String = id,
)

object WorkoutPlanTemplate {

    private val fullBody: List<TemplateSlot> = listOf(
        TemplateSlot("squat-knee", setOf(MovementPattern.SQUAT, MovementPattern.LUNGE, MovementPattern.KNEE_FLEXION)),
        TemplateSlot("hinge-hip-ext", setOf(MovementPattern.HIP_HINGE, MovementPattern.HIP_EXTENSION)),
        TemplateSlot("push", setOf(MovementPattern.HORIZONTAL_PUSH, MovementPattern.VERTICAL_PUSH)),
        TemplateSlot("pull", setOf(MovementPattern.HORIZONTAL_PULL, MovementPattern.VERTICAL_PULL)),
        TemplateSlot(
            "secondary-upper",
            setOf(
                MovementPattern.SHOULDER_ABDUCTION,
                MovementPattern.SHOULDER_EXTERNAL_ROTATION,
                MovementPattern.ELBOW_FLEXION,
                MovementPattern.ELBOW_EXTENSION,
                MovementPattern.HORIZONTAL_PUSH,
                MovementPattern.VERTICAL_PUSH,
                MovementPattern.HORIZONTAL_PULL,
                MovementPattern.VERTICAL_PULL,
            ),
            "secondary upper",
        ),
        TemplateSlot(
            "core",
            setOf(
                MovementPattern.CORE_ANTI_EXTENSION,
                MovementPattern.CORE_ANTI_ROTATION,
                MovementPattern.CORE_FLEXION,
            ),
        ),
    )

    private val upperBody: List<TemplateSlot> = listOf(
        TemplateSlot("h-push", setOf(MovementPattern.HORIZONTAL_PUSH)),
        TemplateSlot("v-push", setOf(MovementPattern.VERTICAL_PUSH)),
        TemplateSlot("h-pull", setOf(MovementPattern.HORIZONTAL_PULL)),
        TemplateSlot("v-pull", setOf(MovementPattern.VERTICAL_PULL)),
        TemplateSlot("shoulder-iso", setOf(MovementPattern.SHOULDER_ABDUCTION, MovementPattern.SHOULDER_EXTERNAL_ROTATION)),
        TemplateSlot("biceps", setOf(MovementPattern.ELBOW_FLEXION)),
        TemplateSlot("triceps", setOf(MovementPattern.ELBOW_EXTENSION)),
    )

    private val lowerBody: List<TemplateSlot> = listOf(
        TemplateSlot("knee-dominant", setOf(MovementPattern.SQUAT, MovementPattern.LUNGE)),
        TemplateSlot("hinge", setOf(MovementPattern.HIP_HINGE)),
        TemplateSlot("glute", setOf(MovementPattern.HIP_EXTENSION, MovementPattern.HIP_ABDUCTION, MovementPattern.HIP_ADDUCTION)),
        TemplateSlot("hamstring-iso", setOf(MovementPattern.KNEE_FLEXION)),
        TemplateSlot("calf", setOf(MovementPattern.CALF_RAISE)),
        TemplateSlot(
            "core",
            setOf(
                MovementPattern.CORE_ANTI_EXTENSION,
                MovementPattern.CORE_ANTI_ROTATION,
                MovementPattern.CORE_FLEXION,
            ),
        ),
    )

    private val push: List<TemplateSlot> = listOf(
        TemplateSlot("h-push", setOf(MovementPattern.HORIZONTAL_PUSH)),
        TemplateSlot("v-push", setOf(MovementPattern.VERTICAL_PUSH)),
        TemplateSlot("shoulder-iso", setOf(MovementPattern.SHOULDER_ABDUCTION, MovementPattern.SHOULDER_EXTERNAL_ROTATION)),
        TemplateSlot("triceps", setOf(MovementPattern.ELBOW_EXTENSION)),
        TemplateSlot("secondary-push", setOf(MovementPattern.HORIZONTAL_PUSH, MovementPattern.VERTICAL_PUSH, MovementPattern.SHOULDER_ABDUCTION)),
    )

    private val pull: List<TemplateSlot> = listOf(
        TemplateSlot("h-pull", setOf(MovementPattern.HORIZONTAL_PULL)),
        TemplateSlot("v-pull", setOf(MovementPattern.VERTICAL_PULL)),
        TemplateSlot("rear-delt-ext-rot", setOf(MovementPattern.SHOULDER_EXTERNAL_ROTATION)),
        TemplateSlot("biceps", setOf(MovementPattern.ELBOW_FLEXION)),
        TemplateSlot("secondary-pull", setOf(MovementPattern.HORIZONTAL_PULL, MovementPattern.VERTICAL_PULL)),
    )

    fun slotsFor(focus: WorkoutDayFocus): List<TemplateSlot> = when (focus) {
        WorkoutDayFocus.FULL_BODY -> fullBody
        WorkoutDayFocus.UPPER_BODY -> upperBody
        WorkoutDayFocus.LOWER_BODY -> lowerBody
        WorkoutDayFocus.PUSH -> push
        WorkoutDayFocus.PULL -> pull
        WorkoutDayFocus.LEGS -> lowerBody // legs reuses lower template (6 slots)
    }
}
