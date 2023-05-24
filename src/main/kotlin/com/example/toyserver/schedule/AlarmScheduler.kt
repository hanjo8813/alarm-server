package com.example.toyserver.schedule

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class AlarmScheduler(
    @Value("\${slack.webhook.url}") val slackWebhookUrl: String
) {

    var client: RestTemplate = RestTemplate()

    @PostConstruct
    fun temp() {
        println(slackWebhookUrl)
    }

//    @Scheduled(fixedDelay = 60 * 1000, zone = "Asia/Seoul")
//    fun leeumSchedule() {
//        println(slackWebhookUrl)
//    }
}