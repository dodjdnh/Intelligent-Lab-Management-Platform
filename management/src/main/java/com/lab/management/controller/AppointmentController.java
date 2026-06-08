package com.lab.management.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.lab.management.common.Result;
import com.lab.management.service.AppointmentService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/appointment")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // 1. 获取预约列表（修改版：只显示今天及未来的预约）
    @GetMapping("/list")
    public Result getList() {
        return Result.success(appointmentService.listActiveAppointments());
    }

    // 2. 新增预约
    @PostMapping("/add")
    public Result add(@RequestBody Map<String, String> params) {
        return Result.success(appointmentService.createAppointment(params));
    }

    // === 3. 新增接口：审核预约 (核心修改点) ===
    @PostMapping("/audit")
    public Result audit(@RequestBody Map<String, Object> params) {
        return Result.success(appointmentService.auditAppointment(params));
    }
}
