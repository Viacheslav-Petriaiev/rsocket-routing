package com.metryus.rsocket.broker.requester.server.api.rest.controller

import io.rsocket.routing.client.spring.SpringRoutingClient
import org.springframework.context.ApplicationListener
import org.springframework.context.PayloadApplicationEvent
import org.springframework.http.MediaType
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * @author Viacheslav Petriaiev
 *         created on 2020-08-19
 */
@RestController
class UserController(private val routingClient: SpringRoutingClient) : ApplicationListener<PayloadApplicationEvent<RSocketRequester>> {

    private lateinit var rSocketRequester: RSocketRequester

    override fun onApplicationEvent(event: PayloadApplicationEvent<RSocketRequester>) {
        rSocketRequester = event.payload
    }

    @GetMapping("/users/{userId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getUserById(@PathVariable userId: String): Mono<Map<*, *>> =
            rSocketRequester
                    .route("getUserById")
                    .metadata(routingClient.address("rsocket-broker-aware-responder-server"))
                    .data(userId)
                    .retrieveMono(Map::class.java)
                    .doOnNext {
                        println("!!!!!!! $it")
                    }
                    .doOnError { error ->
                        error.printStackTrace()
                    }
}