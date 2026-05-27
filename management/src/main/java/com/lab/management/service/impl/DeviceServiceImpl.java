package com.lab.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.management.common.RealtimeEventType;
import com.lab.management.entity.Consumable;
import com.lab.management.entity.DeviceBinding;
import com.lab.management.entity.DeviceDataLog;
import com.lab.management.entity.DeviceRegistry;
import com.lab.management.exception.BusinessException;
import com.lab.management.mapper.ConsumableMapper;
import com.lab.management.mapper.DeviceBindingMapper;
import com.lab.management.mapper.DeviceRegistryMapper;
import com.lab.management.service.AlertService;
import com.lab.management.service.DeviceService;
import com.lab.management.service.RealtimeEventService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRegistryMapper deviceRegistryMapper;
    private final DeviceBindingMapper deviceBindingMapper;
    private final ConsumableMapper consumableMapper;
    private final AlertService alertService;
    private final RealtimeEventService realtimeEventService;

    public DeviceServiceImpl(DeviceRegistryMapper deviceRegistryMapper,
                             DeviceBindingMapper deviceBindingMapper,
                             ConsumableMapper consumableMapper,
                             AlertService alertService,
                             RealtimeEventService realtimeEventService) {
        this.deviceRegistryMapper = deviceRegistryMapper;
        this.deviceBindingMapper = deviceBindingMapper;
        this.consumableMapper = consumableMapper;
        this.alertService = alertService;
        this.realtimeEventService = realtimeEventService;
    }

    @Override
    public void recordHeartbeat(DeviceDataLog data, String transport, String topic) {
        String deviceId = data.getDeviceId();
        DeviceRegistry device = deviceRegistryMapper.selectOne(
                new LambdaQueryWrapper<DeviceRegistry>().eq(DeviceRegistry::getDeviceId, deviceId).last("limit 1")
        );
        DeviceBinding binding = currentBinding(deviceId);

        if (device == null) {
            device = new DeviceRegistry();
            device.setDeviceId(deviceId);
            device.setCreatedAt(LocalDateTime.now());
        }

        device.setLastValue(data.getTemp());
        device.setLastSeen(data.getCreateTime());
        device.setStatus("在线");
        device.setTransport(transport);
        device.setArea(binding != null && notBlank(binding.getArea()) ? binding.getArea() : inferArea(deviceId));
        device.setUpdatedAt(LocalDateTime.now());

        if (device.getId() == null) {
            deviceRegistryMapper.insert(device);
        } else {
            deviceRegistryMapper.updateById(device);
        }

        realtimeEventService.publish(RealtimeEventType.DEVICE_STATUS_CHANGED, toMap(device, LocalDateTime.now()));

        if (data.getTemp() != null && data.getTemp().compareTo(BigDecimal.ZERO) < 0) {
            alertService.createAlert("设备数据异常", "高", "检测到异常传感值", "设备 " + deviceId + " 上报了负数重量值", deviceId);
        }
        if (data.getTemp() != null && data.getTemp().compareTo(new BigDecimal("10")) < 0) {
            alertService.createAlert("库存预警", "中", "设备关联库存偏低", "设备 " + deviceId + " 最近上报值较低，请检查耗材余量", deviceId);
        }
    }

    @Override
    public List<Map<String, Object>> listDevices() {
        List<DeviceRegistry> entities = deviceRegistryMapper.selectList(
                new LambdaQueryWrapper<DeviceRegistry>().orderByDesc(DeviceRegistry::getUpdatedAt)
        );
        List<Map<String, Object>> list = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (DeviceRegistry entity : entities) {
            Map<String, Object> copy = toMap(entity, now);
            if (entity.getLastSeen() != null && Duration.between(entity.getLastSeen(), now).toMinutes() > 10) {
                copy.put("status", "离线");
                if (!"离线".equals(entity.getStatus())) {
                    entity.setStatus("离线");
                    entity.setUpdatedAt(now);
                    deviceRegistryMapper.updateById(entity);
                }
            }
            list.add(copy);
        }
        list.sort(Comparator.comparing(item -> String.valueOf(item.get("lastSeen")), Comparator.reverseOrder()));
        return list;
    }

    @Override
    public Map<String, Object> getMonitorSummary() {
        List<Map<String, Object>> list = listDevices();
        Map<String, Object> summary = new HashMap<>();
        summary.put("devices", list);
        summary.put("onlineCount", list.stream().filter(item -> "在线".equals(item.get("status"))).count());
        summary.put("alertCount", list.stream().filter(item -> {
            Object value = item.get("lastValue");
            return value instanceof BigDecimal && ((BigDecimal) value).compareTo(new BigDecimal("10")) < 0;
        }).count());
        return summary;
    }

    @Override
    public Map<String, Object> saveBinding(Map<String, Object> params) {
        String deviceId = stringValue(params.get("deviceId"));
        Long consumableId = params.get("consumableId") == null || String.valueOf(params.get("consumableId")).isBlank()
                ? null : Long.valueOf(String.valueOf(params.get("consumableId")));
        String area = stringValue(params.get("area"));
        String topic = stringValue(params.get("topic"));

        if (!notBlank(deviceId)) {
            throw new BusinessException("设备编号不能为空");
        }

        Consumable consumable = null;
        if (consumableId != null) {
            consumable = consumableMapper.selectById(consumableId);
            if (consumable == null) {
                throw new BusinessException("绑定耗材不存在");
            }
        }

        DeviceBinding binding = currentBinding(deviceId);
        if (binding == null) {
            binding = new DeviceBinding();
            binding.setDeviceId(deviceId);
            binding.setCreatedAt(LocalDateTime.now());
        }

        binding.setConsumableId(consumableId);
        binding.setConsumableName(consumable == null ? null : consumable.getName());
        binding.setArea(area);
        binding.setTopic(topic);
        binding.setConversionMode(stringValue(params.get("conversionMode")) == null ? "DIRECT_VALUE" : stringValue(params.get("conversionMode")));
        binding.setUnitWeight(parseDecimal(params.get("unitWeight")));
        binding.setTareWeight(parseDecimal(params.get("tareWeight")));
        binding.setEnabledFlag(1);
        binding.setUpdatedAt(LocalDateTime.now());

        if (binding.getId() == null) {
            deviceBindingMapper.insert(binding);
        } else {
            deviceBindingMapper.updateById(binding);
        }

        DeviceRegistry registry = deviceRegistryMapper.selectOne(
                new LambdaQueryWrapper<DeviceRegistry>().eq(DeviceRegistry::getDeviceId, deviceId).last("limit 1")
        );
        if (registry != null) {
            if (notBlank(area)) {
                registry.setArea(area);
            }
            registry.setUpdatedAt(LocalDateTime.now());
            deviceRegistryMapper.updateById(registry);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("deviceId", deviceId);
        result.put("consumableId", consumableId);
        result.put("consumableName", consumable == null ? null : consumable.getName());
        result.put("area", area);
        result.put("topic", topic);
        result.put("conversionMode", binding.getConversionMode());
        result.put("unitWeight", binding.getUnitWeight());
        result.put("tareWeight", binding.getTareWeight());
        return result;
    }

    @Override
    public List<Map<String, Object>> listBindings() {
        List<DeviceBinding> bindings = deviceBindingMapper.selectList(
                new LambdaQueryWrapper<DeviceBinding>().eq(DeviceBinding::getEnabledFlag, 1).orderByDesc(DeviceBinding::getUpdatedAt)
        );
        List<Map<String, Object>> list = new ArrayList<>();
        for (DeviceBinding binding : bindings) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", binding.getId());
            item.put("deviceId", binding.getDeviceId());
            item.put("consumableId", binding.getConsumableId());
            item.put("consumableName", binding.getConsumableName());
            item.put("area", binding.getArea());
            item.put("topic", binding.getTopic());
            item.put("conversionMode", binding.getConversionMode());
            item.put("unitWeight", binding.getUnitWeight());
            item.put("tareWeight", binding.getTareWeight());
            item.put("updatedAt", binding.getUpdatedAt() == null ? null : binding.getUpdatedAt().toString().replace('T', ' '));
            list.add(item);
        }
        return list;
    }

    private String inferArea(String deviceId) {
        int mod = Math.abs(deviceId.hashCode()) % 4;
        if (mod == 0) return "东区";
        if (mod == 1) return "西区";
        if (mod == 2) return "南区";
        return "北区";
    }

    private Map<String, Object> toMap(DeviceRegistry device, LocalDateTime now) {
        Map<String, Object> map = new HashMap<>();
        DeviceBinding binding = currentBinding(device.getDeviceId());
        map.put("id", device.getId());
        map.put("deviceId", device.getDeviceId());
        map.put("area", binding != null && notBlank(binding.getArea()) ? binding.getArea() : device.getArea());
        map.put("transport", device.getTransport());
        map.put("lastValue", device.getLastValue());
        map.put("lastSeen", device.getLastSeen() == null ? null : device.getLastSeen().toString().replace('T', ' '));
        map.put("topic", binding == null ? null : binding.getTopic());
        map.put("consumableId", binding == null ? null : binding.getConsumableId());
        map.put("consumableName", binding == null ? null : binding.getConsumableName());
        map.put("conversionMode", binding == null ? null : binding.getConversionMode());
        map.put("unitWeight", binding == null ? null : binding.getUnitWeight());
        map.put("tareWeight", binding == null ? null : binding.getTareWeight());
        if (device.getLastSeen() != null && Duration.between(device.getLastSeen(), now).toMinutes() > 10) {
            map.put("status", "离线");
        } else {
            map.put("status", device.getStatus());
        }
        return map;
    }

    private DeviceBinding currentBinding(String deviceId) {
        return deviceBindingMapper.selectOne(
                new LambdaQueryWrapper<DeviceBinding>()
                        .eq(DeviceBinding::getDeviceId, deviceId)
                        .eq(DeviceBinding::getEnabledFlag, 1)
                        .last("limit 1")
        );
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private java.math.BigDecimal parseDecimal(Object value) {
        if (value == null) {
            return null;
        }
        String stringValue = String.valueOf(value).trim();
        if (stringValue.isEmpty()) {
            return null;
        }
        return new java.math.BigDecimal(stringValue);
    }
}
