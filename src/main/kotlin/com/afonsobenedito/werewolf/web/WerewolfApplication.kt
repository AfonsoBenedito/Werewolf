package com.afonsobenedito.werewolf.web

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class WerewolfApplication

fun main(args: Array<String>) {
    runApplication<WerewolfApplication>(*args)
}
