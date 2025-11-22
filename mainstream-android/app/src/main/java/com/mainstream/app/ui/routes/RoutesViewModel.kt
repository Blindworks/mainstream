package com.mainstream.app.ui.routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mainstream.app.data.model.PredefinedRoute
import com.mainstream.app.data.repository.RouteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoutesUiState(
    val isLoading: Boolean = false,
    val routes: List<PredefinedRoute> = emptyList(),
    val selectedRoute: PredefinedRoute? = null,
    val error: String? = null
)

@HiltViewModel
class RoutesViewModel @Inject constructor(
    private val routeRepository: RouteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutesUiState())
    val uiState: StateFlow<RoutesUiState> = _uiState.asStateFlow()

    init {
        loadRoutes()
    }

    fun loadRoutes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = routeRepository.getAllRoutesWithStats()
            result.fold(
                onSuccess = { routes ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        routes = routes
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Fehler beim Laden der Routen"
                    )
                }
            )
        }
    }

    fun selectRoute(routeId: Long) {
        viewModelScope.launch {
            val result = routeRepository.getRouteById(routeId)
            result.fold(
                onSuccess = { route ->
                    _uiState.value = _uiState.value.copy(selectedRoute = route)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Fehler beim Laden der Route"
                    )
                }
            )
        }
    }

    fun clearSelectedRoute() {
        _uiState.value = _uiState.value.copy(selectedRoute = null)
    }

    fun refresh() {
        loadRoutes()
    }
}
