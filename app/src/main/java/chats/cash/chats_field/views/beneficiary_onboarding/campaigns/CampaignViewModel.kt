package chats.cash.chats_field.views.beneficiary_onboarding.campaigns

import androidx.lifecycle.ViewModel
import chats.cash.chats_field.offline.OfflineRepository

class CampaignViewModel(offlineRepository: OfflineRepository) : ViewModel() {

    val campaigns = offlineRepository.getAllCampaigns(type = "campaign")

    val cashForWorkCampaigns = offlineRepository.getAllCampaigns(type = "cash-for-work")
}
