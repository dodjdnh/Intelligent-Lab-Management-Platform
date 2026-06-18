package com.lab.management.controller;

import com.lab.management.common.Result;
import com.lab.management.service.agent.AgentChatService;
import com.lab.management.service.agent.AgentQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/agent")
public class AgentController {

    private final AgentQueryService agentQueryService;
    private final AgentChatService agentChatService;

    public AgentController(AgentQueryService agentQueryService, AgentChatService agentChatService) {
        this.agentQueryService = agentQueryService;
        this.agentChatService = agentChatService;
    }

    @GetMapping("/summary/inventory")
    public Result getInventorySummary() {
        return Result.success(agentQueryService.getInventorySummary());
    }

    @GetMapping("/summary/pending")
    public Result getPendingSummary() {
        return Result.success(agentQueryService.getPendingTaskSummary());
    }

    @GetMapping("/tools")
    public Result tools() {
        return Result.success(agentQueryService.getAvailableTools());
    }

    @GetMapping("/tool/invoke")
    public Result invoke(String name) {
        return Result.success(agentQueryService.invokeTool(name));
    }

    @PostMapping("/tool/execute")
    public Result execute(@RequestBody Map<String, Object> params) {
        String name = params.get("name") == null ? null : String.valueOf(params.get("name"));
        Object args = params.get("args");
        Map<String, Object> argMap = args instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
        return Result.success(agentQueryService.executeTool(name, argMap));
    }

    @PostMapping("/chat")
    public Result chat(@RequestBody Map<String, String> params) {
        return Result.success(agentChatService.chat(params.get("message")));
    }
}
