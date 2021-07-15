package com.jake.demo.config;

import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
import org.springframework.stereotype.Component;

import io.rsocket.core.RSocketServer;
import io.rsocket.core.Resume;

@Component
public class RSocketServerResumptionConfig implements RSocketServerCustomizer {

    @Override
    public void customize(RSocketServer rSocketServer) {
        rSocketServer.resume(new Resume());
    }
}
