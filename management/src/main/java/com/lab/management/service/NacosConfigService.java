package com.lab.management.service;

import java.util.Map;

public interface NacosConfigService {

    Map<String, Object> status();

    Map<String, Object> fetchConfigSnapshot();

    Map<String, Object> effectiveConfig();

    Map<String, Object> compareConfig();
}
