package chats.cash.chats_field.di

import chats.cash.chats_field.offline.OfflineViewModel
import chats.cash.chats_field.views.auth.login.LoginViewModel
import chats.cash.chats_field.views.auth.onboarding.OnboardingViewModel
import chats.cash.chats_field.views.beneficiary_search.BeneficiarySearchViewModel
import chats.cash.chats_field.views.auth.viewmodel.RegisterViewModel
import chats.cash.chats_field.views.beneficiary_list.BeneficiaryListViewModel
import chats.cash.chats_field.views.beneficiary_onboarding.ExistingBeneficiaryViewModel
import chats.cash.chats_field.views.beneficiary_onboarding.campaigns.CampaignViewModel
import chats.cash.chats_field.views.cashForWork.CashForWorkViewModel
import chats.cash.chats_field.views.cashForWork.TaskDetailsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        RegisterViewModel(get(), get())
    }
    viewModel {
        OfflineViewModel(get(),get())
    }
    viewModel { CashForWorkViewModel(networkRepository = get()) }

    viewModel { BeneficiarySearchViewModel(repository = get()) }
    viewModel { ExistingBeneficiaryViewModel(repository = get()) }
    viewModel { CampaignViewModel(offlineRepository = get()) }
    viewModel { (campaignId: Int) ->
        BeneficiaryListViewModel(repository = get(),
            campaignId = campaignId)
    }
    viewModel { TaskDetailsViewModel(repository = get()) }
    viewModel { LoginViewModel(repository = get(),get(), get()) }
    viewModel { OnboardingViewModel(repository = get()) }
}
