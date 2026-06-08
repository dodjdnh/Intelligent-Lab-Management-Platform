package com.lab.management.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.lab.management.common.RealtimeEventType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.management.entity.Appointment;
import com.lab.management.entity.User;
import com.lab.management.exception.BusinessException;
import com.lab.management.mapper.AppointmentMapper;
import com.lab.management.mapper.UserMapper;
import com.lab.management.service.AppointmentService;
import com.lab.management.service.HomeStatsService;
import com.lab.management.service.RealtimeEventService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentMapper appointmentMapper;
    private final UserMapper userMapper;
    private final HomeStatsService homeStatsService;
    private final RealtimeEventService realtimeEventService;

    public AppointmentServiceImpl(AppointmentMapper appointmentMapper,
                                  UserMapper userMapper,
                                  HomeStatsService homeStatsService,
                                  RealtimeEventService realtimeEventService) {
        this.appointmentMapper = appointmentMapper;
        this.userMapper = userMapper;
        this.homeStatsService = homeStatsService;
        this.realtimeEventService = realtimeEventService;
    }

    @Override
    public List<Appointment> listActiveAppointments() {
        String today = LocalDate.now().toString();
        LambdaQueryWrapper<Appointment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(Appointment::getReserveDate, today);
        queryWrapper.orderByDesc(Appointment::getId);
        return appointmentMapper.selectList(queryWrapper);
    }

    @Override
    public String createAppointment(Map<String, String> params) {
        long currentUserId = StpUtil.getLoginIdAsLong();
        User currentUser = userMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException("用户不存在");
        }

        String labName = trim(params.get("labName"));
        String dateStr = trim(params.get("date"));
        String inputUserNo = trim(params.get("userNo"));
        String inputUserName = trim(params.get("user"));

        if (labName == null || dateStr == null || inputUserNo == null || inputUserName == null) {
            throw new BusinessException("请填写完整预约信息");
        }

        LocalDate reserveDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        if (!reserveDate.isAfter(LocalDate.now())) {
            throw new BusinessException("只能预约明天及以后的日期");
        }

        if (!currentUser.getUsername().equals(inputUserName)) {
            throw new BusinessException("申请人姓名必须与当前登录账号一致");
        }
        if (currentUser.getUserNo() == null || !currentUser.getUserNo().equals(inputUserNo)) {
            throw new BusinessException("学号/工号验证失败");
        }

        Long count = appointmentMapper.selectCount(
                new LambdaQueryWrapper<Appointment>()
                        .eq(Appointment::getUserId, currentUserId)
                        .in(Appointment::getStatus, "审核中", "已通过")
        );
        if (count >= 2) {
            throw new BusinessException("您已有 2 条进行中的预约，无法继续申请");
        }

        Appointment appointment = new Appointment();
        appointment.setUserId(currentUserId);
        appointment.setLabName(labName);
        appointment.setUserName(currentUser.getUsername());
        appointment.setUserNo(currentUser.getUserNo());
        appointment.setReserveDate(dateStr);
        appointment.setStatus("审核中");

        appointmentMapper.insert(appointment);
        notifyStatsChanged();
        return "预约申请已提交";
    }

    @Override
    public String auditAppointment(Map<String, Object> params) {
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

        Long id = Long.valueOf(idValue.toString());
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new BusinessException("预约记录不存在");
        }
        if (!"审核中".equals(appointment.getStatus())) {
            throw new BusinessException("该预约已审核，请勿重复操作");
        }

        appointment.setStatus(status);
        appointmentMapper.updateById(appointment);
        notifyStatsChanged();
        return "审核操作成功";
    }

    private boolean isAdmin(User user) {
        return user != null && "admin".equals(user.getRole());
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void notifyStatsChanged() {
        homeStatsService.evictCache();
        Map<String, Object> payload = new HashMap<>();
        payload.put("module", "appointment");
        realtimeEventService.publish(RealtimeEventType.APPOINTMENT_AUDITED, payload);
    }
}
