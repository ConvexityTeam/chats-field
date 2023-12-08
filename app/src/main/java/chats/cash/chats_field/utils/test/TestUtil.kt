package chats.cash.chats_field.utils.test

import chats.cash.chats_field.model.ModelCampaign
import chats.cash.chats_field.model.campaignform.CampaignForm
import chats.cash.chats_field.network.response.organization.campaign.Campaign
import chats.cash.chats_field.offline.Beneficiary

class TestUtil {
    companion object {
        fun createBeneficiary(index: Int): Beneficiary {
            return Beneficiary(id = index, firstName = "user$index", lastName = "userLast$index")
        }

        fun createModelCampaign(index: Int): ModelCampaign {
            return ModelCampaign(
                id = 3 * (index + 1),
                OrganisationId = 10 * (index + 1),
                title = "title$index",
                type = "type$index",
                spending = "spending$index",
                description = "description$index",
                status = "status$index",
                is_funded = "funded$index",
                funded_with = "fundedWith$index",
                budget = "budget$index",
                start_date = "startDate$index",
                end_date = "endDate$index",
                ck8 = "ck8$index",
                createdAt = "createdAt$index",
                updatedAt = "justo",
            )
        }

        fun createCampaignForm(index: Int): CampaignForm {
            return CampaignForm(
                beneficiaryId = index * 2,
                campaigns = listOf(createModelCampaign(1)),
                createdAt = "${index}00",
                id = index,
                organisationId = index,
                questions = listOf(),
                title = "title$index",
                updatedAt = "updatedAt$index",
            )
        }

        fun createCampaign(index: Int): Campaign {
            return Campaign(
                id = index, budget = index * 100, title = "campaign $index", createdAt = "$index 0",
                description = "",
                endDate = "",
                location = "",
                organisationMemberId = 0,
                startDate = "",
                status = 0,
                type = "",
                updatedAt = "",
            )
        }
    }
}
