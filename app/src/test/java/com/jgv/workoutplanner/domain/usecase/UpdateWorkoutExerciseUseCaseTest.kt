package com.jgv.workoutplanner.domain.usecase

import com.jgv.workoutplanner.data.repository.DefaultExerciseRepository
import com.jgv.workoutplanner.domain.model.Equipment
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.PlannedExercise
import com.jgv.workoutplanner.domain.model.WorkoutDay
import com.jgv.workoutplanner.domain.model.WorkoutDayFocus
import com.jgv.workoutplanner.domain.model.WorkoutExerciseEdit
import com.jgv.workoutplanner.domain.model.WorkoutExerciseEditResult
import com.jgv.workoutplanner.domain.model.WorkoutPlan
import com.jgv.workoutplanner.testing.FakeProfileRepository
import com.jgv.workoutplanner.testing.FakeWorkoutPlanRepository
import com.jgv.workoutplanner.testing.userProfile
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateWorkoutExerciseUseCaseTest {

    private val exerciseRepo = DefaultExerciseRepository()
    private val evaluate = EvaluateExerciseEligibilityUseCase()
    private val eligible = GetEligibleExercisesUseCase(exerciseRepo, evaluate)

    private fun samplePlan(): WorkoutPlan {
        val day1 = WorkoutDay(
            id = "day-1",
            name = "Full Body 1",
            focus = WorkoutDayFocus.FULL_BODY,
            exercises = listOf(
                PlannedExercise(ExerciseId.PUSH_UP, 3, 8..12, 90, 0),
                PlannedExercise(ExerciseId.LAT_PULLDOWN, 3, 8..12, 90, 1),
                PlannedExercise(ExerciseId.BODYWEIGHT_SQUAT, 3, 8..12, 90, 2),
            ),
        )
        return WorkoutPlan(id = "plan-3-full", name = "Test Plan", days = listOf(day1))
    }

    @Test
    fun `remove normalizes order`() = runTest {
        val plan = samplePlan()
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = plan)
        val profileRepo = FakeProfileRepository(initialProfile = userProfile())
        val useCase = UpdateWorkoutExerciseUseCase(workoutRepo, profileRepo, eligible, exerciseRepo)

        val result = useCase(WorkoutExerciseEdit.Remove("day-1", ExerciseId.LAT_PULLDOWN))
        assertEquals(WorkoutExerciseEditResult.Updated, result)

        val stored = workoutRepo.currentStored.plan!!
        val exercises = stored.days.first().exercises.sortedBy { it.order }
        assertEquals(2, exercises.size)
        assertEquals(listOf(0, 1), exercises.map { it.order })
        assertTrue(exercises.none { it.exerciseId == ExerciseId.LAT_PULLDOWN })
    }

    @Test
    fun `remove last exercise renders empty day`() = runTest {
        val singleDay = WorkoutDay(
            id = "day-1",
            name = "Day",
            focus = WorkoutDayFocus.FULL_BODY,
            exercises = listOf(PlannedExercise(ExerciseId.PUSH_UP, 3, 8..12, 90, 0)),
        )
        val plan = WorkoutPlan(id = "p", name = "p", days = listOf(singleDay))
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = plan)
        val profileRepo = FakeProfileRepository(initialProfile = userProfile())
        val useCase = UpdateWorkoutExerciseUseCase(workoutRepo, profileRepo, eligible, exerciseRepo)

        val result = useCase(WorkoutExerciseEdit.Remove("day-1", ExerciseId.PUSH_UP))
        assertEquals(WorkoutExerciseEditResult.Updated, result)
        assertTrue(workoutRepo.currentStored.plan!!.days.first().exercises.isEmpty())
    }

    @Test
    fun `move up and down normalizes order`() = runTest {
        val plan = samplePlan()
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = plan)
        val profileRepo = FakeProfileRepository(initialProfile = userProfile())
        val useCase = UpdateWorkoutExerciseUseCase(workoutRepo, profileRepo, eligible, exerciseRepo)

        // Move last to first
        val result = useCase(WorkoutExerciseEdit.Move("day-1", ExerciseId.BODYWEIGHT_SQUAT, 0))
        assertEquals(WorkoutExerciseEditResult.Updated, result)

        val exercises = workoutRepo.currentStored.plan!!.days.first().exercises.sortedBy { it.order }
        assertEquals(ExerciseId.BODYWEIGHT_SQUAT, exercises[0].exerciseId)
        assertEquals(listOf(0, 1, 2), exercises.map { it.order })
    }

    @Test
    fun `replace retains position and uses catalog defaults`() = runTest {
        val plan = samplePlan()
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = plan)
        // Generous profile so replacement eligible
        val generous = userProfile(
            experienceLevel = com.jgv.workoutplanner.domain.model.ExperienceLevel.ADVANCED,
            availableEquipment = exerciseRepo.getAllExercises().flatMap { it.requiredEquipment }.toSet() + Equipment.BODYWEIGHT,
        )
        val profileRepo = FakeProfileRepository(initialProfile = generous)
        val useCase = UpdateWorkoutExerciseUseCase(workoutRepo, profileRepo, eligible, exerciseRepo)

        val replacementDef = exerciseRepo.getExercise(ExerciseId.MACHINE_CHEST_PRESS)

        val result = useCase(
            WorkoutExerciseEdit.Replace("day-1", ExerciseId.PUSH_UP, ExerciseId.MACHINE_CHEST_PRESS),
        )
        assertEquals(WorkoutExerciseEditResult.Updated, result)

        val day = workoutRepo.currentStored.plan!!.days.first()
        val replaced = day.exercises.first { it.exerciseId == ExerciseId.MACHINE_CHEST_PRESS }
        assertEquals(0, replaced.order) // retained position
        assertEquals(replacementDef.defaultPrescription.sets.first, replaced.sets)
        assertEquals(replacementDef.defaultPrescription.reps, replaced.repRange)
        assertEquals(replacementDef.defaultPrescription.restSeconds, replaced.restSeconds)
    }

    @Test
    fun `reject duplicate exercise`() = runTest {
        val plan = samplePlan()
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = plan)
        val profileRepo = FakeProfileRepository(initialProfile = userProfile(
            experienceLevel = com.jgv.workoutplanner.domain.model.ExperienceLevel.ADVANCED,
            availableEquipment = exerciseRepo.getAllExercises().flatMap { it.requiredEquipment }.toSet() + Equipment.BODYWEIGHT,
        ))
        val useCase = UpdateWorkoutExerciseUseCase(workoutRepo, profileRepo, eligible, exerciseRepo)

        // Try to add an exercise already present
        val result = useCase(
            WorkoutExerciseEdit.AddExercises("day-1", listOf(ExerciseId.PUSH_UP)),
        )
        assertEquals(WorkoutExerciseEditResult.DuplicateExercise, result)
    }

    @Test
    fun `reject capacity exceeded`() = runTest {
        // FULL_BODY template has 6 slots, we fill 6
        val fullDay = WorkoutDay(
            id = "day-1",
            name = "Full",
            focus = WorkoutDayFocus.FULL_BODY,
            exercises = listOf(
                PlannedExercise(ExerciseId.PUSH_UP, 3, 8..12, 90, 0),
                PlannedExercise(ExerciseId.LAT_PULLDOWN, 3, 8..12, 90, 1),
                PlannedExercise(ExerciseId.BODYWEIGHT_SQUAT, 3, 8..12, 90, 2),
                PlannedExercise(ExerciseId.DEAD_BUG, 3, 8..12, 90, 3),
                PlannedExercise(ExerciseId.GLUTE_BRIDGE, 3, 8..12, 90, 4),
                PlannedExercise(ExerciseId.DUMBBELL_CURL, 3, 8..12, 90, 5),
            ),
        )
        val plan = WorkoutPlan(id = "p", name = "p", days = listOf(fullDay))
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = plan)
        val profileRepo = FakeProfileRepository(initialProfile = userProfile(
            experienceLevel = com.jgv.workoutplanner.domain.model.ExperienceLevel.ADVANCED,
            availableEquipment = exerciseRepo.getAllExercises().flatMap { it.requiredEquipment }.toSet() + Equipment.BODYWEIGHT,
        ))
        val useCase = UpdateWorkoutExerciseUseCase(workoutRepo, profileRepo, eligible, exerciseRepo)

        val result = useCase(
            WorkoutExerciseEdit.AddExercises("day-1", listOf(ExerciseId.MACHINE_CHEST_PRESS)),
        )
        assertEquals(WorkoutExerciseEditResult.CapacityExceeded, result)
    }

    @Test
    fun `add preserves selection order`() = runTest {
        val plan = samplePlan() // 3 exercises, capacity 6 -> 3 remaining
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = plan)
        val generous = userProfile(
            experienceLevel = com.jgv.workoutplanner.domain.model.ExperienceLevel.ADVANCED,
            availableEquipment = exerciseRepo.getAllExercises().flatMap { it.requiredEquipment }.toSet() + Equipment.BODYWEIGHT,
        )
        val profileRepo = FakeProfileRepository(initialProfile = generous)
        val useCase = UpdateWorkoutExerciseUseCase(workoutRepo, profileRepo, eligible, exerciseRepo)

        val toAdd = listOf(ExerciseId.MACHINE_CHEST_PRESS, ExerciseId.DUMBBELL_BENCH_PRESS)
        val result = useCase(WorkoutExerciseEdit.AddExercises("day-1", toAdd))
        assertEquals(WorkoutExerciseEditResult.Updated, result)

        val day = workoutRepo.currentStored.plan!!.days.first()
        val appended = day.exercises.takeLast(2).map { it.exerciseId }
        assertEquals(toAdd, appended)

        day.exercises.takeLast(2).forEach { planned ->
            val definition = exerciseRepo.getExercise(planned.exerciseId)
            assertEquals(definition.defaultPrescription.sets.first, planned.sets)
            assertEquals(definition.defaultPrescription.reps, planned.repRange)
            assertEquals(definition.defaultPrescription.restSeconds, planned.restSeconds)
        }
    }

    @Test
    fun `atomic multi-add all or nothing on duplicate`() = runTest {
        val plan = samplePlan()
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = plan)
        val generous = userProfile(
            experienceLevel = com.jgv.workoutplanner.domain.model.ExperienceLevel.ADVANCED,
            availableEquipment = exerciseRepo.getAllExercises().flatMap { it.requiredEquipment }.toSet() + Equipment.BODYWEIGHT,
        )
        val profileRepo = FakeProfileRepository(initialProfile = generous)
        val useCase = UpdateWorkoutExerciseUseCase(workoutRepo, profileRepo, eligible, exerciseRepo)

        // One new, one duplicate -> should reject all
        val result = useCase(
            WorkoutExerciseEdit.AddExercises("day-1", listOf(ExerciseId.MACHINE_CHEST_PRESS, ExerciseId.PUSH_UP)),
        )
        assertEquals(WorkoutExerciseEditResult.DuplicateExercise, result)
        assertEquals(3, workoutRepo.currentStored.plan!!.days.first().exercises.size)
    }

    @Test
    fun `ineligible replacement rejected`() = runTest {
        val plan = samplePlan()
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = plan)
        // Only bodyweight, beginner -> machine chest press not eligible
        val profileRepo = FakeProfileRepository(initialProfile = userProfile())
        val useCase = UpdateWorkoutExerciseUseCase(workoutRepo, profileRepo, eligible, exerciseRepo)

        val result = useCase(
            WorkoutExerciseEdit.Replace("day-1", ExerciseId.PUSH_UP, ExerciseId.MACHINE_CHEST_PRESS),
        )
        assertEquals(WorkoutExerciseEditResult.IneligibleExercise, result)
    }

    @Test
    fun `plan outdated checked first before existence`() = runTest {
        val plan = samplePlan()
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = plan, initialRequiresRegeneration = true)
        val profileRepo = FakeProfileRepository(initialProfile = userProfile())
        val useCase = UpdateWorkoutExerciseUseCase(workoutRepo, profileRepo, eligible, exerciseRepo)

        // Use invalid day id — should return PlanOutdated, not DayNotFound
        val result = useCase(WorkoutExerciseEdit.Remove("nonexistent-day", ExerciseId.PUSH_UP))
        assertEquals(WorkoutExerciseEditResult.PlanOutdated, result)
    }

    @Test
    fun `invalid target index rejected`() = runTest {
        val plan = samplePlan()
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = plan)
        val profileRepo = FakeProfileRepository(initialProfile = userProfile())
        val useCase = UpdateWorkoutExerciseUseCase(workoutRepo, profileRepo, eligible, exerciseRepo)

        val result = useCase(WorkoutExerciseEdit.Move("day-1", ExerciseId.PUSH_UP, 99))
        assertEquals(WorkoutExerciseEditResult.InvalidTargetIndex, result)
    }

    @Test
    fun `no-op move returns Unchanged`() = runTest {
        val plan = samplePlan()
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = plan)
        val profileRepo = FakeProfileRepository(initialProfile = userProfile())
        val useCase = UpdateWorkoutExerciseUseCase(workoutRepo, profileRepo, eligible, exerciseRepo)

        val result = useCase(WorkoutExerciseEdit.Move("day-1", ExerciseId.PUSH_UP, 0))
        assertEquals(WorkoutExerciseEditResult.Unchanged, result)
    }

    @Test
    fun `missing plan day and exercise return typed failures`() = runTest {
        val profileRepo = FakeProfileRepository(initialProfile = userProfile())

        val missingPlanResult = UpdateWorkoutExerciseUseCase(
            FakeWorkoutPlanRepository(initialPlan = null),
            profileRepo,
            eligible,
            exerciseRepo,
        )(WorkoutExerciseEdit.Remove("day-1", ExerciseId.PUSH_UP))
        assertEquals(WorkoutExerciseEditResult.PlanNotFound, missingPlanResult)

        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = samplePlan())
        val useCase = UpdateWorkoutExerciseUseCase(workoutRepo, profileRepo, eligible, exerciseRepo)
        assertEquals(
            WorkoutExerciseEditResult.DayNotFound,
            useCase(WorkoutExerciseEdit.Remove("missing-day", ExerciseId.PUSH_UP)),
        )
        assertEquals(
            WorkoutExerciseEditResult.ExerciseNotFound,
            useCase(WorkoutExerciseEdit.Remove("day-1", ExerciseId.MACHINE_CHEST_PRESS)),
        )
    }

    @Test
    fun `empty addition and same replacement return Unchanged`() = runTest {
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = samplePlan())
        val profileRepo = FakeProfileRepository(initialProfile = userProfile())
        val useCase = UpdateWorkoutExerciseUseCase(workoutRepo, profileRepo, eligible, exerciseRepo)

        assertEquals(
            WorkoutExerciseEditResult.Unchanged,
            useCase(WorkoutExerciseEdit.AddExercises("day-1", emptyList())),
        )
        assertEquals(
            WorkoutExerciseEditResult.Unchanged,
            useCase(WorkoutExerciseEdit.Replace("day-1", ExerciseId.PUSH_UP, ExerciseId.PUSH_UP)),
        )
    }

    @Test
    fun `addition incompatible with day focus is rejected atomically`() = runTest {
        val original = samplePlan()
        val workoutRepo = FakeWorkoutPlanRepository(initialPlan = original)
        val profileRepo = FakeProfileRepository(initialProfile = userProfile())
        val useCase = UpdateWorkoutExerciseUseCase(workoutRepo, profileRepo, eligible, exerciseRepo)

        val result = useCase(
            WorkoutExerciseEdit.AddExercises("day-1", listOf(ExerciseId.STANDING_CALF_RAISE)),
        )

        assertEquals(WorkoutExerciseEditResult.IneligibleExercise, result)
        assertEquals(original, workoutRepo.currentStored.plan)
    }
}
