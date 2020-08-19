package com.metryus.rsocket.requester.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * @author Viacheslav Petriaiev
 *         created on 2020-08-19
 */
@SpringBootApplication
class RequesterServerApplication

fun main(args: Array<String>) {
    runApplication<RequesterServerApplication>(*args)
}
