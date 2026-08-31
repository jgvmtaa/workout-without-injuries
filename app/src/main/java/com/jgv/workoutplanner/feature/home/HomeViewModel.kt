package com.jgv.workoutplanner.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jgv.workoutplanner.domain.repository.ProfileRepository
import com.jgv.workoutplanner.domain.repository.WorkoutPlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel that combines profile + current plan for Home screen (README §16, task 5.8).
 *
 * Home shows:
 * - Current plan (View plan) if exists, otherwise ability to generate on Plan screen.
 * - Profile summary: days, goal, experience, split.
 * - Active limitations count.
 * - Library browse entry.
 *
 * Deterministic: profile and plan are flows, no randomness.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    profileRepository: ProfileRepository,
    workoutPlanRepository: WorkoutPlanRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        profileRepository.profile,
        workoutPlanRepository.currentPlan,
    ) { profile, plan ->
        if (profile == null) {
            HomeUiState(isLoading = false, hasPlan = false)
        } else {
            HomeUiState(
                isLoading = false,
                daysPerWeek = profile.daysPerWeek,
                goalDisplay = profile.goal.name,
                experienceDisplay = profile.experienceLevel.name,
                split = profile.preferredSplit,
                limitationsCount = profile.movementLimitations.size,
                hasPlan = plan != null,
                planName = plan?.name,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true),
    )
}
