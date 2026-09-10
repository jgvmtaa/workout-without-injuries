package com.jgv.workoutplanner.core.ui

import androidx.annotation.StringRes
import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.domain.model.BodyRegion
import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.ExperienceLevel
import com.jgv.workoutplanner.domain.model.InjuryStatus
import com.jgv.workoutplanner.domain.model.LimitationGroup
import com.jgv.workoutplanner.domain.model.TrainingGoal
import com.jgv.workoutplanner.domain.model.WorkoutSplit
import com.jgv.workoutplanner.domain.model.WorkoutDayFocus

/**
 * Display labels for the domain enums the onboarding screens render (spec §6).
 *
 * The enums carry no copy — the identifier is the enum name and the words live in
 * `strings.xml` — so something has to join them. That join is here rather than in each
 * screen, because the same enum is labelled on the preferences screen, the review
 * screen, and the profile screen; separate copies would drift.
 *
 * Exhaustive `when` over each enum: adding a constant is a compile error until it has a
 * label, which is the whole point of doing it this way rather than with a map.
 *
 * The three *catalog* types — exercises, injuries, limitations — are not here. They
 * carry their own `nameRes` because their copy is content the catalog owns, not a label
 * for a fixed set of options.
 */

@get:StringRes
val TrainingGoal.labelRes: Int
    get() = when (this) {
        TrainingGoal.GENERAL_FITNESS -> R.string.goal_general_fitness
        TrainingGoal.BUILD_MUSCLE -> R.string.goal_build_muscle
        TrainingGoal.BUILD_STRENGTH -> R.string.goal_build_strength
        TrainingGoal.IMPROVE_ENDURANCE -> R.string.goal_improve_endurance
    }

@get:StringRes
val ExperienceLevel.labelRes: Int
    get() = when (this) {
        ExperienceLevel.BEGINNER -> R.string.experience_beginner
        ExperienceLevel.INTERMEDIATE -> R.string.experience_intermediate
        ExperienceLevel.ADVANCED -> R.string.experience_advanced
    }

@get:StringRes
val Equipment.labelRes: Int
    get() = when (this) {
        Equipment.BODYWEIGHT -> R.string.equipment_bodyweight
        Equipment.DUMBBELLS -> R.string.equipment_dumbbells
        Equipment.BARBELL -> R.string.equipment_barbell
        Equipment.BENCH -> R.string.equipment_bench
        Equipment.CABLE_MACHINE -> R.string.equipment_cable_machine
        Equipment.SELECTORIZED_MACHINE -> R.string.equipment_selectorized_machine
        Equipment.PULL_UP_BAR -> R.string.equipment_pull_up_bar
        Equipment.RESISTANCE_BAND -> R.string.equipment_resistance_band
        Equipment.CARDIO_MACHINE -> R.string.equipment_cardio_machine
    }

@get:StringRes
val WorkoutSplit.labelRes: Int
    get() = when (this) {
        WorkoutSplit.FULL_BODY -> R.string.split_full_body
        WorkoutSplit.UPPER_LOWER -> R.string.split_upper_lower
        WorkoutSplit.PUSH_PULL_LEGS -> R.string.split_push_pull_legs
    }

@get:StringRes
val WorkoutDayFocus.labelRes: Int
    get() = when (this) {
        WorkoutDayFocus.FULL_BODY -> R.string.focus_full_body
        WorkoutDayFocus.UPPER_BODY -> R.string.focus_upper_body
        WorkoutDayFocus.LOWER_BODY -> R.string.focus_lower_body
        WorkoutDayFocus.PUSH -> R.string.focus_push
        WorkoutDayFocus.PULL -> R.string.focus_pull
        WorkoutDayFocus.LEGS -> R.string.focus_legs
    }

@get:StringRes
val BodyRegion.labelRes: Int
    get() = when (this) {
        BodyRegion.NECK -> R.string.body_region_neck
        BodyRegion.SHOULDER -> R.string.body_region_shoulder
        BodyRegion.ELBOW -> R.string.body_region_elbow
        BodyRegion.WRIST_HAND -> R.string.body_region_wrist_hand
        BodyRegion.UPPER_BACK -> R.string.body_region_upper_back
        BodyRegion.LOWER_BACK -> R.string.body_region_lower_back
        BodyRegion.HIP -> R.string.body_region_hip
        BodyRegion.KNEE -> R.string.body_region_knee
        BodyRegion.ANKLE_FOOT -> R.string.body_region_ankle_foot
    }

@get:StringRes
val InjuryStatus.labelRes: Int
    get() = when (this) {
        InjuryStatus.CURRENTLY_SYMPTOMATIC -> R.string.injury_status_currently_symptomatic
        InjuryStatus.RECOVERING -> R.string.injury_status_recovering
        InjuryStatus.HISTORICAL -> R.string.injury_status_historical
        InjuryStatus.NOT_SPECIFIED -> R.string.injury_status_not_specified
    }

@get:StringRes
val LimitationGroup.labelRes: Int
    get() = when (this) {
        LimitationGroup.IMPACT_AND_LOCOMOTION -> R.string.limitation_group_impact
        LimitationGroup.KNEE -> R.string.limitation_group_knee
        LimitationGroup.SPINE -> R.string.limitation_group_spine
        LimitationGroup.SHOULDER -> R.string.limitation_group_shoulder
        LimitationGroup.ELBOW -> R.string.limitation_group_elbow
        LimitationGroup.WRIST_AND_GRIP -> R.string.limitation_group_wrist_grip
        LimitationGroup.HIP -> R.string.limitation_group_hip
    }
