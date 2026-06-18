package com.lab.management.service.agent;

import java.util.List;
import java.util.Map;

public interface AgentQueryService {

    Map<String, Object> getInventorySummary();

    Map<String, Object> getPendingTaskSummary();

    List<Map<String, Object>> getAvailableTools();

    Map<String, Object> invokeTool(String toolName);

    Map<String, Object> executeTool(String toolName, Map<String, Object> args);
}
