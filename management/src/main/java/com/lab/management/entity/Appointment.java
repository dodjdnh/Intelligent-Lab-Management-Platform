package com.lab.management.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_appointment")
public class Appointment {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("lab_name")
    private String labName;

    @TableField("user_name")
    private String userName;

    // === 新增字段 ===
    @TableField("user_id")
    private Long userId; // 存用户ID，用于后端统计限制

    @TableField("user_no")
    private String userNo; // 学号/工号

    @TableField("reserve_date")
    private String reserveDate;

    private String status;
}