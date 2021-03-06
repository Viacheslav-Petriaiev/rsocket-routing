package com.metryus.rsocket.broker.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import reactor.core.publisher.Hooks

/**
 * @author Viacheslav Petriaiev
 *         created on 2020-08-19
 */
@SpringBootApplication
class RSocketBrokerServerApplication

fun main(args: Array<String>) {
    Hooks.onOperatorDebug()
    runApplication<RSocketBrokerServerApplication>(*args)
}
