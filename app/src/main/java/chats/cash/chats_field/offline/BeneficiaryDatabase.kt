package chats.cash.chats_field.offline

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.model.campaignform.AllCampaignTypeConverter
import chats.cash.chats_field.model.campaignform.CampaignForm
import chats.cash.chats_field.model.utils.GsonParser
import chats.cash.chats_field.network.response.organization.campaign.Campaign
import com.google.gson.Gson

@Database(entities = [Beneficiary::class, Campaign::class, ModelCampaign::class, CampaignForm::class],
    version = 7,
    exportSchema = false)
@TypeConverters(AllCampaignTypeConverter::class)
abstract class BeneficiaryDatabase : RoomDatabase() {
    abstract fun beneficiaryDao(): BeneficiaryDao

    companion object {
        @Volatile
        private var INSTANCE: BeneficiaryDatabase? = null

        fun getDatabase(context: Context): BeneficiaryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BeneficiaryDatabase::class.java,
                    "beneficiary"
                ).addTypeConverter(AllCampaignTypeConverter(GsonParser(Gson()))) .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}