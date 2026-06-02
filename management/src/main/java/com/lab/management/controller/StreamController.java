package com.lab.management.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.lab.management.exception.BusinessException;
import com.lab.management.service.RealtimeEventService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/stream")
public class StreamController {

    private final RealtimeEventService realtimeEventService;

    public StreamController(RealtimeEventService realtimeEventService) {
        this.realtimeEventService = realtimeEventService;
    }

    @GetMapping("/events")
    public SseEmitter subscribe(@RequestParam("satoken") String token) {
        Object loginId = StpUtil.getLoginIdByToken(token);
        if (loginId == null) {
            throw new BusinessException("登录状态已失效");
        }
        return realtimeEventService.subscribe(String.valueOf(loginId));
    }
}
