package chats.cash.chats_field.network.repository.impact_report

import chats.cash.chats_field.network.NetworkResponse
import chats.cash.chats_field.network.body.impact_report.ImpactReportBody
import chats.cash.chats_field.network.response.impact_report.ImpactReportResponse
import kotlinx.coroutines.flow.Flow

interface ImpactReport {

    suspend fun sendImpactReport(impactReportBody: ImpactReportBody): Flow<NetworkResponse<ImpactReportResponse>>
}
