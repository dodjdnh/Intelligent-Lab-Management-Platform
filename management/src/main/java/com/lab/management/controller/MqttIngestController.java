package com.lab.management.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.lab.management.common.Result;
import com.lab.management.entity.DeviceDataLog;
import com.lab.management.exception.BusinessException;
import com.lab.management.service.DeviceService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/mqtt")
public class MqttIngestController {

    private final DeviceService deviceService;

    public MqttIngestController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @SaIgnore
    @PostMapping("/ingest")
    public Result ingest(@RequestBody Map<String, Object> payload) {
        Object deviceId = payload.get("deviceId");
        Object gravity = payload.get("gravity");
        if (deviceId == null || gravity == null) {
            throw new BusinessException("MQTT 上报参数不完整");
        }

        DeviceDataLog data = new DeviceDataLog();
        data.setDeviceId(String.valueOf(deviceId));
        data.setTemp(new BigDecimal(String.valueOf(gravity)));
        data.setCreateTime(LocalDateTime.now());
        data.setRawContent("MQTT " + deviceId + " -> " + gravity);

        deviceService.recordHeartbeat(data, "MQTT", payload.get("topic") == null ? null : String.valueOf(payload.get("topic")));
        return Result.success("MQTT 数据已接收");
    }
}
