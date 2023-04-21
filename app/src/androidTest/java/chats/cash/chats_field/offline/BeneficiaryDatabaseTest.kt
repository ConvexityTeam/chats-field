package chats.cash.chats_field.offline

import android.content.Context
import androidx.lifecycle.asFlow
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import chats.cash.chats_field.model.campaignform.AllCampaignTypeConverter
import chats.cash.chats_field.model.utils.GsonParser
import chats.cash.chats_field.network.response.organization.campaign.Campaign
import chats.cash.chats_field.utils.test.TestUtil
import com.google.gson.Gson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@SmallTest
class BeneficiaryDatabaseTest {

    private lateinit var db: BeneficiaryDatabase
    private lateinit var beneficiaryDao: BeneficiaryDao

    @Before
    fun createDb()  {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, BeneficiaryDatabase::class.java).allowMainThreadQueries()
            .addTypeConverter(AllCampaignTypeConverter(GsonParser(Gson()))) .fallbackToDestructiveMigration().build()
        beneficiaryDao = db.beneficiaryDao()
    }

    @Test
    @Throws(Exception::class)
    fun insertBeneficiaryTest() = runTest {
        val beneficiary = TestUtil.createBeneficiary(1)
        beneficiaryDao.insertBeneficiary(beneficiary)
        val beneficiaries = beneficiaryDao.getAllBeneficiary().asFlow()
        println(beneficiaries.first().toString())
        assert(beneficiaries.first().contains(beneficiary))
    }

    @Test
    @Throws(Exception::class)
    fun insertCampaignTest() = runTest {
        val campaignsToInsert  = mutableListOf<Campaign>()
        (0..9).forEach {
            campaignsToInsert.add(TestUtil.createCampaign(it))
        }
        beneficiaryDao.insertCampaigns(campaignsToInsert)

        val campaignsInDatabase = beneficiaryDao.getCampaigns().asFlow()

        println(campaignsInDatabase.first().toString())
        assert(campaignsInDatabase.first()==(campaignsToInsert))
    }


    @After
    fun close(){
        try {
            db.close()
        }
        catch(e: Exception){
            e.printStackTrace()
        }
    }



}