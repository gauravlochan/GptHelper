package net.lochan.gpthelper.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.lochan.gpthelper.api.ChatGptService
import net.lochan.gpthelper.model.Chat
import java.io.IOException

class ChatListViewModel(application: Application) : AndroidViewModel(application) {
    private val chatGptService = ChatGptService(application)
    
    private val _chatListState = MutableStateFlow<ChatListState>(ChatListState.Loading)
    val chatListState: StateFlow<ChatListState> = _chatListState.asStateFlow()

    init {
        loadChats()
    }

    fun loadChats(project: String? = "Bookmarks") {
        viewModelScope.launch {
            _chatListState.value = ChatListState.Loading
            try {
                if (!chatGptService.isReady()) {
                    _chatListState.value = ChatListState.Error("Please configure your API key first")
                    return@launch
                }
                
                val chats = chatGptService.getChats(project)
                _chatListState.value = if (chats.isEmpty()) {
                    ChatListState.Empty
                } else {
                    ChatListState.Success(chats)
                }
            } catch (e: IOException) {
                _chatListState.value = ChatListState.Error(
                    when {
                        e.message?.contains("No internet connection") == true -> 
                            "No internet connection. Please check your network and try again."
                        e.message?.contains("Failed to validate API key") == true ->
                            "Invalid API key. Please check your API key in settings."
                        else -> e.message ?: "Failed to load chats"
                    }
                )
            } catch (e: Exception) {
                _chatListState.value = ChatListState.Error("An unexpected error occurred: ${e.message}")
            }
        }
    }

    fun refresh() {
        loadChats()
    }
}

sealed class ChatListState {
    object Loading : ChatListState()
    object Empty : ChatListState()
    data class Success(val chats: List<Chat>) : ChatListState()
    data class Error(val message: String) : ChatListState()
} 