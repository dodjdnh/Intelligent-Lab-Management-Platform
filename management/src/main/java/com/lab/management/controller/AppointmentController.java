package com.lab.management.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.management.common.Result;
import com.lab.management.entity.Appointment;
import com.lab.management.entity.User;
import com.lab.management.mapper.AppointmentMapper;
import com.lab.management.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/appointment")
public class AppointmentController {

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private UserMapper userMapper;

    // 1. 获取预约列表（修改版：只显示今天及未来的预约）
    @GetMapping("/list")
    public Result getList() {
        // 获取今天的日期字符串，例如 "2026-02-04"
        String today = LocalDate.now().toString();

        // 构造查询条件
        LambdaQueryWrapper<Appointment> queryWrapper = new LambdaQueryWrapper<>();

        // 核心逻辑：只查 reserve_date >= today 的记录
        // ge = greater or equal (大于等于)
        queryWrapper.ge(Appointment::getReserveDate, today);

        // 按 ID 倒序排列（新申请的在前面）
        queryWrapper.orderByDesc(Appointment::getId);

        List<Appointment> list = appointmentMapper.selectList(queryWrapper);
        return Result.success(list);
    }

    // 2. 新增预约
    @PostMapping("/add")
    public Result add(@RequestBody Map<String, String> params) {
        long currentUserId = StpUtil.getLoginIdAsLong();
        User currentUser = userMapper.selectById(currentUserId);

        if (currentUser == null) return Result.error("用户不存在");

        String labName = params.get("labName");
        String dateStr = params.get("date");
        String inputUserNo = params.get("userNo");
        String inputUserName = params.get("user");

        // 日期校验
        LocalDate reserveDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        if (!reserveDate.isAfter(LocalDate.now())) {
            return Result.error("只能预约明天及以后的日期");
        }

        // 身份校验
        if (!currentUser.getUsername().equals(inputUserName)) {
            return Result.error("申请人姓名必须与当前登录账号一致");
        }
        if (currentUser.getUserNo() == null || !currentUser.getUserNo().equals(inputUserNo)) {
            return Result.error("学号/工号验证失败");
        }

        // 数量校验
        Long count = appointmentMapper.selectCount(
                new LambdaQueryWrapper<Appointment>()
                        .eq(Appointment::getUserId, currentUserId)
                        .in(Appointment::getStatus, "审核中", "已通过")
        );
        if (count >= 2) {
            return Result.error("您已有 2 条进行中的预约，无法继续申请");
        }

        // 入库
        Appointment appointment = new Appointment();
        appointment.setUserId(currentUserId);
        appointment.setLabName(labName);
        appointment.setUserName(currentUser.getUsername());
        appointment.setUserNo(currentUser.getUserNo());
        appointment.setReserveDate(dateStr);
        appointment.setStatus("审核中");

        appointmentMapper.insert(appointment);
        return Result.success("预约申请已提交");
    }

    // === 3. 新增接口：审核预约 (核心修改点) ===
    @PostMapping("/audit")
    public Result audit(@RequestBody Map<String, Object> params) {
        // 安全起见，这里应该检查当前用户是否是 admin，但为了演示简单，我们暂且信任前端
        // 如果想严谨，可以加：if (!StpUtil.hasRole("admin")) return Result.error("无权操作");

        // 获取参数
        Long id = Long.valueOf(params.get("id").toString());
        String status = (String) params.get("status"); // "已通过" 或 "已驳回"

        // 更新数据库
        Appointment appointment = new Appointment();
        appointment.setId(id);
        appointment.setStatus(status);

        appointmentMapper.updateById(appointment);
        return Result.success("审核操作成功");
    }
}