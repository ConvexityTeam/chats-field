package com.codose.chats.offline

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.codose.chats.model.ModelCampaign
import com.codose.chats.network.response.organization.campaign.Campaign

@Database(entities = [Beneficiary::class, Campaign::class, ModelCampaign::class], version = 6, exportSchema = false)
abstract class BeneficiaryDatabase : RoomDatabase() {
    abstract fun beneficiaryDao() : BeneficiaryDao
    companion object {
        @Volatile
        private var INSTANCE: BeneficiaryDatabase? =null

        fun getDatabase(context: Context) : BeneficiaryDatabase {
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BeneficiaryDatabase::class.java,
                    "beneficiary"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}