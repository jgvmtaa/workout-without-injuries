# Workout Planner — Product And Implementation Specification

> **What this file is.** The numbered specification (§1–§31) that every task in
> [`tasks/`](../tasks/README.md) and most KDoc in the codebase cites as
> "README §N". It was the root `README.md` until commit `3c141c7` replaced that
> file with a short product summary, after which the references dangled. Restored
> here verbatim so they resolve again.
>
> It is a **historical document**: it records the plan as written before
> implementation started, contradictions included (see §4.3 vs §12.1 on the
> three-day split). Where implementation has since diverged, the divergence is
> recorded in [`follow-ups.md`](follow-ups.md), not by editing this file.
> Section numbers are load-bearing — do not renumber.

Workout Planner is an Android app that creates practical workout plans around a
person's training preferences, available equipment, and confirmed movement
limitations. It helps users avoid exercises that may conflict with the
limitations they select while keeping every recommendation understandable and
editable.

## How It Works

1. The user chooses a training goal, experience level, weekly schedule, session
   duration, and available equipment.
2. The user can record previous injuries. The app may suggest related movement
   limitations, but the user decides which limitations currently apply.
3. The app evaluates a built-in exercise catalog against the confirmed
   limitations, available equipment, and experience level.
4. A deterministic planner generates a workout from eligible exercises. The
   same profile produces the same plan.
5. The user can browse the exercise library, understand why an exercise was
   excluded, and remove, replace, reorder, or regenerate planned exercises.
6. The profile and current plan are stored locally so they remain available
   after the app is closed.

## Safety Approach

Workout Planner does not diagnose injuries, prescribe rehabilitation, or
replace a physician or physical therapist. Injury history is used only to
suggest possible movement limitations. Exercise filtering is based on the
limitations the user explicitly confirms, and the app explains each conflict
instead of declaring an exercise universally safe or unsafe for an injury.

## MVP Scope

The MVP is local-first and deterministic. It does not require an account, cloud
service, backend, subscription, social feature, or AI-generated workout advice.
The repository currently contains the product and technical specification plus
the sequential implementation plan in [`tasks/`](tasks/README.md).

## Product And Implementation Specification

1. MVP objective
Build an Android application that:
Collects basic workout preferences.
Lets users identify injuries or movement limitations.
Maintains a static catalog of exercises grouped by muscle group.
Excludes exercises that conflict with the selected limitations.
Generates a simple workout plan from the remaining exercises.
Lets users inspect and manually adjust the generated plan.
Saves the profile and current plan locally.
The MVP is not intended to:
Diagnose injuries.
Prescribe rehabilitation.
Replace a physician or physical therapist.
Generate recommendations through an LLM.
Synchronize data between devices.
Support accounts, subscriptions, or social features.
Dynamically download exercise content.

2. Core product rule
The app should not directly claim:
“This exercise is unsafe for an ACL injury.”
Instead, it should represent rules more conservatively:
“This exercise may conflict with the movement limitations you selected.”
The filtering engine should primarily use functional limitations, such as:
Avoid deep knee flexion.
Avoid jumping.
Avoid loaded spinal flexion.
Avoid unsupported hip-hinge positions.
Avoid overhead pressing.
Avoid painful shoulder abduction.
Avoid high-impact movement.
Avoid loaded wrist extension.
Known injuries can suggest default limitations, but users should confirm those limitations.
Example:
Selected history:
ACL injury

Suggested limitations:
[x] Avoid jumping and landing
[x] Avoid rapid direction changes
[ ] Avoid deep knee flexion
[ ] Avoid unilateral knee-dominant exercises

This avoids treating every person with the same injury identically.

3. MVP user flow
First launch
Welcome
    ↓
Safety notice
    ↓
Training preferences
    ↓
Injury history
    ↓
Movement limitations
    ↓
Review profile
    ↓
Generate plan
    ↓
Plan screen

Returning user
Home
    ├── Current plan
    ├── Exercise library
    └── Profile and limitations


4. MVP screens
4.1 Welcome screen
Purpose:
Briefly explain what the app does.
Start onboarding.
Content:
Build workouts around your preferences and movement limitations.

The app filters exercises based on the information you provide. It does not diagnose injuries or replace professional medical advice.

Actions:
Get started
Review safety information

4.2 Safety notice screen
The user must acknowledge:
The app is not medical advice.
Users should stop exercises that cause pain.
Users with a recent injury, surgery, or clinician restriction should follow professional guidance.
The exercise filters are general and may not match an individual case.
Store a local Boolean:
hasAcceptedSafetyNotice: Boolean


4.3 Training preferences screen
Collect:
Primary goal
Experience level
Training days per week
Approximate session duration
Available equipment
Preferred workout split
Goals
enum class TrainingGoal {
    GENERAL_FITNESS,
    BUILD_MUSCLE,
    BUILD_STRENGTH,
    IMPROVE_ENDURANCE
}

Experience levels
enum class ExperienceLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

Training frequency
Initially support:
2 days
3 days
4 days
5 days

Session duration
30 minutes
45 minutes
60 minutes
75 minutes

Equipment
enum class Equipment {
    BODYWEIGHT,
    DUMBBELLS,
    BARBELL,
    BENCH,
    CABLE_MACHINE,
    SELECTORIZED_MACHINE,
    PULL_UP_BAR,
    RESISTANCE_BAND,
    CARDIO_MACHINE
}

Workout splits
For the first MVP:
enum class WorkoutSplit {
    FULL_BODY,
    UPPER_LOWER,
    PUSH_PULL_LEGS
}

Only expose compatible combinations:
2 days: Full body
3 days: Full body or Push/Pull/Legs
4 days: Upper/Lower
5 days: Upper/Lower plus optional full body
The first implementation may automatically choose the split based on training frequency rather than asking the user.

4.4 Injury history screen
Organize injuries by body region.
enum class BodyRegion {
    NECK,
    SHOULDER,
    ELBOW,
    WRIST_HAND,
    UPPER_BACK,
    LOWER_BACK,
    HIP,
    KNEE,
    ANKLE_FOOT
}

Initial injury options:
Shoulder
Previous shoulder injury
Rotator cuff injury
Shoulder instability
Shoulder impingement
Previous shoulder surgery
Pain without a diagnosis
Elbow
Previous elbow injury
Tendon-related elbow pain
Previous elbow surgery
Pain without a diagnosis
Wrist and hand
Previous wrist injury
Previous wrist surgery
Pain without a diagnosis
Lower back
Disc-related condition
Previous lower-back injury
Previous lower-back surgery
Recurrent lower-back pain
Pain without a diagnosis
Hip
Previous hip injury
Hip impingement
Previous hip surgery
Pain without a diagnosis
Knee
ACL injury
Meniscus injury
Patellar tendon issue
Kneecap-related pain
Previous knee surgery
Pain without a diagnosis
Ankle and foot
Previous ankle sprain
Achilles tendon issue
Previous ankle or foot surgery
Pain without a diagnosis
Model:
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
    ANKLE_FOOT_UNDIAGNOSED_PAIN
}

Selection model:
data class SelectedInjury(
    val injuryId: InjuryId,
    val affectedSide: BodySide = BodySide.NOT_SPECIFIED,
    val status: InjuryStatus = InjuryStatus.HISTORICAL
)

enum class BodySide {
    LEFT,
    RIGHT,
    BOTH,
    NOT_SPECIFIED
}

enum class InjuryStatus {
    CURRENTLY_SYMPTOMATIC,
    HISTORICAL,
    RECOVERING,
    NOT_SPECIFIED
}

The filtering engine should not rely on left versus right during the MVP. Preserve it for future unilateral plan adjustments.

4.5 Movement limitations screen
This is the primary input used by the exercise filter.
enum class MovementLimitation {
    AVOID_HIGH_IMPACT,
    AVOID_JUMPING,
    AVOID_RUNNING,
    AVOID_RAPID_DIRECTION_CHANGE,

    AVOID_DEEP_KNEE_FLEXION,
    AVOID_KNEELING,
    AVOID_SINGLE_LEG_LOADING,
    AVOID_HEAVY_KNEE_LOADING,

    AVOID_LOADED_SPINAL_FLEXION,
    AVOID_LOADED_SPINAL_EXTENSION,
    AVOID_SPINAL_ROTATION,
    AVOID_UNSUPPORTED_HIP_HINGE,
    AVOID_HIGH_SPINAL_COMPRESSION,

    AVOID_OVERHEAD_PRESSING,
    AVOID_DEEP_SHOULDER_EXTENSION,
    AVOID_WIDE_GRIP_PRESSING,
    AVOID_SHOULDER_ABDUCTION,
    AVOID_INTERNAL_ROTATION_UNDER_LOAD,

    AVOID_HEAVY_ELBOW_FLEXION,
    AVOID_HEAVY_ELBOW_EXTENSION,

    AVOID_LOADED_WRIST_EXTENSION,
    AVOID_LOADED_WRIST_FLEXION,
    AVOID_PRONATED_GRIP,
    AVOID_SUPINATED_GRIP,

    AVOID_DEEP_HIP_FLEXION,
    AVOID_WIDE_HIP_ABDUCTION,
    AVOID_UNILATERAL_BALANCE_DEMAND
}

The injury catalog provides suggested limitations.
Example:
data class InjuryDefinition(
    val id: InjuryId,
    @StringRes val nameRes: Int,
    val bodyRegion: BodyRegion,
    val suggestedLimitations: Set<MovementLimitation>
)

Example definition:
InjuryDefinition(
    id = InjuryId.KNEE_ACL,
    nameRes = R.string.injury_acl,
    bodyRegion = BodyRegion.KNEE,
    suggestedLimitations = setOf(
        MovementLimitation.AVOID_JUMPING,
        MovementLimitation.AVOID_RAPID_DIRECTION_CHANGE
    )
)

Suggested limitations must not be applied silently.
The screen should say:
Based on the history you selected, consider whether any of these movements should be excluded.

Select only the limitations that currently apply to you.

Users may:
Add suggested limitations.
Remove suggested limitations.
Add unrelated limitations manually.

5. Exercise data model
5.1 Muscle groups
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
    CORE
}

An exercise can have one primary group and multiple secondary groups.

5.2 Movement patterns
enum class MovementPattern {
    HORIZONTAL_PUSH,
    VERTICAL_PUSH,
    HORIZONTAL_PULL,
    VERTICAL_PULL,
    SQUAT,
    LUNGE,
    HIP_HINGE,
    KNEE_FLEXION,
    HIP_EXTENSION,
    HIP_ABDUCTION,
    HIP_ADDUCTION,
    ELBOW_FLEXION,
    ELBOW_EXTENSION,
    SHOULDER_ABDUCTION,
    SHOULDER_EXTERNAL_ROTATION,
    CALF_RAISE,
    CORE_ANTI_EXTENSION,
    CORE_ANTI_ROTATION,
    CORE_FLEXION,
    CARRY
}


5.3 Exercise definition
data class ExerciseDefinition(
    val id: ExerciseId,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
    val primaryMuscle: MuscleGroup,
    val secondaryMuscles: Set<MuscleGroup>,
    val movementPattern: MovementPattern,
    val requiredEquipment: Set<Equipment>,
    val difficulty: ExerciseDifficulty,
    val conflictingLimitations: Set<MovementLimitation>,
    val tags: Set<ExerciseTag>,
    val defaultPrescription: ExercisePrescription
)

Supporting types:
enum class ExerciseDifficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

enum class ExerciseTag {
    COMPOUND,
    ISOLATION,
    UNILATERAL,
    BILATERAL,
    MACHINE_SUPPORTED,
    CHEST_SUPPORTED,
    REQUIRES_BALANCE,
    HIGH_IMPACT,
    BODYWEIGHT,
    FREE_WEIGHT
}

data class ExercisePrescription(
    val sets: IntRange,
    val reps: IntRange,
    val restSeconds: Int
)

Use stable IDs instead of strings:
enum class ExerciseId {
    PUSH_UP,
    MACHINE_CHEST_PRESS,
    DUMBBELL_BENCH_PRESS,
    INCLINE_DUMBBELL_PRESS,
    CABLE_CHEST_FLY,

    LAT_PULLDOWN,
    ASSISTED_PULL_UP,
    PULL_UP,
    SEATED_CABLE_ROW,
    CHEST_SUPPORTED_DUMBBELL_ROW,
    ISO_LATERAL_ROW,

    MACHINE_SHOULDER_PRESS,
    SEATED_DUMBBELL_SHOULDER_PRESS,
    CABLE_LATERAL_RAISE,
    DUMBBELL_LATERAL_RAISE,
    CABLE_REAR_DELT_FLY,
    FACE_PULL,

    DUMBBELL_CURL,
    HAMMER_CURL,
    CABLE_CURL,

    CABLE_TRICEPS_PRESSDOWN,
    OVERHEAD_CABLE_TRICEPS_EXTENSION,
    ASSISTED_DIP,
    DIP,

    BODYWEIGHT_SQUAT,
    GOBLET_SQUAT,
    LEG_PRESS,
    LEG_EXTENSION,
    BULGARIAN_SPLIT_SQUAT,
    WALKING_LUNGE,
    STEP_UP,

    DUMBBELL_ROMANIAN_DEADLIFT,
    SEATED_LEG_CURL,
    LYING_LEG_CURL,

    GLUTE_BRIDGE,
    HIP_THRUST,
    CABLE_PULL_THROUGH,
    HIP_ABDUCTION_MACHINE,
    HIP_ADDUCTION_MACHINE,

    STANDING_CALF_RAISE,
    SEATED_CALF_RAISE,

    DEAD_BUG,
    BIRD_DOG,
    FRONT_PLANK,
    SIDE_PLANK,
    PALLOF_PRESS,
    CABLE_CRUNCH
}


6. Static resource strategy
Use Android string resources for all user-facing copy:
res/values/strings.xml

Examples:
<string name="exercise_machine_chest_press">Machine chest press</string>
<string name="exercise_machine_chest_press_description">
    Press the handles forward while keeping your upper back supported.
</string>

<string name="injury_acl">ACL injury</string>
<string name="limitation_avoid_jumping">Avoid jumping and landing</string>

Do not use resource strings as database identifiers.
Bad:
id = "Machine Chest Press"

Good:
id = ExerciseId.MACHINE_CHEST_PRESS
nameRes = R.string.exercise_machine_chest_press

For the initial MVP, define the exercise catalog in Kotlin:
ExerciseCatalog.kt

This is preferable to raw JSON at the start because:
The compiler validates enum values.
Refactoring is safer.
No parsing layer is required.
Tests can directly inspect definitions.
Invalid equipment and limitation values fail at compile time.
Move the catalog to JSON, Room, or a server only when non-developers need to manage it or the catalog becomes too large.

7. Example exercise definitions
object ExerciseCatalog {

    val exercises: List<ExerciseDefinition> = listOf(
        ExerciseDefinition(
            id = ExerciseId.MACHINE_CHEST_PRESS,
            nameRes = R.string.exercise_machine_chest_press,
            descriptionRes = R.string.exercise_machine_chest_press_description,
            primaryMuscle = MuscleGroup.CHEST,
            secondaryMuscles = setOf(
                MuscleGroup.SHOULDERS,
                MuscleGroup.TRICEPS
            ),
            movementPattern = MovementPattern.HORIZONTAL_PUSH,
            requiredEquipment = setOf(Equipment.SELECTORIZED_MACHINE),
            difficulty = ExerciseDifficulty.BEGINNER,
            conflictingLimitations = setOf(
                MovementLimitation.AVOID_WIDE_GRIP_PRESSING
            ),
            tags = setOf(
                ExerciseTag.COMPOUND,
                ExerciseTag.MACHINE_SUPPORTED,
                ExerciseTag.BILATERAL
            ),
            defaultPrescription = ExercisePrescription(
                sets = 3..4,
                reps = 8..12,
                restSeconds = 90
            )
        ),
        ExerciseDefinition(
            id = ExerciseId.DUMBBELL_ROMANIAN_DEADLIFT,
            nameRes = R.string.exercise_dumbbell_romanian_deadlift,
            descriptionRes = R.string.exercise_dumbbell_romanian_deadlift_description,
            primaryMuscle = MuscleGroup.HAMSTRINGS,
            secondaryMuscles = setOf(
                MuscleGroup.GLUTES,
                MuscleGroup.BACK
            ),
            movementPattern = MovementPattern.HIP_HINGE,
            requiredEquipment = setOf(Equipment.DUMBBELLS),
            difficulty = ExerciseDifficulty.INTERMEDIATE,
            conflictingLimitations = setOf(
                MovementLimitation.AVOID_UNSUPPORTED_HIP_HINGE,
                MovementLimitation.AVOID_LOADED_SPINAL_FLEXION
            ),
            tags = setOf(
                ExerciseTag.COMPOUND,
                ExerciseTag.FREE_WEIGHT,
                ExerciseTag.BILATERAL
            ),
            defaultPrescription = ExercisePrescription(
                sets = 3..4,
                reps = 6..12,
                restSeconds = 120
            )
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
            conflictingLimitations = emptySet(),
            tags = setOf(
                ExerciseTag.BILATERAL
            ),
            defaultPrescription = ExercisePrescription(
                sets = 2..3,
                reps = 8..15,
                restSeconds = 60
            )
        )
    )
}


8. Exercise eligibility engine
Keep the engine independent from Android and Compose.
data class ExerciseEligibility(
    val exercise: ExerciseDefinition,
    val isEligible: Boolean,
    val exclusionReasons: Set<ExclusionReason>
)

sealed interface ExclusionReason {
    data class ConflictingLimitation(
        val limitation: MovementLimitation
    ) : ExclusionReason

    data class MissingEquipment(
        val equipment: Equipment
    ) : ExclusionReason

    data class AboveExperienceLevel(
        val requiredLevel: ExerciseDifficulty
    ) : ExclusionReason
}

Filter:
class EvaluateExerciseEligibilityUseCase {

    operator fun invoke(
        exercise: ExerciseDefinition,
        profile: UserProfile
    ): ExerciseEligibility {
        val reasons = buildSet {
            exercise.conflictingLimitations
                .intersect(profile.movementLimitations)
                .forEach {
                    add(ExclusionReason.ConflictingLimitation(it))
                }

            exercise.requiredEquipment
                .filterNot(profile.availableEquipment::contains)
                .forEach {
                    add(ExclusionReason.MissingEquipment(it))
                }

            if (!profile.experienceLevel.supports(exercise.difficulty)) {
                add(
                    ExclusionReason.AboveExperienceLevel(
                        exercise.difficulty
                    )
                )
            }
        }

        return ExerciseEligibility(
            exercise = exercise,
            isEligible = reasons.isEmpty(),
            exclusionReasons = reasons
        )
    }
}

Experience comparison:
fun ExperienceLevel.supports(
    difficulty: ExerciseDifficulty
): Boolean {
    return when (this) {
        ExperienceLevel.BEGINNER ->
            difficulty == ExerciseDifficulty.BEGINNER

        ExperienceLevel.INTERMEDIATE ->
            difficulty != ExerciseDifficulty.ADVANCED

        ExperienceLevel.ADVANCED ->
            true
    }
}

Whether advanced exercises should actually be hidden from beginners can be reconsidered. For the MVP, experience could instead affect ranking rather than strict exclusion.

9. Filtering behavior
Use three categories rather than simply showing or hiding exercises.
Available
No conflicts were found.
Excluded
The exercise conflicts with a confirmed movement limitation.
Unavailable
The user does not have the necessary equipment.
This gives users visibility into why an exercise is missing.
Example:
Dumbbell Romanian deadlift

Excluded from plan:
You selected “Avoid unsupported hip-hinge positions.”

Allow users to inspect excluded exercises, but do not automatically include them in generated plans.
A later version may support:
Include anyway

For the first MVP, omit that override to keep behavior simpler.

10. User profile model
data class UserProfile(
    val goal: TrainingGoal,
    val experienceLevel: ExperienceLevel,
    val daysPerWeek: Int,
    val sessionDurationMinutes: Int,
    val preferredSplit: WorkoutSplit,
    val availableEquipment: Set<Equipment>,
    val selectedInjuries: Set<SelectedInjury>,
    val movementLimitations: Set<MovementLimitation>,
    val hasAcceptedSafetyNotice: Boolean
)

Use immutable collections at domain boundaries where practical.

11. Plan model
data class WorkoutPlan(
    val id: String,
    val name: String,
    val days: List<WorkoutDay>
)

data class WorkoutDay(
    val id: String,
    val name: String,
    val focus: WorkoutDayFocus,
    val exercises: List<PlannedExercise>
)

data class PlannedExercise(
    val exerciseId: ExerciseId,
    val sets: Int,
    val repRange: IntRange,
    val restSeconds: Int,
    val order: Int
)

enum class WorkoutDayFocus {
    FULL_BODY,
    UPPER_BODY,
    LOWER_BODY,
    PUSH,
    PULL,
    LEGS
}

The plan should reference ExerciseId, not duplicate the complete exercise definition.

12. Initial plan-generation algorithm
Do not attempt sophisticated optimization in the MVP.
12.1 Determine split
fun determineSplit(daysPerWeek: Int): WorkoutSplit =
    when (daysPerWeek) {
        2 -> WorkoutSplit.FULL_BODY
        3 -> WorkoutSplit.PUSH_PULL_LEGS
        4 -> WorkoutSplit.UPPER_LOWER
        else -> WorkoutSplit.UPPER_LOWER
    }

12.2 Filter catalog
Remove exercises that:
Conflict with selected movement limitations.
Require unavailable equipment.
Are unsuitable for the selected experience level, when applicable.
12.3 Group remaining exercises
Group by:
Primary muscle.
Movement pattern.
Workout-day focus.
12.4 Select exercises
Example full-body template:
1 squat or knee-dominant exercise
1 hip-hinge or hip-extension exercise
1 horizontal or vertical push
1 horizontal or vertical pull
1 secondary upper-body exercise
1 core exercise

Example upper-body template:
1 horizontal push
1 vertical or secondary push
1 horizontal pull
1 vertical pull
1 shoulder isolation exercise
1 biceps exercise
1 triceps exercise

Example lower-body template:
1 knee-dominant exercise
1 hip-hinge exercise
1 glute-focused exercise
1 hamstring isolation exercise
1 calf exercise
1 core exercise

12.5 Deterministic ranking
Each exercise receives a score.
data class RankedExercise(
    val exercise: ExerciseDefinition,
    val score: Int
)

Possible scoring:
+5 exact required movement pattern
+3 matches preferred equipment
+2 beginner-friendly
+2 machine-supported when balance limitations exist
+1 compound exercise for an early workout slot
-2 duplicates a movement already selected
-3 adds a second unilateral exercise

Always break ties using a stable field such as ExerciseId.name.
This ensures the same profile always produces the same plan.

13. Manual plan adjustment
The generated plan screen should allow:
Remove exercise.
Replace exercise.
Reorder exercise.
Regenerate entire plan.
Replacement flow:
Tap exercise
    ↓
Replace
    ↓
Show eligible alternatives
    ↓
Select replacement

Alternatives should preferably match:
The same movement pattern.
The same primary muscle.
The available equipment.
The confirmed movement limitations.
Example:
class GetExerciseReplacementsUseCase {

    operator fun invoke(
        currentExerciseId: ExerciseId,
        profile: UserProfile
    ): List<ExerciseDefinition>
}


14. Exercise library
The exercise library should support:
Browse by muscle group.
Search by exercise name.
Filter by equipment.
Show available and excluded exercises.
Inspect exclusion reasons.
Open exercise details.
Muscle-group tabs or chips:
All
Chest
Back
Shoulders
Biceps
Triceps
Quadriceps
Hamstrings
Glutes
Calves
Core

Exercise row:
Machine chest press
Chest · Machine · Compound

Available

Excluded row:
Dumbbell Romanian deadlift
Hamstrings · Dumbbells · Compound

Excluded by 1 limitation


15. Exercise detail screen
Show:
Exercise name.
Description.
Primary muscle.
Secondary muscles.
Movement pattern.
Required equipment.
Default sets and repetitions.
Availability status.
Reasons for exclusion.
Do not include complex technique videos or anatomical illustrations in the MVP.

16. Home screen
Sections:
Current plan
[View plan]

Profile summary
4 days · Build muscle · Intermediate

Active limitations
3 selected

Exercise library
[Browse exercises]

The home screen does not need analytics or workout history for the first boilerplate iteration.

17. Navigation
Suggested destinations:
@Serializable
sealed interface AppRoute {

    @Serializable
    data object Welcome : AppRoute

    @Serializable
    data object SafetyNotice : AppRoute

    @Serializable
    data object Preferences : AppRoute

    @Serializable
    data object InjuryHistory : AppRoute

    @Serializable
    data object MovementLimitations : AppRoute

    @Serializable
    data object ProfileReview : AppRoute

    @Serializable
    data object Home : AppRoute

    @Serializable
    data object Plan : AppRoute

    @Serializable
    data object ExerciseLibrary : AppRoute

    @Serializable
    data class ExerciseDetails(
        val exerciseId: ExerciseId
    ) : AppRoute

    @Serializable
    data class ExerciseReplacement(
        val workoutDayId: String,
        val exerciseId: ExerciseId
    ) : AppRoute

    @Serializable
    data object Profile : AppRoute
}

Use type-safe navigation when supported by the selected Navigation library version.

18. Architecture
Use a standard unidirectional data-flow structure:
Compose screen
    ↓ user action
ViewModel
    ↓
Use case
    ↓
Repository
    ↓
Local data source

Android’s architecture guidance recommends separating the UI and data layers, while using a domain layer when reusable or sufficiently complex business logic warrants it. The eligibility and plan-generation logic justify a small domain layer here.
Compose screens should receive immutable UI state and emit events rather than owning business state. This follows Compose’s state-hoisting and unidirectional-data-flow guidance.

19. Suggested module structure
Start with one application module. Do not introduce multiple Gradle modules before the MVP needs them.
app/
└── src/main/java/com/example/injuryplanner/
    ├── App.kt
    ├── MainActivity.kt
    │
    ├── navigation/
    │   ├── AppNavigation.kt
    │   └── AppRoute.kt
    │
    ├── core/
    │   ├── ui/
    │   │   ├── AppScaffold.kt
    │   │   ├── AppTopBar.kt
    │   │   ├── LoadingContent.kt
    │   │   └── EmptyContent.kt
    │   └── designsystem/
    │       ├── AppTheme.kt
    │       ├── Color.kt
    │       ├── Type.kt
    │       └── Dimens.kt
    │
    ├── domain/
    │   ├── model/
    │   │   ├── BodyRegion.kt
    │   │   ├── Equipment.kt
    │   │   ├── ExerciseDefinition.kt
    │   │   ├── ExerciseEligibility.kt
    │   │   ├── ExerciseId.kt
    │   │   ├── InjuryDefinition.kt
    │   │   ├── InjuryId.kt
    │   │   ├── MovementLimitation.kt
    │   │   ├── UserProfile.kt
    │   │   └── WorkoutPlan.kt
    │   │
    │   ├── repository/
    │   │   ├── ExerciseRepository.kt
    │   │   ├── ProfileRepository.kt
    │   │   └── WorkoutPlanRepository.kt
    │   │
    │   └── usecase/
    │       ├── EvaluateExerciseEligibilityUseCase.kt
    │       ├── GenerateWorkoutPlanUseCase.kt
    │       ├── GetEligibleExercisesUseCase.kt
    │       ├── GetExerciseReplacementsUseCase.kt
    │       ├── GetSuggestedLimitationsUseCase.kt
    │       └── UpdateWorkoutExerciseUseCase.kt
    │
    ├── data/
    │   ├── catalog/
    │   │   ├── ExerciseCatalog.kt
    │   │   └── InjuryCatalog.kt
    │   │
    │   ├── local/
    │   │   ├── ProfileDataStore.kt
    │   │   └── WorkoutPlanDataStore.kt
    │   │
    │   └── repository/
    │       ├── DefaultExerciseRepository.kt
    │       ├── DefaultProfileRepository.kt
    │       └── DefaultWorkoutPlanRepository.kt
    │
    ├── feature/
    │   ├── onboarding/
    │   │   ├── welcome/
    │   │   ├── safety/
    │   │   ├── preferences/
    │   │   ├── injuries/
    │   │   ├── limitations/
    │   │   └── review/
    │   │
    │   ├── home/
    │   ├── plan/
    │   ├── exerciselibrary/
    │   ├── exercisedetails/
    │   └── profile/
    │
    └── di/
        └── AppModule.kt


20. UI state and events
Each screen should expose one state model.
Example:
data class InjuryHistoryUiState(
    val injuryGroups: List<InjuryGroupUiModel> = emptyList(),
    val selectedInjuries: Set<InjuryId> = emptySet(),
    val canContinue: Boolean = true
)

Events:
sealed interface InjuryHistoryEvent {
    data class ToggleInjury(
        val injuryId: InjuryId
    ) : InjuryHistoryEvent

    data object Continue : InjuryHistoryEvent
    data object Back : InjuryHistoryEvent
}

Composable:
@Composable
fun InjuryHistoryRoute(
    viewModel: InjuryHistoryViewModel,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    InjuryHistoryScreen(
        state = state,
        onEvent = viewModel::onEvent
    )
}

Previewable screen:
@Composable
fun InjuryHistoryScreen(
    state: InjuryHistoryUiState,
    onEvent: (InjuryHistoryEvent) -> Unit
)

Do not pass NavController into individual screen composables.

21. Local persistence
Use DataStore for:
Onboarding completion.
Safety acknowledgement.
User profile.
Movement limitations.
Current generated plan.
DataStore is intended as the modern replacement for SharedPreferences and supports coroutine and Flow-based access.
For the first implementation, choose one of these approaches:
Simpler boilerplate
Use Preferences DataStore and serialize the profile and plan as JSON strings.
Advantages:
Fast to implement.
Minimal schema work.
Easy to inspect.
Disadvantages:
Less type-safe persistence.
Migration becomes more manual.
Better long-term option
Use Proto DataStore.
Advantages:
Typed schema.
Explicit defaults.
Better migration discipline.
Disadvantages:
More initial setup.
Recommendation:
Use Proto DataStore if the boilerplate is intended to grow into the real app. Use Preferences DataStore if this is primarily a disposable prototype.
Do not introduce Room until you add:
Workout-session history.
Per-set tracking.
Search across a much larger dynamic exercise catalog.
User-created exercises.
Historical progress queries.

22. Dependency injection
Use Hilt for:
Repositories.
DataStore providers.
Use cases.
ViewModels.
The static catalogs can be provided as singleton dependencies:
@Provides
@Singleton
fun provideExerciseCatalog(): List<ExerciseDefinition> =
    ExerciseCatalog.exercises

Alternatively, keep static catalogs directly inside repository implementations until tests require injection.

23. Recommended dependencies
Use stable versions selected from the current Android documentation and version catalog at implementation time.
Conceptual dependency set:
[dependencies]
compose-bom
activity-compose
material3
lifecycle-runtime-compose
lifecycle-viewmodel-compose
navigation-compose
kotlinx-serialization-json
datastore
hilt-android
hilt-navigation-compose
junit
kotlinx-coroutines-test
turbine

Navigation Compose supplies NavHost, composable destinations, and rememberNavController; current Android guidance should be checked when choosing between the established Navigation Compose APIs and Navigation 3.
For an MVP, favor the navigation approach with which the project team is most productive. Avoid adopting a newer navigation API solely for novelty.

24. Testing strategy
The filtering and generation engine should receive most of the testing effort.
24.1 Exercise eligibility tests
@Test
fun `exercise is excluded when a conflicting limitation is selected`() {
    val profile = profile(
        limitations = setOf(
            MovementLimitation.AVOID_UNSUPPORTED_HIP_HINGE
        )
    )

    val result = useCase(
        exercise = dumbbellRomanianDeadlift,
        profile = profile
    )

    assertFalse(result.isEligible)
}

24.2 Equipment tests
@Test
fun `cable exercise is unavailable without cable machine`() {
    // Arrange
    // Act
    // Assert missing-equipment reason
}

24.3 Suggested-limitation tests
@Test
fun `ACL history suggests jumping and direction-change limitations`() {
    // Verify suggestion only.
    // Do not mutate the confirmed profile automatically.
}

24.4 Plan-generation tests
Test that:
Every generated exercise is eligible.
Every generated exercise uses available equipment.
Required workout movement slots are filled where possible.
No exercise appears twice in the same workout.
The same input always returns the same plan.
Missing movement categories produce a warning rather than a crash.
An extremely restrictive profile still returns a valid partial result.
24.5 ViewModel tests
Test:
Selection changes.
Continue-button state.
Loading and persistence.
Regeneration.
Exercise replacement.
24.6 Compose tests
Limit initial Compose tests to important flows:
Complete onboarding.
Select and deselect an injury.
Confirm suggested limitations.
Generate a plan.
Replace an exercise.
View an exclusion reason.

25. Empty and edge states
The boilerplate must account for:
No injuries selected
Continue normally with no suggested limitations.
Injury selected, no limitation confirmed
Allow continuation. Injury history alone should not necessarily exclude exercises.
No eligible exercise for a plan slot
Show:
No matching exercise was found for this section using your current equipment and movement limitations.

Do not reintroduce an excluded exercise automatically.
All exercises excluded
Show:
Your current selections exclude every exercise in this category. Review your movement limitations or build the workout manually with professional guidance.

Equipment list empty
Treat bodyweight as the only available equipment, or require at least one equipment selection.
Profile changed after generating a plan
Mark the current plan as outdated:
val requiresRegeneration: Boolean

Show:
Your profile has changed. Regenerate the plan to apply the new limitations.

Do not silently modify an existing plan.

26. Initial exercise catalog target
Aim for approximately 45–60 exercises.
Suggested distribution:
Chest:       5–7
Back:        7–9
Shoulders:   6–8
Biceps:      3–5
Triceps:     4–6
Quadriceps:  6–8
Hamstrings:  4–6
Glutes:      5–7
Calves:      2–3
Core:        5–7

Each movement category should have alternatives across different equipment types.
Example horizontal pulling alternatives:
Seated cable row.
Chest-supported dumbbell row.
Iso-lateral machine row.
Resistance-band row.
This is necessary because filtering only works well when each movement has several possible substitutes.

27. Implementation phases
Phase 1 — Project foundation
Create:
Compose application.
Material 3 theme.
Navigation shell.
Hilt setup.
Package structure.
Placeholder screens.
Static string resources.
Completion criteria:
Every destination can be navigated to.
Each screen has a preview.
No business logic is implemented yet.
Phase 2 — Domain and catalogs
Create:
Enums and domain models.
Exercise catalog.
Injury catalog.
Repository interfaces.
Static repository implementations.
Completion criteria:
Exercises can be queried by muscle group.
Injuries can be queried by body region.
Catalog validation tests pass.
Phase 3 — Onboarding
Implement:
Safety acknowledgement.
Preferences.
Injury selection.
Suggested and manual movement limitations.
Profile review.
Local profile persistence.
Completion criteria:
The user can leave and reopen the application without losing the profile.
Suggested limitations are never confirmed automatically.
Phase 4 — Exercise filtering
Implement:
Eligibility use case.
Exclusion reasons.
Exercise-library filters.
Exercise-detail availability state.
Completion criteria:
Every exclusion is explainable.
Filtering is deterministic.
Unit tests cover each limitation category.
Phase 5 — Plan generation
Implement:
Split selection.
Workout templates.
Candidate ranking.
Stable exercise selection.
Partial-plan handling.
Plan persistence.
Completion criteria:
No generated plan contains an excluded exercise.
Equivalent input always generates the same output.
Restrictive profiles do not crash generation.
Phase 6 — Plan editing
Implement:
Remove exercise.
Replace exercise.
Reorder exercises.
Regenerate plan.
Outdated-plan warning after profile changes.
Completion criteria:
Replacements respect all profile filters.
Plan edits persist locally.
Phase 7 — MVP polish
Add:
Empty states.
Accessibility labels.
Loading and error states.
Confirmation dialogs.
Basic UI tests.
Catalog-content review.
Safety-copy review.

28. Definition of MVP completion
The MVP is complete when a new user can:
Accept the safety notice.
Select a goal, schedule, experience level, and equipment.
Select zero or more injuries.
Review suggested movement limitations.
Confirm the limitations that currently apply.
Generate a deterministic workout plan.
Understand why an exercise was excluded.
Browse exercises by muscle group.
Replace an exercise with an eligible alternative.
Close and reopen the app without losing their profile or plan.

29. Features deliberately deferred
Do not include these in the initial boilerplate:
OpenAI or another LLM.
Natural-language injury parsing.
Authentication.
Cloud synchronization.
Backend.
Workout logging.
Individual set tracking.
Progressive-overload calculations.
Pain tracking.
Recovery questionnaires.
Exercise videos.
User-created exercises.
Social sharing.
Wear OS.
Nutrition planning.
Clinician portal.
Subscription billing.
The first major feature after MVP should probably be workout execution and tracking, not AI.

30. Post-MVP direction
Version 1.1 — Workout tracking
Add:
Start workout.
Record sets, repetitions, weight, and RPE.
Complete workout.
Review recent sessions.
Suggest the previous session’s values.
This likely introduces Room.
Version 1.2 — Adaptation
Add:
Exercise likes and dislikes.
User-reported discomfort per exercise.
Temporary exercise exclusion.
Volume adjustment.
Simple progression suggestions.
Version 1.3 — Natural-language onboarding
Add:
Describe your history and preferences.

Use an LLM only to propose structured values:
Free text
    ↓
Structured candidate profile
    ↓
User review
    ↓
Confirmed deterministic profile

The LLM must not directly write a workout plan or assign a diagnosis.
Version 1.4 — Remote exercise catalog
Move exercise and limitation definitions to a backend only when:
Content updates need to ship without app releases.
Medical or training reviewers need an administrative interface.
Rules need versioning.
Experiments require multiple catalogs.
The catalog becomes too large for practical Kotlin maintenance.

31. Recommended first coding task
Create the domain module structure inside the app module and implement:
ExerciseId
MuscleGroup
MovementPattern
Equipment
MovementLimitation
ExerciseDefinition
UserProfile
ExerciseCatalog
EvaluateExerciseEligibilityUseCase
Eligibility unit tests
Do this before building the complete onboarding UI.
Once the filtering model is proven, the Compose screens become straightforward representations of that model.
The most important architectural boundary is:
The UI collects confirmed limitations.
The domain engine determines eligibility.
The static catalog describes exercises.
No Compose code contains exercise-safety rules.

