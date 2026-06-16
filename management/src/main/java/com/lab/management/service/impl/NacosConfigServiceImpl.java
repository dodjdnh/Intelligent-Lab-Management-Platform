package com.lab.management.service.impl;

import com.lab.management.config.properties.NacosProperties;
import com.lab.management.service.NacosConfigService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

@Service
public class NacosConfigServiceImpl implements NacosConfigService {

    private final NacosProperties nacosProperties;
    @Value("${spring.datasource.url:}")
    private String datasourceUrl;
    @Value("${app.minio.endpoint:}")
    private String minioEndpoint;
    @Value("${app.agent.model:}")
    private String agentModel;

    public NacosConfigServiceImpl(NacosProperties nacosProperties) {
        this.nacosProperties = nacosProperties;
    }

    @Override
    public Map<String, Object> status() {
        Map<String, Object> result = new HashMap<>();
        result.put("enabled", Boolean.TRUE.equals(nacosProperties.getEnabled()));
        result.put("serverAddr", nacosProperties.getServerAddr());
        result.put("namespace", nacosProperties.getNamespace());
        result.put("group", nacosProperties.getGroup());
        result.put("dataId", nacosProperties.getDataId());
        result.put("connected", false);

        if (Boolean.TRUE.equals(nacosProperties.getEnabled()) && notBlank(nacosProperties.getServerAddr())) {
            try {
                String response = restClient().get()
                        .uri("/nacos/v1/console/health/readiness")
                        .retrieve()
                        .body(String.class);
                result.put("connected", response != null && response.contains("UP"));
                result.put("readiness", response);
            } catch (Exception ex) {
                result.put("readiness", "UNAVAILABLE");
                result.put("error", ex.getClass().getSimpleName());
            }
        }
        return result;
    }

    @Override
    public Map<String, Object> fetchConfigSnapshot() {
        Map<String, Object> result = status();
        result.put("content", "");
        if (!Boolean.TRUE.equals(nacosProperties.getEnabled()) || !notBlank(nacosProperties.getDataId())) {
            return result;
        }

        try {
            String content = restClient().get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/nacos/v1/cs/configs")
                            .queryParam("dataId", nacosProperties.getDataId())
                            .queryParam("group", defaultGroup())
                            .queryParam("tenant", defaultNamespace())
                            .build())
                    .retrieve()
                    .body(String.class);
            result.put("content", content == null ? "" : content);
        } catch (Exception ex) {
            result.put("content", "");
            result.put("error", ex.getClass().getSimpleName());
        }
        return result;
    }

    @Override
    public Map<String, Object> effectiveConfig() {
        Map<String, Object> snapshot = fetchConfigSnapshot();
        Map<String, Object> result = new HashMap<>(snapshot);
        Map<String, Object> local = new HashMap<>();
        local.put("spring.datasource.url", datasourceUrl);
        local.put("app.minio.endpoint", minioEndpoint);
        local.put("app.agent.model", agentModel);
        result.put("local", local);

        String content = snapshot.get("content") == null ? "" : snapshot.get("content").toString();
        result.put("remoteParsed", parseRemote(content));
        return result;
    }

    @Override
    public Map<String, Object> compareConfig() {
        Map<String, Object> effective = effectiveConfig();
        Map<String, Object> local = castMap(effective.get("local"));
        Map<String, Object> remote = castMap(effective.get("remoteParsed"));
        TreeSet<String> keys = new TreeSet<>();
        keys.addAll(local.keySet());
        keys.addAll(remote.keySet());

        List<Map<String, Object>> items = new java.util.ArrayList<>();
        for (String key : keys) {
            Object localValue = local.get(key);
            Object remoteValue = remote.get(key);
            Map<String, Object> item = new HashMap<>();
            item.put("key", key);
            item.put("localValue", stringify(localValue));
            item.put("remoteValue", stringify(remoteValue));
            item.put("status", compareStatus(localValue, remoteValue));
            items.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("localCount", local.size());
        result.put("remoteCount", remote.size());
        result.put("differentCount", items.stream().filter(item -> !"一致".equals(item.get("status"))).count());
        return result;
    }

    private Map<String, Object> parseRemote(String content) {
        Map<String, Object> parsed = new HashMap<>();
        if (content == null || content.trim().isEmpty()) {
            return parsed;
        }
        try {
            if (content.contains(":") && content.contains("\n")) {
                Object yamlData = new Yaml().load(content);
                if (yamlData instanceof Map<?, ?> map) {
                    flatten("", map, parsed);
                    return parsed;
                }
            }
        } catch (Exception ignored) {
        }

        try {
            Properties properties = new Properties();
            properties.load(new java.io.StringReader(content));
            for (String name : properties.stringPropertyNames()) {
                parsed.put(name, properties.getProperty(name));
            }
        } catch (Exception ignored) {
        }
        return parsed;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return new HashMap<>();
    }

    private String compareStatus(Object localValue, Object remoteValue) {
        String local = stringify(localValue);
        String remote = stringify(remoteValue);
        if (local.isEmpty() && remote.isEmpty()) {
            return "空";
        }
        if (local.isEmpty()) {
            return "仅远端";
        }
        if (remote.isEmpty()) {
            return "仅本地";
        }
        return local.equals(remote) ? "一致" : "不一致";
    }

    private String stringify(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private void flatten(String prefix, Map<?, ?> source, Map<String, Object> target) {
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            String key = prefix.isEmpty() ? String.valueOf(entry.getKey()) : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> nested) {
                flatten(key, nested, target);
            } else {
                target.put(key, value);
            }
        }
    }

    private RestClient restClient() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(normalizeBaseUrl(nacosProperties.getServerAddr()))
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return builder.build();
    }

    private String normalizeBaseUrl(String serverAddr) {
        String trimmed = serverAddr.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        return "http://" + trimmed;
    }

    private String defaultGroup() {
        return notBlank(nacosProperties.getGroup()) ? nacosProperties.getGroup().trim() : "DEFAULT_GROUP";
    }

    private String defaultNamespace() {
        return notBlank(nacosProperties.getNamespace()) ? nacosProperties.getNamespace().trim() : "";
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
