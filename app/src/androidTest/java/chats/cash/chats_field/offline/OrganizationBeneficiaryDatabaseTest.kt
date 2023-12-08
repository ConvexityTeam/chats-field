package chats.cash.chats_field.offline

import android.content.Context
import androidx.lifecycle.asFlow
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.model.campaignform.AllCampaignTypeConverter
import chats.cash.chats_field.model.campaignform.CampaignForm
import chats.cash.chats_field.model.utils.GsonParser
import chats.cash.chats_field.network.body.survey.SubmitSurveyAnswerBody
import chats.cash.chats_field.network.response.organization.campaign.Campaign
import chats.cash.chats_field.utils.test.TestUtil
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class OrganizationBeneficiaryDatabaseTest {

    private lateinit var db: BeneficiaryDatabase
    private lateinit var beneficiaryDao: BeneficiaryDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            BeneficiaryDatabase::class.java,
        ).allowMainThreadQueries()
            .addTypeConverter(AllCampaignTypeConverter(GsonParser(Gson()))).fallbackToDestructiveMigration().build()
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
        val campaignsToInsert = mutableListOf<Campaign>()
        (0..9).forEach {
            campaignsToInsert.add(TestUtil.createCampaign(it))
        }
        beneficiaryDao.insertCampaigns(campaignsToInsert)

        val campaignsInDatabase = beneficiaryDao.getCampaigns().asFlow()

        println(campaignsInDatabase.first().toString())
        assert(campaignsInDatabase.first() == (campaignsToInsert))
    }

    @Test
    fun insertAllCampaignsTest() = runTest {
        val campaignsToInsert = mutableListOf<ModelCampaign>()
        (0..9).forEach {
            campaignsToInsert.add(TestUtil.createModelCampaign(it))
        }
        beneficiaryDao.insertAllCampaigns(campaignsToInsert)
        val result = beneficiaryDao.geAllLiveCampaigns("type0").asFlow().first()
        println(result)
        assert(result.isNotEmpty())
        assert(result.first().id == 3)
    }

    @Test
    fun insertAllCampaignsForms() = runTest {
        val campaignsFormsToInsert = mutableListOf<CampaignForm>()
        (0..9).forEach {
            campaignsFormsToInsert.add(TestUtil.createCampaignForm(it))
        }
        beneficiaryDao.insertAllCampaignsForms(campaignsFormsToInsert)
        val result = beneficiaryDao.getAllCampaignForms().first()
        println(result)
        assert(result.isNotEmpty() && result.size > 8)
    }

    @Test
    fun insertSurveyAnswer() = runTest {
        val answer = SubmitSurveyAnswerBody("is@gmail.com", 1, 2, 3, emptyList())
        beneficiaryDao.insertSurveyAnswer(answer)
        val result = beneficiaryDao.getSurveyAnswer("is@gmail.com")
        println(result)
        assert(result != null && result.campaignId == 1)
    }

    @Test
    fun deleteBeneficiary() = runTest {
        val beneficiary = TestUtil.createBeneficiary(1)
        beneficiaryDao.insertBeneficiary(beneficiary)
        val beneficiaries = beneficiaryDao.getAllBeneficiary().asFlow()
        println(beneficiaries.first().toString())
        assert(beneficiaries.first().contains(beneficiary))
        beneficiaryDao.deleteBeneficiary(beneficiary)
        val beneficiaries2 = beneficiaryDao.getAllBeneficiary().asFlow()
        assert(beneficiaries2.first().contains(beneficiary).not())
    }

    @Test
    fun deleteSurveyAnswer() = runTest {
        val answer = SubmitSurveyAnswerBody("is@gmail.com", 1, 2, 3, emptyList())
        beneficiaryDao.insertSurveyAnswer(answer)
        val result = beneficiaryDao.getSurveyAnswer("is@gmail.com")
        println(result)
        assert(result != null && result.campaignId == 1)
        beneficiaryDao.deleteSurveyAnswer(answer)
        val result2 = beneficiaryDao.getSurveyAnswer("is@gmail.com")
        assert(result2 == null)
    }

    @Test
    fun getAllBeneficiary() = runTest {
        val beneficiary = TestUtil.createBeneficiary(1)
        beneficiaryDao.insertBeneficiary(beneficiary)
        val beneficiaries = beneficiaryDao.getAllBeneficiary().asFlow()
        println(beneficiaries.first().toString())
        assert(beneficiaries.first().contains(beneficiary))
    }

    @Test
    fun getCampaigns() = runTest {
        val result0 = beneficiaryDao.geAllLiveCampaigns("type0").asFlow().first()
        assert(result0.isEmpty())
        val campaignsToInsert = mutableListOf<ModelCampaign>()
        (0..9).forEach {
            campaignsToInsert.add(TestUtil.createModelCampaign(it))
        }
        beneficiaryDao.insertAllCampaigns(campaignsToInsert)
        val result = beneficiaryDao.geAllLiveCampaigns("type0").asFlow().first()
        println(result)
        assert(result.isNotEmpty())
        assert(result.first().id == 3)
    }

    @Test
    fun getAllCampaignForms() = runTest {
        val result0 = beneficiaryDao.getAllCampaignForms().first()
        assert(result0.isEmpty())
        val campaignsFormsToInsert = mutableListOf<CampaignForm>()
        (0..9).forEach {
            campaignsFormsToInsert.add(TestUtil.createCampaignForm(it))
        }
        beneficiaryDao.insertAllCampaignsForms(campaignsFormsToInsert)
        val result = beneficiaryDao.getAllCampaignForms().first()
        println(result)
        assert(result.isNotEmpty() && result.size > 8)
    }

    @Test
    fun deleteCampaigns() = runTest {
        val campaignsFormsToInsert = mutableListOf<Campaign>()
        (0..9).forEach {
            campaignsFormsToInsert.add(TestUtil.createCampaign(it))
        }
        beneficiaryDao.insertCampaigns(campaignsFormsToInsert)
        val result = beneficiaryDao.getCampaigns().asFlow().first()
        println(result)
        assert(result.isNotEmpty() && result.size > 8)
        beneficiaryDao.deleteCampaigns()
        val result0 = beneficiaryDao.getCampaigns().asFlow().first()
        assert(result0.isEmpty())
    }

    @Test
    fun deleteCampaignsForms() = runTest {
        val campaignsFormsToInsert = mutableListOf<CampaignForm>()
        (0..9).forEach {
            campaignsFormsToInsert.add(TestUtil.createCampaignForm(it))
        }
        beneficiaryDao.insertAllCampaignsForms(campaignsFormsToInsert)
        val result = beneficiaryDao.getAllCampaignForms().first()
        println(result)
        assert(result.isNotEmpty() && result.size > 8)
        beneficiaryDao.deleteAllCampaignsForms()
        val result0 = beneficiaryDao.getAllCampaignForms().first()
        assert(result0.isEmpty())
    }

//
//    fun deleteBeneficiaries()

    @After
    fun close() {
        try {
            db.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
