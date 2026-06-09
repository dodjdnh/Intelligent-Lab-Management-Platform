package com.lab.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.management.entity.Appointment;
import com.lab.management.entity.Consumable;
import com.lab.management.entity.ConsumableApply;
import com.lab.management.mapper.AppointmentMapper;
import com.lab.management.mapper.ConsumableApplyMapper;
import com.lab.management.mapper.ConsumableMapper;
import com.lab.management.service.HomeStatsService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class HomeStatsServiceImpl implements HomeStatsService {

    private static final String CACHE_KEY = "lab:home:stats";

    private final AppointmentMapper appointmentMapper;
    private final ConsumableMapper consumableMapper;
    private final ConsumableApplyMapper applyMapper;
    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;

    public HomeStatsServiceImpl(AppointmentMapper appointmentMapper,
                                ConsumableMapper consumableMapper,
                                ConsumableApplyMapper applyMapper,
                                ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        this.appointmentMapper = appointmentMapper;
        this.consumableMapper = consumableMapper;
        this.applyMapper = applyMapper;
        this.redisTemplateProvider = redisTemplateProvider;
    }

    @Override
    public Map<String, Object> getStats() {
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            try {
                String cached = redisTemplate.opsForValue().get(CACHE_KEY);
                if (cached != null && !cached.isEmpty()) {
                    return parse(cached);
                }
            } catch (Exception ignored) {
            }
        }

        Map<String, Object> stats = buildStats();
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(CACHE_KEY, serialize(stats), Duration.ofSeconds(30));
            } catch (Exception ignored) {
            }
        }
        return stats;
    }

    @Override
    public void evictCache() {
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            try {
                redisTemplate.delete(CACHE_KEY);
            } catch (Exception ignored) {
            }
        }
    }

    private Map<String, Object> buildStats() {
        Map<String, Object> stats = new HashMap<>();
        String today = LocalDate.now().toString();
        Long todayReserveCount = appointmentMapper.selectCount(
                new LambdaQueryWrapper<Appointment>().eq(Appointment::getReserveDate, today)
        );
        Long warningCount = consumableMapper.selectCount(
                new LambdaQueryWrapper<Consumable>().lt(Consumable::getCount, 20)
        );
        Long pendingAppointment = appointmentMapper.selectCount(
                new LambdaQueryWrapper<Appointment>().eq(Appointment::getStatus, "审核中")
        );
        Long pendingApply = applyMapper.selectCount(
                new LambdaQueryWrapper<ConsumableApply>().eq(ConsumableApply::getStatus, "审核中")
        );
        stats.put("todayReserve", todayReserveCount);
        stats.put("consumableWarning", warningCount);
        stats.put("pendingTask", pendingAppointment + pendingApply);
        stats.put("onlineUser", 1);
        return stats;
    }

    private String serialize(Map<String, Object> stats) {
        return stats.get("todayReserve") + "," + stats.get("consumableWarning") + "," + stats.get("pendingTask") + "," + stats.get("onlineUser");
    }

    private Map<String, Object> parse(String cached) {
        String[] parts = cached.split(",");
        Map<String, Object> stats = new HashMap<>();
        stats.put("todayReserve", Long.parseLong(parts[0]));
        stats.put("consumableWarning", Long.parseLong(parts[1]));
        stats.put("pendingTask", Long.parseLong(parts[2]));
        stats.put("onlineUser", Integer.parseInt(parts[3]));
        return stats;
    }
}
