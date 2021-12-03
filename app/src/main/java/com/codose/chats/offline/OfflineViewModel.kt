package com.codose.chats.offline

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OfflineViewModel(application : Application) : AndroidViewModel(application) {
    val database by lazy { BeneficiaryDatabase.getDatabase(application.applicationContext) }
    val offlineRepository by lazy { OfflineRepository(database.beneficiaryDao()) }

    val getBeneficiaries = offlineRepository.getAllBeneficiary()
    fun insert(beneficiary: Beneficiary){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                offlineRepository.insert(beneficiary)
            }
        }
    }

    fun delete(beneficiary: Beneficiary){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                offlineRepository.delete(beneficiary)
            }
        }
    }


}