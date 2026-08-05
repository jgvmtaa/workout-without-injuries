package com.jgv.workoutplanner.feature.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Reference ViewModel for the architecture baseline (README §18, §22).
 *
 * It holds no business logic — Phase 1 deliberately has none. What it does establish
 * is the wiring every later screen copies: `@HiltViewModel` construction injection,
 * a single immutable [HomeUiState] exposed as a [StateFlow], and a screen that reads
 * that state rather than owning it.
 */
@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}
