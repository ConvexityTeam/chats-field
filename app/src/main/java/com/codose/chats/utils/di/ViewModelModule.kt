package com.codose.chats.utils.di

import com.codose.chats.offline.OfflineViewModel
import com.codose.chats.views.beneficiary_search.BeneficiarySearchViewModel
import com.codose.chats.views.auth.viewmodel.RegisterViewModel
import com.codose.chats.views.beneficiary_onboarding.ExistingBeneficiaryViewModel
import com.codose.chats.views.beneficiary_onboarding.campaigns.CampaignViewModel
import com.codose.chats.views.cashForWork.CashForWorkViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        RegisterViewModel(get(), get())
    }
    viewModel {
        OfflineViewModel(get())
    }
    viewModel {
        CashForWorkViewModel(get())
    }
    viewModel {
        BeneficiarySearchViewModel(service = get())
    }
    viewModel { ExistingBeneficiaryViewModel(repository = get()) }
    viewModel { CampaignViewModel(offlineRepository = get()) }
}
