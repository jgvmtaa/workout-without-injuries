package com.jgv.workoutplanner.data.catalog

import com.jgv.workoutplanner.R
import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.ExerciseDefinition
import com.jgv.workoutplanner.domain.model.ExerciseDifficulty
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.ExercisePrescription
import com.jgv.workoutplanner.domain.model.ExerciseTag
import com.jgv.workoutplanner.domain.model.MovementLimitation
import com.jgv.workoutplanner.domain.model.MovementPattern
import com.jgv.workoutplanner.domain.model.MuscleGroup

/**
 * The static exercise catalog (spec §7, §26).
 *
 * Kotlin rather than JSON on purpose (spec §6): the compiler rejects an unknown
 * equipment or limitation value, refactors reach every entry, and there is no parsing
 * layer to fail at runtime. It moves to a data file only once non-developers need to
 * edit it.
 *
 * ## What the numbers have to satisfy
 * spec §26 sets a per-muscle distribution and asks that every movement pattern have
 * alternatives across equipment types — filtering is only useful if a user who loses an
 * exercise has somewhere else to go. `ExerciseCatalogTest` enforces both, so a future
 * addition cannot quietly break the balance.
 *
 * ## conflictingLimitations
 * This is the safety-relevant field and the one worth reviewing carefully. Each entry
 * lists the movements the *standard* execution demands. Two rules kept it honest:
 *
 *  - Only list what the movement genuinely requires. Padding the list buries the user
 *    in exclusions and leaves them with nothing to train.
 *  - List everything it does require. A missed conflict means the app recommends
 *    something the user told us to avoid, which is the one failure that matters.
 *
 * Where a limitation is about load rather than shape, the loaded variant carries the
 * conflict and the bodyweight one does not — a goblet squat conflicts with
 * `AVOID_HIGH_SPINAL_COMPRESSION`, a bodyweight squat does not.
 */
object ExerciseCatalog {

    val exercises: List<ExerciseDefinition> = listOf(

        // ---------------------------------------------------------------- Chest

        ExerciseDefinition(
            id = ExerciseId.PUSH_UP,
            nameRes = R.string.exercise_push_up,
            descriptionRes = R.string.exercise_push_up_description,
            primaryMuscle = MuscleGroup.CHEST,
            secondaryMuscles = setOf(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS, MuscleGroup.CORE),
            movementPattern = MovementPattern.HORIZONTAL_PUSH,
            requiredEquipment = setOf(Equipment.BODYWEIGHT),
            difficulty = ExerciseDifficulty.BEGINNER,
            // Hands are planted on the floor, so bodyweight goes through an extended wrist.
            conflictingLimitations = setOf(
                MovementLimitation.AVOID_LOADED_WRIST_EXTENSION,
                MovementLimitation.AVOID_WIDE_GRIP_PRESSING,
            ),
            tags = setOf(ExerciseTag.COMPOUND, ExerciseTag.BODYWEIGHT, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 3..4, reps = 8..15, restSeconds = 60),
        ),
        ExerciseDefinition(
            id = ExerciseId.MACHINE_CHEST_PRESS,
            nameRes = R.string.exercise_machine_chest_press,
            descriptionRes = R.string.exercise_machine_chest_press_description,
            primaryMuscle = MuscleGroup.CHEST,
            secondaryMuscles = setOf(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS),
            movementPattern = MovementPattern.HORIZONTAL_PUSH,
            requiredEquipment = setOf(Equipment.SELECTORIZED_MACHINE),
            difficulty = ExerciseDifficulty.BEGINNER,
            conflictingLimitations = setOf(MovementLimitation.AVOID_WIDE_GRIP_PRESSING),
            tags = setOf(
                ExerciseTag.COMPOUND,
                ExerciseTag.MACHINE_SUPPORTED,
                ExerciseTag.CHEST_SUPPORTED,
                ExerciseTag.BILATERAL,
            ),
            defaultPrescription = ExercisePrescription(sets = 3..4, reps = 8..12, restSeconds = 90),
        ),
        ExerciseDefinition(
            id = ExerciseId.DUMBBELL_BENCH_PRESS,
            nameRes = R.string.exercise_dumbbell_bench_press,
            descriptionRes = R.string.exercise_dumbbell_bench_press_description,
            primaryMuscle = MuscleGroup.CHEST,
            secondaryMuscles = setOf(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS),
            movementPattern = MovementPattern.HORIZONTAL_PUSH,
            requiredEquipment = setOf(Equipment.DUMBBELLS, Equipment.BENCH),
            difficulty = ExerciseDifficulty.INTERMEDIATE,
            // Nothing stops the dumbbells at chest level, so the shoulder can travel deep.
            conflictingLimitations = setOf(
                MovementLimitation.AVOID_WIDE_GRIP_PRESSING,
                MovementLimitation.AVOID_DEEP_SHOULDER_EXTENSION,
            ),
            tags = setOf(ExerciseTag.COMPOUND, ExerciseTag.FREE_WEIGHT, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 3..4, reps = 8..12, restSeconds = 90),
        ),
        ExerciseDefinition(
            id = ExerciseId.INCLINE_DUMBBELL_PRESS,
            nameRes = R.string.exercise_incline_dumbbell_press,
            descriptionRes = R.string.exercise_incline_dumbbell_press_description,
            primaryMuscle = MuscleGroup.CHEST,
            secondaryMuscles = setOf(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS),
            movementPattern = MovementPattern.HORIZONTAL_PUSH,
            requiredEquipment = setOf(Equipment.DUMBBELLS, Equipment.BENCH),
            difficulty = ExerciseDifficulty.INTERMEDIATE,
            conflictingLimitations = setOf(
                MovementLimitation.AVOID_WIDE_GRIP_PRESSING,
                MovementLimitation.AVOID_DEEP_SHOULDER_EXTENSION,
            ),
            tags = setOf(ExerciseTag.COMPOUND, ExerciseTag.FREE_WEIGHT, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 3..4, reps = 8..12, restSeconds = 90),
        ),
        ExerciseDefinition(
            id = ExerciseId.BARBELL_BENCH_PRESS,
            nameRes = R.string.exercise_barbell_bench_press,
            descriptionRes = R.string.exercise_barbell_bench_press_description,
            primaryMuscle = MuscleGroup.CHEST,
            secondaryMuscles = setOf(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS),
            movementPattern = MovementPattern.HORIZONTAL_PUSH,
            requiredEquipment = setOf(Equipment.BARBELL, Equipment.BENCH),
            difficulty = ExerciseDifficulty.INTERMEDIATE,
            // The fixed bar dictates grip width and holds the wrists in extension.
            conflictingLimitations = setOf(
                MovementLimitation.AVOID_WIDE_GRIP_PRESSING,
                MovementLimitation.AVOID_DEEP_SHOULDER_EXTENSION,
                MovementLimitation.AVOID_LOADED_WRIST_EXTENSION,
                MovementLimitation.AVOID_PRONATED_GRIP,
            ),
            tags = setOf(ExerciseTag.COMPOUND, ExerciseTag.FREE_WEIGHT, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 3..5, reps = 5..10, restSeconds = 120),
        ),
        ExerciseDefinition(
            id = ExerciseId.CABLE_CHEST_FLY,
            nameRes = R.string.exercise_cable_chest_fly,
            descriptionRes = R.string.exercise_cable_chest_fly_description,
            primaryMuscle = MuscleGroup.CHEST,
            secondaryMuscles = setOf(MuscleGroup.SHOULDERS),
            movementPattern = MovementPattern.HORIZONTAL_PUSH,
            requiredEquipment = setOf(Equipment.CABLE_MACHINE),
            difficulty = ExerciseDifficulty.INTERMEDIATE,
            // Each rep starts with the arms open wide and pulled back.
            conflictingLimitations = setOf(
                MovementLimitation.AVOID_DEEP_SHOULDER_EXTENSION,
                MovementLimitation.AVOID_WIDE_GRIP_PRESSING,
            ),
            tags = setOf(ExerciseTag.ISOLATION, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 2..3, reps = 10..15, restSeconds = 60),
        ),

        // ----------------------------------------------------------------- Back

        ExerciseDefinition(
            id = ExerciseId.LAT_PULLDOWN,
            nameRes = R.string.exercise_lat_pulldown,
            descriptionRes = R.string.exercise_lat_pulldown_description,
            primaryMuscle = MuscleGroup.BACK,
            secondaryMuscles = setOf(MuscleGroup.BICEPS, MuscleGroup.SHOULDERS),
            movementPattern = MovementPattern.VERTICAL_PULL,
            requiredEquipment = setOf(Equipment.CABLE_MACHINE),
            difficulty = ExerciseDifficulty.BEGINNER,
            conflictingLimitations = setOf(MovementLimitation.AVOID_PRONATED_GRIP),
            tags = setOf(
                ExerciseTag.COMPOUND,
                ExerciseTag.MACHINE_SUPPORTED,
                ExerciseTag.BILATERAL,
            ),
            defaultPrescription = ExercisePrescription(sets = 3..4, reps = 8..12, restSeconds = 90),
        ),
        ExerciseDefinition(
            id = ExerciseId.ASSISTED_PULL_UP,
            nameRes = R.string.exercise_assisted_pull_up,
            descriptionRes = R.string.exercise_assisted_pull_up_description,
            primaryMuscle = MuscleGroup.BACK,
            secondaryMuscles = setOf(MuscleGroup.BICEPS, MuscleGroup.SHOULDERS),
            movementPattern = MovementPattern.VERTICAL_PULL,
            requiredEquipment = setOf(Equipment.SELECTORIZED_MACHINE),
            difficulty = ExerciseDifficulty.BEGINNER,
            // The counterweight is what separates this from PULL_UP: the elbow and
            // shoulder load is chosen rather than fixed at bodyweight.
            conflictingLimitations = setOf(MovementLimitation.AVOID_PRONATED_GRIP),
            tags = setOf(
                ExerciseTag.COMPOUND,
                ExerciseTag.MACHINE_SUPPORTED,
                ExerciseTag.BILATERAL,
            ),
            defaultPrescription = ExercisePrescription(sets = 3..4, reps = 6..12, restSeconds = 90),
        ),
        ExerciseDefinition(
            id = ExerciseId.PULL_UP,
            nameRes = R.string.exercise_pull_up,
            descriptionRes = R.string.exercise_pull_up_description,
            primaryMuscle = MuscleGroup.BACK,
            secondaryMuscles = setOf(MuscleGroup.BICEPS, MuscleGroup.SHOULDERS, MuscleGroup.CORE),
            movementPattern = MovementPattern.VERTICAL_PULL,
            requiredEquipment = setOf(Equipment.PULL_UP_BAR),
            difficulty = ExerciseDifficulty.ADVANCED,
            conflictingLimitations = setOf(
                MovementLimitation.AVOID_PRONATED_GRIP,
                MovementLimitation.AVOID_DEEP_SHOULDER_EXTENSION,
                MovementLimitation.AVOID_HEAVY_ELBOW_FLEXION,
            ),
            tags = setOf(ExerciseTag.COMPOUND, ExerciseTag.BODYWEIGHT, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 3..4, reps = 4..10, restSeconds = 120),
        ),
        ExerciseDefinition(
            id = ExerciseId.SEATED_CABLE_ROW,
            nameRes = R.string.exercise_seated_cable_row,
            descriptionRes = R.string.exercise_seated_cable_row_description,
            primaryMuscle = MuscleGroup.BACK,
            secondaryMuscles = setOf(MuscleGroup.BICEPS, MuscleGroup.SHOULDERS),
            movementPattern = MovementPattern.HORIZONTAL_PULL,
            requiredEquipment = setOf(Equipment.CABLE_MACHINE),
            difficulty = ExerciseDifficulty.BEGINNER,
            // Reaching forward at the start of each rep rounds the back under load.
            conflictingLimitations = setOf(MovementLimitation.AVOID_LOADED_SPINAL_FLEXION),
            tags = setOf(ExerciseTag.COMPOUND, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 3..4, reps = 8..12, restSeconds = 90),
        ),
        ExerciseDefinition(
            id = ExerciseId.CHEST_SUPPORTED_DUMBBELL_ROW,
            nameRes = R.string.exercise_chest_supported_dumbbell_row,
            descriptionRes = R.string.exercise_chest_supported_dumbbell_row_description,
            primaryMuscle = MuscleGroup.BACK,
            secondaryMuscles = setOf(MuscleGroup.BICEPS, MuscleGroup.SHOULDERS),
            movementPattern = MovementPattern.HORIZONTAL_PULL,
            requiredEquipment = setOf(Equipment.DUMBBELLS, Equipment.BENCH),
            difficulty = ExerciseDifficulty.BEGINNER,
            // The bench carries the torso: no spinal load, which makes this the fallback
            // horizontal pull for anyone with a back limitation.
            conflictingLimitations = emptySet(),
            tags = setOf(
                ExerciseTag.COMPOUND,
                ExerciseTag.CHEST_SUPPORTED,
                ExerciseTag.FREE_WEIGHT,
                ExerciseTag.BILATERAL,
            ),
            defaultPrescription = ExercisePrescription(sets = 3..4, reps = 8..12, restSeconds = 90),
        ),
        ExerciseDefinition(
            id = ExerciseId.ISO_LATERAL_ROW,
            nameRes = R.string.exercise_iso_lateral_row,
            descriptionRes = R.string.exercise_iso_lateral_row_description,
            primaryMuscle = MuscleGroup.BACK,
            secondaryMuscles = setOf(MuscleGroup.BICEPS, MuscleGroup.SHOULDERS),
            movementPattern = MovementPattern.HORIZONTAL_PULL,
            requiredEquipment = setOf(Equipment.SELECTORIZED_MACHINE),
            difficulty = ExerciseDifficulty.BEGINNER,
            conflictingLimitations = emptySet(),
            tags = setOf(
                ExerciseTag.COMPOUND,
                ExerciseTag.MACHINE_SUPPORTED,
                ExerciseTag.CHEST_SUPPORTED,
                ExerciseTag.UNILATERAL,
            ),
            defaultPrescription = ExercisePrescription(sets = 3..4, reps = 8..12, restSeconds = 90),
        ),
        ExerciseDefinition(
            id = ExerciseId.BARBELL_ROW,
            nameRes = R.string.exercise_barbell_row,
            descriptionRes = R.string.exercise_barbell_row_description,
            primaryMuscle = MuscleGroup.BACK,
            secondaryMuscles = setOf(MuscleGroup.BICEPS, MuscleGroup.SHOULDERS, MuscleGroup.HAMSTRINGS),
            movementPattern = MovementPattern.HORIZONTAL_PULL,
            requiredEquipment = setOf(Equipment.BARBELL),
            difficulty = ExerciseDifficulty.ADVANCED,
            // Every rep is performed from an unsupported hinge with the bar loading the spine.
            conflictingLimitations = setOf(
                MovementLimitation.AVOID_UNSUPPORTED_HIP_HINGE,
                MovementLimitation.AVOID_LOADED_SPINAL_FLEXION,
                MovementLimitation.AVOID_HIGH_SPINAL_COMPRESSION,
                MovementLimitation.AVOID_PRONATED_GRIP,
            ),
            tags = setOf(ExerciseTag.COMPOUND, ExerciseTag.FREE_WEIGHT, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 3..4, reps = 6..10, restSeconds = 120),
        ),
        ExerciseDefinition(
            id = ExerciseId.BAND_ROW,
            nameRes = R.string.exercise_band_row,
            descriptionRes = R.string.exercise_band_row_description,
            primaryMuscle = MuscleGroup.BACK,
            secondaryMuscles = setOf(MuscleGroup.BICEPS, MuscleGroup.SHOULDERS),
            movementPattern = MovementPattern.HORIZONTAL_PULL,
            requiredEquipment = setOf(Equipment.RESISTANCE_BAND),
            difficulty = ExerciseDifficulty.BEGINNER,
            conflictingLimitations = emptySet(),
            tags = setOf(ExerciseTag.COMPOUND, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 2..4, reps = 10..15, restSeconds = 60),
        ),

        // ------------------------------------------------------------ Shoulders

        ExerciseDefinition(
            id = ExerciseId.MACHINE_SHOULDER_PRESS,
            nameRes = R.string.exercise_machine_shoulder_press,
            descriptionRes = R.string.exercise_machine_shoulder_press_description,
            primaryMuscle = MuscleGroup.SHOULDERS,
            secondaryMuscles = setOf(MuscleGroup.TRICEPS),
            movementPattern = MovementPattern.VERTICAL_PUSH,
            requiredEquipment = setOf(Equipment.SELECTORIZED_MACHINE),
            difficulty = ExerciseDifficulty.BEGINNER,
            conflictingLimitations = setOf(MovementLimitation.AVOID_OVERHEAD_PRESSING),
            tags = setOf(
                ExerciseTag.COMPOUND,
                ExerciseTag.MACHINE_SUPPORTED,
                ExerciseTag.BILATERAL,
            ),
            defaultPrescription = ExercisePrescription(sets = 3..4, reps = 8..12, restSeconds = 90),
        ),
        ExerciseDefinition(
            id = ExerciseId.SEATED_DUMBBELL_SHOULDER_PRESS,
            nameRes = R.string.exercise_seated_dumbbell_shoulder_press,
            descriptionRes = R.string.exercise_seated_dumbbell_shoulder_press_description,
            primaryMuscle = MuscleGroup.SHOULDERS,
            secondaryMuscles = setOf(MuscleGroup.TRICEPS, MuscleGroup.CORE),
            movementPattern = MovementPattern.VERTICAL_PUSH,
            requiredEquipment = setOf(Equipment.DUMBBELLS, Equipment.BENCH),
            difficulty = ExerciseDifficulty.INTERMEDIATE,
            // The dumbbells travel out and up, so the arm abducts as well as presses.
            conflictingLimitations = setOf(
                MovementLimitation.AVOID_OVERHEAD_PRESSING,
                MovementLimitation.AVOID_SHOULDER_ABDUCTION,
            ),
            tags = setOf(ExerciseTag.COMPOUND, ExerciseTag.FREE_WEIGHT, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 3..4, reps = 8..12, restSeconds = 90),
        ),
        ExerciseDefinition(
            id = ExerciseId.DUMBBELL_LATERAL_RAISE,
            nameRes = R.string.exercise_dumbbell_lateral_raise,
            descriptionRes = R.string.exercise_dumbbell_lateral_raise_description,
            primaryMuscle = MuscleGroup.SHOULDERS,
            secondaryMuscles = emptySet(),
            movementPattern = MovementPattern.SHOULDER_ABDUCTION,
            requiredEquipment = setOf(Equipment.DUMBBELLS),
            difficulty = ExerciseDifficulty.BEGINNER,
            // Raising a weight out to the side is abduction with the shoulder internally
            // rotated — the exact position an impinged shoulder objects to.
            conflictingLimitations = setOf(
                MovementLimitation.AVOID_SHOULDER_ABDUCTION,
                MovementLimitation.AVOID_INTERNAL_ROTATION_UNDER_LOAD,
            ),
            tags = setOf(ExerciseTag.ISOLATION, ExerciseTag.FREE_WEIGHT, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 2..4, reps = 10..15, restSeconds = 60),
        ),
        ExerciseDefinition(
            id = ExerciseId.CABLE_LATERAL_RAISE,
            nameRes = R.string.exercise_cable_lateral_raise,
            descriptionRes = R.string.exercise_cable_lateral_raise_description,
            primaryMuscle = MuscleGroup.SHOULDERS,
            secondaryMuscles = emptySet(),
            movementPattern = MovementPattern.SHOULDER_ABDUCTION,
            requiredEquipment = setOf(Equipment.CABLE_MACHINE),
            difficulty = ExerciseDifficulty.INTERMEDIATE,
            conflictingLimitations = setOf(
                MovementLimitation.AVOID_SHOULDER_ABDUCTION,
                MovementLimitation.AVOID_INTERNAL_ROTATION_UNDER_LOAD,
            ),
            tags = setOf(ExerciseTag.ISOLATION, ExerciseTag.UNILATERAL),
            defaultPrescription = ExercisePrescription(sets = 2..3, reps = 10..15, restSeconds = 60),
        ),
        ExerciseDefinition(
            id = ExerciseId.BAND_LATERAL_RAISE,
            nameRes = R.string.exercise_band_lateral_raise,
            descriptionRes = R.string.exercise_band_lateral_raise_description,
            primaryMuscle = MuscleGroup.SHOULDERS,
            secondaryMuscles = emptySet(),
            movementPattern = MovementPattern.SHOULDER_ABDUCTION,
            requiredEquipment = setOf(Equipment.RESISTANCE_BAND),
            difficulty = ExerciseDifficulty.BEGINNER,
            // A band is lightest at the bottom, where an irritable shoulder is most
            // sensitive, so internal rotation under load is not listed here.
            conflictingLimitations = setOf(MovementLimitation.AVOID_SHOULDER_ABDUCTION),
            tags = setOf(ExerciseTag.ISOLATION, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 2..3, reps = 12..20, restSeconds = 45),
        ),
        ExerciseDefinition(
            id = ExerciseId.CABLE_REAR_DELT_FLY,
            nameRes = R.string.exercise_cable_rear_delt_fly,
            descriptionRes = R.string.exercise_cable_rear_delt_fly_description,
            primaryMuscle = MuscleGroup.SHOULDERS,
            secondaryMuscles = setOf(MuscleGroup.BACK),
            movementPattern = MovementPattern.SHOULDER_EXTERNAL_ROTATION,
            requiredEquipment = setOf(Equipment.CABLE_MACHINE),
            difficulty = ExerciseDifficulty.INTERMEDIATE,
            conflictingLimitations = emptySet(),
            tags = setOf(ExerciseTag.ISOLATION, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 2..3, reps = 12..15, restSeconds = 60),
        ),
        ExerciseDefinition(
            id = ExerciseId.FACE_PULL,
            nameRes = R.string.exercise_face_pull,
            descriptionRes = R.string.exercise_face_pull_description,
            primaryMuscle = MuscleGroup.SHOULDERS,
            secondaryMuscles = setOf(MuscleGroup.BACK),
            movementPattern = MovementPattern.SHOULDER_EXTERNAL_ROTATION,
            requiredEquipment = setOf(Equipment.CABLE_MACHINE),
            difficulty = ExerciseDifficulty.BEGINNER,
            conflictingLimitations = emptySet(),
            tags = setOf(ExerciseTag.ISOLATION, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 2..3, reps = 12..20, restSeconds = 60),
        ),
        ExerciseDefinition(
            id = ExerciseId.BAND_PULL_APART,
            nameRes = R.string.exercise_band_pull_apart,
            descriptionRes = R.string.exercise_band_pull_apart_description,
            primaryMuscle = MuscleGroup.SHOULDERS,
            secondaryMuscles = setOf(MuscleGroup.BACK),
            movementPattern = MovementPattern.SHOULDER_EXTERNAL_ROTATION,
            requiredEquipment = setOf(Equipment.RESISTANCE_BAND),
            difficulty = ExerciseDifficulty.BEGINNER,
            conflictingLimitations = emptySet(),
            tags = setOf(ExerciseTag.ISOLATION, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 2..3, reps = 12..20, restSeconds = 45),
        ),

        // --------------------------------------------------------------- Biceps

        ExerciseDefinition(
            id = ExerciseId.DUMBBELL_CURL,
            nameRes = R.string.exercise_dumbbell_curl,
            descriptionRes = R.string.exercise_dumbbell_curl_description,
            primaryMuscle = MuscleGroup.BICEPS,
            secondaryMuscles = emptySet(),
            movementPattern = MovementPattern.ELBOW_FLEXION,
            requiredEquipment = setOf(Equipment.DUMBBELLS),
            difficulty = ExerciseDifficulty.BEGINNER,
            conflictingLimitations = setOf(
                MovementLimitation.AVOID_HEAVY_ELBOW_FLEXION,
                MovementLimitation.AVOID_SUPINATED_GRIP,
            ),
            tags = setOf(ExerciseTag.ISOLATION, ExerciseTag.FREE_WEIGHT, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 2..4, reps = 8..12, restSeconds = 60),
        ),
        ExerciseDefinition(
            id = ExerciseId.HAMMER_CURL,
            nameRes = R.string.exercise_hammer_curl,
            descriptionRes = R.string.exercise_hammer_curl_description,
            primaryMuscle = MuscleGroup.BICEPS,
            secondaryMuscles = emptySet(),
            movementPattern = MovementPattern.ELBOW_FLEXION,
            requiredEquipment = setOf(Equipment.DUMBBELLS),
            difficulty = ExerciseDifficulty.BEGINNER,
            // Neutral grip throughout, so this is the curl that survives a grip limitation.
            conflictingLimitations = setOf(MovementLimitation.AVOID_HEAVY_ELBOW_FLEXION),
            tags = setOf(ExerciseTag.ISOLATION, ExerciseTag.FREE_WEIGHT, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 2..4, reps = 8..12, restSeconds = 60),
        ),
        ExerciseDefinition(
            id = ExerciseId.CABLE_CURL,
            nameRes = R.string.exercise_cable_curl,
            descriptionRes = R.string.exercise_cable_curl_description,
            primaryMuscle = MuscleGroup.BICEPS,
            secondaryMuscles = emptySet(),
            movementPattern = MovementPattern.ELBOW_FLEXION,
            requiredEquipment = setOf(Equipment.CABLE_MACHINE),
            difficulty = ExerciseDifficulty.BEGINNER,
            conflictingLimitations = setOf(
                MovementLimitation.AVOID_HEAVY_ELBOW_FLEXION,
                MovementLimitation.AVOID_SUPINATED_GRIP,
            ),
            tags = setOf(ExerciseTag.ISOLATION, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 2..3, reps = 10..15, restSeconds = 60),
        ),
        ExerciseDefinition(
            id = ExerciseId.BAND_CURL,
            nameRes = R.string.exercise_band_curl,
            descriptionRes = R.string.exercise_band_curl_description,
            primaryMuscle = MuscleGroup.BICEPS,
            secondaryMuscles = emptySet(),
            movementPattern = MovementPattern.ELBOW_FLEXION,
            requiredEquipment = setOf(Equipment.RESISTANCE_BAND),
            difficulty = ExerciseDifficulty.BEGINNER,
            conflictingLimitations = setOf(MovementLimitation.AVOID_SUPINATED_GRIP),
            tags = setOf(ExerciseTag.ISOLATION, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 2..3, reps = 12..20, restSeconds = 45),
        ),

        // -------------------------------------------------------------- Triceps

        ExerciseDefinition(
            id = ExerciseId.CABLE_TRICEPS_PRESSDOWN,
            nameRes = R.string.exercise_cable_triceps_pressdown,
            descriptionRes = R.string.exercise_cable_triceps_pressdown_description,
            primaryMuscle = MuscleGroup.TRICEPS,
            secondaryMuscles = emptySet(),
            movementPattern = MovementPattern.ELBOW_EXTENSION,
            requiredEquipment = setOf(Equipment.CABLE_MACHINE),
            difficulty = ExerciseDifficulty.BEGINNER,
            conflictingLimitations = setOf(
                MovementLimitation.AVOID_HEAVY_ELBOW_EXTENSION,
                MovementLimitation.AVOID_PRONATED_GRIP,
            ),
            tags = setOf(ExerciseTag.ISOLATION, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 2..3, reps = 10..15, restSeconds = 60),
        ),
        ExerciseDefinition(
            id = ExerciseId.OVERHEAD_CABLE_TRICEPS_EXTENSION,
            nameRes = R.string.exercise_overhead_cable_triceps_extension,
            descriptionRes = R.string.exercise_overhead_cable_triceps_extension_description,
            primaryMuscle = MuscleGroup.TRICEPS,
            secondaryMuscles = emptySet(),
            movementPattern = MovementPattern.ELBOW_EXTENSION,
            requiredEquipment = setOf(Equipment.CABLE_MACHINE),
            difficulty = ExerciseDifficulty.INTERMEDIATE,
            // Held overhead throughout, which is the position an overhead-pressing
            // limitation is about even though nothing is being pressed.
            conflictingLimitations = setOf(
                MovementLimitation.AVOID_HEAVY_ELBOW_EXTENSION,
                MovementLimitation.AVOID_OVERHEAD_PRESSING,
            ),
            tags = setOf(ExerciseTag.ISOLATION, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 2..3, reps = 10..15, restSeconds = 60),
        ),
        ExerciseDefinition(
            id = ExerciseId.BAND_TRICEPS_PRESSDOWN,
            nameRes = R.string.exercise_band_triceps_pressdown,
            descriptionRes = R.string.exercise_band_triceps_pressdown_description,
            primaryMuscle = MuscleGroup.TRICEPS,
            secondaryMuscles = emptySet(),
            movementPattern = MovementPattern.ELBOW_EXTENSION,
            requiredEquipment = setOf(Equipment.RESISTANCE_BAND),
            difficulty = ExerciseDifficulty.BEGINNER,
            conflictingLimitations = setOf(MovementLimitation.AVOID_PRONATED_GRIP),
            tags = setOf(ExerciseTag.ISOLATION, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 2..3, reps = 12..20, restSeconds = 45),
        ),
        ExerciseDefinition(
            id = ExerciseId.ASSISTED_DIP,
            nameRes = R.string.exercise_assisted_dip,
            descriptionRes = R.string.exercise_assisted_dip_description,
            primaryMuscle = MuscleGroup.TRICEPS,
            secondaryMuscles = setOf(MuscleGroup.CHEST, MuscleGroup.SHOULDERS),
            movementPattern = MovementPattern.ELBOW_EXTENSION,
            requiredEquipment = setOf(Equipment.SELECTORIZED_MACHINE),
            difficulty = ExerciseDifficulty.INTERMEDIATE,
            // Counterweighted, but the shoulder still travels behind the body and the
            // wrists still bear the load.
            conflictingLimitations = setOf(
                MovementLimitation.AVOID_DEEP_SHOULDER_EXTENSION,
                MovementLimitation.AVOID_HEAVY_ELBOW_EXTENSION,
                MovementLimitation.AVOID_LOADED_WRIST_EXTENSION,
            ),
            tags = setOf(
                ExerciseTag.COMPOUND,
                ExerciseTag.MACHINE_SUPPORTED,
                ExerciseTag.BILATERAL,
            ),
            defaultPrescription = ExercisePrescription(sets = 3..4, reps = 6..12, restSeconds = 90),
        ),
        ExerciseDefinition(
            id = ExerciseId.DIP,
            nameRes = R.string.exercise_dip,
            descriptionRes = R.string.exercise_dip_description,
            primaryMuscle = MuscleGroup.TRICEPS,
            secondaryMuscles = setOf(MuscleGroup.CHEST, MuscleGroup.SHOULDERS),
            movementPattern = MovementPattern.ELBOW_EXTENSION,
            requiredEquipment = setOf(Equipment.BODYWEIGHT),
            difficulty = ExerciseDifficulty.ADVANCED,
            conflictingLimitations = setOf(
                MovementLimitation.AVOID_DEEP_SHOULDER_EXTENSION,
                MovementLimitation.AVOID_HEAVY_ELBOW_EXTENSION,
                MovementLimitation.AVOID_LOADED_WRIST_EXTENSION,
            ),
            tags = setOf(ExerciseTag.COMPOUND, ExerciseTag.BODYWEIGHT, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 3..4, reps = 5..10, restSeconds = 120),
        ),

        // ----------------------------------------------------------- Quadriceps

        ExerciseDefinition(
            id = ExerciseId.BODYWEIGHT_SQUAT,
            nameRes = R.string.exercise_bodyweight_squat,
            descriptionRes = R.string.exercise_bodyweight_squat_description,
            primaryMuscle = MuscleGroup.QUADRICEPS,
            secondaryMuscles = setOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.CORE),
            movementPattern = MovementPattern.SQUAT,
            requiredEquipment = setOf(Equipment.BODYWEIGHT),
            difficulty = ExerciseDifficulty.BEGINNER,
            // Unloaded, so only the shape of the movement conflicts, not the load.
            conflictingLimitations = setOf(MovementLimitation.AVOID_DEEP_KNEE_FLEXION),
            tags = setOf(ExerciseTag.COMPOUND, ExerciseTag.BODYWEIGHT, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 2..4, reps = 10..20, restSeconds = 60),
        ),
        ExerciseDefinition(
            id = ExerciseId.GOBLET_SQUAT,
            nameRes = R.string.exercise_goblet_squat,
            descriptionRes = R.string.exercise_goblet_squat_description,
            primaryMuscle = MuscleGroup.QUADRICEPS,
            secondaryMuscles = setOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.CORE),
            movementPattern = MovementPattern.SQUAT,
            requiredEquipment = setOf(Equipment.DUMBBELLS),
            difficulty = ExerciseDifficulty.BEGINNER,
            conflictingLimitations = setOf(
                MovementLimitation.AVOID_DEEP_KNEE_FLEXION,
                MovementLimitation.AVOID_HEAVY_KNEE_LOADING,
                MovementLimitation.AVOID_DEEP_HIP_FLEXION,
                MovementLimitation.AVOID_HIGH_SPINAL_COMPRESSION,
            ),
            tags = setOf(ExerciseTag.COMPOUND, ExerciseTag.FREE_WEIGHT, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 3..4, reps = 8..12, restSeconds = 90),
        ),
        ExerciseDefinition(
            id = ExerciseId.LEG_PRESS,
            nameRes = R.string.exercise_leg_press,
            descriptionRes = R.string.exercise_leg_press_description,
            primaryMuscle = MuscleGroup.QUADRICEPS,
            secondaryMuscles = setOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS),
            movementPattern = MovementPattern.SQUAT,
            requiredEquipment = setOf(Equipment.SELECTORIZED_MACHINE),
            difficulty = ExerciseDifficulty.BEGINNER,
            // The seat takes the spinal load, which is why this survives a back
            // limitation where a goblet squat does not.
            conflictingLimitations = setOf(
                MovementLimitation.AVOID_DEEP_KNEE_FLEXION,
                MovementLimitation.AVOID_HEAVY_KNEE_LOADING,
                MovementLimitation.AVOID_DEEP_HIP_FLEXION,
            ),
            tags = setOf(
                ExerciseTag.COMPOUND,
                ExerciseTag.MACHINE_SUPPORTED,
                ExerciseTag.BILATERAL,
            ),
            defaultPrescription = ExercisePrescription(sets = 3..4, reps = 8..15, restSeconds = 90),
        ),
        ExerciseDefinition(
            id = ExerciseId.LEG_EXTENSION,
            nameRes = R.string.exercise_leg_extension,
            descriptionRes = R.string.exercise_leg_extension_description,
            primaryMuscle = MuscleGroup.QUADRICEPS,
            secondaryMuscles = emptySet(),
            movementPattern = MovementPattern.SQUAT,
            requiredEquipment = setOf(Equipment.SELECTORIZED_MACHINE),
            difficulty = ExerciseDifficulty.BEGINNER,
            // Isolated load straight through the knee, but never a deep bend.
            conflictingLimitations = setOf(MovementLimitation.AVOID_HEAVY_KNEE_LOADING),
            tags = setOf(
                ExerciseTag.ISOLATION,
                ExerciseTag.MACHINE_SUPPORTED,
                ExerciseTag.BILATERAL,
            ),
            defaultPrescription = ExercisePrescription(sets = 2..3, reps = 10..15, restSeconds = 60),
        ),
        ExerciseDefinition(
            id = ExerciseId.BULGARIAN_SPLIT_SQUAT,
            nameRes = R.string.exercise_bulgarian_split_squat,
            descriptionRes = R.string.exercise_bulgarian_split_squat_description,
            primaryMuscle = MuscleGroup.QUADRICEPS,
            secondaryMuscles = setOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.CORE),
            movementPattern = MovementPattern.LUNGE,
            requiredEquipment = setOf(Equipment.BENCH),
            difficulty = ExerciseDifficulty.ADVANCED,
            conflictingLimitations = setOf(
                MovementLimitation.AVOID_SINGLE_LEG_LOADING,
                MovementLimitation.AVOID_UNILATERAL_BALANCE_DEMAND,
                MovementLimitation.AVOID_DEEP_KNEE_FLEXION,
            ),
            tags = setOf(
                ExerciseTag.COMPOUND,
                ExerciseTag.BODYWEIGHT,
                ExerciseTag.UNILATERAL,
                ExerciseTag.REQUIRES_BALANCE,
            ),
            defaultPrescription = ExercisePrescription(sets = 3..4, reps = 8..12, restSeconds = 90),
        ),
        ExerciseDefinition(
            id = ExerciseId.WALKING_LUNGE,
            nameRes = R.string.exercise_walking_lunge,
            descriptionRes = R.string.exercise_walking_lunge_description,
            primaryMuscle = MuscleGroup.QUADRICEPS,
            secondaryMuscles = setOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.CORE),
            movementPattern = MovementPattern.LUNGE,
            requiredEquipment = setOf(Equipment.BODYWEIGHT),
            difficulty = ExerciseDifficulty.INTERMEDIATE,
            conflictingLimitations = setOf(
                MovementLimitation.AVOID_SINGLE_LEG_LOADING,
                MovementLimitation.AVOID_UNILATERAL_BALANCE_DEMAND,
                MovementLimitation.AVOID_DEEP_KNEE_FLEXION,
            ),
            tags = setOf(
                ExerciseTag.COMPOUND,
                ExerciseTag.BODYWEIGHT,
                ExerciseTag.UNILATERAL,
                ExerciseTag.REQUIRES_BALANCE,
            ),
            defaultPrescription = ExercisePrescription(sets = 2..3, reps = 10..16, restSeconds = 75),
        ),
        ExerciseDefinition(
            id = ExerciseId.STEP_UP,
            nameRes = R.string.exercise_step_up,
            descriptionRes = R.string.exercise_step_up_description,
            primaryMuscle = MuscleGroup.QUADRICEPS,
            secondaryMuscles = setOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.CORE),
            movementPattern = MovementPattern.LUNGE,
            requiredEquipment = setOf(Equipment.BENCH),
            difficulty = ExerciseDifficulty.BEGINNER,
            conflictingLimitations = setOf(
                MovementLimitation.AVOID_SINGLE_LEG_LOADING,
                MovementLimitation.AVOID_UNILATERAL_BALANCE_DEMAND,
                MovementLimitation.AVOID_DEEP_KNEE_FLEXION,
            ),
            tags = setOf(
                ExerciseTag.COMPOUND,
                ExerciseTag.BODYWEIGHT,
                ExerciseTag.UNILATERAL,
                ExerciseTag.REQUIRES_BALANCE,
            ),
            defaultPrescription = ExercisePrescription(sets = 2..3, reps = 8..12, restSeconds = 75),
        ),

        // ----------------------------------------------------------- Hamstrings

        ExerciseDefinition(
            id = ExerciseId.DUMBBELL_ROMANIAN_DEADLIFT,
            nameRes = R.string.exercise_dumbbell_romanian_deadlift,
            descriptionRes = R.string.exercise_dumbbell_romanian_deadlift_description,
            primaryMuscle = MuscleGroup.HAMSTRINGS,
            secondaryMuscles = setOf(MuscleGroup.GLUTES, MuscleGroup.BACK),
            movementPattern = MovementPattern.HIP_HINGE,
            requiredEquipment = setOf(Equipment.DUMBBELLS),
            difficulty = ExerciseDifficulty.INTERMEDIATE,
            conflictingLimitations = setOf(
                MovementLimitation.AVOID_UNSUPPORTED_HIP_HINGE,
                MovementLimitation.AVOID_LOADED_SPINAL_FLEXION,
            ),
            tags = setOf(ExerciseTag.COMPOUND, ExerciseTag.FREE_WEIGHT, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 3..4, reps = 6..12, restSeconds = 120),
        ),
        ExerciseDefinition(
            id = ExerciseId.SINGLE_LEG_ROMANIAN_DEADLIFT,
            nameRes = R.string.exercise_single_leg_romanian_deadlift,
            descriptionRes = R.string.exercise_single_leg_romanian_deadlift_description,
            primaryMuscle = MuscleGroup.HAMSTRINGS,
            secondaryMuscles = setOf(MuscleGroup.GLUTES, MuscleGroup.BACK, MuscleGroup.CORE),
            movementPattern = MovementPattern.HIP_HINGE,
            requiredEquipment = setOf(Equipment.DUMBBELLS),
            difficulty = ExerciseDifficulty.ADVANCED,
            conflictingLimitations = setOf(
                MovementLimitation.AVOID_UNSUPPORTED_HIP_HINGE,
                MovementLimitation.AVOID_LOADED_SPINAL_FLEXION,
                MovementLimitation.AVOID_SINGLE_LEG_LOADING,
                MovementLimitation.AVOID_UNILATERAL_BALANCE_DEMAND,
            ),
            tags = setOf(
                ExerciseTag.COMPOUND,
                ExerciseTag.FREE_WEIGHT,
                ExerciseTag.UNILATERAL,
                ExerciseTag.REQUIRES_BALANCE,
            ),
            defaultPrescription = ExercisePrescription(sets = 2..3, reps = 8..12, restSeconds = 90),
        ),
        ExerciseDefinition(
            id = ExerciseId.SEATED_LEG_CURL,
            nameRes = R.string.exercise_seated_leg_curl,
            descriptionRes = R.string.exercise_seated_leg_curl_description,
            primaryMuscle = MuscleGroup.HAMSTRINGS,
            secondaryMuscles = setOf(MuscleGroup.CALVES),
            movementPattern = MovementPattern.KNEE_FLEXION,
            requiredEquipment = setOf(Equipment.SELECTORIZED_MACHINE),
            difficulty = ExerciseDifficulty.BEGINNER,
            // Performed with the hips bent to roughly ninety degrees for the whole set.
            conflictingLimitations = setOf(MovementLimitation.AVOID_DEEP_HIP_FLEXION),
            tags = setOf(
                ExerciseTag.ISOLATION,
                ExerciseTag.MACHINE_SUPPORTED,
                ExerciseTag.BILATERAL,
            ),
            defaultPrescription = ExercisePrescription(sets = 3..4, reps = 8..15, restSeconds = 75),
        ),
        ExerciseDefinition(
            id = ExerciseId.LYING_LEG_CURL,
            nameRes = R.string.exercise_lying_leg_curl,
            descriptionRes = R.string.exercise_lying_leg_curl_description,
            primaryMuscle = MuscleGroup.HAMSTRINGS,
            secondaryMuscles = setOf(MuscleGroup.CALVES),
            movementPattern = MovementPattern.KNEE_FLEXION,
            requiredEquipment = setOf(Equipment.SELECTORIZED_MACHINE),
            difficulty = ExerciseDifficulty.BEGINNER,
            // Face down, so the lower back tends to arch as the heels are pulled in.
            conflictingLimitations = setOf(MovementLimitation.AVOID_LOADED_SPINAL_EXTENSION),
            tags = setOf(
                ExerciseTag.ISOLATION,
                ExerciseTag.MACHINE_SUPPORTED,
                ExerciseTag.BILATERAL,
            ),
            defaultPrescription = ExercisePrescription(sets = 3..4, reps = 8..15, restSeconds = 75),
        ),
        ExerciseDefinition(
            id = ExerciseId.NORDIC_HAMSTRING_CURL,
            nameRes = R.string.exercise_nordic_hamstring_curl,
            descriptionRes = R.string.exercise_nordic_hamstring_curl_description,
            primaryMuscle = MuscleGroup.HAMSTRINGS,
            secondaryMuscles = setOf(MuscleGroup.GLUTES, MuscleGroup.CORE),
            movementPattern = MovementPattern.KNEE_FLEXION,
            requiredEquipment = setOf(Equipment.BODYWEIGHT),
            difficulty = ExerciseDifficulty.ADVANCED,
            // Performed kneeling with bodyweight through the knees the whole time.
            conflictingLimitations = setOf(
                MovementLimitation.AVOID_KNEELING,
                MovementLimitation.AVOID_HEAVY_KNEE_LOADING,
            ),
            tags = setOf(ExerciseTag.ISOLATION, ExerciseTag.BODYWEIGHT, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 2..3, reps = 3..8, restSeconds = 120),
        ),

        // --------------------------------------------------------------- Glutes

        ExerciseDefinition(
            id = ExerciseId.GLUTE_BRIDGE,
            nameRes = R.string.exercise_glute_bridge,
            descriptionRes = R.string.exercise_glute_bridge_description,
            primaryMuscle = MuscleGroup.GLUTES,
            secondaryMuscles = setOf(MuscleGroup.HAMSTRINGS, MuscleGroup.CORE),
            movementPattern = MovementPattern.HIP_EXTENSION,
            requiredEquipment = setOf(Equipment.BODYWEIGHT),
            difficulty = ExerciseDifficulty.BEGINNER,
            conflictingLimitations = setOf(MovementLimitation.AVOID_LOADED_SPINAL_EXTENSION),
            tags = setOf(ExerciseTag.ISOLATION, ExerciseTag.BODYWEIGHT, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 2..3, reps = 10..20, restSeconds = 60),
        ),
        ExerciseDefinition(
            id = ExerciseId.HIP_THRUST,
            nameRes = R.string.exercise_hip_thrust,
            descriptionRes = R.string.exercise_hip_thrust_description,
            primaryMuscle = MuscleGroup.GLUTES,
            secondaryMuscles = setOf(MuscleGroup.HAMSTRINGS, MuscleGroup.QUADRICEPS),
            movementPattern = MovementPattern.HIP_EXTENSION,
            requiredEquipment = setOf(Equipment.BARBELL, Equipment.BENCH),
            difficulty = ExerciseDifficulty.INTERMEDIATE,
            conflictingLimitations = setOf(
                MovementLimitation.AVOID_LOADED_SPINAL_EXTENSION,
                MovementLimitation.AVOID_DEEP_HIP_FLEXION,
            ),
            tags = setOf(ExerciseTag.COMPOUND, ExerciseTag.FREE_WEIGHT, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 3..4, reps = 8..12, restSeconds = 90),
        ),
        ExerciseDefinition(
            id = ExerciseId.CABLE_PULL_THROUGH,
            nameRes = R.string.exercise_cable_pull_through,
            descriptionRes = R.string.exercise_cable_pull_through_description,
            primaryMuscle = MuscleGroup.GLUTES,
            secondaryMuscles = setOf(MuscleGroup.HAMSTRINGS, MuscleGroup.BACK),
            movementPattern = MovementPattern.HIP_HINGE,
            requiredEquipment = setOf(Equipment.CABLE_MACHINE),
            difficulty = ExerciseDifficulty.BEGINNER,
            // The cable pulls horizontally rather than down, so the spine is not
            // compressed — but it is still an unsupported hinge.
            conflictingLimitations = setOf(MovementLimitation.AVOID_UNSUPPORTED_HIP_HINGE),
            tags = setOf(ExerciseTag.COMPOUND, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 3..4, reps = 10..15, restSeconds = 75),
        ),
        ExerciseDefinition(
            id = ExerciseId.HIP_ABDUCTION_MACHINE,
            nameRes = R.string.exercise_hip_abduction_machine,
            descriptionRes = R.string.exercise_hip_abduction_machine_description,
            primaryMuscle = MuscleGroup.GLUTES,
            secondaryMuscles = emptySet(),
            movementPattern = MovementPattern.HIP_ABDUCTION,
            requiredEquipment = setOf(Equipment.SELECTORIZED_MACHINE),
            difficulty = ExerciseDifficulty.BEGINNER,
            conflictingLimitations = setOf(MovementLimitation.AVOID_WIDE_HIP_ABDUCTION),
            tags = setOf(
                ExerciseTag.ISOLATION,
                ExerciseTag.MACHINE_SUPPORTED,
                ExerciseTag.BILATERAL,
            ),
            defaultPrescription = ExercisePrescription(sets = 2..3, reps = 12..20, restSeconds = 60),
        ),
        ExerciseDefinition(
            id = ExerciseId.BAND_HIP_ABDUCTION,
            nameRes = R.string.exercise_band_hip_abduction,
            descriptionRes = R.string.exercise_band_hip_abduction_description,
            primaryMuscle = MuscleGroup.GLUTES,
            secondaryMuscles = emptySet(),
            movementPattern = MovementPattern.HIP_ABDUCTION,
            requiredEquipment = setOf(Equipment.RESISTANCE_BAND),
            difficulty = ExerciseDifficulty.BEGINNER,
            conflictingLimitations = setOf(MovementLimitation.AVOID_WIDE_HIP_ABDUCTION),
            tags = setOf(ExerciseTag.ISOLATION, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 2..3, reps = 12..20, restSeconds = 45),
        ),
        ExerciseDefinition(
            id = ExerciseId.HIP_ADDUCTION_MACHINE,
            nameRes = R.string.exercise_hip_adduction_machine,
            descriptionRes = R.string.exercise_hip_adduction_machine_description,
            primaryMuscle = MuscleGroup.GLUTES,
            secondaryMuscles = emptySet(),
            movementPattern = MovementPattern.HIP_ADDUCTION,
            requiredEquipment = setOf(Equipment.SELECTORIZED_MACHINE),
            difficulty = ExerciseDifficulty.BEGINNER,
            // Adduction is the working direction, but every rep *starts* with the legs
            // held wide apart, which is the position the limitation is about.
            conflictingLimitations = setOf(MovementLimitation.AVOID_WIDE_HIP_ABDUCTION),
            tags = setOf(
                ExerciseTag.ISOLATION,
                ExerciseTag.MACHINE_SUPPORTED,
                ExerciseTag.BILATERAL,
            ),
            defaultPrescription = ExercisePrescription(sets = 2..3, reps = 12..20, restSeconds = 60),
        ),
        ExerciseDefinition(
            id = ExerciseId.BAND_HIP_ADDUCTION,
            nameRes = R.string.exercise_band_hip_adduction,
            descriptionRes = R.string.exercise_band_hip_adduction_description,
            primaryMuscle = MuscleGroup.GLUTES,
            secondaryMuscles = setOf(MuscleGroup.CORE),
            movementPattern = MovementPattern.HIP_ADDUCTION,
            requiredEquipment = setOf(Equipment.RESISTANCE_BAND),
            difficulty = ExerciseDifficulty.BEGINNER,
            // Performed standing on the opposite leg.
            conflictingLimitations = setOf(MovementLimitation.AVOID_UNILATERAL_BALANCE_DEMAND),
            tags = setOf(
                ExerciseTag.ISOLATION,
                ExerciseTag.UNILATERAL,
                ExerciseTag.REQUIRES_BALANCE,
            ),
            defaultPrescription = ExercisePrescription(sets = 2..3, reps = 12..20, restSeconds = 45),
        ),

        // --------------------------------------------------------------- Calves

        ExerciseDefinition(
            id = ExerciseId.STANDING_CALF_RAISE,
            nameRes = R.string.exercise_standing_calf_raise,
            descriptionRes = R.string.exercise_standing_calf_raise_description,
            primaryMuscle = MuscleGroup.CALVES,
            secondaryMuscles = emptySet(),
            movementPattern = MovementPattern.CALF_RAISE,
            requiredEquipment = setOf(Equipment.BODYWEIGHT),
            difficulty = ExerciseDifficulty.BEGINNER,
            conflictingLimitations = emptySet(),
            tags = setOf(ExerciseTag.ISOLATION, ExerciseTag.BODYWEIGHT, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 3..4, reps = 12..20, restSeconds = 45),
        ),
        ExerciseDefinition(
            id = ExerciseId.SEATED_CALF_RAISE,
            nameRes = R.string.exercise_seated_calf_raise,
            descriptionRes = R.string.exercise_seated_calf_raise_description,
            primaryMuscle = MuscleGroup.CALVES,
            secondaryMuscles = emptySet(),
            movementPattern = MovementPattern.CALF_RAISE,
            requiredEquipment = setOf(Equipment.SELECTORIZED_MACHINE),
            difficulty = ExerciseDifficulty.BEGINNER,
            conflictingLimitations = emptySet(),
            tags = setOf(
                ExerciseTag.ISOLATION,
                ExerciseTag.MACHINE_SUPPORTED,
                ExerciseTag.BILATERAL,
            ),
            defaultPrescription = ExercisePrescription(sets = 3..4, reps = 12..20, restSeconds = 45),
        ),

        // ----------------------------------------------------------------- Core

        ExerciseDefinition(
            id = ExerciseId.DEAD_BUG,
            nameRes = R.string.exercise_dead_bug,
            descriptionRes = R.string.exercise_dead_bug_description,
            primaryMuscle = MuscleGroup.CORE,
            secondaryMuscles = emptySet(),
            movementPattern = MovementPattern.CORE_ANTI_EXTENSION,
            requiredEquipment = setOf(Equipment.BODYWEIGHT),
            difficulty = ExerciseDifficulty.BEGINNER,
            // Lying on the back with no load and no spinal movement: this is the core
            // exercise that survives almost every limitation set.
            conflictingLimitations = emptySet(),
            tags = setOf(ExerciseTag.ISOLATION, ExerciseTag.BODYWEIGHT, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 2..3, reps = 8..12, restSeconds = 45),
        ),
        ExerciseDefinition(
            id = ExerciseId.BIRD_DOG,
            nameRes = R.string.exercise_bird_dog,
            descriptionRes = R.string.exercise_bird_dog_description,
            primaryMuscle = MuscleGroup.CORE,
            secondaryMuscles = setOf(MuscleGroup.GLUTES, MuscleGroup.BACK),
            movementPattern = MovementPattern.CORE_ANTI_ROTATION,
            requiredEquipment = setOf(Equipment.BODYWEIGHT),
            difficulty = ExerciseDifficulty.BEGINNER,
            conflictingLimitations = setOf(MovementLimitation.AVOID_KNEELING),
            tags = setOf(ExerciseTag.ISOLATION, ExerciseTag.BODYWEIGHT, ExerciseTag.UNILATERAL),
            defaultPrescription = ExercisePrescription(sets = 2..3, reps = 8..12, restSeconds = 45),
        ),
        ExerciseDefinition(
            id = ExerciseId.FRONT_PLANK,
            nameRes = R.string.exercise_front_plank,
            descriptionRes = R.string.exercise_front_plank_description,
            primaryMuscle = MuscleGroup.CORE,
            secondaryMuscles = setOf(MuscleGroup.SHOULDERS),
            movementPattern = MovementPattern.CORE_ANTI_EXTENSION,
            requiredEquipment = setOf(Equipment.BODYWEIGHT),
            difficulty = ExerciseDifficulty.BEGINNER,
            // On the forearms, so the wrists stay neutral — unlike a push-up.
            conflictingLimitations = emptySet(),
            tags = setOf(ExerciseTag.ISOLATION, ExerciseTag.BODYWEIGHT, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 2..3, reps = 1..1, restSeconds = 45),
        ),
        ExerciseDefinition(
            id = ExerciseId.SIDE_PLANK,
            nameRes = R.string.exercise_side_plank,
            descriptionRes = R.string.exercise_side_plank_description,
            primaryMuscle = MuscleGroup.CORE,
            secondaryMuscles = setOf(MuscleGroup.SHOULDERS, MuscleGroup.GLUTES),
            movementPattern = MovementPattern.CORE_ANTI_ROTATION,
            requiredEquipment = setOf(Equipment.BODYWEIGHT),
            difficulty = ExerciseDifficulty.BEGINNER,
            conflictingLimitations = emptySet(),
            tags = setOf(ExerciseTag.ISOLATION, ExerciseTag.BODYWEIGHT, ExerciseTag.UNILATERAL),
            defaultPrescription = ExercisePrescription(sets = 2..3, reps = 1..1, restSeconds = 45),
        ),
        ExerciseDefinition(
            id = ExerciseId.PALLOF_PRESS,
            nameRes = R.string.exercise_pallof_press,
            descriptionRes = R.string.exercise_pallof_press_description,
            primaryMuscle = MuscleGroup.CORE,
            secondaryMuscles = emptySet(),
            movementPattern = MovementPattern.CORE_ANTI_ROTATION,
            requiredEquipment = setOf(Equipment.CABLE_MACHINE),
            difficulty = ExerciseDifficulty.BEGINNER,
            // Resisting rotation is the opposite of performing it, so a
            // no-spinal-rotation limitation does not exclude this.
            conflictingLimitations = emptySet(),
            tags = setOf(ExerciseTag.ISOLATION, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 2..3, reps = 8..15, restSeconds = 60),
        ),
        ExerciseDefinition(
            id = ExerciseId.CABLE_CRUNCH,
            nameRes = R.string.exercise_cable_crunch,
            descriptionRes = R.string.exercise_cable_crunch_description,
            primaryMuscle = MuscleGroup.CORE,
            secondaryMuscles = emptySet(),
            movementPattern = MovementPattern.CORE_FLEXION,
            requiredEquipment = setOf(Equipment.CABLE_MACHINE),
            difficulty = ExerciseDifficulty.INTERMEDIATE,
            // Rounding the spine against a load, from a kneeling position.
            conflictingLimitations = setOf(
                MovementLimitation.AVOID_LOADED_SPINAL_FLEXION,
                MovementLimitation.AVOID_KNEELING,
            ),
            tags = setOf(ExerciseTag.ISOLATION, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 2..3, reps = 10..15, restSeconds = 60),
        ),
        ExerciseDefinition(
            id = ExerciseId.HANGING_KNEE_RAISE,
            nameRes = R.string.exercise_hanging_knee_raise,
            descriptionRes = R.string.exercise_hanging_knee_raise_description,
            primaryMuscle = MuscleGroup.CORE,
            secondaryMuscles = setOf(MuscleGroup.QUADRICEPS),
            movementPattern = MovementPattern.CORE_FLEXION,
            requiredEquipment = setOf(Equipment.PULL_UP_BAR),
            difficulty = ExerciseDifficulty.INTERMEDIATE,
            conflictingLimitations = setOf(
                MovementLimitation.AVOID_LOADED_SPINAL_FLEXION,
                MovementLimitation.AVOID_DEEP_HIP_FLEXION,
                MovementLimitation.AVOID_PRONATED_GRIP,
            ),
            tags = setOf(ExerciseTag.ISOLATION, ExerciseTag.BODYWEIGHT, ExerciseTag.BILATERAL),
            defaultPrescription = ExercisePrescription(sets = 2..3, reps = 8..15, restSeconds = 60),
        ),
    )
}
