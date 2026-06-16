package com.lab.management.controller;

import com.lab.management.common.Result;
import com.lab.management.service.NacosConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/nacos")
public class NacosController {

    private final NacosConfigService nacosConfigService;

    public NacosController(NacosConfigService nacosConfigService) {
        this.nacosConfigService = nacosConfigService;
    }

    @GetMapping("/status")
    public Result status() {
        return Result.success(nacosConfigService.status());
    }

    @GetMapping("/config")
    public Result config() {
        return Result.success(nacosConfigService.fetchConfigSnapshot());
    }

    @GetMapping("/effective")
    public Result effective() {
        return Result.success(nacosConfigService.effectiveConfig());
    }

    @GetMapping("/compare")
    public Result compare() {
        return Result.success(nacosConfigService.compareConfig());
    }
}
