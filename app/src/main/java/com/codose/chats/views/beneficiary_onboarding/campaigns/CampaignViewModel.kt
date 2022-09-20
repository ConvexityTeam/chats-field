package com.codose.chats.views.beneficiary_onboarding.campaigns

import androidx.lifecycle.ViewModel
import com.codose.chats.offline.OfflineRepository

class CampaignViewModel(offlineRepository: OfflineRepository) : ViewModel() {

    val campaigns = offlineRepository.getAllCampaigns(type = "campaign")
}
