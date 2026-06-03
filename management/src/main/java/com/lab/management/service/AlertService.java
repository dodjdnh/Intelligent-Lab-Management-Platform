package com.lab.management.service;

import java.util.List;
import java.util.Map;

public interface AlertService {

    List<Map<String, Object>> listAlerts();

    Map<String, Object> createAlert(String type, String level, String title, String message, String sourceId);

    Map<String, Object> acknowledgeAlert(String id);
}
