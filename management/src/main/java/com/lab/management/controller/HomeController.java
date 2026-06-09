package com.lab.management.controller;

import com.lab.management.common.Result;
import com.lab.management.service.HomeStatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home")
public class HomeController {

    private final HomeStatsService homeStatsService;

    public HomeController(HomeStatsService homeStatsService) {
        this.homeStatsService = homeStatsService;
    }

    @GetMapping("/stats")
    public Result getStats() {
        return Result.success(homeStatsService.getStats());
    }
}
