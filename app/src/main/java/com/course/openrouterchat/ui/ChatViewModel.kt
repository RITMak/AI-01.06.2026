package com.course.openrouterchat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.course.openrouterchat.data.OpenRouterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: OpenRouterRepository = OpenRouterRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadModels()
    }

    fun onQueryChange(text: String) {
        _uiState.update { it.copy(query = text) }
    }

    fun onModelSelected(modelId: String) {
        _uiState.update { it.copy(selectedModelId = modelId, error = null) }
    }

    fun onSend() {
        val state = _uiState.value
        val modelId = state.selectedModelId ?: return
        val query = state.query.trim()
        if (query.isBlank() || state.isLoading) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    response = "",
                )
            }

            repository.sendChat(modelId, query)
                .onSuccess { answer ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            response = answer,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Ошибка запроса",
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun loadModels() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingModels = true, error = null) }

            repository.getFreeModels()
                .onSuccess { models ->
                    if (models.isEmpty()) {
                        _uiState.update {
                            it.copy(
                                isLoadingModels = false,
                                models = emptyList(),
                                selectedModelId = null,
                                error = "Нет бесплатных моделей",
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoadingModels = false,
                                models = models,
                                selectedModelId = models.first().id,
                                error = null,
                            )
                        }
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingModels = false,
                            error = error.message ?: "Не удалось загрузить модели",
                        )
                    }
                }
        }
    }
}
