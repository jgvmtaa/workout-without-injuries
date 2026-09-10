package com.jgv.workoutplanner.domain.model

/**
 * Muscle groups an exercise trains (spec §5.1).
 *
 * An exercise has exactly one primary group and any number of secondary ones. The
 * library browses by primary group, and plan generation balances coverage across
 * them (spec §12.3).
 */
enum class MuscleGroup {
    CHEST,
    BACK,
    SHOULDERS,
    BICEPS,
    TRICEPS,
    QUADRICEPS,
    HAMSTRINGS,
    GLUTES,
    CALVES,
    CORE,
}
