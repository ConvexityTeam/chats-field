package chats.cash.chats_field.utils.test

import chats.cash.chats_field.network.response.organization.campaign.Campaign
import chats.cash.chats_field.offline.Beneficiary

class TestUtil {
 companion object {
     fun createBeneficiary(index: Int): Beneficiary {
         return Beneficiary(id = index, firstName = "user$index", lastName = "userLast$index")
     }

     fun createCampaign(index: Int): Campaign {
        return Campaign( id = index, budget = index * 100, title = "campaign $index", createdAt = "$index 0",
            description = "",
            endDate = "",
            location = "",
            organisationMemberId = 0,
            startDate = "",
            status = 0,
            type = "",
            updatedAt = ""
        )
     }
 }
}