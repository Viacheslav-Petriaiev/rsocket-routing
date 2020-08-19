package com.metryus.rsocket.requester.server.api.rest

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
class UserController(private val rSocketRequester: RSocketRequester) {

    @GetMapping("/users/{userId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getUserById(@PathVariable userId: String): Mono<Map<*, *>> =
            rSocketRequester
                    .route("getUserById")
                    .data(userId)
                    .retrieveMono(Map::class.java)
                    .doOnNext {
                        println("!!!!!!! $it")
                    }
                    .doOnError { error ->
                        error.printStackTrace()
                    }
}