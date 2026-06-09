package com.lab.management.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.management.common.RealtimeEventType;
import com.lab.management.entity.Consumable;
import com.lab.management.entity.ConsumableApply;
import com.lab.management.entity.DeviceBinding;
import com.lab.management.entity.InventoryChangeLog;
import com.lab.management.entity.User;
import com.lab.management.exception.BusinessException;
import com.lab.management.mapper.ConsumableApplyMapper;
import com.lab.management.mapper.ConsumableMapper;
import com.lab.management.mapper.DeviceBindingMapper;
import com.lab.management.mapper.InventoryChangeLogMapper;
import com.lab.management.mapper.UserMapper;
import com.lab.management.service.ConsumableService;
import com.lab.management.service.HomeStatsService;
import com.lab.management.service.RealtimeEventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConsumableServiceImpl implements ConsumableService {

    private final ConsumableMapper consumableMapper;
    private final ConsumableApplyMapper applyMapper;
    private final UserMapper userMapper;
    private final DeviceBindingMapper deviceBindingMapper;
    private final InventoryChangeLogMapper inventoryChangeLogMapper;
    private final HomeStatsService homeStatsService;
    private final RealtimeEventService realtimeEventService;

    public ConsumableServiceImpl(ConsumableMapper consumableMapper,
                                 ConsumableApplyMapper applyMapper,
                                 UserMapper userMapper,
                                 DeviceBindingMapper deviceBindingMapper,
                                 InventoryChangeLogMapper inventoryChangeLogMapper,
                                 HomeStatsService homeStatsService,
                                 RealtimeEventService realtimeEventService) {
        this.consumableMapper = consumableMapper;
        this.applyMapper = applyMapper;
        this.userMapper = userMapper;
        this.deviceBindingMapper = deviceBindingMapper;
        this.inventoryChangeLogMapper = inventoryChangeLogMapper;
        this.homeStatsService = homeStatsService;
        this.realtimeEventService = realtimeEventService;
    }

    @Override
    public List<Consumable> listConsumables() {
        return consumableMapper.selectList(null);
    }

    @Override
    public List<ConsumableApply> listApplyRecords() {
        LambdaQueryWrapper<ConsumableApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ConsumableApply::getId);

        long currentUserId = StpUtil.getLoginIdAsLong();
        User currentUser = userMapper.selectById(currentUserId);
        if (!isAdmin(currentUser)) {
            wrapper.eq(ConsumableApply::getUserId, currentUserId);
        }
        return applyMapper.selectList(wrapper);
    }

    @Override
    public List<Map<String, Object>> listInventoryChangeLogs() {
        List<InventoryChangeLog> logs = inventoryChangeLogMapper.selectList(
                new LambdaQueryWrapper<InventoryChangeLog>().orderByDesc(InventoryChangeLog::getCreatedAt).last("limit 50")
        );
        List<Map<String, Object>> result = new ArrayList<>();
        for (InventoryChangeLog log : logs) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", log.getId());
            item.put("consumableId", log.getConsumableId());
            item.put("consumableName", log.getConsumableName());
            item.put("deviceId", log.getDeviceId());
            item.put("changeType", log.getChangeType());
            item.put("beforeCount", log.getBeforeCount());
            item.put("afterCount", log.getAfterCount());
            item.put("deltaCount", log.getDeltaCount());
            item.put("sensorValue", log.getSensorValue());
            item.put("conversionMode", log.getConversionMode());
            item.put("operator", log.getOperator());
            item.put("remark", log.getRemark());
            item.put("createdAt", log.getCreatedAt() == null ? null : log.getCreatedAt().toString().replace('T', ' '));
            result.add(item);
        }
        return result;
    }

    @Override
    public String createApply(Map<String, Object> params) {
        Object idValue = params.get("id");
        Object numValue = params.get("num");
        if (idValue == null || numValue == null) {
            throw new BusinessException("申请参数不完整");
        }

        Long consumableId = Long.valueOf(idValue.toString());
        Integer num = Integer.valueOf(numValue.toString());
        if (num <= 0) {
            throw new BusinessException("申请数量必须大于 0");
        }

        Consumable item = consumableMapper.selectById(consumableId);
        if (item == null) {
            throw new BusinessException("物品不存在");
        }
        if (item.getCount() == null || item.getCount() < num) {
            throw new BusinessException("当前库存不足，无法申请");
        }

        User user = userMapper.selectById(StpUtil.getLoginIdAsLong());
        if (user == null) {
            throw new BusinessException("当前用户不存在");
        }

        ConsumableApply apply = new ConsumableApply();
        apply.setConsumableId(consumableId);
        apply.setConsumableName(item.getName());
        apply.setUserId(user.getId());
        apply.setUserName(user.getUsername());
        apply.setUserNo(user.getUserNo());
        apply.setNum(num);
        apply.setStatus("审核中");

        applyMapper.insert(apply);
        notifyInventoryChanged("apply.created", apply.getConsumableId());
        return "申请已提交，等待管理员审核";
    }

    @Override
    @Transactional
    public String auditApply(Map<String, Object> params) {
        User currentUser = userMapper.selectById(StpUtil.getLoginIdAsLong());
        if (!isAdmin(currentUser)) {
            throw new BusinessException("无权操作");
        }

        Object idValue = params.get("id");
        String status = params.get("status") == null ? null : params.get("status").toString();
        if (idValue == null || status == null) {
            throw new BusinessException("审核参数不完整");
        }
        if (!"已通过".equals(status) && !"已驳回".equals(status)) {
            throw new BusinessException("审核状态非法");
        }

        Long applyId = Long.valueOf(idValue.toString());
        ConsumableApply apply = applyMapper.selectById(applyId);
        if (apply == null) {
            throw new BusinessException("申请记录不存在");
        }
        if (!"审核中".equals(apply.getStatus())) {
            throw new BusinessException("该申请已审核，请勿重复操作");
        }

        Consumable consumable = consumableMapper.selectById(apply.getConsumableId());
        if (consumable == null) {
            throw new BusinessException("对应耗材不存在");
        }

        if ("已通过".equals(status)) {
            if (consumable.getCount() == null || consumable.getCount() < apply.getNum()) {
                throw new BusinessException("当前库存不足，无法通过申请");
            }
            int beforeCount = safeCount(consumable.getCount());
            consumable.setCount(consumable.getCount() - apply.getNum());
            consumableMapper.updateById(consumable);
            recordInventoryChange(consumable, null, "apply.approved", beforeCount, consumable.getCount(), null,
                    null, currentUser.getUsername(), "领用审核通过，扣减 " + apply.getNum());
        }

        apply.setStatus(status);
        applyMapper.updateById(apply);
        notifyInventoryChanged("apply.audited", apply.getConsumableId());
        return "审核完成";
    }

    @Override
    public String addConsumable(Consumable consumable) {
        User currentUser = userMapper.selectById(StpUtil.getLoginIdAsLong());
        if (!isAdmin(currentUser)) {
            throw new BusinessException("无权操作");
        }
        if (consumable == null || isBlank(consumable.getName()) || isBlank(consumable.getSpecification())
                || consumable.getCount() == null || consumable.getCount() <= 0 || isBlank(consumable.getUnit())) {
            throw new BusinessException("请填写完整入库信息");
        }

        LambdaQueryWrapper<Consumable> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Consumable::getName, consumable.getName())
                .eq(Consumable::getSpecification, consumable.getSpecification());

        Consumable existingItem = consumableMapper.selectOne(wrapper);
        if (existingItem != null) {
            int beforeCount = safeCount(existingItem.getCount());
            int newCount = beforeCount + consumable.getCount();
            existingItem.setCount(newCount);
            consumableMapper.updateById(existingItem);
            recordInventoryChange(existingItem, null, "consumable.merged", beforeCount, newCount, null,
                    null, currentUser.getUsername(), "人工入库合并 +" + consumable.getCount());
            notifyInventoryChanged("consumable.merged", existingItem.getId());
            return "入库成功，库存已合并";
        }

        consumableMapper.insert(consumable);
        recordInventoryChange(consumable, null, "consumable.created", 0, safeCount(consumable.getCount()), null,
                null, currentUser.getUsername(), "新品首次入库 +" + consumable.getCount());
        notifyInventoryChanged("consumable.created", consumable.getId());
        return "新品入库成功";
    }

    @Override
    @Transactional
    public Map<String, Object> syncInventoryFromDevice(String deviceId, java.math.BigDecimal sensorValue) {
        DeviceBinding binding = deviceBindingMapper.selectOne(
                new LambdaQueryWrapper<DeviceBinding>()
                        .eq(DeviceBinding::getDeviceId, deviceId)
                        .eq(DeviceBinding::getEnabledFlag, 1)
                        .last("limit 1")
        );
        if (binding == null || binding.getConsumableId() == null) {
            return null;
        }

        Consumable consumable = consumableMapper.selectById(binding.getConsumableId());
        if (consumable == null) {
            return null;
        }

        int beforeCount = safeCount(consumable.getCount());
        int newCount = calculateCount(binding, sensorValue);
        consumable.setCount(newCount);
        consumableMapper.updateById(consumable);
        recordInventoryChange(consumable, deviceId, "device.synced", beforeCount, newCount, sensorValue,
                binding.getConversionMode(), "iot-ingest", buildDeviceRemark(binding, sensorValue));
        notifyInventoryChanged("device.synced", consumable.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("consumableId", consumable.getId());
        result.put("consumableName", consumable.getName());
        result.put("beforeCount", beforeCount);
        result.put("count", newCount);
        result.put("deltaCount", newCount - beforeCount);
        result.put("mode", binding.getConversionMode());
        result.put("deviceId", deviceId);
        return result;
    }

    private boolean isAdmin(User user) {
        return user != null && "admin".equals(user.getRole());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private int calculateCount(DeviceBinding binding, java.math.BigDecimal sensorValue) {
        BigDecimal safeSensorValue = sensorValue == null ? BigDecimal.ZERO : sensorValue;
        String mode = binding.getConversionMode();
        if ("WEIGHT_TO_COUNT".equals(mode)
                && binding.getUnitWeight() != null
                && binding.getUnitWeight().compareTo(java.math.BigDecimal.ZERO) > 0) {
            java.math.BigDecimal tare = binding.getTareWeight() == null ? java.math.BigDecimal.ZERO : binding.getTareWeight();
            java.math.BigDecimal netWeight = safeSensorValue.subtract(tare);
            if (netWeight.compareTo(java.math.BigDecimal.ZERO) < 0) {
                netWeight = java.math.BigDecimal.ZERO;
            }
            return netWeight.divide(binding.getUnitWeight(), 0, java.math.RoundingMode.DOWN).intValue();
        }
        return safeSensorValue.max(java.math.BigDecimal.ZERO).intValue();
    }

    private void recordInventoryChange(Consumable consumable,
                                       String deviceId,
                                       String changeType,
                                       int beforeCount,
                                       int afterCount,
                                       BigDecimal sensorValue,
                                       String conversionMode,
                                       String operator,
                                       String remark) {
        InventoryChangeLog log = new InventoryChangeLog();
        log.setConsumableId(consumable.getId());
        log.setConsumableName(consumable.getName());
        log.setDeviceId(deviceId);
        log.setChangeType(changeType);
        log.setBeforeCount(beforeCount);
        log.setAfterCount(afterCount);
        log.setDeltaCount(afterCount - beforeCount);
        log.setSensorValue(sensorValue);
        log.setConversionMode(conversionMode);
        log.setOperator(operator);
        log.setRemark(remark);
        log.setCreatedAt(LocalDateTime.now());
        inventoryChangeLogMapper.insert(log);
    }

    private String buildDeviceRemark(DeviceBinding binding, BigDecimal sensorValue) {
        if ("WEIGHT_TO_COUNT".equals(binding.getConversionMode())) {
            return "重量换算同步，原始值=" + sensorValue + ", 单件重量=" + binding.getUnitWeight() + ", 皮重=" + binding.getTareWeight();
        }
        return "设备直接值同步，原始值=" + sensorValue;
    }

    private int safeCount(Integer count) {
        return count == null ? 0 : count;
    }

    private void notifyInventoryChanged(String action, Long consumableId) {
        homeStatsService.evictCache();
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", action);
        payload.put("consumableId", consumableId);
        realtimeEventService.publish(RealtimeEventType.INVENTORY_CHANGED, payload);
    }
}
