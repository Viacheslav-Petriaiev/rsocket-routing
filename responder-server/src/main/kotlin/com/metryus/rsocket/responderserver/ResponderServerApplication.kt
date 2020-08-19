package com.metryus.rsocket.responderserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ResponderServerApplication

fun main(args: Array<String>) {
	runApplication<ResponderServerApplication>(*args)
}
