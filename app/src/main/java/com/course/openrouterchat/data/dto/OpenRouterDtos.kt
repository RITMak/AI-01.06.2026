package com.course.openrouterchat.data.dto

data class ModelsResponse(
    val data: List<ModelDto> = emptyList(),
)

data class ModelDto(
    val id: String,
    val name: String? = null,
    val pricing: PricingDto? = null,
)

data class PricingDto(
    val prompt: String? = null,
    val completion: String? = null,
)

data class ChatRequest(
    val model: String,
    val messages: List<ChatMessageDto>,
    val stream: Boolean = false,
)

data class ChatMessageDto(
    val role: String,
    val content: String,
)

data class ChatResponse(
    val choices: List<ChatChoiceDto>? = null,
)

data class ChatChoiceDto(
    val message: ChatMessageDto? = null,
)

data class ErrorResponse(
    val error: ErrorBody? = null,
)

data class ErrorBody(
    val message: String? = null,
    val code: Int? = null,
)
