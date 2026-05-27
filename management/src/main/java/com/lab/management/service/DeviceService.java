package com.lab.management.service;

import com.lab.management.entity.DeviceDataLog;

import java.util.List;
import java.util.Map;

public interface DeviceService {

    void recordHeartbeat(DeviceDataLog data, String transport, String topic);

    List<Map<String, Object>> listDevices();

    Map<String, Object> getMonitorSummary();

    Map<String, Object> saveBinding(Map<String, Object> params);

    List<Map<String, Object>> listBindings();
}
