package com.sam.askme

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import com.sam.askme.data.AskMeRepository
import com.sam.askme.data.model.ChatMessage
import com.sam.askme.util.ChatUiState
import com.sam.askme.data.model.Participant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AskMeViewModel @Inject constructor(
   private val askMeRepository: AskMeRepository
) : ViewModel() {

    private val chat = askMeRepository.chetList

    private val _uiState: MutableStateFlow<ChatUiState> =
        MutableStateFlow(ChatUiState(chat.history.map { content ->
            ChatMessage(
                text = content.parts.first().asTextOrNull() ?: "",
                participant = if (content.role == "user") Participant.ME else Participant.ASKME,
                isPending = true
            )
        }))
    val uiState: StateFlow<ChatUiState> =
        _uiState.asStateFlow()


    fun sendMessage(userMessage: String,
                    selectedImages: List<Bitmap>? = null) {
        _uiState.value.addMessage(
            ChatMessage(
                text = userMessage,
                participant = Participant.ME,
                isPending = false
            )
        )

        viewModelScope.launch {
            try {
                _uiState.value.replaceLastPendingMessage()
                _uiState.value.addMessage(
                    ChatMessage(
                        text = "",
                        participant = Participant.ASKME,
                        isPending = true
                    )
                )
                if(selectedImages.isNullOrEmpty().not()) {
                    val inputContent = content {
                        for (bitmap in selectedImages!!) {
                            image(bitmap)
                        }
                        text(userMessage)
                    }
                    val response = askMeRepository.getImageResponse(inputContent)
                    response.collect { text ->

                        _uiState.value.addTextToMessage(
                            text.text.toString()
                        )
                    }
                } else {
                    val response = chat.sendMessageStream(userMessage)
                    response.collect { text ->
                        _uiState.value.addTextToMessage(
                            text.text.toString()
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value.replaceLastPendingMessage()
                _uiState.value.addMessage(
                    ChatMessage(
                        text = e.localizedMessage!!.toString(),
                        participant = Participant.ERROR
                    )
                )
            }
        }
    }
}