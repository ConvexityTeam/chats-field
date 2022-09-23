package com.codose.chats.views.auth.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codose.chats.offline.OfflineRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import timber.log.Timber

class OnboardingViewModel(private val repository: OfflineRepository) : ViewModel() {

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable)
    }

    fun clearAllTables() {
        viewModelScope.launch(exceptionHandler) { repository.deleteAllTables() }
    }
}
