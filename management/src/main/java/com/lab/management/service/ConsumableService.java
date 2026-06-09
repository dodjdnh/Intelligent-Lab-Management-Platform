package com.lab.management.service;

import com.lab.management.entity.Consumable;
import com.lab.management.entity.ConsumableApply;

import java.util.List;
import java.util.Map;

public interface ConsumableService {

    List<Consumable> listConsumables();

    List<ConsumableApply> listApplyRecords();

    String createApply(Map<String, Object> params);

    String auditApply(Map<String, Object> params);

    String addConsumable(Consumable consumable);

    Map<String, Object> syncInventoryFromDevice(String deviceId, java.math.BigDecimal sensorValue);

    List<Map<String, Object>> listInventoryChangeLogs();
}
