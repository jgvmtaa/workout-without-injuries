package com.jgv.workoutplanner.core.ui

import androidx.annotation.StringRes
import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.domain.model.ExerciseAvailability
import com.jgv.workoutplanner.domain.model.ExerciseDifficulty
import com.jgv.workoutplanner.domain.model.ExerciseTag
import com.jgv.workoutplanner.domain.model.MovementPattern
import com.jgv.workoutplanner.domain.model.MuscleGroup

@get:StringRes
val MuscleGroup.labelRes: Int
    get() = when (this) {
        MuscleGroup.CHEST -> R.string.muscle_chest
        MuscleGroup.BACK -> R.string.muscle_back
        MuscleGroup.SHOULDERS -> R.string.muscle_shoulders
        MuscleGroup.BICEPS -> R.string.muscle_biceps
        MuscleGroup.TRICEPS -> R.string.muscle_triceps
        MuscleGroup.QUADRICEPS -> R.string.muscle_quadriceps
        MuscleGroup.HAMSTRINGS -> R.string.muscle_hamstrings
        MuscleGroup.GLUTES -> R.string.muscle_glutes
        MuscleGroup.CALVES -> R.string.muscle_calves
        MuscleGroup.CORE -> R.string.muscle_core
    }

@get:StringRes
val MovementPattern.labelRes: Int
    get() = when (this) {
        MovementPattern.HORIZONTAL_PUSH -> R.string.movement_horizontal_push
        MovementPattern.VERTICAL_PUSH -> R.string.movement_vertical_push
        MovementPattern.HORIZONTAL_PULL -> R.string.movement_horizontal_pull
        MovementPattern.VERTICAL_PULL -> R.string.movement_vertical_pull
        MovementPattern.SQUAT -> R.string.movement_squat
        MovementPattern.LUNGE -> R.string.movement_lunge
        MovementPattern.HIP_HINGE -> R.string.movement_hip_hinge
        MovementPattern.KNEE_FLEXION -> R.string.movement_knee_flexion
        MovementPattern.HIP_EXTENSION -> R.string.movement_hip_extension
        MovementPattern.HIP_ABDUCTION -> R.string.movement_hip_abduction
        MovementPattern.HIP_ADDUCTION -> R.string.movement_hip_adduction
        MovementPattern.ELBOW_FLEXION -> R.string.movement_elbow_flexion
        MovementPattern.ELBOW_EXTENSION -> R.string.movement_elbow_extension
        MovementPattern.SHOULDER_ABDUCTION -> R.string.movement_shoulder_abduction
        MovementPattern.SHOULDER_EXTERNAL_ROTATION -> R.string.movement_shoulder_external_rotation
        MovementPattern.CALF_RAISE -> R.string.movement_calf_raise
        MovementPattern.CORE_ANTI_EXTENSION -> R.string.movement_core_anti_extension
        MovementPattern.CORE_ANTI_ROTATION -> R.string.movement_core_anti_rotation
        MovementPattern.CORE_FLEXION -> R.string.movement_core_flexion
        MovementPattern.CARRY -> R.string.movement_carry
    }

@get:StringRes
val ExerciseTag.labelRes: Int
    get() = when (this) {
        ExerciseTag.COMPOUND -> R.string.tag_compound
        ExerciseTag.ISOLATION -> R.string.tag_isolation
        ExerciseTag.UNILATERAL -> R.string.tag_unilateral
        ExerciseTag.BILATERAL -> R.string.tag_bilateral
        ExerciseTag.MACHINE_SUPPORTED -> R.string.tag_machine_supported
        ExerciseTag.CHEST_SUPPORTED -> R.string.tag_chest_supported
        ExerciseTag.REQUIRES_BALANCE -> R.string.tag_requires_balance
        ExerciseTag.HIGH_IMPACT -> R.string.tag_high_impact
        ExerciseTag.BODYWEIGHT -> R.string.tag_bodyweight
        ExerciseTag.FREE_WEIGHT -> R.string.tag_free_weight
    }

@get:StringRes
val ExerciseDifficulty.labelRes: Int
    get() = when (this) {
        ExerciseDifficulty.BEGINNER -> R.string.difficulty_beginner
        ExerciseDifficulty.INTERMEDIATE -> R.string.difficulty_intermediate
        ExerciseDifficulty.ADVANCED -> R.string.difficulty_advanced
    }

@get:StringRes
val ExerciseAvailability.labelRes: Int
    get() = when (this) {
        ExerciseAvailability.AVAILABLE -> R.string.availability_available
        ExerciseAvailability.EXCLUDED -> R.string.availability_excluded
        ExerciseAvailability.UNAVAILABLE -> R.string.availability_unavailable
    }
