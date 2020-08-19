package com.metryus.rsocket.broker.responder.server.api.rsocket.controller

import com.metryus.rsocket.broker.responder.server.domain.User
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 * @author Viacheslav Petriaiev
 *         created on 2020-08-19
 */
@Controller
class UserController {

    @MessageMapping("getUserById")
    fun getUserById(@Payload userId: String): Mono<User> {
        return User(id = userId, name = "testUserName").toMono()
    }
}