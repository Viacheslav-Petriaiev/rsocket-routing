package com.metryus.rsocket.requester.server.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.rsocket.core.RSocketConnector
import io.rsocket.loadbalance.LoadbalanceRSocketClient
import io.rsocket.loadbalance.LoadbalanceRSocketSource
import io.rsocket.transport.netty.client.WebsocketClientTransport
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.util.ClassUtils
import org.springframework.web.util.pattern.PathPatternRouteMatcher
import reactor.core.publisher.Flux
import reactor.util.retry.Retry
import java.time.Duration

/**
 * @author Viacheslav Petriaiev
 *         created on 2020-08-19
 */
@Configuration
class RSocketConfig {

    @Bean
    fun rSocketStrategies(customizers: ObjectProvider<RSocketStrategiesCustomizer>): RSocketStrategies {
        val builder = RSocketStrategies.builder()
        if (ClassUtils.isPresent("org.springframework.web.util.pattern.PathPatternRouteMatcher", null)) {
            builder.routeMatcher(PathPatternRouteMatcher())
        }
        customizers.orderedStream().forEach { customizer: RSocketStrategiesCustomizer -> customizer.customize(builder) }
        return builder.build()
    }

    @Bean
    fun jacksonJsonRSocketStrategyCustomizer(objectMapper: ObjectMapper): RSocketStrategiesCustomizer {
        val supportedTypes = arrayOf(MediaType.APPLICATION_JSON,
                MediaType("application", "*+json"))

        return RSocketStrategiesCustomizer { strategy: RSocketStrategies.Builder ->
            strategy.decoder(Jackson2JsonDecoder(objectMapper, *supportedTypes))
            strategy.encoder(Jackson2JsonEncoder(objectMapper, *supportedTypes))
        }
    }

    @Bean
    fun rSocketRequester(discoveryClient: ReactiveDiscoveryClient,
                         rSocketStrategies: RSocketStrategies): RSocketRequester {
        return Flux
                .interval(Duration.ZERO, Duration.ofMillis(5000))
                .onBackpressureDrop()
                .concatMap {
                    discoveryClient.services
                            .flatMap { serviceId ->
                                discoveryClient.getInstances("responder-server")
                            }
                            .map { serviceInstance ->
//                                RSocketConnector
//                                        .create()
//                                        .dataMimeType(MediaType.APPLICATION_JSON.toString())
//                                        .metadataMimeType(MediaType.APPLICATION_JSON.toString())
//                                        .reconnect(Retry.backoff(Long.MAX_VALUE, Duration.ofMillis(100))
//                                                .maxBackoff(Duration.ofSeconds(5)))
//                                        .connect(WebsocketClientTransport.create(serviceInstance.uri
//                                                .resolve("/rsocket")))
//                                        .doOnNext { rsocket ->
//                                            println("!!!!!!!!!!! $rsocket")
//                                        }
//                                        .doOnError { error ->
//                                            error.printStackTrace()
//                                        }
//                                        .block()

                                val rSocketMono = RSocketConnector
                                        .create()
                                        .dataMimeType(MediaType.APPLICATION_JSON.toString())
                                        .metadataMimeType(MediaType.APPLICATION_JSON.toString())
                                        .reconnect(Retry.backoff(Long.MAX_VALUE, Duration.ofMillis(100))
                                                .maxBackoff(Duration.ofSeconds(5)))
                                        .connect(WebsocketClientTransport.create(serviceInstance.uri
                                                .resolve("/rsocket")))
                                        .doOnNext { rsocket ->
                                            println("!!!!!!!!!!! $rsocket")
                                        }
                                        .doOnError { error ->
                                            error.printStackTrace()
                                        }
                                LoadbalanceRSocketSource.from(serviceInstance.instanceId, rSocketMono)
                            }.collectList()

                }
                .doOnError { error ->
                    error.printStackTrace()
                }
                .let { loadBalanceRSocketSources ->
                    LoadbalanceRSocketClient.create(loadBalanceRSocketSources)
                }
                .let { rSocketClient ->
                    RSocketRequester.wrap(
                            rSocketClient.source().block()!!,
                            MediaType.APPLICATION_JSON,
                            MediaType.APPLICATION_JSON,
                            rSocketStrategies
                    )
                }
    }

}