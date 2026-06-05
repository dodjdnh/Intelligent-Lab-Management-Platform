package com.lab.management.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

public interface RealtimeEventService {

    SseEmitter subscribe(String subscriberId);

    void publish(String eventType, Map<String, Object> payload);
}
