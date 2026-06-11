package com.lab.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.lab.management.config.properties.IotProperties;
import com.lab.management.entity.DeviceDataLog;
import com.lab.management.exception.BusinessException;
import com.lab.management.mapper.DeviceDataLogMapper;
import com.lab.management.service.ConsumableService;
import com.lab.management.service.DeviceService;
import cn.dev33.satoken.annotation.SaIgnore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/hardware")
public class SensorDataController {

    @Autowired
    private DeviceDataLogMapper deviceDataLogMapper;

    @Autowired
    private IotProperties iotProperties;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private ConsumableService consumableService;

    @SaIgnore
    @PostMapping("/upload")
    @Transactional // 保证两个表要么同时成功，要么同时失败
    public String receiveData(@RequestBody DeviceDataLog data) {
        if (Boolean.FALSE.equals(iotProperties.getUploadEnabled())) {
            throw new BusinessException("IoT 数据上报已关闭");
        }
        if (data == null || data.getDeviceId() == null || data.getDeviceId().trim().isEmpty()) {
            throw new BusinessException("设备标识不能为空");
        }
        if (data.getTemp() == null) {
            throw new BusinessException("传感器数值不能为空");
        }

        // 1. 设置基础信息
        data.setCreateTime(LocalDateTime.now());
        data.setRawContent("Sensor RFID: " + data.getDeviceId() + " | Val: " + data.getTemp());
        deviceService.recordHeartbeat(data, "HTTP", null);

        // 2. 更新或插入传感器状态日志 (device_data_logs)
        UpdateWrapper<DeviceDataLog> logUw = new UpdateWrapper<>();
        logUw.eq("device_id", data.getDeviceId());

        if (deviceDataLogMapper.selectCount(new QueryWrapper<DeviceDataLog>().eq("device_id", data.getDeviceId())) > 0) {
            deviceDataLogMapper.update(data, logUw);
        } else {
            deviceDataLogMapper.insert(data);
        }

        // 3. 【核心业务】按绑定规则同步库存
        if (data.getDeviceId() != null) {
            java.util.Map<String, Object> syncResult = consumableService.syncInventoryFromDevice(data.getDeviceId(), data.getTemp());
            if (syncResult != null) {
                return "Update Success: " + syncResult.get("consumableName") + " stock updated to " + syncResult.get("count");
            }
        }

        return "Log Updated (No RFID match found in sys_consumable)";
    }
}
