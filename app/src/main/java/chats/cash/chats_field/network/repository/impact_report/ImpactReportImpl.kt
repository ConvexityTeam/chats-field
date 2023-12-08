package chats.cash.chats_field.network.repository.impact_report

import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.network.api.interfaces.ConvexityDataSourceInterface
import chats.cash.chats_field.network.body.impact_report.ImpactReportBody
import chats.cash.chats_field.network.response.impact_report.ImpactReportResponse
import chats.cash.chats_field.utils.PreferenceUtilInterface
import kotlinx.coroutines.flow.Flow

class ImpactReportImpl(
    private val dataSource: ConvexityDataSourceInterface,
    private val preferenceUtil: PreferenceUtilInterface,
) : ImpactReport {

    override suspend fun sendImpactReport(impactReportBody: ImpactReportBody): Flow<NetworkResponse<ImpactReportResponse>> {
        return dataSource.submitImpactReport(impactReportBody, preferenceUtil.getNGOToken())
    }
}
