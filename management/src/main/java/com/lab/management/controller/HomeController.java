package com.lab.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.management.common.Result;
import com.lab.management.entity.Appointment;
import com.lab.management.entity.Consumable;
import com.lab.management.entity.ConsumableApply;
import com.lab.management.mapper.AppointmentMapper;
import com.lab.management.mapper.ConsumableApplyMapper;
import com.lab.management.mapper.ConsumableMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/home")
public class HomeController {

    @Autowired
    private AppointmentMapper appointmentMapper;
    @Autowired
    private ConsumableMapper consumableMapper;
    @Autowired
    private ConsumableApplyMapper applyMapper;

    @GetMapping("/stats")
    public Result getStats() {
        Map<String, Object> stats = new HashMap<>();

        // 1. 统计今日预约数
        // 逻辑：查询 sys_appointment 表中 reserve_date 等于今天的记录
        String today = LocalDate.now().toString();
        Long todayReserveCount = appointmentMapper.selectCount(
                new LambdaQueryWrapper<Appointment>().eq(Appointment::getReserveDate, today)
        );
        stats.put("todayReserve", todayReserveCount);

        // 2. 统计耗材预警数
        // 逻辑：查询 sys_consumable 表中 count < 20 的记录 (假设20是警戒线)
        Long warningCount = consumableMapper.selectCount(
                new LambdaQueryWrapper<Consumable>().lt(Consumable::getCount, 20)
        );
        stats.put("consumableWarning", warningCount);

        // 3. 统计待审核任务总数 (预约审核 + 耗材审核)
        // 逻辑：查询 status = '审核中' 的记录
        Long pendingAppointment = appointmentMapper.selectCount(
                new LambdaQueryWrapper<Appointment>().eq(Appointment::getStatus, "审核中")
        );
        Long pendingApply = applyMapper.selectCount(
                new LambdaQueryWrapper<ConsumableApply>().eq(ConsumableApply::getStatus, "审核中")
        );
        stats.put("pendingTask", pendingAppointment + pendingApply);

        // 4. (可选) 统计在线人数/总用户数
        // 暂时返回一个模拟值，或者你可以注入 UserMapper 去查总人数
        stats.put("onlineUser", 1); // 模拟值

        return Result.success(stats);
    }
}