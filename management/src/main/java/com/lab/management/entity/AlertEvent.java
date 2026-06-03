package com.lab.management.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_alert_event")
public class AlertEvent {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String type;
    private String level;
    private String title;
    private String message;

    @TableField("source_id")
    private String sourceId;

    private String status;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("acknowledged_at")
    private LocalDateTime acknowledgedAt;
}
