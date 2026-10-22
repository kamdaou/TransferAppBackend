package com.ktconsulting.transfer_app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class TransferAppApplication

fun main(args: Array<String>) {
    runApplication<TransferAppApplication>(*args)
}
