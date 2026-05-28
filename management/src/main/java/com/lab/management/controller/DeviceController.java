package com.lab.management.controller;

import com.lab.management.common.Result;
import com.lab.management.service.DeviceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/device")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping("/list")
    public Result list() {
        return Result.success(deviceService.listDevices());
    }

    @GetMapping("/monitor")
    public Result monitor() {
        return Result.success(deviceService.getMonitorSummary());
    }

    @GetMapping("/bindings")
    public Result bindings() {
        return Result.success(deviceService.listBindings());
    }

    @PostMapping("/binding/save")
    public Result saveBinding(@RequestBody Map<String, Object> params) {
        return Result.success(deviceService.saveBinding(params));
    }
}
