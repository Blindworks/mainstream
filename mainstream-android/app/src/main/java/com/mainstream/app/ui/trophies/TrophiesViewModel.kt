package com.mainstream.app.ui.trophies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mainstream.app.data.model.Trophy
import com.mainstream.app.data.model.TrophyProgress
import com.mainstream.app.data.model.UserTrophy
import com.mainstream.app.data.repository.TrophyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrophiesUiState(
    val isLoading: Boolean = false,
    val allTrophies: List<Trophy> = emptyList(),
    val userTrophies: List<UserTrophy> = emptyList(),
    val trophyProgress: List<TrophyProgress> = emptyList(),
    val todaysTrophy: Trophy? = null,
    val weeklyTrophies: List<UserTrophy> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class TrophiesViewModel @Inject constructor(
    private val trophyRepository: TrophyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrophiesUiState())
    val uiState: StateFlow<TrophiesUiState> = _uiState.asStateFlow()

    init {
        loadTrophies()
    }

    fun loadTrophies() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Load all trophies
                val allTrophiesResult = trophyRepository.getAllTrophies()
                val allTrophies = allTrophiesResult.getOrNull() ?: emptyList()

                // Load user trophies
                val userTrophiesResult = trophyRepository.getUserTrophies()
                val userTrophies = userTrophiesResult.getOrNull() ?: emptyList()

                // Load trophy progress
                val progressResult = trophyRepository.getTrophyProgress()
                val progress = progressResult.getOrNull() ?: emptyList()

                // Load today's trophy
                val todaysTrophyResult = trophyRepository.getTodaysTrophy()
                val todaysTrophy = todaysTrophyResult.getOrNull()

                // Load weekly trophies
                val weeklyResult = trophyRepository.getWeeklyTrophies()
                val weeklyTrophies = weeklyResult.getOrNull() ?: emptyList()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    allTrophies = allTrophies,
                    userTrophies = userTrophies,
                    trophyProgress = progress,
                    todaysTrophy = todaysTrophy,
                    weeklyTrophies = weeklyTrophies
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Fehler beim Laden der Trophäen"
                )
            }
        }
    }

    fun refresh() {
        loadTrophies()
    }
}
