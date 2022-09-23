package com.codose.chats.di

import com.codose.chats.offline.OfflineViewModel
import com.codose.chats.views.auth.login.LoginViewModel
import com.codose.chats.views.auth.onboarding.OnboardingViewModel
import com.codose.chats.views.beneficiary_search.BeneficiarySearchViewModel
import com.codose.chats.views.auth.viewmodel.RegisterViewModel
import com.codose.chats.views.beneficiary_list.BeneficiaryListViewModel
import com.codose.chats.views.beneficiary_onboarding.ExistingBeneficiaryViewModel
import com.codose.chats.views.beneficiary_onboarding.campaigns.CampaignViewModel
import com.codose.chats.views.cashForWork.CashForWorkViewModel
import com.codose.chats.views.cashForWork.TaskDetailsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        RegisterViewModel(get(), get())
    }
    viewModel {
        OfflineViewModel(get())
    }
    viewModel { CashForWorkViewModel(networkRepository = get()) }

    viewModel { BeneficiarySearchViewModel(repository = get()) }
    viewModel { ExistingBeneficiaryViewModel(repository = get()) }
    viewModel { CampaignViewModel(offlineRepository = get()) }
    viewModel { BeneficiaryListViewModel(repository = get()) }
    viewModel { TaskDetailsViewModel(repository = get()) }
    viewModel { LoginViewModel(repository = get()) }
    viewModel { OnboardingViewModel(repository = get()) }
}
