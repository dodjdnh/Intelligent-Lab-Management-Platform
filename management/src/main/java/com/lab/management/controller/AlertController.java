package com.lab.management.controller;

import com.lab.management.common.Result;
import com.lab.management.exception.BusinessException;
import com.lab.management.service.AlertService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/alert")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/list")
    public Result list() {
        return Result.success(alertService.listAlerts());
    }

    @PostMapping("/ack")
    public Result ack(@RequestBody Map<String, String> params) {
        String id = params.get("id");
        Map<String, Object> alert = alertService.acknowledgeAlert(id);
        if (alert == null) {
            throw new BusinessException("告警不存在");
        }
        return Result.success("告警已确认");
    }
}
