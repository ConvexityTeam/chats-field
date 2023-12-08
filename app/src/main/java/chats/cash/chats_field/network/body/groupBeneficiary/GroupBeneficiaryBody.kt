package chats.cash.chats_field.network.body.groupBeneficiary

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import chats.cash.chats_field.offline.Beneficiary

@Entity(tableName = "Group Beneficiary")
data class GroupBeneficiaryBody(
    @PrimaryKey(autoGenerate = false)
    var id: String,
    @Embedded("group")
    val group: GroupBeneficiaryGroupDetails,
    val member: List<GroupBeneficiaryMember>,
    @Embedded("group_beneficiary")
    val representative: Beneficiary,
    val campaignId: Int,
)

data class GroupBeneficiaryGroupDetails(
    val group_name: String,
    val group_category: String,
)

data class GroupBeneficiaryMember(
    val full_name: String,
    val profile_pic: String,
    val dob: String,
)
