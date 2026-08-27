package com.jgv.workoutplanner.feature.exercisedetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.jgv.workoutplanner.core.WhileUiSubscribed
import com.jgv.workoutplanner.domain.model.ExerciseId
import com.jgv.workoutplanner.domain.model.availability
import com.jgv.workoutplanner.domain.repository.ExerciseRepository
import com.jgv.workoutplanner.domain.repository.ProfileRepository
import com.jgv.workoutplanner.domain.usecase.EvaluateExerciseEligibilityUseCase
import com.jgv.workoutplanner.navigation.AppRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Resolves the exercise and its eligibility for the current profile (README §15).
 *
 * Navigation argument [ExerciseId] is typed via AppRoute (§17), so an unknown id
 * cannot be navigated to. Profile may be null on first launch — detail still shows
 * the definition with no eligibility, rather than crashing.
 */
@HiltViewModel
class ExerciseDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    exerciseRepository: ExerciseRepository,
    profileRepository: ProfileRepository,
    private val evaluateEligibility: EvaluateExerciseEligibilityUseCase,
) : ViewModel() {

    private val route: AppRoute.ExerciseDetails = savedStateHandle.toRoute()
    val exerciseId: ExerciseId = route.exerciseId

    private val definition = exerciseRepository.getExercise(exerciseId)

    val uiState: StateFlow<ExerciseDetailsUiState> =
        profileRepository.profile
            .map { profile ->
                if (profile != null) {
                    val eligibility = evaluateEligibility(definition, profile)
                    ExerciseDetailsUiState(
                        isLoading = false,
                        definition = definition,
                        eligibility = eligibility,
                        availability = eligibility.availability(),
                        currentExperienceLevel = profile.experienceLevel,
                    )
                } else {
                    ExerciseDetailsUiState(
                        isLoading = false,
                        definition = definition,
                        eligibility = null,
                        availability = null,
                        currentExperienceLevel = null,
                    )
                }
            }.stateIn(
                scope = viewModelScope,
                started = WhileUiSubscribed,
                initialValue = ExerciseDetailsUiState(isLoading = true, definition = definition),
            )
}
