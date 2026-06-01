package com.course.openrouterchat.ui

import com.course.openrouterchat.data.model.ModelUi

data class ChatUiState(
    val models: List<ModelUi> = emptyList(),
    val selectedModelId: String? = null,
    val query: String = "",
    val response: String = "",
    val isLoading: Boolean = false,
    val isLoadingModels: Boolean = true,
    val error: String? = null,
) {
    val selectedModel: ModelUi?
        get() = models.find { it.id == selectedModelId }

    val canSend: Boolean
        get() = !isLoading &&
            !isLoadingModels &&
            query.isNotBlank() &&
            selectedModelId != null &&
            models.isNotEmpty()
}
