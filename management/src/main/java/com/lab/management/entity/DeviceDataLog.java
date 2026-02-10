package com.lab.management.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("device_data_logs")
public class DeviceDataLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    // 将对方传的 RFID 映射到数据库的 device_id 字段上
    @JsonProperty("RFID")
    @TableField("device_id")
    private String deviceId;

    // 将对方传的 gravity 映射到数据库的 temp 字段上
    @JsonProperty("gravity")
    private BigDecimal temp;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("raw_content")
    private String rawContent;
}