package com.lab.management.service.agent.impl;

import com.lab.management.config.properties.AgentProperties;
import com.lab.management.exception.BusinessException;
import com.lab.management.service.agent.AgentChatService;
import com.lab.management.service.agent.AgentQueryService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AgentChatServiceImpl implements AgentChatService {

    private final AgentProperties agentProperties;
    private final AgentQueryService agentQueryService;

    public AgentChatServiceImpl(AgentProperties agentProperties, AgentQueryService agentQueryService) {
        this.agentProperties = agentProperties;
        this.agentQueryService = agentQueryService;
    }

    @Override
    public Map<String, Object> chat(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new BusinessException("问题不能为空");
        }

        String trimmed = message.trim();
        if (Boolean.TRUE.equals(agentProperties.getEnabled()) && hasRemoteAgentConfig()) {
            try {
                return remoteChat(trimmed);
            } catch (Exception ignored) {
                return localFallback(trimmed, true);
            }
        }
        return localFallback(trimmed, false);
    }

    private Map<String, Object> remoteChat(String message) {
        RestClient client = RestClient.builder()
                .baseUrl(trim(agentProperties.getBaseUrl()))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + trim(agentProperties.getApiKey()))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", trim(agentProperties.getModel()));
        payload.put("messages", List.of(
                Map.of("role", "system", "content", defaultSystemPrompt()),
                Map.of("role", "user", "content", message)
        ));
        payload.put("temperature", 0.2);

        Map response = client.post()
                .uri("/chat/completions")
                .body(payload)
                .retrieve()
                .body(Map.class);

        Object content = extractContent(response);
        Map<String, Object> result = new HashMap<>();
        result.put("mode", "remote");
        result.put("answer", content == null ? "模型未返回有效内容" : content.toString());
        return result;
    }

    private Map<String, Object> localFallback(String message, boolean remoteFailed) {
        if (message.contains("库存")) {
            return toolStyleAnswer("inventory.summary", agentQueryService.getInventorySummary(), remoteFailed);
        }
        if (message.contains("审批") || message.contains("待办") || message.contains("预约")) {
            return toolStyleAnswer("pending.summary", agentQueryService.getPendingTaskSummary(), remoteFailed);
        }
        if (message.contains("设备") || message.contains("监控")) {
            return toolStyleAnswer("device.summary", agentQueryService.invokeTool("device.summary"), remoteFailed);
        }
        if (message.contains("告警")) {
            return toolStyleAnswer("alert.summary", agentQueryService.invokeTool("alert.summary"), remoteFailed);
        }
        if (message.contains("文件")) {
            return toolStyleAnswer("file.summary", agentQueryService.invokeTool("file.summary"), remoteFailed);
        }
        if (message.contains("配置") || message.contains("nacos")) {
            return toolStyleAnswer("nacos.effective", agentQueryService.invokeTool("nacos.effective"), remoteFailed);
        }

        Map<String, Object> inventory = agentQueryService.getInventorySummary();
        Map<String, Object> pending = agentQueryService.getPendingTaskSummary();

        StringBuilder answer = new StringBuilder();
        answer.append("当前系统已接入本地助手模式。");
        if (remoteFailed) {
            answer.append("远程模型调用失败，已回退到本地摘要回答。");
        }
        answer.append("\n");
        answer.append("问题：").append(message).append("\n");
        answer.append("库存摘要：共有 ").append(inventory.get("totalKinds")).append(" 种耗材，预警耗材 ")
                .append(inventory.get("warningKinds")).append(" 种。").append("\n");
        answer.append("任务摘要：待审核预约 ").append(pending.get("pendingAppointment")).append(" 条，待审核领用 ")
                .append(pending.get("pendingApply")).append(" 条，今日预约 ").append(pending.get("todayAppointment")).append(" 条。").append("\n");
        answer.append("当前可直接支持的能力：查询库存、查询待审批、文件中心检索入口、设备与告警态势查看。");

        Map<String, Object> result = new HashMap<>();
        result.put("mode", remoteFailed ? "fallback" : "local");
        result.put("answer", answer.toString());
        return result;
    }

    private Map<String, Object> toolStyleAnswer(String toolName, Map<String, Object> payload, boolean remoteFailed) {
        Map<String, Object> result = new HashMap<>();
        result.put("mode", remoteFailed ? "fallback" : "local");
        result.put("tool", toolName);
        result.put("payload", payload);
        result.put("answer", "已调用工具 `" + toolName + "`，结果已返回到结构化数据中。");
        return result;
    }

    private Object extractContent(Map response) {
        if (response == null) {
            return null;
        }
        Object choices = response.get("choices");
        if (!(choices instanceof List) || ((List<?>) choices).isEmpty()) {
            return null;
        }
        Object first = ((List<?>) choices).get(0);
        if (!(first instanceof Map)) {
            return null;
        }
        Object message = ((Map<?, ?>) first).get("message");
        if (!(message instanceof Map)) {
            return null;
        }
        return ((Map<?, ?>) message).get("content");
    }

    private boolean hasRemoteAgentConfig() {
        return notBlank(agentProperties.getBaseUrl()) && notBlank(agentProperties.getApiKey()) && notBlank(agentProperties.getModel());
    }

    private String defaultSystemPrompt() {
        if (notBlank(agentProperties.getSystemPrompt())) {
            return trim(agentProperties.getSystemPrompt());
        }
        return "你是智慧实验室管理平台的业务助手。优先基于库存、预约、待办与设备管理语境进行简洁回答。";
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
