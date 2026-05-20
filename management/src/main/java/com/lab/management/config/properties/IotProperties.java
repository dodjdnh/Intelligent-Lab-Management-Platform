package com.lab.management.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.iot")
public class IotProperties {
    private Boolean uploadEnabled;
    private String uploadSecret;
    private String transport;
    private Boolean mqttEnabled;
    private String mqttBroker;
    private String mqttTopicPrefix;

    public Boolean getUploadEnabled() {
        return uploadEnabled;
    }

    public void setUploadEnabled(Boolean uploadEnabled) {
        this.uploadEnabled = uploadEnabled;
    }

    public String getUploadSecret() {
        return uploadSecret;
    }

    public void setUploadSecret(String uploadSecret) {
        this.uploadSecret = uploadSecret;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public Boolean getMqttEnabled() {
        return mqttEnabled;
    }

    public void setMqttEnabled(Boolean mqttEnabled) {
        this.mqttEnabled = mqttEnabled;
    }

    public String getMqttBroker() {
        return mqttBroker;
    }

    public void setMqttBroker(String mqttBroker) {
        this.mqttBroker = mqttBroker;
    }

    public String getMqttTopicPrefix() {
        return mqttTopicPrefix;
    }

    public void setMqttTopicPrefix(String mqttTopicPrefix) {
        this.mqttTopicPrefix = mqttTopicPrefix;
    }
}
