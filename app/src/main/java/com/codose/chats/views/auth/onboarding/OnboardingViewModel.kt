package com.codose.chats.views.auth.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codose.chats.offline.BeneficiaryDatabase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class OnboardingViewModel(private val database: BeneficiaryDatabase) : ViewModel() {

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable)
    }

    fun clearAllTables() {
        viewModelScope.launch(exceptionHandler) {
            withContext(Dispatchers.IO) { database.clearAllTables() }
        }
    }
}
