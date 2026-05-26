package com.lab.management.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_device_binding")
public class DeviceBinding {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("device_id")
    private String deviceId;

    @TableField("consumable_id")
    private Long consumableId;

    @TableField("consumable_name")
    private String consumableName;

    private String area;
    private String topic;

    @TableField("conversion_mode")
    private String conversionMode;

    @TableField("unit_weight")
    private java.math.BigDecimal unitWeight;

    @TableField("tare_weight")
    private java.math.BigDecimal tareWeight;

    @TableField("enabled_flag")
    private Integer enabledFlag;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
