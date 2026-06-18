package com.lab.management.service.agent.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.management.service.AlertService;
import com.lab.management.service.AppointmentService;
import com.lab.management.service.ConsumableService;
import com.lab.management.service.DeviceService;
import com.lab.management.service.FileStorageService;
import com.lab.management.service.NacosConfigService;
import com.lab.management.entity.Appointment;
import com.lab.management.entity.Consumable;
import com.lab.management.entity.ConsumableApply;
import com.lab.management.mapper.AppointmentMapper;
import com.lab.management.mapper.ConsumableApplyMapper;
import com.lab.management.mapper.ConsumableMapper;
import com.lab.management.service.agent.AgentQueryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class AgentQueryServiceImpl implements AgentQueryService {

    private final ConsumableMapper consumableMapper;
    private final AppointmentMapper appointmentMapper;
    private final ConsumableApplyMapper consumableApplyMapper;
    private final DeviceService deviceService;
    private final AlertService alertService;
    private final FileStorageService fileStorageService;
    private final AppointmentService appointmentService;
    private final ConsumableService consumableService;
    private final NacosConfigService nacosConfigService;

    public AgentQueryServiceImpl(ConsumableMapper consumableMapper,
                                 AppointmentMapper appointmentMapper,
                                 ConsumableApplyMapper consumableApplyMapper,
                                 DeviceService deviceService,
                                 AlertService alertService,
                                 FileStorageService fileStorageService,
                                 AppointmentService appointmentService,
                                 ConsumableService consumableService,
                                 NacosConfigService nacosConfigService) {
        this.consumableMapper = consumableMapper;
        this.appointmentMapper = appointmentMapper;
        this.consumableApplyMapper = consumableApplyMapper;
        this.deviceService = deviceService;
        this.alertService = alertService;
        this.fileStorageService = fileStorageService;
        this.appointmentService = appointmentService;
        this.consumableService = consumableService;
        this.nacosConfigService = nacosConfigService;
    }

    @Override
    public Map<String, Object> getInventorySummary() {
        Map<String, Object> summary = new HashMap<>();
        long totalKinds = consumableMapper.selectCount(null);
        long warningKinds = consumableMapper.selectCount(
                new LambdaQueryWrapper<Consumable>().lt(Consumable::getCount, 20)
        );
        summary.put("totalKinds", totalKinds);
        summary.put("warningKinds", warningKinds);
        return summary;
    }

    @Override
    public Map<String, Object> getPendingTaskSummary() {
        Map<String, Object> summary = new HashMap<>();
        long pendingAppointment = appointmentMapper.selectCount(
                new LambdaQueryWrapper<Appointment>().eq(Appointment::getStatus, "审核中")
        );
        long pendingApply = consumableApplyMapper.selectCount(
                new LambdaQueryWrapper<ConsumableApply>().eq(ConsumableApply::getStatus, "审核中")
        );
        long todayAppointment = appointmentMapper.selectCount(
                new LambdaQueryWrapper<Appointment>().eq(Appointment::getReserveDate, LocalDate.now().toString())
        );
        summary.put("pendingAppointment", pendingAppointment);
        summary.put("pendingApply", pendingApply);
        summary.put("todayAppointment", todayAppointment);
        return summary;
    }

    @Override
    public List<Map<String, Object>> getAvailableTools() {
        List<Map<String, Object>> tools = new ArrayList<>();
        tools.add(tool("inventory.summary", "查询库存摘要"));
        tools.add(tool("pending.summary", "查询待审批任务摘要"));
        tools.add(tool("device.summary", "查询设备在线与上报摘要"));
        tools.add(tool("alert.summary", "查询最新告警列表"));
        tools.add(tool("file.summary", "查询文件中心列表"));
        tools.add(tool("inventory.log.summary", "查询最近库存变更日志"));
        tools.add(tool("appointment.create", "创建预约申请"));
        tools.add(tool("consumable.apply", "提交耗材领用申请"));
        tools.add(tool("alert.ack", "确认指定告警"));
        tools.add(tool("nacos.effective", "查看有效配置视图"));
        tools.add(tool("nacos.compare", "比较本地与远端配置差异"));
        return tools;
    }

    @Override
    public Map<String, Object> invokeTool(String toolName) {
        if ("inventory.summary".equals(toolName)) {
            return getInventorySummary();
        }
        if ("pending.summary".equals(toolName)) {
            return getPendingTaskSummary();
        }
        if ("device.summary".equals(toolName)) {
            return deviceService.getMonitorSummary();
        }
        if ("alert.summary".equals(toolName)) {
            Map<String, Object> result = new HashMap<>();
            result.put("alerts", alertService.listAlerts());
            return result;
        }
        if ("file.summary".equals(toolName)) {
            Map<String, Object> result = new HashMap<>();
            result.put("files", fileStorageService.listFiles());
            return result;
        }
        if ("inventory.log.summary".equals(toolName)) {
            Map<String, Object> result = new HashMap<>();
            result.put("logs", consumableService.listInventoryChangeLogs());
            return result;
        }
        if ("nacos.effective".equals(toolName)) {
            return nacosConfigService.effectiveConfig();
        }
        if ("nacos.compare".equals(toolName)) {
            return nacosConfigService.compareConfig();
        }
        Map<String, Object> result = new HashMap<>();
        result.put("error", "未知工具");
        result.put("toolName", toolName);
        return result;
    }

    @Override
    public Map<String, Object> executeTool(String toolName, Map<String, Object> args) {
        Map<String, Object> result = new HashMap<>();
        if ("appointment.create".equals(toolName)) {
            Map<String, String> params = new HashMap<>();
            params.put("labName", stringArg(args, "labName"));
            params.put("date", stringArg(args, "date"));
            params.put("userNo", stringArg(args, "userNo"));
            params.put("user", stringArg(args, "user"));
            result.put("message", appointmentService.createAppointment(params));
            result.put("tool", toolName);
            return result;
        }
        if ("consumable.apply".equals(toolName)) {
            Map<String, Object> params = new HashMap<>();
            params.put("id", stringArg(args, "id"));
            params.put("num", stringArg(args, "num"));
            result.put("message", consumableService.createApply(params));
            result.put("tool", toolName);
            return result;
        }
        if ("alert.ack".equals(toolName)) {
            String id = stringArg(args, "id");
            result.put("tool", toolName);
            result.put("alert", alertService.acknowledgeAlert(id));
            return result;
        }
        result.put("tool", toolName);
        result.put("error", "该工具不支持执行");
        return result;
    }

    private Map<String, Object> tool(String name, String description) {
        Map<String, Object> tool = new HashMap<>();
        tool.put("name", name);
        tool.put("description", description);
        return tool;
    }

    private String stringArg(Map<String, Object> args, String key) {
        Object value = args == null ? null : args.get(key);
        return value == null ? null : String.valueOf(value);
    }
}
