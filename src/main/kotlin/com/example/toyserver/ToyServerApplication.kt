package com.example.toyserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class ToyServerApplication

fun main(args: Array<String>) {
    runApplication<ToyServerApplication>(*args)
}
