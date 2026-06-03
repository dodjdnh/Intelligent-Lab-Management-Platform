package com.lab.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.management.common.RealtimeEventType;
import com.lab.management.entity.AlertEvent;
import com.lab.management.mapper.AlertEventMapper;
import com.lab.management.service.AlertService;
import com.lab.management.service.RealtimeEventService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AlertServiceImpl implements AlertService {

    private final AlertEventMapper alertEventMapper;
    private final RealtimeEventService realtimeEventService;

    public AlertServiceImpl(AlertEventMapper alertEventMapper, RealtimeEventService realtimeEventService) {
        this.alertEventMapper = alertEventMapper;
        this.realtimeEventService = realtimeEventService;
    }

    @Override
    public List<Map<String, Object>> listAlerts() {
        List<AlertEvent> entities = alertEventMapper.selectList(
                new LambdaQueryWrapper<AlertEvent>().orderByDesc(AlertEvent::getCreatedAt)
        );
        List<Map<String, Object>> list = new ArrayList<>();
        for (AlertEvent entity : entities) {
            list.add(toMap(entity));
        }
        list.sort(Comparator.comparing(item -> String.valueOf(item.get("createdAt")), Comparator.reverseOrder()));
        return list;
    }

    @Override
    public Map<String, Object> createAlert(String type, String level, String title, String message, String sourceId) {
        AlertEvent alert = new AlertEvent();
        alert.setType(type);
        alert.setLevel(level);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setSourceId(sourceId);
        alert.setStatus("未确认");
        alert.setCreatedAt(LocalDateTime.now());
        alertEventMapper.insert(alert);
        Map<String, Object> result = toMap(alert);
        realtimeEventService.publish(RealtimeEventType.ALERT_CREATED, result);
        return result;
    }

    @Override
    public Map<String, Object> acknowledgeAlert(String id) {
        AlertEvent alert = alertEventMapper.selectById(id);
        if (alert == null) {
            return null;
        }
        alert.setStatus("已确认");
        alert.setAcknowledgedAt(LocalDateTime.now());
        alertEventMapper.updateById(alert);
        Map<String, Object> result = toMap(alert);
        realtimeEventService.publish(RealtimeEventType.ALERT_CREATED, result);
        return result;
    }

    private Map<String, Object> toMap(AlertEvent alert) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", alert.getId());
        map.put("type", alert.getType());
        map.put("level", alert.getLevel());
        map.put("title", alert.getTitle());
        map.put("message", alert.getMessage());
        map.put("sourceId", alert.getSourceId());
        map.put("status", alert.getStatus());
        map.put("createdAt", alert.getCreatedAt() == null ? null : alert.getCreatedAt().toString().replace('T', ' '));
        map.put("acknowledgedAt", alert.getAcknowledgedAt() == null ? null : alert.getAcknowledgedAt().toString().replace('T', ' '));
        return map;
    }
}
