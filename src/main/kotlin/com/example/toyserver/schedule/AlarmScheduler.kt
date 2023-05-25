package com.example.toyserver.schedule

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient

//@Component
class AlarmScheduler(
    @Value("\${slack.webhook.url}") val slackWebhookUrl: String
) {

    var mapper: ObjectMapper = ObjectMapper()

    @Scheduled(fixedDelay = 30 * 1000)
    fun leeumSchedule() {
        leeumService("20230527")
        leeumService("20230528")
        leeumService("20230604")
    }

    fun leeumService(date: String) {
        val apiClient: WebClient = WebClient.builder()
            .baseUrl("https://ticket.leeum.org/leeum/personal/getTimeListJson.do")
            .build()

        val builder = MultipartBodyBuilder()
        builder.part("foundCd", "M30")
        builder.part("locCd", "3")
        builder.part("eventCd", "202202")
        builder.part("placeGrp", "2")
        builder.part("planExhibit", "")
        builder.part("limitedMuzCd", "010000")
        builder.part("limitedEventCd", "202202")
        builder.part("selectGbn", "202202")
        builder.part("date", date)

        try {
            val response: String? = apiClient.post()
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(String::class.java)
                .block()
            val infoByTimes: JsonNode = mapper.readTree(response).path("timeList")

            var isOpen = false
            val sb = StringBuilder()
            for (info: JsonNode in infoByTimes) {
                val time: String = info.path("SCHEDULE_TIME").asText()
                val rsvCnt: Int = info.path("RSV_CNT").asInt()  // 예약 확정
                val entCnt: Int = info.path("ENT_CNT").asInt()  // 예약창 진입한 사람 수
                val grpRsvCnt: Int = info.path("GRP_RSV_CNT").asInt()   // 그룹 예약 확정
                val personCnt : Int = info.path("PERSON_CNT").asInt()   // 총 자리 수

                val remain = personCnt - (rsvCnt + entCnt + grpRsvCnt)
                if (remain > 0) {
                    isOpen = true
                    sb.append(date).append("-").append(time).append(" : ").append(remain).append("자리 open\n")
                }
            }
            sb.append("예약 : https://ticket.leeum.org/leeum/personal/exhibitList.do")

            if (isOpen)
                sendSlack(sb.toString())
        } catch (e: Exception) {
            sendSlack("오류 발생 : $e")
            e.printStackTrace()
        }
    }

    fun sendSlack(message: String) {
        WebClient.builder().baseUrl(slackWebhookUrl).build()
            .post()
            .bodyValue(mapOf("text" to message))
            .retrieve()
            .bodyToMono(String::class.java)
            .block()
    }
}