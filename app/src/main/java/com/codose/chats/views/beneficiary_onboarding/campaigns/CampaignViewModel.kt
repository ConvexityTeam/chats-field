package com.codose.chats.views.beneficiary_onboarding.campaigns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.codose.chats.offline.OfflineRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import timber.log.Timber

class CampaignViewModel(offlineRepository: OfflineRepository) : ViewModel() {

    val campaigns = offlineRepository.getAllCampaigns()
}
