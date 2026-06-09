package com.lab.management.service;

import java.util.Map;

public interface HomeStatsService {

    Map<String, Object> getStats();

    void evictCache();
}
