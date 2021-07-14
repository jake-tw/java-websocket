package com.jake.demo.controller;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PreDestroy;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;

import com.jake.demo.model.Message;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@Slf4j
public class RSocketController {

    public static final String SERVER = "Server";
    public static final String CLIENT = "Client";
    public static final String REQUEST = "Request";
    public static final String RESPONSE = "Response";
    public static final String STREAM = "Stream";
    public static final String CHANNEL = "Channel";

    private final List<RSocketRequester> clients = new ArrayList<>();

    @PreDestroy
    void shutdown() {
        clients.stream().forEach(requester -> requester.rsocket().dispose());
    }
    
    @ConnectMapping("shell-client")
    void connectShellClientAndAskForTelemetry(RSocketRequester requester, @Payload String client) {

        requester.rsocket().onClose().doFirst(() -> {
            // The Mono's doFirst() method gets called before any calls to subscribe()
            log.info("Client: {} CONNECTED.", client);
            clients.add(requester);
        }).doOnError(error -> {
            // There is a problem with the connection
            log.warn("Channel to client {} CLOSED", client);
        }).doFinally(consumer -> {
            // The Mono's doFinally() method is triggered when the RSocket connection has closed
            clients.remove(requester);
            log.info("Client {} DISCONNECTED", client);
        }).subscribe();

        // Send request-stream to get client's heart beat
        // requester.route("client-status")
        //          .data("OPEN")
        //          .retrieveFlux(String.class)
        //          .doOnNext(s -> log.info("Client: {} Free Memory: {}.", client, s))
        //          .subscribe();
    }

    @MessageMapping("request-response")
    public Mono<Message> requestResponse(final Message request) {
        log.info("Received request-response request: {}", request);
        
        return Mono.just(new Message(SERVER, RESPONSE));
    }

    @MessageMapping("fire-and-forget")
    public Mono<Void> fireAndForget(final Message request) {
        log.info("Received fire-and-forget request: {}", request);
        
        return Mono.empty();
    }

    @MessageMapping("request-stream")
    public Flux<Message> requestStream(final Message request) {
        log.info("Received stream-request: {}", request);

        return Flux.interval(Duration.ofSeconds(1))
                   .map(index -> new Message(SERVER, STREAM, index));
    }

    @MessageMapping("request-channel")
    public Flux<Message> requestChannel(final Flux<Duration> settings) {
        log.info("Received channel-request...");

        return settings
                .doOnNext(setting -> log.info("Channel frequency setting is {} second(s).", setting.getSeconds()))
                .filter(setting -> setting.getSeconds() > 0 && setting.getSeconds() < 5)
                .doOnCancel(() -> log.warn("The client cancelled the channel."))
                .flatMap(setting -> Flux.interval(setting).map(index -> new Message(SERVER, CHANNEL, setting.getSeconds())));
    }
}