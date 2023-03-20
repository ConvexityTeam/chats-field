package chats.cash.chats_field.network.repository

import chats.cash.chats_field.CoroutineRule
import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.model.campaignform.CampaignForm
import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.network.api.interfaces.BeneficiaryInterface
import chats.cash.chats_field.network.repository.fakes.FakeBeneficiaryRepository
import chats.cash.chats_field.network.response.campaign.CampaignSurveyResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class BeneficiaryRepositoryTest {

    lateinit var fakeBeneficiaryRepository: BeneficiaryInterface

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = CoroutineRule()


    @Before
    fun setUp() {
        fakeBeneficiaryRepository = FakeBeneficiaryRepository()
    }

    @After
    fun tearDown() {

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
     fun getAllCampaigns() = runTest {
        val expectedResult = listOf(
            ModelCampaign(1,2,null,null,null,null,null,null,null,null,
            null,null,null,null,null,"1sec"),
            ModelCampaign(2,2,null,null,null,null,null,null,null,null,
                null,null,null,null,null,"1sec")
        )
        val campaigns = fakeBeneficiaryRepository.getAllCampaigns()
        assert(campaigns.first() is NetworkResponse.Success && (campaigns.first() as NetworkResponse.Success).body == expectedResult)
    }

    @Test
   fun getCampaignSurvey() = runTest{
        val expectedCampaignSurvey = CampaignSurveyResponse.CampaignSurveyResponseData(1,2,"",1,2, emptyList(),"2sec")
        val campaignSurvey = fakeBeneficiaryRepository.getCampaignSurvey(2)
        println((campaignSurvey.first() as NetworkResponse.Success).body.toString())
        assert(campaignSurvey.first() is NetworkResponse.Success && (campaignSurvey.first() as NetworkResponse.Success).body == expectedCampaignSurvey)
    }

    @Test
    fun getAllCampaignForms() = runTest{
        val expectedAllCampaign  = listOf(ModelCampaign(1,2,null,null,null,null,null,null,null,null,
        null,null,null,null,null,"1sec"),
        ModelCampaign(2,2,null,null,null,null,null,null,null,null,
            null,null,null,null,null,"1sec"))

        val expectedAllCampaignForms = listOf(CampaignForm(1,expectedAllCampaign,"2sec",1,2, emptyList(),"","2sec"))

        val allCampaignsForm = fakeBeneficiaryRepository.getAllCampaignForms()

        println((allCampaignsForm.first() as NetworkResponse.Success).body.toString())

        assert(allCampaignsForm.first() is NetworkResponse.Success && (allCampaignsForm.first() as NetworkResponse.Success).body == expectedAllCampaignForms)
    }
}