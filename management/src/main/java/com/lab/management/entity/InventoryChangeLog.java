package com.lab.management.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_inventory_change_log")
public class InventoryChangeLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("consumable_id")
    private Long consumableId;

    @TableField("consumable_name")
    private String consumableName;

    @TableField("device_id")
    private String deviceId;

    @TableField("change_type")
    private String changeType;

    @TableField("before_count")
    private Integer beforeCount;

    @TableField("after_count")
    private Integer afterCount;

    @TableField("delta_count")
    private Integer deltaCount;

    @TableField("sensor_value")
    private BigDecimal sensorValue;

    @TableField("conversion_mode")
    private String conversionMode;

    private String operator;

    private String remark;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
