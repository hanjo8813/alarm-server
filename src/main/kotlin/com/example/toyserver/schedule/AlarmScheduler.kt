package com.example.toyserver.schedule

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient


@Component
class AlarmScheduler(
    @Value("\${slack.webhook.url}") val slackWebhookUrl: String
) {

    var mapper: ObjectMapper = ObjectMapper()

    @Scheduled(fixedDelay = 3 * 60 * 1000)
    fun leeumSchedule() {
        val apiClient: WebClient = WebClient.builder()
            .baseUrl("https://ticket.leeum.org/leeum/personal/getTimeListJson.do")
            .build()

        val multipartBodyBuilder = MultipartBodyBuilder()
        multipartBodyBuilder.part("foundCd", "M30")
        multipartBodyBuilder.part("locCd", "3")
        multipartBodyBuilder.part("eventCd", "202202")
        multipartBodyBuilder.part("placeGrp", "2")
        multipartBodyBuilder.part("planExhibit", "")
        multipartBodyBuilder.part("limitedMuzCd", "010000")
        multipartBodyBuilder.part("limitedEventCd", "202202")
        multipartBodyBuilder.part("selectGbn", "202202")
        multipartBodyBuilder.part("date", "20230604")

        val response: String? = apiClient.post()
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
            .retrieve()
            .bodyToMono(String::class.java)
            .block()
        val infoByTimes: JsonNode = mapper.readTree(response).path("timeList")

        var isOpen: Boolean = false
        val sb = StringBuilder()
        for (info: JsonNode in infoByTimes) {
            val time: String = info.path("SCHEDULE_TIME").asText()
            val rsvCnt: Int = info.path("RSV_CNT").asInt()
            val entCnt: Int = info.path("ENT_CNT").asInt()
            val crpRsvCnt: Int = info.path("GRP_RSV_CNT").asInt()
            val personCnt: Int = info.path("PERSON_CNT").asInt()

            val remain = (rsvCnt + entCnt) - (crpRsvCnt + personCnt)
            if (remain > 0) {
                isOpen = true
                sb.append(time).append(" - ").append(remain).append("자리 open\n")
            }
        }
        sb.append("예약 : https://ticket.leeum.org/leeum/personal/exhibitList.do")

        if (!isOpen)
            return

        WebClient.builder().baseUrl(slackWebhookUrl).build()
            .post()
            .bodyValue(mapOf("text" to sb.toString()))
            .retrieve()
            .bodyToMono(String::class.java)
            .block()
    }
}