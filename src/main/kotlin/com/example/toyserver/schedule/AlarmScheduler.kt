package com.example.toyserver.schedule

import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.client.RestTemplate

//@Component
class AlarmScheduler(
    @Value("\${slack.webhook.url}") val slackWebhookUrl: String
) {

    var client: RestTemplate = RestTemplate()

    @Scheduled(fixedDelay = 5 * 1000, zone = "Asia/Seoul")
    fun leeumSchedule() {
        println(slackWebhookUrl)
    }
}