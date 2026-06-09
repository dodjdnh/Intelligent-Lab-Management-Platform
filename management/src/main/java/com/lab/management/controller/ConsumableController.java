package com.lab.management.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.lab.management.common.Result;
import com.lab.management.entity.Consumable;
import com.lab.management.service.ConsumableService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/consumable")
public class ConsumableController {

    private final ConsumableService consumableService;

    public ConsumableController(ConsumableService consumableService) {
        this.consumableService = consumableService;
    }

    // 1. 耗材库存列表 (所有人可见)
    @GetMapping("/list")
    public Result list() {
        return Result.success(consumableService.listConsumables());
    }

    // 2. 申请记录列表 (修改修复版)
    @GetMapping("/apply-list")
    public Result applyList() {
        return Result.success(consumableService.listApplyRecords());
    }

    @GetMapping("/change-log")
    public Result changeLog() {
        return Result.success(consumableService.listInventoryChangeLogs());
    }

    // 3. 提交申请 (学生操作)
    @PostMapping("/apply")
    public Result apply(@RequestBody Map<String, Object> params) {
        return Result.success(consumableService.createApply(params));
    }

    // 4. 审核申请 (管理员操作)
    @PostMapping("/audit")
    public Result audit(@RequestBody Map<String, Object> params) {
        return Result.success(consumableService.auditApply(params));
    }

    // 5. 入库 (管理员操作 - 修改版：支持自动合并库存)
    @PostMapping("/add")
    public Result add(@RequestBody Consumable consumable) {
        return Result.success(consumableService.addConsumable(consumable));
    }
}
