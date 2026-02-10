package com.lab.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.lab.management.entity.DeviceDataLog;
import com.lab.management.entity.Consumable; // 对应 sys_consumable 的实体类
import com.lab.management.mapper.DeviceDataLogMapper;
import com.lab.management.mapper.ConsumableMapper; // 对应 sys_consumable 的 Mapper
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
    private ConsumableMapper consumableMapper;

    @SaIgnore
    @PostMapping("/upload")
    @Transactional // 保证两个表要么同时成功，要么同时失败
    public String receiveData(@RequestBody DeviceDataLog data) {
        // 1. 设置基础信息
        data.setCreateTime(LocalDateTime.now());
        data.setRawContent("Sensor RFID: " + data.getDeviceId() + " | Val: " + data.getTemp());

        // 2. 更新或插入传感器状态日志 (device_data_logs)
        UpdateWrapper<DeviceDataLog> logUw = new UpdateWrapper<>();
        logUw.eq("device_id", data.getDeviceId());

        if (deviceDataLogMapper.selectCount(new QueryWrapper<DeviceDataLog>().eq("device_id", data.getDeviceId())) > 0) {
            deviceDataLogMapper.update(data, logUw);
        } else {
            deviceDataLogMapper.insert(data);
        }

        // 3. 【核心业务】同步更新耗材库存 (sys_consumable)
        if (data.getDeviceId() != null) {
            // 根据 RFID 查找对应的物品
            QueryWrapper<Consumable> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("rfid", data.getDeviceId());
            Consumable consumable = consumableMapper.selectOne(queryWrapper);

            if (consumable != null) {
                // 将传感器的数值（重力）同步到库存 count 字段
                // 转换逻辑：直接将 BigDecimal 转为 Integer 存入 count
                consumable.setCount(data.getTemp().intValue());
                consumableMapper.updateById(consumable);
                return "Update Success: " + consumable.getName() + " stock updated to " + consumable.getCount();
            }
        }

        return "Log Updated (No RFID match found in sys_consumable)";
    }
}