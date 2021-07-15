package com.jake.demo;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.rsocket.context.LocalRSocketServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;

import com.jake.demo.controller.RSocketController;
import com.jake.demo.model.Message;

import io.rsocket.SocketAcceptor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@Slf4j
class SpringBootRSocketDemoApplicationTests {

    private static RSocketRequester requester;

    @BeforeAll
    public static void setupOnce(
            @LocalRSocketServerPort Integer port,
            @Autowired RSocketRequester.Builder builder, 
            @Autowired RSocketStrategies strategies) {
        
        SocketAcceptor responder = RSocketMessageHandler.responder(strategies, new ClientHandler());

        requester = builder
                .setupRoute("shell-client")
                .setupData(UUID.randomUUID().toString())
                .rsocketConnector(connector -> connector.acceptor(responder))
                .tcp("127.0.0.1", port);
    }

    @AfterAll
    public static void tearDownOnce() throws InterruptedException {
        // make sure to print the server's log before dispose
        TimeUnit.SECONDS.sleep(1);
        requester.dispose();
    }

    private static class ClientHandler {
        @MessageMapping("client-status")
        public Flux<String> statusUpdate(String status) {
            log.info("Connection {}", status);
            return Flux.interval(Duration.ofSeconds(5)).map(index -> String.valueOf(Runtime.getRuntime().freeMemory()));
        }
    }

    @Test
    public void testRequestResponse() {
        Mono<Message> result = requester.route("request-response")
                .data(new Message(RSocketController.CLIENT, RSocketController.REQUEST)).retrieveMono(Message.class);

        StepVerifier.create(result).consumeNextWith(message -> {
            log.info("Assert request response: {}", message);
            Assertions.assertEquals(RSocketController.SERVER, message.getOrigin(), "origin");
            Assertions.assertEquals(RSocketController.RESPONSE, message.getInteraction(), "interaction");
            Assertions.assertEquals(0, message.getIndex(), "index");
        }).verifyComplete();
    }

    @Test
    public void testFireAndForget() {
        Mono<Void> result = requester.route("fire-and-forget")
                .data(new Message(RSocketController.CLIENT, RSocketController.REQUEST))
                .send();
        
        StepVerifier.create(result).verifyComplete();
    }

    @Test
    public void testRequestStream() {

        Flux<Message> result = requester.route("request-stream")
                .data(new Message(RSocketController.CLIENT, RSocketController.REQUEST))
                .retrieveFlux(Message.class)
                .take(10);

        AtomicInteger index = new AtomicInteger(0);
        StepVerifier.create(result).thenConsumeWhile(x -> true, message -> {
            log.info("Assert request stream: {}", message);
            Assertions.assertEquals(RSocketController.SERVER, message.getOrigin(), "origin");
            Assertions.assertEquals(RSocketController.STREAM, message.getInteraction(), "interaction");
            Assertions.assertEquals(index.getAndIncrement(), message.getIndex(), "index");
        }).verifyComplete();
    }

    @Test
    public void testRequestChannel() throws InterruptedException {
        Flux<Message> result = requester.route("request-channel")
                .data(Flux.interval(Duration.ofSeconds(1)).take(10))
                .retrieveFlux(Message.class)
                .take(20);
        
        StepVerifier.create(result).thenConsumeWhile(x -> true, message -> {
            log.info("Assert request channel: {}", message);
            Assertions.assertEquals(RSocketController.SERVER, message.getOrigin(), "origin");
            Assertions.assertEquals(RSocketController.CHANNEL, message.getInteraction(), "interaction");
        }).verifyComplete();
    }
}
