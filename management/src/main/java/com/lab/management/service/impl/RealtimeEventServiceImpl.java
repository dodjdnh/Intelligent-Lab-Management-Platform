package com.lab.management.service.impl;

import com.lab.management.service.RealtimeEventService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RealtimeEventServiceImpl implements RealtimeEventService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter subscribe(String subscriberId) {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.put(subscriberId, emitter);
        emitter.onCompletion(() -> emitters.remove(subscriberId));
        emitter.onTimeout(() -> emitters.remove(subscriberId));
        emitter.onError(ex -> emitters.remove(subscriberId));
        try {
            emitter.send(SseEmitter.event().name("connected").data("connected"));
        } catch (IOException ex) {
            emitters.remove(subscriberId);
        }
        return emitter;
    }

    @Override
    public void publish(String eventType, Map<String, Object> payload) {
        emitters.forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name(eventType).data(payload));
            } catch (IOException ex) {
                emitters.remove(id);
            }
        });
    }
}
